# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# Keep rules for our app packages
-keep class com.merative.healthpass.models.** { *; }
-keep class com.merative.healthpass.ui.debug.** { *; }

# Common
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature, InnerClasses
-keepattributes *Annotation*
-keepattributes Exceptions
-dontwarn javax.annotation.**

## Dagger
-dontwarn com.google.errorprone.annotations.**

# Gson
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keep class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Crashlytics
-keepattributes SourceFile,LineNumberTable
-keep class com.google.firebase.crashlytics** { *; }
-dontwarn com.google.firebase.crashlytics**

#Okhttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**
-dontnote okhttp3.**

#sonIgnore
-keepclassmembers class * {
    @com.fasterxml.jackson.annotation.* *;
}
-dontwarn com.fasterxml.jackson.databind.**