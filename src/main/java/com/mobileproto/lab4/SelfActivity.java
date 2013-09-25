package com.mobileproto.lab4;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.app.Activity;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

public class SelfActivity extends Activity {
    double prevLat, prevLong;
    double prevTime, curTime;
    double curLat, curLong;
    double gps_velocity;
    Thread vel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_self);
        Log.e("View","Successfully set Content View");
        /*
        try{StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);}catch (Exception e){}
        */

        Log.e("Policy","Successfully set Policy");
        //Go to Second Activity: Map
        Button goMap = (Button) findViewById(R.id.map_button);
        goMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vel.interrupt(); // Interrupts the updates.
                Intent i = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(i);
            }
        });Log.e("Map Button","Successfully Initialized");

        // explicitly enable GPS
        Intent enableGPS = new Intent(
                "android.location.GPS_ENABLED_CHANGE");
        enableGPS.putExtra("enabled", true);
        sendBroadcast(enableGPS);
        Log.e("GPS","Successfully Enabled");

        //Initialize GPS and grab views
        final GPS gps = new GPS(this);
        final TextView velocity = (TextView) findViewById(R.id.velocity_display);
        final TextView location = (TextView) findViewById(R.id.gps_display);
        Log.e("GPS","Successfully Initialized");

        //Grab Initial Data
        prevLat = gps.getLatitude();
        prevLong = gps.getLongitude();
        prevTime = SystemClock.uptimeMillis();
        Log.e("GPS","Successfully Grabbed Data");

        //Thread
        vel = new Thread(){
            public void run(){
                try {
                    final String phoneName = ((TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
                    while(!isInterrupted()){
                        runOnUiThread( new Runnable() {
                            @Override
                            public void run() {
                                curLat = gps.getLatitude();
                                curLong = gps.getLongitude();
                                curTime = SystemClock.uptimeMillis() + 120;

                                gps_velocity = convert(prevLat, prevLong, curLat,curLong);//Math.sqrt((curLat-prevLat)*(curLat-prevLat)+(curLong-prevLong)*(curLong-prevLong))*6371000.0/((curTime-prevTime)/1000.0);

                                Log.e("Latitude",  String.valueOf(curLat));
                                Log.e("Longitude", String.valueOf(curLong));
                                Log.e("Velocity",  String.valueOf(gps_velocity));

                                location.setText("Lat:" + String.valueOf(curLat) + "\n Long:" + String.valueOf(curLong));
                                velocity.setText(String.valueOf(gps_velocity) + " m/s");
                                location.invalidate();
                                velocity.invalidate();
                                //sendJson(curLat, curLong, gps_velocity,phoneName);

                                prevLat = curLat;
                                prevLong = curLong;
                                prevTime = curTime;
                            }});Thread.sleep(100);
                        }
                    }catch (InterruptedException e){Log.e("ServerThread","Stopped");}
                }};Log.e("ServerThread","Successfully created server thread.");vel.start();
    }
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    protected void sendJson(final double lat, final double lon, final double vel, String phoneName){
        HttpClient client = new DefaultHttpClient();
        HttpConnectionParams.setConnectionTimeout(client.getParams(),5000);
        HttpResponse response;
        JSONObject json = new JSONObject();

        try {
            HttpPost post = new HttpPost("http://10.41.24.16/post");
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

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }

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
}
