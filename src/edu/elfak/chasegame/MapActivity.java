package edu.elfak.chasegame;

//import java.util.ArrayList;
import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
/*import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;*/
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

public class MapActivity extends FragmentActivity {

	SupportMapFragment mapFragment;
	private GoogleMap mMap;
	private MapUpdateReceiver dataUpdateReceiver;
	private HashMap<String, Marker> playerMarkers;
	private HashMap<String, Marker> itemMarkers;
	private Polygon boundaries;
	IntentFilter intentFilter;
	private ToggleButton screenLockButton;
	
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
		
		//if(markers == null)
			playerMarkers = new HashMap<String, Marker>();
			itemMarkers = new HashMap<String, Marker>();
			
		screenLockButton = (ToggleButton) findViewById(R.id.screenLockButton);
	}
	
	private void drawItems(ArrayList<ObjectOnMap> items, BitmapDescriptor icon){
		for (int i = 0; i < items.size(); i++) {
			MarkerOptions markerOptions = new MarkerOptions();
			markerOptions.position(items.get(i).getLatlng());
			markerOptions.icon(icon);
			markerOptions.title(items.get(i).getName());
			
			Marker marker = mMap.addMarker(markerOptions);
					
			itemMarkers.put(items.get(i).getId(), marker);
		}
	}
	
	public void onResume(){
		if (dataUpdateReceiver == null) dataUpdateReceiver = new MapUpdateReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction("UPDATE_MAP_OBJECT_TAG");
		intentFilter.addAction("UPDATE_MAP_TAG");
		intentFilter.addAction("DRAW_ITEMS");
		registerReceiver(dataUpdateReceiver, intentFilter);
		
		sendBroadcast(new Intent("REQ_INITIALISE_DATA"));
		
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
	    	String action = intent.getAction();
	    	
	    	if(action.equals("UPDATE_MAP_TAG")&&(screenLockButton.isChecked())){
	    		LatLng latLng = (LatLng) intent.getExtras().get("location");
	        	mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));
	    	}
	    	else if(action.equals("UPDATE_MAP_OBJECT_TAG")){
	    		LatLng latLng = (LatLng) intent.getExtras().get("location");
	    		String id =  intent.getExtras().getString("objectId");
	    		Log.v("UPDATE MARKER",id + " " + latLng.toString());
	    		Marker marker = playerMarkers.get(id);
	    		if(marker == null){
	    			MarkerOptions markerOptions = new MarkerOptions();
	    			markerOptions.position(latLng);
	    			markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.androidmarker));
	    			markerOptions.title(id);		
	    			marker = mMap.addMarker(markerOptions);		
	    			playerMarkers.put(id, marker);
	    		}
	    		else{
	    			marker.setPosition(latLng);
	    		}
	    	}
	    	else if(action.equals("DRAW_ITEMS")){
	    		ArrayList<ObjectOnMap> items = intent.getExtras().getParcelableArrayList("items");
	    		ArrayList<ObjectOnMap> buildings = intent.getExtras().getParcelableArrayList("buildings");
	    		drawItems(items,BitmapDescriptorFactory.fromResource(R.drawable.wooden_crate));
	    		drawItems(buildings,BitmapDescriptorFactory.fromResource(R.drawable.building));
	    		boundaries = drawBoundaries((LatLng) intent.getExtras().get("mapCenter"), mMap);
	    	}
	    }
	}


}

