# Central Play - R8 / ProGuard production rules
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Retrofit annotations/interfaces
-if interface * { @retrofit2.http.* <methods>; }
-keep,allowoptimization,allowshrinking,allowobfuscation class <3>
-dontwarn retrofit2.**

# Gson fields mapped by @SerializedName
-keepclassmembers,allowobfuscation class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Central Play network/model DTOs conservative for the first release.
-keep class com.centralplay.app.model.** { *; }
-keep class com.centralplay.app.catalog.** { *; }

# Media3 / OkHttp / Kotlin warnings that do not affect runtime.
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn kotlinx.coroutines.**
