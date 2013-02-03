package edu.elfak.chasegame;

import java.util.ArrayList;
import java.util.HashMap;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
//import android.util.Log;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
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
	private ImageView radarThiefIcon;
	private ArrayList<ImageView> radarCopIcons;
	
	
	private View jammerButton;
	private View vestButton;
	
	static boolean  jammerButtonEnabled =  false;
	static boolean vestButtonEnabled = false;
	
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
		jammerButton =  findViewById(R.id.jammerButton);
		vestButton = findViewById(R.id.vestButton);

		radarThiefIcon = (ImageView) findViewById(R.id.radarThief);
		radarCopIcons = new ArrayList<ImageView>();
		radarCopIcons.add((ImageView) findViewById(R.id.radarCop1));
		radarCopIcons.add((ImageView) findViewById(R.id.radarCop2));
		radarCopIcons.add((ImageView) findViewById(R.id.radarCop3));
		
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
			jammerButton.setOnClickListener(this);
			vestButton.setOnClickListener(this);
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

	@Override
	public void onClick(View v) {
		switch(v.getId()){
		case(R.id.shootButton):
			sendBroadcast(new Intent("SHOT_IS_FIRED"));
			break;
		case(R.id.vestButton):
			 vestButton.setVisibility(View.GONE);
			sendBroadcast(new Intent("BECAME_BULLETPROOF"));
			break;
		case(R.id.jammerButton):
			jammerButton.setVisibility(View.GONE);
			sendBroadcast(new Intent("ACTIVATE_JAMMER"));
		}
	}
	
	private void drawItems(ArrayList<ObjectOnMap> items){
		for (int i = 0; i < items.size(); i++) {
			ObjectOnMap object = items.get(i);
			MarkerOptions markerOptions = new MarkerOptions();
			markerOptions.visible(true);
			markerOptions.position(items.get(i).getLatlng());
			markerOptions.title(items.get(i).getName());
			if(object.getType().equals("item")){
				if(object.getName().equals("Pancir"))
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.vest_icon));
				else if(object.getName().equals("Ometac"))
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.jammer_icon));
				else
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.wooden_crate));
			}
				
			else{			
				if(object.isBank()){
					if(object.getValue()>0)
						markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.dollar_icon));
					else
						markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bank_robed));
				}
					
				if(object.isPoliceStation())
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.policestation));
				if(object.isSafeHouse())
					markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.safehouse));
			}
				
			Marker marker = mMap.addMarker(markerOptions);
			Log.v("MARKER ADDED",marker.getId() +" "  + marker.getTitle() + " "  + marker.getPosition().toString());		
			itemMarkers.put(items.get(i).getId(), marker);
		}
	}
	
	@Override
	public void onResume(){
		if (dataUpdateReceiver == null) dataUpdateReceiver = new MapUpdateReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction("UPDATE_MAP_OBJECT_TAG");
		intentFilter.addAction("REMOVE_MAP_OBJECT_TAG");
		intentFilter.addAction("UPDATE_MAP_TAG");
		intentFilter.addAction("DRAW_ITEMS_TAG");
		intentFilter.addAction("BULLETS_UPDATE_TAG");
		intentFilter.addAction("ENABLE_VEST_BUTTON_TAG");
		intentFilter.addAction("ENABLE_JAMMER_BUTTON_TAG");
		registerReceiver(dataUpdateReceiver, intentFilter);
		
		if(itemMarkers.size()==0)
			sendBroadcast(new Intent("REQ_INITIALISE_DATA"));
	
		jammerButton.setEnabled(jammerButtonEnabled);
		vestButton.setEnabled(vestButtonEnabled);
		Log.v("JAMMER BUTTON","" + jammerButtonEnabled);
		
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
	    
		public void updateRadar(Context context, ArrayList<Double> thiefDistance, ArrayList<Double> policemanDistance){
			DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
    		int padding = (int)((12 * displayMetrics.density) + 0.5);
    		//thief icon
    		int x;
    		double density = 0.62*displayMetrics.density;
    		int displacement = (int)(12*displayMetrics.density);
    		if (thiefDistance.get(0)<200)
    			x = (int)(thiefDistance.get(0)*density);
    		else x = (int)(200*density);
    		//int leftPadding = 18; //(int)(((12+x) * displayMetrics.density) + 0.5);
    		radarThiefIcon.setPadding(x+displacement, padding, 0, 0);
    		//Log.v("TEST","thief="+x);
    		//policeman icons
    		for (int j = 0; j < policemanDistance.size(); j++) {
    			if (policemanDistance.get(j)<200)
        			x = (int)(policemanDistance.get(j)*density);
        		else x = (int)(200*density);
        		//leftPadding = (int)(((12+x) * displayMetrics.density) + 0.5);
        		radarCopIcons.get(j).setPadding(x+displacement, padding, 0, 0);
        		Log.v("TEST","policeman"+j+"="+displayMetrics.density);
    		}
		}
		
		@SuppressWarnings("unchecked")
		@Override
	    public void onReceive(Context context, Intent intent) {
	    	String action = intent.getAction();
	    	
	    	if(action.equals("UPDATE_MAP_TAG")){
	    		if((screenLockButton.isChecked())){
	    			LatLng latLng = (LatLng) intent.getExtras().get("location");
	    			mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
	    		}
	    		updateRadar(context,(ArrayList<Double>)intent.getExtras().get("thiefDistance")
	    						   ,(ArrayList<Double>)intent.getExtras().get("policemanDistance"));    		
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
	    	else if(action.equals("REMOVE_MAP_OBJECT_TAG")){
		    	ObjectOnMap item = (ObjectOnMap) intent.getExtras().get("object");
	    		Marker m = itemMarkers.get(item.getId());
	    		Log.v("MARKER DELETE",item.getId() +" "  + m.getTitle() + " "  + m.getPosition().toString());
	    		m.setVisible(false);
	    		m.remove();
	    		
	    		itemMarkers.remove(item.getId());	    		
	    	}
	    	else if(action.equals("ENABLE_VEST_BUTTON_TAG")){
	    		vestButtonEnabled = true;
				vestButton.setEnabled(true);
	    	}
	    	else if(action.equals("ENABLE_JAMMER_BUTTON_TAG")){
	    		jammerButtonEnabled =  true;
				jammerButton.setEnabled(true);
	    	}
	    	else if(action.equals("BANK_ROBBED_UPDATE_MAP")){ 
	    		ObjectOnMap bank = (ObjectOnMap) intent.getExtras().get("bank");
	    		Marker m = itemMarkers.get(bank.getId());
	    		m.setVisible(false);
	    		m.remove();
	    		
	    		MarkerOptions markerOptions = new MarkerOptions();
				markerOptions.visible(true);
				markerOptions.position(bank.getLatlng());
				markerOptions.title(bank.getName());
				markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.bank_robed));
				mMap.addMarker(markerOptions); 		
	    	}
	    	else if(action.equals("DRAW_ITEMS_TAG")){
	    		ArrayList<ObjectOnMap> items = intent.getExtras().getParcelableArrayList("items");
	    		ArrayList<ObjectOnMap> buildings = intent.getExtras().getParcelableArrayList("buildings");
	    		if(items.size()>0)
	    				drawItems(items);
	    		drawItems(buildings);
	    		boundaries = drawBoundaries((LatLng) intent.getExtras().get("mapCenter"), mMap);
	    	}
	    	else if(action.equals("BULLETS_UPDATE_TAG")){
	    		int remainingBullets = intent.getExtras().getInt("remainingBullets");
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
}

