package com.example.actuallyradarnow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.app.Activity;
import android.os.AsyncTask;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;

public class MainActivity extends Activity {

    public String radarSite;
    public int rSelected = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialLoad();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actions, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public Dialog onCreateDialogSingleChoice() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick Radar")
               .setSingleChoiceItems(R.array.radarNames, rSelected,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which != 0) {
                                    String[] radarIdent = getResources().getStringArray(R.array.radarIdents);
                                    radarSite = radarIdent[which];
                                }
                                rSelected = which;
                            }
                        })

               .setPositiveButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        webViewLoad();
                    }
               })

               .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
               });

        return builder.create();
    }

    public void initialLoad() {
        WebView radar_view = new WebView(this);
        setContentView(radar_view);
        radar_view.getSettings().setBuiltInZoomControls(true);
        radar_view.getSettings().setDisplayZoomControls(false);
        radar_view.getSettings().setLoadWithOverviewMode(false);
        radar_view.getSettings().setUseWideViewPort(true);
        radar_view.loadUrl("http://radar.weather.gov/Conus/RadarImg/latest.gif");
    }

    public void webViewLoad() {
        WebView radar_view = new WebView(this);
        setContentView(radar_view);
        radar_view.getSettings().setBuiltInZoomControls(true);
        radar_view.getSettings().setDisplayZoomControls(false);
        radar_view.getSettings().setLoadWithOverviewMode(false);
        radar_view.getSettings().setUseWideViewPort(true);

        if (rSelected != 0) {
          radar_view.loadUrl("http://radar.weather.gov/lite/N0R/" + radarSite + "_0.png");
        } else {
            radar_view.loadUrl("http://radar.weather.gov/Conus/RadarImg/latest.gif");
        }
        pauseGone();
    }

    public void loop() {
        WebView radar_view = new WebView(this);
        setContentView(radar_view);
        radar_view.getSettings().setBuiltInZoomControls(true);
        radar_view.getSettings().setDisplayZoomControls(false);
        radar_view.getSettings().setLoadWithOverviewMode(false);
        radar_view.getSettings().setUseWideViewPort(false);
        if (rSelected != 0) {
            radar_view.loadUrl("http://radar.weather.gov/lite/N0R/" + radarSite + "_loop.gif");
        } else {
            radar_view.loadUrl("http://radar.weather.gov/Conus/Loop/NatLoop.gif");
        }
        playGone();
    }

    public void playGone() {
        findViewById(R.id.action_play).setVisibility(View.GONE);
        findViewById(R.id.action_pause).setVisibility(View.VISIBLE);
    }

    public void pauseGone() {
        findViewById(R.id.action_play).setVisibility(View.VISIBLE);;
        findViewById(R.id.action_pause).setVisibility(View.GONE);;
    }

    public void getLocation() {
        LocationWorker locationTask = new LocationWorker();
        locationTask .execute(new Boolean[] {true});

    }

    public void getClosestRadar(float latitude, float longitude) {

        String[] radarIdent = getResources().getStringArray(R.array.radarIdents);
        String[] radarLat = getResources().getStringArray(R.array.radarLat);
        String[] radarLong = getResources().getStringArray(R.array.radarLong);
        int i;
        double distanceLow = 141.0;
        for (i = 0; i < 154; i++) {
            double distance = getDistanceFromLatLon(latitude, longitude, Float.parseFloat(radarLat[i]), Float.parseFloat(radarLong[i]));
            if (distance < distanceLow) {
                distanceLow = distance;
                rSelected = ++i;
                radarSite = radarIdent[rSelected];
            }
        }
        webViewLoad();


    }

    public double getDistanceFromLatLon(double lat1, double lon1, double lat2, double lon2) {
        double dLat = ( (lat2-lat1) * (Math.PI/180) ); //delta latitude
        double dLon = ( (lon2-lon1) * (Math.PI/180) ); //delta longitude
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(lat1 * (Math.PI/180)) * Math.cos(lat2 * (Math.PI/180)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return 3959 * c; //distance in miles
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        switch (item.getItemId()) {

            case R.id.action_pause:
                webViewLoad();
                return true;

            case R.id.action_play:
                loop();
                return true;

            case R.id.action_radarpick:
                Dialog dialog = onCreateDialogSingleChoice();
                dialog.show();
                return true;

            case R.id.action_getlocation:
                getLocation();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }
    class LocationWorker extends AsyncTask<Boolean, Integer, Boolean> {
        LocationHelper myLocationHelper = new LocationHelper(MainActivity.this);
        @Override
        protected void onPreExecute() {}

        @Override
        protected void onPostExecute(Boolean result) {
                float currentLat = myLocationHelper.getLat();
                float currentLong = myLocationHelper.getLong();
                getClosestRadar(currentLat, currentLong);
        }

        @Override
        protected Boolean doInBackground(Boolean... params) {

            //while the location helper has not got a lock
            while(myLocationHelper.gotLocation() == false){
                //do nothing, just wait
            }
            //once done return true
            return true;
        }
    }
}
