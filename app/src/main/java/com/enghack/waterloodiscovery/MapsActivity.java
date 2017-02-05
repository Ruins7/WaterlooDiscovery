package com.enghack.waterloodiscovery;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.enghack.waterloodiscovery.Entity.User;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        Button sub = (Button) findViewById(R.id.submitlocation);
        LocationSubmit ls = new LocationSubmit();
        sub.setOnClickListener(ls);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        LatLng waterloo = new LatLng(43.4643, -80.5204);
        mMap.addMarker(new MarkerOptions().position(waterloo).title("Marker in Waterloo"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(waterloo));
    }

    //submit 监听事件
    class LocationSubmit implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Toast.makeText(MapsActivity.this, "Submit location ...", Toast.LENGTH_SHORT).show();

        }
    }

}
