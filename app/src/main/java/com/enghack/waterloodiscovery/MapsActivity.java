package com.enghack.waterloodiscovery;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.enghack.waterloodiscovery.Entity.User;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMyLocationButtonClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class MapsActivity extends AppCompatActivity
        implements
        OnMyLocationButtonClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback,
        GoogleMap.OnMarkerClickListener,
        LocationListener {
    private static final LatLng WATERLOO1 = new LatLng(43.4643, -80.5204);
    private Marker mWaterloo1;
    private static final LatLng WATERLOO2 = new LatLng(43.4644, -80.5204);
    private Marker mWaterloo2;
    private GoogleApiClient client;
    private GoogleMap mMap;
    private static TextView desctext;

    /**
     * Request code for location permission request.
     */
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    /**
     * Flag indicating whether a requested permission has been denied after returning in
     */
    private boolean mPermissionDenied = false;

    protected LocationManager locationManager;
    protected LocationListener locationListener;
    protected Context context;

    protected String latitude, longitude;
    protected boolean gps_enabled, network_enabled;

    private static HttpClient client2;
    private static HttpPost request;
    private static HttpEntity entity;
    private static HttpResponse response;

    private static Intent intent_ivtime;
    private static User loginuser;
    private static String desc;
    private static boolean ret;

    private static String urlbase;
    private static String url;
    private static String url2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

        urlbase = this.getResources().getString(R.string.url);
        url = urlbase + "checkin";
        url2 = urlbase + "start";

        desctext = (TextView) findViewById(R.id.desc);
        desctext.setText(desc);

        loginuser = (User) getIntent().getSerializableExtra("loginuser");
        if(loginuser.isProgress() == false){
            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);

            if(loginuser.getTask_id() == 0){
                builder.setTitle("");

                builder.setMessage("Do you want to explor waterloo?");
            }else{
                builder.setTitle("Do you want continue?");

                builder.setMessage("Current round:" + loginuser.getTask_id());

            }


            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //continue
                    Log.v("yes", "yessss");
                    loginuser.setTask_id(loginuser.getTask_id());
                    //post start
                    //启动通信线程
                    PostStarConn po = new PostStarConn();
                    po.start();
                }
            });
            builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //logoff
                    desc = "";
                    intent_ivtime = new Intent();
                    intent_ivtime.setClass(MapsActivity.this, LoginActivity.class);
                    MapsActivity.this.startActivity(intent_ivtime);
                }
            });
            builder.show();
        }



        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);


    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;

        // Set a listener for marker click.
        mMap.setOnMarkerClickListener(this);
        enableMyLocation();
        mMap.setOnMyLocationButtonClickListener(this);
        //setUpClusterer();
    }

    private void goToSecondActivity() {
        //jump to second activity
    }

    public boolean onMarkerClick(final Marker marker) {
        /**
         * Called when the user clicks a marker.
         */
        // Retrieve the data from the marker.
        Integer clickCount = (Integer) marker.getTag();
        // Check if a click count was set, then display the click count.
        if (clickCount != null) {
            goToSecondActivity();
        }
        // Return false to indicate that we have not consumed the event and that we wish
        // for the default behavior to occur (which is for the camera to move such that the
        // marker is centered and for the marker's info window to open, if it has one).
        return false;
    }

    private void enableMyLocation() {
        /**
         * Enables the My Location layer if the fine location permission has been granted.
         */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission to access the location is missing.
            PermissionUtils.requestPermission(this, LOCATION_PERMISSION_REQUEST_CODE,
                    Manifest.permission.ACCESS_FINE_LOCATION, true);
        } else {
            if (mMap != null) {
                // Access to the location has been granted to the app.
                mMap.setMyLocationEnabled(true);
            }
        }


        Button sub = (Button) findViewById(R.id.submitlocation);
        LocationSubmit ls = new LocationSubmit();
        sub.setOnClickListener(ls);

    }


    @Override
    public boolean onMyLocationButtonClick() {
        //Toast.makeText(this, "Locating", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        //GoogleMap.getMyLocation();
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMISSION_REQUEST_CODE) {
            return;
        }

        if (PermissionUtils.isPermissionGranted(permissions, grantResults,
                Manifest.permission.ACCESS_FINE_LOCATION)) {
            // Enable the my location layer if the permission has been granted.
            enableMyLocation();
        } else {
            // Display the missing permission error dialog when the fragments resume.
            mPermissionDenied = true;
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        if (mPermissionDenied) {
            // Permission was not granted, display error dialog.
            showMissingPermissionError();
            mPermissionDenied = false;
        }
    }

    private void showMissingPermissionError() {
        /**
         * Displays a dialog with error message explaining that the location permission is missing.
         */
        PermissionUtils.PermissionDeniedDialog
                .newInstance(true).show(getSupportFragmentManager(), "dialog");
    }

    @Override
    public void onStart() {
        super.onStart();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.enghack.waterloodiscovery/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();
        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.enghack.waterloodiscovery/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude() + "";
        longitude = location.getLongitude() + "";
        Log.v("latitude", latitude);
        Log.v("longitude", longitude);
        // Toast.makeText(MapsActivity.this, "Latitude: "+location.getLatitude() + ", Longitude:" +location.getLongitude(), Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    //submit 监听事件
    class LocationSubmit implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(MapsActivity.this, "Submitting location", Toast.LENGTH_SHORT).show();

            //启动通信线程
            LocationSubmitConn lsConn = new LocationSubmitConn();
            lsConn.start();
        }
    }

    //submit location to server
    class LocationSubmitConn extends Thread {

        private String statusMessage = "";
        private String time;

        @Override
        public void run() {
            // 初始化消息循环队列，需要在Handler创建之前
            Looper.prepare();
            client2 = new DefaultHttpClient();
            HttpPost request;
            try {
                request = new HttpPost(new URI(url));
                //json 封装
                JSONObject json = new JSONObject();
                json.put("email", loginuser.getUsername());
                json.put("lat", latitude);
                json.put("lng", longitude);

                StringEntity sentity = new StringEntity(json.toString(), "utf-8");
                request.addHeader("Content-Type", "application/json");
                request.setEntity(sentity);

                try {
                    response = client2.execute(request);
                } catch (Exception e) {
                    statusMessage = "something wrong with network";
                }

                int statecode = response.getStatusLine().getStatusCode();


                Log.v("connect status", String.valueOf(statecode));
                if (statecode == 200) { //请求成功
                    entity = response.getEntity();
                    if (entity != null) {
                        String out = EntityUtils.toString(entity, "UTF-8");
                        Log.i("result from server", out);

                        JSONObject jsonobj = new JSONObject(out);
                        ret = jsonobj.getBoolean("ret");
                        time = jsonobj.getString("time");

                    } else if (entity == null) {
                        //未请求到返回数据
                        statusMessage = "There is no return information from server";
                    }
                }

            } catch (IOException e) {
                //e.printStackTrace();
            } catch (JSONException e) {
                //e.printStackTrace();
            } catch (URISyntaxException e) {
                //e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!statusMessage.equals("")){
                        Toast.makeText(MapsActivity.this, statusMessage , Toast.LENGTH_SHORT).show();
                        }
                        if(ret == true && loginuser.getTask_id() <= 5){
                            AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                            builder.setTitle("Congrat ! Time is: "+ time+".Continue?");

                            loginuser.setTask_id(loginuser.getTask_id()+1);
                            builder.setMessage("Current round:" + loginuser.getTask_id());

                            builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                                @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //continue

                                loginuser.setTask_id(loginuser.getTask_id());
                                //post start
                                //启动通信线程
                                PostStarConn po = new PostStarConn();
                                po.start();
                            }
                        });
                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //logoff
                                intent_ivtime = new Intent();
                                intent_ivtime.setClass(MapsActivity.this, LoginActivity.class);
                                MapsActivity.this.startActivity(intent_ivtime);
                            }
                        });
                        builder.show();

                    }else if(ret == false && loginuser.getTask_id() <= 5){
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setTitle("");

                        loginuser.setTask_id(loginuser.getTask_id());
                        builder.setMessage("Ooops ! You need to try again.");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                    }else{
                        AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
                        builder.setTitle("");

                        loginuser.setTask_id(loginuser.getTask_id()+1);
                        builder.setMessage("Congratulations !! Click Yes to log out.");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                //logoff
                                intent_ivtime = new Intent();
                                intent_ivtime.setClass(MapsActivity.this, LoginActivity.class);
                                MapsActivity.this.startActivity(intent_ivtime);

                            }
                        });
                        builder.show();
                    }
                }
            });
        }

    }

    //post star new round 监听事件
    class PostStar implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            //启动通信线程
            PostStarConn psConn = new PostStarConn();
            psConn.start();


        }
    }

    //post star a round to server
    class PostStarConn extends Thread {

        private String statusMessage = "";

        @Override
        public void run() {
            // 初始化消息循环队列，需要在Handler创建之前
            Looper.prepare();
            client2 = new DefaultHttpClient();
            HttpPost request;
            try {
                request = new HttpPost(new URI(url2));
                //json 封装
                JSONObject json = new JSONObject();
                json.put("email", loginuser.getUsername());
                Log.v("taskid",loginuser.getTask_id()+"");
                json.put("taskid", loginuser.getTask_id());

                StringEntity sentity = new StringEntity(json.toString(), "utf-8");
                request.addHeader("Content-Type", "application/json");
                request.setEntity(sentity);

                try {
                    response = client2.execute(request);
                } catch (Exception e) {
                    statusMessage = "something wrong with network";
                }

                int statecode = response.getStatusLine().getStatusCode();


                Log.v("connect status", String.valueOf(statecode));
                if (statecode == 200) { //请求成功
                    entity = response.getEntity();
                    if (entity != null) {
                        String out = EntityUtils.toString(entity, "UTF-8");
                        Log.i("result from server", out);

                        //json解析
                        JSONObject jsonobj = new JSONObject(out);
                        loginuser.setTask_id(jsonobj.getInt("id"));
                        //TODO
                        desc = jsonobj.getString("desc");

                    } else if (entity == null) {
                        //未请求到返回数据
                        statusMessage = "There is no return information from server";
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(!statusMessage.equals("")){
                        Toast.makeText(MapsActivity.this, statusMessage , Toast.LENGTH_SHORT).show();
                    }
                    desctext.setText(desc);
                }
            });
        }

    }

}
