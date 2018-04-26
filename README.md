Astronaut Q & A SDK for Android

minSdkVersion 21

==================
add this line in gradle.properties

BetaApiUrl=http://beta.astrnt.co/api/v2/
LiveApiUrl=http://app.astrnt.co/api/v2/

==================
add this line in build.gradle

    flavorDimensions "mode"
    productFlavors {
        beta {
            dimension "mode"
            applicationIdSuffix ".betasdk"
            buildConfigField "boolean", "BETA", "true"
            buildConfigField("String", "API_URL", "\"${BetaApiUrl}\"")
        }
        live {
            dimension "mode"
            buildConfigField "boolean", "BETA", "false"
            buildConfigField("String", "API_URL", "\"${LiveApiUrl}\"")
        }
    }

==================
SetUp SDK in your Application Class

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
==================

add this code for enable service compress and upload to your AndroidManifest.xml
    <service android:name="co.astrnt.qasdk.videocompressor.services.VideoCompressService"/>
    <service android:name="co.astrnt.qasdk.upload.SingleVideoUploadService"/>
==================

and call this code for start Compressing your Video
    File file = new File(videoUri.getPath());
    VideoCompressService.start(context, file.getAbsolutePath(), AstrntSDK.getCurrentQuestion().getId());

after video compress success, that will be triggered SingleVideoUploadService
