package com.mobileproto.lab3;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by chris on 9/21/13.
 */
public class PulseActivity extends Activity {
    double prevLat, prevLong;
    double prevTime, curTime;
    double curLat, curLong;
    double gps_velocity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        final GPS gps = new GPS(this);

        prevLat = gps.getLatitude();
        prevLong = gps.getLongitude();
        prevTime = SystemClock.uptimeMillis();

        Button sendPulse = (Button) findViewById(R.id.pulse_button);

        sendPulse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    URL url = new URL("http://www.vogella.com");
                    final HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    readData(con.getInputStream());
                }catch (Exception e) {
                    e.printStackTrace();
                }
            };
        });

        Thread vel = new Thread(){
            public void run(){
                try {
                    while(!isInterrupted()){
                        curLat = gps.getLatitude();
                        curLong = gps.getLongitude();
                        curTime = SystemClock.uptimeMillis();

                        gps_velocity = Math.sqrt((curLat-prevLat)*(curLat-prevLat)+(curLong-prevLong)*(curLong-prevLong))*6371000.0/((curTime-prevTime)/1000.0);

                        Log.e("Latitude", String.valueOf(curLat));
                        Log.e("Longitude", String.valueOf(curLong));
                        Log.e("Velocity",  String.valueOf(gps_velocity));


                        //Update website with information here

                    }Thread.sleep(1000);
                }catch (InterruptedException e){};
            }};vel.start();


        Thread server = new Thread(){
            public void run(){

            }};if(isNetworkAvailable()){server.start();}
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public ArrayList<Double> readData(InputStream in){
        BufferedReader reader = null;
        int prev = 0;
        ArrayList<Double> data = new ArrayList<Double>();
        try{
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
            while ((line = reader.readLine())!=null){
                for (int i = 0; i < line.length(); i++){
                    if (line.charAt(i) == '-'  || line.charAt(i) == '|'){
                        data.add(Double.parseDouble(line.substring(prev,i)));prev = i;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }
        }
        return data;
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
}
