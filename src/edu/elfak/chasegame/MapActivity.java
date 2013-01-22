package edu.elfak.chasegame;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
//import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

public class MapActivity extends FragmentActivity implements OnClickListener {

	SupportMapFragment mapFragment;
	private GoogleMap mMap;
	private MapUpdateReceiver dataUpdateReceiver;
	private HashMap<String, Marker> playerMarkers;
	private HashMap<String, Marker> itemMarkers;
	private Polygon boundaries;
	IntentFilter intentFilter;
	private ToggleButton screenLockButton;
	
	private ImageView bullet;
	private ImageButton shootButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		
		playerMarkers = new HashMap<String, Marker>();
		itemMarkers = new HashMap<String, Marker>();
			
		screenLockButton = (ToggleButton) findViewById(R.id.screenLockButton);
		
		View imBut;
		if(GameService.isThief){
			imBut =  findViewById(R.id.shootButton);
			imBut.setVisibility(View.GONE);
			imBut =  findViewById(R.id.jammerButton);
			imBut.setVisibility(View.VISIBLE);
			imBut = findViewById(R.id.vestButton);
			imBut.setVisibility(View.VISIBLE);
			imBut = findViewById(R.id.bullet1);
			imBut.setVisibility(View.GONE);
			imBut = findViewById(R.id.bullet2);
			imBut.setVisibility(View.GONE);
			imBut = findViewById(R.id.bullet3);
			imBut.setVisibility(View.GONE);
		}
		else{
			imBut =  findViewById(R.id.jammerButton);
			imBut.setVisibility(View.GONE);
			imBut = findViewById(R.id.vestButton);
			imBut.setVisibility(View.GONE);
			shootButton = (ImageButton) findViewById(R.id.shootButton);
			shootButton.setOnClickListener(this);
		}
	}
	
	private void drawItems(ArrayList<ObjectOnMap> items){
		for (int i = 0; i < items.size(); i++) {
			MarkerOptions markerOptions = new MarkerOptions();
			markerOptions.position(items.get(i).getLatlng());
			markerOptions.title(items.get(i).getName());
			if(items.get(i).getType().compareTo("item")==0)
				markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.wooden_crate));
			else{			
				if(items.get(i).isBank())
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.dollar_icon));
				if(items.get(i).isPoliceStation())
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.policestation));
				if(items.get(i).isSafeHouse())
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.safehouse));
			}
				
			Marker marker = mMap.addMarker(markerOptions);
					
			itemMarkers.put(items.get(i).getId(), marker);
		}
	}
	
	@Override
	public void onResume(){
		if (dataUpdateReceiver == null) dataUpdateReceiver = new MapUpdateReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction("UPDATE_MAP_OBJECT_TAG");
		intentFilter.addAction("UPDATE_MAP_TAG");
		intentFilter.addAction("DRAW_ITEMS");
		intentFilter.addAction("BULLETS_UPDATE");
		registerReceiver(dataUpdateReceiver, intentFilter);
		
		sendBroadcast(new Intent("REQ_INITIALISE_DATA"));
		
		super.onResume();
	}
	
	@Override
	public void onPause(){
		if (dataUpdateReceiver != null) unregisterReceiver(dataUpdateReceiver);
		super.onPause();
	}

	private Polygon drawBoundaries(LatLng location, GoogleMap myMap){
		PolygonOptions rectOptions = new PolygonOptions();
		double radius = 0.01;
		int numPoints = 30;
		double phase = 2 * Math.PI / numPoints;
		for (int i = 0; i <= numPoints; i++) {
			rectOptions.add(new LatLng(location.latitude + radius * Math.sin(i * phase),
					location.longitude + radius * Math.cos(i * phase)*1.4));
		    }
		rectOptions.strokeColor(Color.RED);
		rectOptions.geodesic(true);
		rectOptions.strokeWidth(3);
		return myMap.addPolygon(rectOptions);

	}
	
	private class MapUpdateReceiver extends BroadcastReceiver {
	    

		@Override
	    public void onReceive(Context context, Intent intent) {
	    	String action = intent.getAction();
	    	
	    	if(action.equals("UPDATE_MAP_TAG")&&(screenLockButton.isChecked())){
	    		LatLng latLng = (LatLng) intent.getExtras().get("location");
	    		double distance = GameService.calculateDistance(
	    				mMap.getCameraPosition().target, latLng);
	    		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
	        	
	    	}
	    	else if(action.equals("UPDATE_MAP_OBJECT_TAG")){
		    	ObjectOnMap player = (ObjectOnMap) intent.getExtras().get("object");
	    		Marker marker = playerMarkers.get(player.getId());
	    		if(marker == null){
	    			MarkerOptions markerOptions = new MarkerOptions();
	    			markerOptions.position(player.getLatlng());
	    			if(player.getName().compareTo("thief")==0)
	    				markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.thief));
	    			else
	    				markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.policeman));
	    			markerOptions.title(player.getName());		
	    			marker = mMap.addMarker(markerOptions);		
	    			playerMarkers.put(player.getId(), marker);
	    		}
	    		else{
	    			marker.setPosition(player.getLatlng());
	    		}
	    	}
	    	else if(action.equals("DRAW_ITEMS")){
	    		ArrayList<ObjectOnMap> items = intent.getExtras().getParcelableArrayList("items");
	    		ArrayList<ObjectOnMap> buildings = intent.getExtras().getParcelableArrayList("buildings");
	    		drawItems(items);
	    		drawItems(buildings);
	    		boundaries = drawBoundaries((LatLng) intent.getExtras().get("mapCenter"), mMap);
	    	}
	    	else if(action.equals("BULLETS_UPDATE")){
	    		int remainingBullets = intent.getExtras().getInt("remainingBullets");
	    		//Log.v("remaining",String.valueOf(remainingBullets));
		    	if(remainingBullets == 3){
		    		bullet = (ImageView) findViewById(R.id.bullet1);
		    		bullet.setAlpha(255);
		    		bullet = (ImageView) findViewById(R.id.bullet2);
		    		bullet.setAlpha(255);
		    		bullet = (ImageView) findViewById(R.id.bullet3);
		    		bullet.setAlpha(255);
		    		shootButton.setClickable(true);
		    		shootButton.setEnabled(true);
		    	}	
		    	if(remainingBullets == 2){
		    		bullet = (ImageView) findViewById(R.id.bullet1);
		    		bullet.setAlpha(255);
		    		bullet = (ImageView) findViewById(R.id.bullet2);
		    		bullet.setAlpha(255);
		    		bullet = (ImageView) findViewById(R.id.bullet3);
		    		bullet.setAlpha(100);
		    	}
		    	if(remainingBullets == 1){
		    		bullet = (ImageView) findViewById(R.id.bullet1);
		    		bullet.setAlpha(255);
		    		bullet = (ImageView) findViewById(R.id.bullet2);
		    		bullet.setAlpha(100);
		    		bullet = (ImageView) findViewById(R.id.bullet3);
		    		bullet.setAlpha(100);
		    	}
		    	if(remainingBullets == 0){
		    		bullet = (ImageView) findViewById(R.id.bullet1);
		    		bullet.setAlpha(100);
		    		bullet = (ImageView) findViewById(R.id.bullet2);
		    		bullet.setAlpha(100);
		    		bullet = (ImageView) findViewById(R.id.bullet3);
		    		bullet.setAlpha(100);
		    		
		    		shootButton.setClickable(false);
		    		shootButton.setEnabled(false);
		    	}
	    	}
	    }
	}

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case(R.id.shootButton):
			sendBroadcast(new Intent("SHOT_IS_FIRED"));
		//	Log.v("","Shot");
			break;
		}
	}

}

