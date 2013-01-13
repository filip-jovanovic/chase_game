package edu.elfak.chasegame;

import android.content.Context;
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

	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		// Get the location manager
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		provider = locationManager.NETWORK_PROVIDER;
		Location lastKnownLocation = locationManager.getLastKnownLocation(provider);

		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		LatLng latLng = new LatLng(lastKnownLocation.getLatitude(),
				lastKnownLocation.getLongitude());
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
		
		LatLng loc = new LatLng(43.321244, 21.895838);
		Polygon p = drawBoundaries(loc, mMap);
		
	}

	/* Request updates at startup */
	@Override
	protected void onResume() {
		super.onResume();
	}

	/* Remove the locationlistener updates when Activity is paused */
	@Override
	protected void onPause() {
		super.onPause();
	}

	
	public void onLocationChanged(Location location) {
		/*LatLng latLng;
		if(TIME_DIFFERENCE<(location.getTime()-timeOfLastLocation))
		{	
			// this location is 10s "away" from last one
			latLng = new LatLng(location.getLatitude(),location.getLongitude());
			
			// set current location on screen to
			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
			timeOfLastLocation = location.getTime();
			// TODO : send it to others
			// put marker
			mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.fromResource(R.drawable.androidmarker)));
		}*/

	}

	public void onProviderDisabled(String provider) {
		
	}

	public void onProviderEnabled(String provider) {
	
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		
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
}
