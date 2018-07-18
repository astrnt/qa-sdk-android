### Astronaut Q & A SDK for Android

**minSdkVersion 21**
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
```
	dependencies {
	        implementation 'com.github.astrnt:qa-sdk-android:1.0.0'
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

5. Add this code for enable service compress and upload to your **AndroidManifest.xml**
```
    <service android:name="co.astrnt.qasdk.videocompressor.services.VideoCompressService"/>
    <service android:name="co.astrnt.qasdk.upload.SingleVideoUploadService"/>
```

4. And call this code for start Compressing your Video
```
    File file = new File(videoUri.getPath());
    VideoCompressService.start(context, file.getAbsolutePath(), AstrntSDK.getCurrentQuestion().getId());
```

After Video Compress success, that will be triggered **SingleVideoUploadService** for automatically upload to **Astronaut Server**

###### For more detail you can see our sample.
