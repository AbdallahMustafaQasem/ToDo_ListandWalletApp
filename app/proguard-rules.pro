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

# ---- Room ----
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**

# ---- Hilt / Dagger ----
-keepclassmembers,allowobfuscation class * {
    @javax.inject.* <fields>;
    @dagger.* <fields>;
    <init>(...);
}
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }

# ---- Kotlinx Serialization ----
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class **$$serializer {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.abdallah.taskvault.**$$serializer { *; }
-keepclassmembers class com.abdallah.taskvault.** {
    *** Companion;
}
-keepclasseswithmembers class com.abdallah.taskvault.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ---- Google Sign-In / Play Services Auth ----
-keep class com.google.android.gms.auth.** { *; }
-keep class com.google.android.gms.common.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# ---- Firebase Auth + Firestore ----
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# ---- Glance ----
-keep class androidx.glance.** { *; }

# ---- Keep line numbers for crash reporting ----
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile