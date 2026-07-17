# StringFog local integration

This folder contains the shared StringFog Gradle configuration.

The plugin classpath is declared in the root build.gradle:

```gradle
buildscript {
    dependencies {
        classpath "com.github.megatronking.stringfog:gradle-plugin:5.2.0"
        classpath "com.github.megatronking.stringfog:xor:5.0.0"
    }
}
```

The app applies the reusable script from the root build.gradle:

```gradle
apply from: rootProject.file("lib_plugin/string_fog/string_fog.gradle")
```