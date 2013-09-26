package com.mobileproto.lab4;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class SelfActivity extends Activity {
    double prevLat, prevLong;
    double prevTime, curTime;
    double curLat, curLong;
    double gps_velocity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self);

        //Go to MapActivity
        Button goMap = (Button) findViewById(R.id.map_button);
        goMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(i);
            }
        });Log.e("Map Button","Successfully Initialized");

        //****************************************************************//

        //explicitly enable GPS
        Intent enableGPS = new Intent(
                "android.location.GPS_ENABLED_CHANGE");
        enableGPS.putExtra("enabled", true);
        sendBroadcast(enableGPS);
        Log.e("GPS","Successfully Enabled");

        //Check Network
        checkNetwork();

        //****************************************************************//

        //Initiate Timer
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask(){
            final String phoneName = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
            //Initialize GPS and grab views
            final GPS gps = new GPS(getApplicationContext());
            final TextView velocity = (TextView) findViewById(R.id.velocity_display);
            final TextView location = (TextView) findViewById(R.id.location_display);
            @Override
            public void run(){
                new AsyncTask<Void, Void, Void>(){
                    @Override
                    protected void onPreExecute(){
                        prevLat = gps.getLatitude();
                        prevLong = gps.getLongitude();
                        prevTime = SystemClock.uptimeMillis();
                        Log.e("GPS","Successfully Grabbed Data");
                    }
                    protected Void doInBackground(Void... voids){
                        curLat = gps.getLatitude();
                        curLong = gps.getLongitude();
                        curTime = SystemClock.uptimeMillis();

                        gps_velocity = convert(prevLat, prevLong, curLat,curLong)*1000/(curTime - prevTime);

                        Log.e("Latitude",  String.valueOf(curLat));
                        Log.e("Longitude", String.valueOf(curLong));
                        Log.e("Velocity",  String.valueOf(gps_velocity));

                        //sendJson(curLat, curLong, gps_velocity,phoneName);

                        prevLat = curLat;
                        prevLong = curLong;
                        prevTime = curTime;
                        publishProgress(voids);
                        return null;
                    }
                    protected void onProgressUpdate(Void... voids){
                        location.setText("Lat:" + String.valueOf(curLat) + "\n Long:" + String.valueOf(curLong));
                        velocity.setText(String.valueOf(gps_velocity) + " m/s");
                        location.invalidate();
                        velocity.invalidate();
                    }
                }.execute();
            }
        },0,500);
    }


    //****************************************************************//
    //Sending a POST to the server.
    protected void sendJson(final double lat, final double lon, final double vel, String phoneName){
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(),5000);
        HttpResponse response;
        JSONObject json = new JSONObject();

        try {
            HttpPost post = new HttpPost("http://10.41.88.218:5000/post");
            json.put("phone", phoneName);
            json.put("lat",lat);
            json.put("lon",lon);
            json.put("vel",vel);
            StringEntity se = new StringEntity(json.toString());
            se.setContentType(new BasicHeader(HTTP.CONTENT_TYPE,"application/json"));
            post.setEntity(se);
            response = client.execute(post);
        }catch (Exception e) {
            e.printStackTrace();
            Log.e("Server", "Cannot Establish Connection");
        }
    }

    //****************************************************************//

    //Check if the phone is connected to the network
    public void checkNetwork() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            Toast.makeText(getApplicationContext(),"Oh nice, you're on the network!",Toast.LENGTH_SHORT).show();
        }
        else{
        Toast.makeText(getApplicationContext(),"The game will run better if you're connected to wifi!",Toast.LENGTH_LONG).show();}
    }

    // Convert Longitude, Latitude degrees/s to m/s
    public double convert(double lat1, double lon1, double lat2, double lon2){  // generally used geo measurement function
        double R = 6378.137; // Radius of earth in KM
        double dLat = (lat2 - lat1) * Math.PI / 180;
        double dLon = (lon2 - lon1) * Math.PI / 180;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(lat1 * Math.PI / 180) * Math.cos(lat2 * Math.PI / 180) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d * 1000; // meters
    }

    //Options menu
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
}