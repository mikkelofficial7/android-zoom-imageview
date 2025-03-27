# Android Image View with Zoomable

Latest stable version:

[![](https://jitpack.io/v/mikkelofficial7/android-zoom-imageview.svg)](https://jitpack.io/#mikkelofficial7/android-zoom-imageview)

1. Add this gradle in ```build.gradle(:app)``` :
```
dependencies {
   implementation 'com.github.mikkelofficial7:android-zoom-imageview:v1.1'
}
 ```
or gradle.kts:
```
dependencies {
  implementation("com.github.mikkelofficial7:android-zoom-imageview:v1.1")
}
 ```

2. Add it in your root settings.gradle at the end of repositories:
```
repositories {
  mavenCentral()
  maven { url 'https://jitpack.io' }
}
```
3. In your ```Activity``` or ```Fragment``` xml, add this:
```
<com.lib.zoomimageengine.ZoomImageView
    android:layout_height="wrap_content"
    android:layout_width="wrap_content" />
```