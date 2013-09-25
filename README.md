Lab 3: Yes! We are finally using sensors
===

Just like the title says, in this lab we are finally using sensors. Get excited.

Git & Github
---

Before we start with Android, we'd like to do a quick review of Github.

Why do we use git? _Version Control (:_
Some git commands we would like to cover:

```
git add .
git commit -m "commit message"
git push origin master
git pull
```

Sensors
---

First, a quick overview of the different sensors. And by sensors we are not just going to limit ourselves to the "hardware" sensors, but we are going to touch on a bunch of sensors that you specifically need to ask permission for from your user in order to use.

Yeah, you know that annoying "Can Facebook use your location?" popup. Those things are actually useful...

Android's definition:
<blockquote><p>"The Android sensor framework lets you access many types of sensors. Some of these sensors are hardware-based and some are software-based. Hardware-based sensors are physical components built into a handset or tablet device. They derive their data by directly measuring specific environmental properties, such as acceleration, geomagnetic field strength, or angular change. Software-based sensors are not physical devices, although they mimic hardware-based sensors. Software-based sensors derive their data from one or more of the hardware-based sensors and are sometimes called virtual sensors or synthetic sensors. The linear acceleration sensor and the gravity sensor are examples of software-based sensors. Table 1 summarizes the sensors that are supported by the Android platform."</p>
<p><cite>- Android [Intro to Sensors](http://developer.android.com/guide/topics/sensors/sensors_overview.html)</cite></p></blockquote>

Some sensors that we are going to cover:

* Internet (who knew)
* Flashlight
* Camera
* GPS

First, lets talk about how these are used in apps. (Make a list on the board on functionality and apps that people have use these sensors in)

Next, there are a few things that you should be aware of when working with sensors:
* Don't test your code on the emulator
* Verify sensors before you use them
* Choose sensor delays carefully

Read more at Android's [Intro to Sensors](http://developer.android.com/guide/topics/sensors/sensors_overview.html)

### Permissions

Next, lets talk about permissions.

The new Android Manifest... (is quite a manifest)

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.mobileproto.lab3"
    android:versionCode="1"
    android:versionName="1.0" >

    <uses-sdk
        android:minSdkVersion="7"
        android:targetSdkVersion="18" />
    <uses-permission
        android:name="android.permission.FLASHLIGHT"/>
    <uses-permission
        android:name="android.permission.INTERNET"/>
    <uses-permission
        android:name="android.permission.ACCESS_COARSE_LOCATION"/>
    <uses-permission
        android:name="android.permission.ACCESS_FINE_LOCATION"/>
    <uses-permission
        android:name="android.permission.CAMERA"/>
    <uses-permission
        android:name="android.permission.RECORD_AUDIO"/>

    <application
        android:allowBackup="true"
        android:icon="@drawable/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/AppTheme" >
        <activity
            android:name="com.mobileproto.lab3.MainActivity"
            android:label="@string/app_name" >
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
        <activity
            android:name="com.mobileproto.lab3.CameraActivity"/>
        <activity
            android:name="com.mobileproto.lab3.GPSActivity"/>
        <activity
            android:name="com.mobileproto.lab3.MicrophoneActivity"/>
        <activity
            android:name="com.mobileproto.lab3.FlashLightActivity"/>
        <activity
            android:name="com.mobileproto.lab3.InternetActivity"/>
    </application>

</manifest>
```

Notice that you need to explicitly ask the user for all of these permissions. It is the #1 debugging problem with sensors (aka people forget to ask for permission and then cannot figure out why they can't access the sensor), so just keep it in the back of your mind.

### GPS

Next, let's take a look at one of the sensors, GPS. Note that the classes used here are specifically for use with this lab, i.e. we provide several wrapper classes. Working with location is hard! We won't reinvent the wheel.

```java
public class GPSActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);

        final GPS gps = new GPS(this);

        final Button button = (Button) findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gps.canGetLocation()){
                    TextView lat = (TextView) findViewById(R.id.lat);
                    TextView lon = (TextView) findViewById(R.id.lon);
                    lat.setText("Lattitude: " + String.valueOf(gps.getLatitude()));
                    lon.setText("Longitude: " + String.valueOf(gps.getLongitude()));
                    Log.v("Lattitude", String.valueOf(gps.getLatitude()));
                    new RemoteGeocode(){
                        @Override
                        public void onPostExecute(String result){
                            TextView xml = (TextView) findViewById(R.id.textView);
                            xml.setText(result);
                        }
                    }.execute(RemoteGeocode.generateUri(gps.getLatitude(), gps.getLongitude()));
                }
                button.setText("Refresh");
            }
        });


    }

}
```

### Camera

A more complicated sensor such as the camera involves its own UI. This way of invoking and using hte sensor uses an external Intent... again, much of the code here is used by helper classes in the code we provide. Let us know any questions you have about how to get them working.

```java
public class CameraActivity extends Activity {


    private static final int TAKE_PICTURE = 0;
    private Uri mUri;
    private Bitmap mPhoto;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        Button take_picture = (Button) findViewById(R.id.takePicture);

        take_picture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
                File f = new File(Environment.getExternalStorageDirectory(), System.currentTimeMillis()/1000L + ".jpg");
                i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
                mUri = Uri.fromFile(f);
                startActivityForResult(i, TAKE_PICTURE);
            }
        });

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case TAKE_PICTURE:
                if (resultCode == Activity.RESULT_OK) {
                    getContentResolver().notifyChange(mUri, null);
                    ContentResolver cr = getContentResolver();
                    try {
                        mPhoto = android.provider.MediaStore.Images.Media.getBitmap(cr, mUri);
                        ((ImageView)findViewById(R.id.photoHolder)).setImageBitmap(mPhoto);
                    } catch (Exception e) {
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }
}
```

Homework
===

Now this is where you guys get to start being creative. Your task is to create a coherent app with the following:

* At least two views
* A way to store your important sensor data
* A way to integrate at least two sensors
* The actual idea behind the app itself is up to you

It is due on Monday at the start of class.
