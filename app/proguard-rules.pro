# Keep all gRPC classes (since they are needed at runtime)
-keep class io.grpc.** { *; }
-keep class io.netty.** { *; }
-keep class org.eclipse.jetty.** { *; }
-keep class org.slf4j.** { *; }
-keep class reactor.blockhound.integration.** { *; }

# Keep all Vision API classes
-keep class com.google.cloud.vision.** { *; }

# Prevent warnings about missing classes in gRPC or Vision API
-dontwarn io.grpc.**
-dontwarn com.google.cloud.vision.**

# If you use WebView with JavaScript interface, specify it here (uncomment and customize)
# -keepclassmembers class fqcn.of.javascript.interface.for.webview {
#    public *;
# }

# Keep line number and source file attributes for debugging stack traces
-keepattributes SourceFile,LineNumberTable

# Optionally, rename the source file attribute to hide the original source file name
# -renamesourcefileattribute SourceFile
