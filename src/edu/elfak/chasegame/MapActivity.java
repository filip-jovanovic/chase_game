package edu.elfak.chasegame;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapActivity extends FragmentActivity {

	SupportMapFragment mapFragment;
	private GoogleMap mMap;
	private static long TIME_DIFFERENCE = 10000;
	private LocationManager locationManager;
	private String provider;
	private long timeOfLastLocation;
	private MapUpdateReceiver dataUpdateReceiver;
	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
	}

	public void onResume(){
		if (dataUpdateReceiver == null) dataUpdateReceiver = new MapUpdateReceiver();
		IntentFilter intentFilter = new IntentFilter("UPDATE_MAP_TAG");
		registerReceiver(dataUpdateReceiver, intentFilter);
		super.onResume();
	}
	
	public void onPause(){
		if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
		super.onPause();
	}

	private Polygon drawBoundaries(LatLng location, GoogleMap myMap){
		
		// Instantiates a new Polygon object and adds points in a counterclockwise
		// order to define a rectangle
		
		PolygonOptions rectOptions = new PolygonOptions();

		double radius = 0.012;
		int numPoints = 30;
		double phase = 2 * Math.PI / numPoints;
		for (int i = 0; i <= numPoints; i++) {
			rectOptions.add(new LatLng(location.latitude + radius * Math.sin(i * phase),
					location.longitude + radius * Math.cos(i * phase)*1.4));
		    }
		// Set the rectangle's stroke color to red
		rectOptions.strokeColor(Color.RED);
		rectOptions.geodesic(true);
		rectOptions.strokeWidth(3);
		// Get back the mutable Polygon
		return myMap.addPolygon(rectOptions);

	}
	
	private class MapUpdateReceiver extends BroadcastReceiver {
	    @Override
	    public void onReceive(Context context, Intent intent) {
	        if (intent.getAction().equals("UPDATE_MAP_TAG")) {
	        	LatLng latLng = (LatLng) intent.getExtras().get("newLocation");
	        	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
				mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.androidmarker)));
	        }
	    }
	}
}

