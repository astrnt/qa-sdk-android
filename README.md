### Astronaut Q & A SDK for Android
Currently we just support SDK for Video Interview

##### Make sure you register as Developer Partner at Astronaut. If you haven't, you can register [here](https://developers.astrnt.co/)
---
Requirements :
* minSdkVersion 21
* Allow Permission Camera
* Allow Permission Modify Audio Settings
* Allow Permission Record Audio
* Allow Permission Write External Storage
---
#### Setup SDK

1. Add the JitPack repository to your build file
Add it in your root **build.gradle** at the end of repositories:
```
	allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}
```
2. Add the dependency
check the version [here](https://github.com/astrnt/qa-sdk-android/releases)

```
	dependencies {
	        implementation 'com.github.astrnt:qa-sdk-android:latest-version'
	}
```

3. Add this line in **app/build.gradle**
```
    flavorDimensions "mode"
    productFlavors {
        beta {
            dimension "mode"
            applicationIdSuffix ".betasdk"
            buildConfigField "boolean", "BETA", "true"
            buildConfigField("String", "API_URL", '"http://beta.astrnt.co/api/v2/"')
            buildConfigField ("int", "SDK_VERSION", "100")
        }
        live {
            dimension "mode"
            buildConfigField "boolean", "BETA", "false"
            buildConfigField("String", "API_URL", '"http://app.astrnt.co/api/v2/"')
            buildConfigField ("int", "SDK_VERSION", "100")
        }
    }
```

4. SetUp SDK in your **Application Class**
```
    private static AstrntSDK astrntSDK;

    public static AstronautApi getApi() {
        return astrntSDK.getApi();
    }

    private void setUpSDK() {
        if (astrntSDK == null) {
            astrntSDK = new AstrntSDK(this, BuildConfig.API_URL, BuildConfig.DEBUG);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        setUpSDK();
    }
```

5. Last, add this code at **AndroidManifest.xml** for enable service compress and upload
```
    <service android:name="co.astrnt.qasdk.videocompressor.services.VideoCompressService"/>
    <service android:name="co.astrnt.qasdk.upload.SingleVideoUploadService"/>
```
---

#### Compress And Upload Video

* After success recording video, You can call this code for start Compressing your Video
```
    File file = new File(videoUri.getPath());
    VideoCompressService.start(context, file.getAbsolutePath(), AstrntSDK.getCurrentQuestion().getId());
```

* After Video Compress success, that will be triggered **SingleVideoUploadService** for automatically upload to **Astronaut Server**

##### For more detail you can see our [sample here.](https://github.com/astrnt/qa-sdk-android/tree/master/sample)
---
##### Supports
Sorry, we no longer answer questions from repository issues. If you need any assistance, please contact developers@astrnt.co
