package edu.elfak.chasegame;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gcm.GCMBaseIntentService;
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
//import android.util.Log;

public class GameService extends Service implements LocationListener {

	private ArrayList<ObjectOnMap> buildings;
	private ArrayList<ObjectOnMap> items;
	private ArrayList<ObjectOnMap> players;

	private ArrayList<ObjectOnMap> gatheredItems;

	private int mapId;
	public int gameId;
	public int numberOfPolicemen;

	private long timeOfLastLocation;

	private String gameName;
	private String provider;
	private String playerName;
	public String registrationId;
	private LocationManager locationManager;
	private boolean isThief;

	private final long TIME_DIFFERENCE = 5000;
	public static final String GCM_ANNOUNCE_TAG = "announce";
	private static final int MAX_AMMO = 3;

	private LatLng mapCenter;
	private int ammo;
	private boolean buletproof;
	private boolean jammer;
	private boolean gameStarted;
	public static boolean isRuning = false;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		isRuning = true;
		gameStarted = false;

		buildings = new ArrayList<ObjectOnMap>();
		items = new ArrayList<ObjectOnMap>();
		players = new ArrayList<ObjectOnMap>();
		gatheredItems = new ArrayList<ObjectOnMap>();

		IntentFilter intentFilter = new IntentFilter(GCMBaseIntentService.TAG);
		intentFilter.addAction("REQ_INITIALISE_DATA");
		intentFilter.addAction("SHOT_IS_FIRED");
		registerReceiver(broadcastReceiver, intentFilter);

		Bundle ib = intent.getExtras();
		mapId = ib.getInt("mapId");
		mapCenter = (LatLng) ib.get("mapCenter");
		gameName = ib.getString("gameName");
		String role = ib.getString("role");
		if (role.compareTo("thief") == 0)
			isThief = true;
		registrationId = ib.getBundle("dataBundle").getString("registrationId");
		playerName = ib.getBundle("dataBundle").getString("playerName");

		numberOfPolicemen = 0;

		if (isThief) {
			players.add(new ObjectOnMap(0, 0, registrationId, role, 0, "player"));

			HttpHelper.flushParameters();
			HttpHelper.addParameter("name", gameName);
			HttpHelper.addParameter("mapId", String.valueOf(mapId));
			HttpHelper.addParameter("thief", registrationId);
			String result = HttpHelper
					.sendValuesToUrl(HttpHelper.CREATE_GAME_URL);
			gameId = Integer.valueOf(result);
		} else {
			ammo = MAX_AMMO;
			numberOfPolicemen++;
			gameId = ib.getInt("game_id");
			players.add(new ObjectOnMap(0, 0, ib.getString("thief"), "thief",
					0, "player"));
			players.add(new ObjectOnMap(0, 0, ib.getString("cop_1"),
					"policeman1", 0, "player"));
			String cop_2Id = ib.getString("cop_2");
			String cop_3Id = ib.getString("cop_3");
			if (cop_2Id.compareTo("empty") != 0) {
				players.add(new ObjectOnMap(0, 0, cop_2Id, "policeman2", 0,
						"player"));
				numberOfPolicemen++;
			}
			if (cop_3Id.compareTo("empty") != 0) {
				players.add(new ObjectOnMap(0, 0, cop_3Id, "policeman3", 0,
						"player"));
				numberOfPolicemen++;
			}
			
			

			// let other players be informed about new player
			ArrayList<String> receivers = new ArrayList<String>();
			for (int i = 0; i < players.size(); i++) {
				String id = players.get(i).getId();
				if (id.compareTo(registrationId) != 0) // RETURN FOR TESTING
														// WITH DIFFERENT
														// DEVICES !!!
					receivers.add(id);
			}
			HttpHelper.sendGcmMessage(GCM_ANNOUNCE_TAG, registrationId,
					receivers);
		}

		// populate items and buildings from server
		ArrayList<ObjectOnMap> allObjects = HttpHelper.getObjectsList(String
				.valueOf(mapId));
		ObjectOnMap ob = null;
		for (int i = 0; i < allObjects.size(); i++) {
			ob = allObjects.get(i);
			if (ob.getType() == "building")
				buildings.add(ob);
			else
				items.add(ob);
		}
		// registrovanje nove lokacije u tabelu player_locations
		HttpHelper.flushParameters();
		HttpHelper.addParameter("game_id", String.valueOf(gameId));
		HttpHelper.addParameter("player_id", registrationId);
		HttpHelper.sendValuesToUrl(HttpHelper.ADD_NEW_PLAYER_LOC_URL);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		provider = LocationManager.GPS_PROVIDER;
		locationManager.requestLocationUpdates(provider, 1000, 0, this);

		return START_STICKY;
	}

	@Override
	public void onLocationChanged(Location location) {
		LatLng newCoordinates;
		newCoordinates = new LatLng(location.getLatitude(),
				location.getLongitude());
		updateMapView(newCoordinates);
		
		checkAndProcessColision(newCoordinates);
		
		// TODO check timer !
		if (TIME_DIFFERENCE < (location.getTime() - timeOfLastLocation)) {
			// this location is 10s "away" from last one

			timeOfLastLocation = location.getTime();
			
			HttpHelper.flushParameters();
			HttpHelper.addParameter("game_id", String.valueOf(gameId));
			HttpHelper.addParameter("player_id", registrationId);
			HttpHelper.addParameter("latitude",
					String.valueOf(newCoordinates.latitude));
			HttpHelper.addParameter("longitude",
					String.valueOf(newCoordinates.longitude));
			HttpHelper.sendValuesToUrl(HttpHelper.PLAYER_LOC_URL);
		}
	}

	private void checkAndProcessColision(LatLng newCoordinates) {
		ObjectOnMap object = null;
		
		for (int i = 0; i < buildings.size(); i++) {
			object = buildings.get(i);
			if (calculateDistance(newCoordinates, object.getLatlng()) < 10.0) {
				if (object.isPoliceStation()) {
					if (!isThief)
						refillAmmo();
				} else if (object.isSafeHouse()) {
					//TODO Nisam siguran sta se ovde desava...
				} else if (object.isBank()) {
					if (isThief) {
						int bankId = object.getBankId();
						int numOfNecessaryItems = 0,numOfGatheredNecessaryItems = 0;	
						for(int j = 0; j<items.size(); j++){
							if(bankId == items.get(j).getBankId())
								numOfNecessaryItems++;
						}
						for(int j = 0; j<items.size(); j++){
							if(bankId == items.get(j).getBankId())
								numOfGatheredNecessaryItems++;
						}
						if(numOfNecessaryItems==numOfGatheredNecessaryItems){}
						//	Log.v("BANK ROBED!",String.valueOf(bankId));		
						//TODO Announce to others, add money to thief, check end game
					}

				}
			}
		}
		if (isThief) {
			for (int i = 0; i < items.size(); i++) {
				object = items.get(i);
				if (calculateDistance(newCoordinates, object.getLatlng()) < 10.0) {
					if(!gatheredItems.contains(object)){
						gatheredItems.add(object);
						//TODO : remove it from map
						if(object.getName().contains("Pancir")){
							buletproof = true;
							//TODO: Show button
						}
						if(object.getName().contains("Ometac")){
							jammer = true;
							//TODO: Show button
						}
					}

				}
			}
		}
	}

	private void refillAmmo() {		
		if( ammo != MAX_AMMO )
			ammo = MAX_AMMO;
		//Log.v("REFILL AMO",String.valueOf(ammo));
		
		Intent i = new Intent("BULLETS_UPDATE");
		i.putExtra("remainingBullets", ammo);
		sendBroadcast(i);
	}

	private void updateMapView(LatLng latLng) {
		Intent i = new Intent("UPDATE_MAP_TAG");
		i.putExtra("location", latLng);
		sendBroadcast(i);
	}

	private void updateMapObject(ObjectOnMap object) {
		Intent i = new Intent("UPDATE_MAP_OBJECT_TAG");
		i.putExtra("objectId", object.getId());
		i.putExtra("location", object.getLatlng());
		sendBroadcast(i);

	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
		unregisterReceiver(broadcastReceiver);
		locationManager.removeUpdates(this);
		isRuning = false;
		// obrisi igrace iz baze i game tabele
		HttpHelper.flushParameters();
		HttpHelper.addParameter("game_id", String.valueOf(gameId));
		HttpHelper.addParameter("player_id", registrationId);
		HttpHelper.addParameter("place", String.valueOf(numberOfPolicemen));

		String result = HttpHelper.sendValuesToUrl(HttpHelper.EXIT_GAME);
	}

	// broadcast receiver that handles messages from GCM
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (action.equals(GCMBaseIntentService.TAG)) {
				Bundle message = intent.getExtras();
				//Log.v("GCM Received", message.toString());
				if (message.containsKey(GCM_ANNOUNCE_TAG)) {
					// numberOfPolicemen++;
					/*Log.v("GCM Received",
							"Player added: "
									+ message.getString(GCM_ANNOUNCE_TAG));*/
					// RETURN FOR MULTIPLE DEVICE!
					players.add(new ObjectOnMap(0, 0, message
							.getString(GCM_ANNOUNCE_TAG), "policeman"
							+ String.valueOf(numberOfPolicemen), 0, "player"));

				} else if (message.containsKey("player_locations")) {
					//Log.v("player locations", "");
					String playerId = null;
					LatLng newLocation = null;
					ArrayList<String> player_ids = new ArrayList<String>();
					try {
						JSONArray jsonArray = new JSONArray(
								message.getString("player_locations"));
						JSONObject jsonObject;
					/*	Log.v("GCM Primio",
								"Poruka: "
										+ message.getString("player_locations"));*/
						int length = jsonArray.length();
						for (int i = 0; i < length; i++) {
							jsonObject = jsonArray.getJSONObject(i);
							playerId = jsonObject.getString("player_id");
							player_ids.add(playerId);
							newLocation = new LatLng(
									jsonObject.getDouble("latitude"),
									jsonObject.getDouble("longitude"));
							for (int j = 0; j < players.size(); j++) {
								String id = players.get(j).getId();
								if (id.equals(playerId)) {
									players.get(j).setLatlng(newLocation);
									updateMapObject(players.get(j));
								}
							}
						}
						if (length < players.size()) {
							for (int i = 0; i < players.size(); i++) {
								if (!player_ids
										.contains(players.get(i).getId())) {
									//Log.v("GCM Obrisao", players.get(i).getId());
									players.remove(i);

								}
							}
						}
					} catch (JSONException e) {
					}
				}
			} else if (action.equals("REQ_INITIALISE_DATA")) {

				updateMapView(mapCenter);

				Intent j = new Intent("DRAW_ITEMS");
				j.putExtra("items", items);
				j.putExtra("mapCenter", mapCenter);
				j.putExtra("buildings", buildings);
				sendBroadcast(j);
			}
			else if (action.equals("SHOT_IS_FIRED")) {
				
				ammo--;
				//Log.v("ammo",String.valueOf(ammo));
				Intent i = new Intent("BULLETS_UPDATE");
				i.putExtra("remainingBullets", ammo);
				sendBroadcast(i);
			}
		}
	};

	public static double calculateDistance(LatLng p1, LatLng p2) {
		double theta = p1.longitude - p2.longitude;
		double dist = Math.sin(deg2rad(p1.latitude))
				* Math.sin(deg2rad(p2.latitude))
				+ Math.cos(deg2rad(p1.latitude))
				* Math.cos(deg2rad(p2.latitude)) * Math.cos(deg2rad(theta));
		return (rad2deg(Math.acos(dist)) * 111189.57696);
	}

	private static double deg2rad(double deg) {
		return (deg * Math.PI / 180.0);
	}

	private static double rad2deg(double rad) {
		return (rad * 180.0 / Math.PI);
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
}
