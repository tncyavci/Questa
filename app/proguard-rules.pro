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

# Keep your model classes
-keep class com.example.questa.data.model.** { *; }

# Koin
-keepnames class androidx.lifecycle.ViewModel
-keepclassmembers public class * extends androidx.lifecycle.ViewModel { public <init>(...); }
-keepclassmembers class * { public <init>(...); }
-keep class org.koin.** { *; }

# Firebase
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod
-keepattributes InnerClasses
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.auth.** { *; }

# Jetpack Compose
-dontwarn androidx.compose.**
-keep class androidx.compose.** { *; }
-keep class androidx.lifecycle.* { *; }
-keep class androidx.lifecycle.viewmodel.* { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# Kotlin coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keep class kotlinx.coroutines.** { *; }

# Coil
-keep class coil.** { *; }

# JSR 305 annotations are for embedding nullability information.
-dontwarn javax.annotation.**

# Ignore JSR 305 annotations for embedding nullability information.
-dontwarn javax.annotation.**

# Uncomment for DexGuard only
#-keepresourcexmlelements manifest/application/meta-data@value=GlideModule