QRCodePlus for Android
===================
QRCodePlus is based on [ZXing](https://github.com/zxing/zxing) project and optimized for QR code recognition by applying several specific algorithms.

1. Use the native implementation for some time-consuming algorithms.
2. Increase the contrast of low-contrast images.
3. Use random binarization methods rather than fixed one.
4. Optimized finder pattern finder.
5. Adaptive recognition params which dynamically adjusted according to previous decode failure hint.

Experiments show that this library has better QR code recognition results than ZXing, especially for some corrupted QR codes. Such as:

![1](https://raw.github.com/teejoe/qrcodeplus/master/testimage/4.png)
![2](https://raw.github.com/teejoe/qrcodeplus/master/testimage/1.png)

More test QR code images can be found in *testimage/*.

Demo app can be downloaded from Google Play. [<img height='62' width='161' src='https://play.google.com/intl/en_us/badges/images/generic/en_badge_web_generic.png'/>](https://play.google.com/store/apps/details?id=com.m2x.qrcodeplusdemo).


Library
==============
The library is divided into two parts: *core* & *scanner*. The *core* library is based on ZXing's core library, which contains core functions for QR code decoding. The *scanner* library is based on ZXing's BarcodeScanner for Android, which wraps up the core library and integrates camera operations for convenience.


Gradle integration
==================

Minimum code for Gradle integration, place code in your `build.gradle`

```
dependencies {
    compile 'com.m2x.qrcodeplus:core:1.0.1'
    compile 'com.m2x.qrcodeplus:scanner:1.0.1'
}
```

Usage
============

* Add code in your layout.xml:

    ```
    <SurfaceView
        android:id="@+id/surface_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>

    <com.m2x.qrcodescanner.ViewfinderView
        android:id="@+id/viewfinder_view"
        android:layout_width="match_parent"
        android:layout_height="match_parent"/>
    ```
    
* Create QRCodeScanner in Activity.onCreate():

	```
    SurfaceView surfaceView = (SurfaceView)findViewById(R.id.surface_view);
    ViewfinderView viewFinderView = (ViewfinderView)findViewById(R.id.viewfinder_view);
    mQRCodeScanner = new QRCodeScanner(this, surfaceView, viewFinderView) {
        @Override
        protected void onFatalError(String message) {
            //error    
        }

        @Override
        protected void handleDecodeSuccess(DecodeResult result) {
            //handle decode result
        }
    };
	```
	
* Attach to Activity's lifecycle.

    ```
    @Override
    public void onResume() {
        super.onResume();
        mQRCodeScanner.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mQRCodeScanner.pause();
    }
   ```

License
============

    Copyright 2014-2017 M2X

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
