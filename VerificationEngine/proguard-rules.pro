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


# Keep rules for our app packages
-keepclassmembers class com.merative.watson.healthpass.verificationengine.models.** { *; }
-keepclassmembers enum com.merative.watson.healthpass.verifiablecredential.models.** { *; }

# Common
-keepattributes Signature, InnerClasses
-keepattributes *Annotation*
-keepattributes Exceptions
-dontwarn javax.annotation.**

# Gson
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer
-keep class com.google.gson.examples.android.model.** { <fields>; }
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}