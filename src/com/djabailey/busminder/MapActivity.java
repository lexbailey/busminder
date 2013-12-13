package com.djabailey.busminder;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class MapActivity extends FragmentActivity {

    private GoogleMap mMap;
    private LatLng SelectedLocation;
    private Marker BusStop;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map);
        setUpMapIfNeeded();
        Button btnSelect = (Button)findViewById(R.id.btnSelect);
        btnSelect.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				jobDone();
				
			}
		});
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    private void jobDone(){
    	Intent result = new Intent("com.djabailey.RESULT_ACTION");
    	result.putExtra("Lat", SelectedLocation.latitude);
    	result.putExtra("Lng", SelectedLocation.longitude);
    	setResult(Activity.RESULT_OK, result);
    	finish();
    }
    
    private void setUpMap() {
        BusStop = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Bus Stop"));
        SelectedLocation = new LatLng(54,-3);
        mMap.setOnMapClickListener(new OnMapClickListener() {
			
			@Override
			public void onMapClick(LatLng arg0) {
				SelectedLocation = new LatLng(arg0.latitude,arg0.longitude);
				BusStop.setPosition(arg0);
			}
		});
		
    }
    
}

