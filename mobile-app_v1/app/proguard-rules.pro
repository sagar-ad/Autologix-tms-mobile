# AutoLogix TMS ProGuard Rules
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn retrofit2.**
-keepattributes *Annotation*
-keepattributes Signature
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class com.autologix.tms.data.models.** { *; }
-keep class com.autologix.tms.domain.models.** { *; }
