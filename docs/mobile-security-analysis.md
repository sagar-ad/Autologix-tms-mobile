# AutoLogix TMS - Mobile Security & Multi-Organization Architecture

**Document Reference:** `docs/mobile-security-analysis.md`  
**System:** AutoLogix Trolley Management System (TMS)  
**Target Client:** Native Android Mobile Application (`mobile-app_v1`)  
**Backend:** NestJS Multi-Tenant Engine  
**Author:** Senior Android Solution Architect & Lead Developer  
**Status:** Baseline Documentation (Awaiting Approval)

---

## 1. Security Architecture Principles

Operating an enterprise mobile application within automotive manufacturing facilities, supplier warehouses, and external transport routes introduces distinct cyber-physical security vectors:
* Shared industrial ruggedized tablets/scanners (Zebra, Honeywell, Samsung Knox) used across multiple shifts.
* Rogue wireless access points and untrusted cellular networks.
* Unauthorized data access across distinct tenant organizations (e.g. Ford, Toyota, Mahindra, Tata Motors manufacturing plants operating on shared TMS infrastructure).

The AutoLogix TMS Android application adheres to the **Zero Trust Device & Defense-in-Depth** model:

```
+-------------------------------------------------------------------------+
|                         Android Client Device                           |
|  +--------------------------+             +---------------------------+ |
|  | Hardware Keystore (TEE)  |             | Biometric Prompt          | |
|  | MasterKey AES256-GCM     |             | (Strong Biometrics/PIN)   | |
|  +------------+-------------+             +-------------+-------------+ |
|               |                                         |               |
|  +------------v-----------------------------------------v-------------+ |
|  |     EncryptedSharedPreferences / Encrypted DataStore               | |
|  |     • Scoped Access Token (15-min TTL)                             | |
|  |     • Cryptographic Refresh Token (7-day TTL)                      | |
|  |     • Current Active Tenant ID (`x-organization-id`)               | |
|  +------------------------------------+-------------------------------+ |
|                                       |                                 |
|                               +-------v-------+                         |
|                               | OkHttp Client |                         |
|                               +-------+-------+                         |
+---------------------------------------|---------------------------------+
                                        | TLS 1.3 + Certificate Pinning
                                        | Header: Authorization: Bearer <jwt>
                                        | Header: x-organization-id: <uuid>
                                        v
+-------------------------------------------------------------------------+
|                        NestJS API Gateway / Ingress                     |
|  +-------------------------------------------------------------------+  |
|  | 1. JwtAuthGuard (Validates Signature, Expiry, User Status)        |  |
|  | 2. TenantGuard (Validates User belongs to x-organization-id)       |  |
|  | 3. RolesGuard (Validates RBAC Permissions against Route Handler)  |  |
|  +-------------------------------------------------------------------+  |
+-------------------------------------------------------------------------+
```

---

## 2. Multi-Organization Data Isolation

AutoLogix TMS enforces strict logical data isolation across organizations:

### 2.1 Tenant Context Propagation
1. Upon successful login (`POST /auth/login`), the backend returns an array of authorized `organizations[]` for the user along with a `currentOrgId`.
2. The Android client stores the active `organizationId` inside encrypted preferences.
3. Every outgoing HTTP request executed by the mobile app automatically attaches:
   ```http
   Authorization: Bearer <jwt_access_token>
   x-organization-id: 3fa85f64-5717-4562-b3fc-2c963f66afa6
   x-app-version: 1.0.0
   x-client-platform: ANDROID
   ```
4. **Server-Side Enforcement:** The NestJS `TenantGuard` extracts both the token's `sub` (userId) and the `x-organization-id` header. It queries the organizational membership table before delegating to database repositories. If a rogue client transmits an organization ID to which the user has not been assigned, the request is rejected with `403 Forbidden`.

### 2.2 Multi-Organization Switching Workflow
* Mobile users with cross-organizational privileges (e.g. 3rd-party logistics managers or OEM quality auditors) can switch organizations via the in-app tenant switcher.
* Switching invokes `POST /organizations/switch`.
* The server issues an updated scoped JWT token explicitly bounded to the new tenant. The local cache and Room database are partitioned per `organizationId`, preventing cross-tenant data leakage.

---

## 3. Token Lifecycle & Authentication Architecture

### 3.1 Token Pair Specifications
* **Access Token:**
  * **Type:** JWT (RS256 or HS256)
  * **TTL:** 15 minutes
  * **Claims:** `sub`, `email`, `roles`, `orgId`, `permissions`, `iat`, `exp`
* **Refresh Token:**
  * **Type:** Cryptographically random opaque 256-bit token (or signed long-lived JWT)
  * **TTL:** 7 days with sliding window revocation
  * **Revocation:** On explicit logout (`POST /auth/logout`) or password change.

### 3.2 Automated Token Refresh via OkHttp Authenticator
In Kotlin, token refreshing is implemented using an OkHttp `Authenticator` with synchronous thread-locking to prevent concurrent refresh storms:

```kotlin
class TokenAuthenticator(
    private val tokenStorage: SecureTokenStorage,
    private val authService: Provider<AuthApiService>
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // Prevent infinite loops if refresh fails
        if (responseCount(response) >= 2) return null

        synchronized(this) {
            val currentToken = tokenStorage.getAccessToken()
            val requestToken = response.request.header("Authorization")
                ?.removePrefix("Bearer ")

            // If another thread already refreshed the token, retry with newest token
            if (currentToken != null && currentToken != requestToken) {
                return response.request.newBuilder()
                    .header("Authorization", "Bearer $currentToken")
                    .build()
            }

            val refreshToken = tokenStorage.getRefreshToken() ?: return null
            val refreshResponse = authService.get().refreshTokenSync(RefreshTokenDto(refreshToken))

            if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                val newTokens = refreshResponse.body()!!
                tokenStorage.saveTokens(newTokens.accessToken, newTokens.refreshToken)

                return response.request.newBuilder()
                    .header("Authorization", "Bearer ${newTokens.accessToken}")
                    .build()
            } else {
                // Refresh token expired or revoked - trigger force logout
                tokenStorage.clearAll()
                SessionEventBus.postLogoutEvent(SessionExpiredReason.SESSION_EXPIRED)
                return null
            }
        }
    }
}
```

---

## 4. Secure On-Device Storage (Android Keystore)

Mobile devices in manufacturing yards can be lost, misplaced, or physically accessed. Therefore:
* **No Plaintext SharedPreferences:** Tokens, passwords, user credentials, and session state are **NEVER** stored in default unencrypted XML files.
* **EncryptedSharedPreferences (Jetpack Security):**
  * AES-256 GCM encryption for values.
  * AES-256 SIV encryption for keys.
  * Master encryption key stored securely in the hardware-backed **Android Keystore System (TEE / StrongBox Keymaster)**.
* **Database Encryption:** Local Room SQLite database (caching offline scans and trolley catalogs) is encrypted via **SQLCipher** using a 256-bit key derived from the Keystore.

---

## 5. Network Security & Transport Layer Integrity

### 5.1 Cleartext Traffic Prohibition
Android `AndroidManifest.xml` explicitly disallows cleartext HTTP traffic:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    android:usesCleartextTraffic="false"
    ...>
```

### 5.2 Network Security Configuration (`res/xml/network_security_config.xml`)
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="false">
        <domain includeSubdomains="true">api.autologix-tms.com</domain>
        <!-- SHA-256 Certificate Pinning for Production Environment -->
        <pin-set expiration="2027-12-31">
            <pin digest="SHA-256">47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=</pin>
            <!-- Backup Pin -->
            <pin digest="SHA-256">YLh1dUR9y6Kja30RrAn7JKODskECM6Fe5KU5CC4guK4=</pin>
        </pin-set>
    </domain-config>
</network-security-config>
```

---

## 6. Role-Based Access Control (RBAC) on Mobile

The mobile application dynamically adapts its UI, navigation graph, and permitted actions based on the user's validated roles:

| Screen / Feature | Super Admin | Org Admin | Maintenance Manager | Maintenance Tech | Logistics Operator | Viewer |
| :--- | :---: | :---: | :---: | :---: | :---: | :---: |
| **Trolley Scan & 360 View** | Yes | Yes | Yes | Yes | Yes | Yes |
| **Gate IN / Gate OUT Scan** | Yes | Yes | No | No | **Primary** | View Only |
| **Batch Movement Dispatch** | Yes | Yes | No | No | **Primary** | No |
| **PM Schedule Calendar** | Yes | Yes | Yes | **Primary** | No | View Only |
| **Execute PM Checklist** | Yes | Yes | Yes | **Primary** | No | No |
| **Report Damage Incident** | Yes | Yes | Yes | Yes | Yes | No |
| **Approve / Scrap Damage** | Yes | Yes | **Primary** | No | No | No |
| **Assign Work Orders** | Yes | Yes | **Primary** | No | No | No |
| **Org Switcher** | Yes | Yes (if multi-assigned) | Conditional | Conditional | Conditional | Conditional |

---

## 7. Device Integrity & Biometric Authentication

1. **Biometric Unlock (BiometricPrompt):**
   * Operators working on shift can lock the app without logging out completely. Re-entering requires Fingerprint / Face authentication validated against `BiometricManager.Authenticators.BIOMETRIC_STRONG`.
2. **Root & Emulator Detection:**
   * Enterprise builds verify device integrity using SafetyNet / Google Play Integrity API and checks for su-binaries, Magisk, or test-keys.
3. **Camera & Scoped Storage:**
   * Uses modern Android 10+ Scoped Storage (`MediaStore`). Photos taken for damage reports are stored in private app cache (`context.cacheDir`) and deleted immediately after successful upload to `/media/upload`.
