package edu.elfak.chasegame;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.internal.l;
import com.google.android.gms.maps.model.LatLng;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;

public class GameService extends Service implements LocationListener {
	
		private ArrayList<ObjectOnMap> buildings;
		private ArrayList<ObjectOnMap> items;
		private ArrayList<ObjectOnMap> players;
		public static int gameId;
		private int mapId;
		private String gameName;
		public static int numberOfPolicemen;
		private LocationManager locationManager;
		private String provider;
		private long timeOfLastLocation;
		public static final String GCM_ANNOUNCE_TAG = "announce";
		private static final long TIME_DIFFERENCE = 5000;
		public static String registrationId;
		private String playerName;

		private LatLng mapCenter;
		public static boolean isRuning = false;

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			
			isRuning = true;
			
			buildings = new ArrayList<ObjectOnMap>();
			items = new ArrayList<ObjectOnMap>();
			players = new ArrayList<ObjectOnMap>();
			
			IntentFilter intentFilter = new IntentFilter(GCMIntentService.TAG);
			intentFilter.addAction("REQ_INITIALISE_DATA");
			registerReceiver(broadcastReceiver,intentFilter);
			
			mapId = intent.getExtras().getInt("mapId");
			mapCenter = (LatLng) intent.getExtras().get("mapCenter");
			
			gameName = intent.getExtras().getString("gameName");			
			String role = intent.getExtras().getString("role");
			numberOfPolicemen = 0;
			
			registrationId = intent.getExtras().getBundle("dataBundle").getString("registrationId");
			playerName = intent.getExtras().getBundle("dataBundle").getString("playerName");
			
			if(role.compareTo("thief")==0)
			{
				players.add(new ObjectOnMap(0,0,registrationId,role,"player"));
				ArrayList<String> parameters = new ArrayList<String>();
				ArrayList<String> values = new ArrayList<String>();
				parameters.add("name");
				parameters.add("mapId");
				parameters.add("thief");
				values.add(gameName);
				values.add(String.valueOf(mapId));
				values.add(registrationId);
				String result = "empty";
				result = HTTPHelper.sendValuesToUrl(parameters, values, HTTPHelper.CREATE_GAME_URL);
				Log.v("thief Game id: ",result);
				gameId = Integer.valueOf(result);
			}
			else
			{
				numberOfPolicemen++;
				gameId = intent.getExtras().getInt("game_id");
				String thiefId = intent.getExtras().getString("thief");
				players.add(new ObjectOnMap(0,0,thiefId,"thief","player"));
				
				String cop_1Id = intent.getExtras().getString("cop_1");
				players.add(new ObjectOnMap(0,0,cop_1Id,"policeman1","player"));	
				String cop_2Id = intent.getExtras().getString("cop_2");
				if(cop_2Id.compareTo("empty")!=0)
				{
					players.add(new ObjectOnMap(0,0,cop_2Id,"policeman2","player"));	
					numberOfPolicemen++;
				}
				String cop_3Id = intent.getExtras().getString("cop_3");
				if(cop_3Id.compareTo("empty")!=0)
				{
					players.add(new ObjectOnMap(0,0,cop_3Id,"policeman3","player"));
					numberOfPolicemen++;
				}	
				
			}
			// let other players be informed about new player
			if(role.compareTo("thief")!=0)
				announceNewPlayer(registrationId);
			
			// populate items and buildings from server
			ArrayList<ObjectOnMap> allObjects = HTTPHelper.getBuildingAndItemList(String.valueOf(mapId));
			ObjectOnMap ob = null;
			for(int i = 0; i<allObjects.size(); i++)
			{
				ob = allObjects.get(i);
				if(ob.getType()=="building")
					buildings.add(ob);
				else
					items.add(ob);	
			}
			
		   //registrovanje nove lokacije u tabelu player_locations
		   ArrayList<String> parameters = new ArrayList<String>();
		   ArrayList<String> values = new ArrayList<String>();
		   parameters.add("game_id");
		   parameters.add("player_id");
		   values.add(String.valueOf(gameId));
		   values.add(registrationId);
		   HTTPHelper.sendValuesToUrl(parameters, values, HTTPHelper.ADD_NEW_PLAYER_LOC_URL);
					   
		   locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		   provider = LocationManager.GPS_PROVIDER;
		  
		   locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
		   return START_STICKY;
		}
		
		public void onLocationChanged(Location location) {
			LatLng latLng;
			if(TIME_DIFFERENCE<(location.getTime()-timeOfLastLocation))
			{	
				// this location is 10s "away" from last one
				latLng = new LatLng(location.getLatitude(),location.getLongitude());
				updateMapView(latLng);
				// set current location on screen to
				
				timeOfLastLocation = location.getTime();
				
				ArrayList<String> parameters = new ArrayList<String>();
				ArrayList<String> values = new ArrayList<String>();
				parameters.add("game_id");
				parameters.add("player_id");
				parameters.add("latitude");
				parameters.add("longitude");
				values.add(String.valueOf(gameId));
				values.add(registrationId);
				values.add(String.valueOf(latLng.latitude));
				values.add(String.valueOf(latLng.longitude));
				HTTPHelper.sendValuesToUrl(parameters, values, "playerLocations.php");
				
			}

		}
		
		private void updateMapView(LatLng latLng){
			Intent i = new Intent("UPDATE_MAP_TAG");
			i.putExtra("location", latLng);
			sendBroadcast(i);
		}
		
		private void updateMapObject(ObjectOnMap oom){
			Intent i = new Intent("UPDATE_MAP_OBJECT_TAG");
			i.putExtra("objectId", oom.getId());
			i.putExtra("location", oom.getLatlng());
			Log.v("update map object!",oom.getId() + " " + oom.getLatlng().toString());
			sendBroadcast(i);
			
		}
		
		private void announceNewPlayer(String newPlayerId)
		{			
			ArrayList<String>receivers = new ArrayList<String>();
			for(int i = 0; i<players.size(); i++){
				String id = players.get(i).getId();
				if(id.compareTo(newPlayerId)!=0)		// RETURN FOR TESTING WITH DIFFERENT DEVICES !!!
					receivers.add(id);
			}
			HTTPHelper.sendGCMMessage(GCM_ANNOUNCE_TAG, newPlayerId, receivers);
		}	
				
		@Override
		public IBinder onBind(Intent arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
		public void onDestroy (){
		    super.onDestroy();
			unregisterReceiver(broadcastReceiver);
			locationManager.removeUpdates(this);
			isRuning = false;
		}
		
		// broadcast receiver that handles messages from GCM
		private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
					 
			@Override public void onReceive(Context context, Intent intent) { 
				
				String action = intent.getAction();
				
				if(action.equals(GCMIntentService.TAG))
				{
					Bundle message = intent.getExtras();
					Log.v("GCM Received", message.toString());
					if(message.containsKey(GCM_ANNOUNCE_TAG)){
						numberOfPolicemen++;
						Log.v("GCM Received","Player added: "+ message.getString(GCM_ANNOUNCE_TAG));
							//		RETURN FOR MULTIPLE DEVICE!
						players.add(new ObjectOnMap(0,0,message.getString(GCM_ANNOUNCE_TAG),
								"policeman" + String.valueOf(numberOfPolicemen),"player"));
						
					}
					if(message.containsKey("player_locations")){
						Log.v("player locations","");
						String playerId = null;
						LatLng newLocation = null;
						
						try {
							JSONArray ja = new JSONArray(message.getString("player_locations"));
							JSONObject jo;
							Log.v("GCM Primio","Poruka: "+ message.getString("player_locations"));
							int length = ja.length();
							for(int i = 0; i<length; i++){
								jo = ja.getJSONObject(i);
								
								playerId = jo.getString("player_id");
								newLocation = new LatLng(jo.getDouble("latitude"), jo.getDouble("longitude"));
								for(int j = 0; j<players.size(); j++){
									String id = players.get(j).getId();
									if(id.equals(playerId)){
										players.get(j).setLatlng(newLocation);
										updateMapObject(players.get(j));
									}
								}
							}
						} catch (JSONException e) {
						}	
					}
				}
				else if (action.equals("REQ_INITIALISE_DATA")){
					
					updateMapView(mapCenter);
				
					Intent j = new Intent("DRAW_ITEMS");
					j.putExtra("items", items);
					j.putExtra("mapCenter",mapCenter);
					j.putExtra("buildings",buildings);
					sendBroadcast(j);
				}
									
			}
		
		};

		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
}		 
