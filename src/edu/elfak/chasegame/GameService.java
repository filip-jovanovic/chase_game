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
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

//import android.util.Log;

public class GameService extends Service implements LocationListener {

	public static final String GCM_ANNOUNCE_TAG = "announce";
	public static final String GCM_POLICEWIN_TAG = "police won";
	public static final String GCM_THIEF_WIN_TAG = "thief won";
	public static final String GCM_CANSTART_TAG = "game can start";
	public static final String GCM_TIMEISUP_TAG = "time is up";
	public static final String GCM_SHOW_THIEF_TAG = "show thief";
	public static final String GCM_START_TAG = "start";
	public static final String GCM_BULLETPROOF_VALUE_TAG = "isBulletproof";
	public static final String GCM_PLAYER_EXITED = "player exited";
	private static final String GCM_BANK_ROBBED_UPDATE_MAP = "bankIsRobbed";

	private static final int MAX_AMMO = 3;
	private static final int MONEY_LIMIT = 35000;
	private static final int VEST_PERIOD = 900000;
	private static final int LOCATION_UPDATE_PERIOD = 360000;
	private static final long GAME_DURATION = 7200000;
	private final long TIME_DIFFERENCE = 10000;
	private static final double PICKUP_DISTANCE = 10.0;
	private static final double KILL_DISTANCE = 30.0;
	public static int ammo = MAX_AMMO;
	
	private ArrayList<ObjectOnMap> buildings;
	private ArrayList<ObjectOnMap> items;
	private ArrayList<ObjectOnMap> players;

	private static ArrayList<ObjectOnMap> gatheredItems;
	public static ArrayList<ObjectOnMap> getGatheredItems() {
		return gatheredItems;
	}
	private ArrayList<ObjectOnMap> robbedBanks;

	public static boolean isRuning = false; // da li je servis pokrenut

	private int mapId;
	public int gameId;
	public int numberOfPolicemen;
	public int playerPosition;
	private long timeOfLastLocation;
	// za testiranje
	public int test = 0;
	public int playerAbsent = 0;

	private String gameName;
	private String provider;
	public String registrationId;
	private LocationManager locationManager;

	public static boolean isThief;
	
	
	private boolean gameStarted;
	public CountDownTimer gameTimer;
	private LatLng mapCenter;
	private boolean bulletproofActive;
	private boolean jammerActive;
	private Handler vestHandler;
	public static int moneyGathered;
	private boolean vestAvailable;
	private boolean jammerAvailable;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		isThief = false;
		isRuning = true;
		gameStarted = false;
		jammerActive = false;
		bulletproofActive = false;
		jammerAvailable = false;
		vestAvailable = false;
		moneyGathered = 0;

		timeOfLastLocation = System.currentTimeMillis();
		buildings = new ArrayList<ObjectOnMap>();
		items = new ArrayList<ObjectOnMap>();
		players = new ArrayList<ObjectOnMap>();

		IntentFilter intentFilter = new IntentFilter(GCMBaseIntentService.TAG);
		intentFilter.addAction("REQ_INITIALISE_DATA");
		intentFilter.addAction("SHOT_IS_FIRED");
		intentFilter.addAction("BECAME_BULLETPROOF");
		intentFilter.addAction("ACTIVATE_JAMMER");
		intentFilter.addAction("GAME_RESTART");
		registerReceiver(broadcastReceiver, intentFilter);

		Bundle ib = intent.getExtras();
		mapId = ib.getInt("mapId");
		mapCenter = (LatLng) ib.get("mapCenter");
		gameName = ib.getString("gameName");
		String role = ib.getString("role");
		if (role.equals("thief"))
			isThief = true;
		registrationId = ib.getBundle("dataBundle").getString("registrationId");
		ib.getBundle("dataBundle").getString("playerName");

		numberOfPolicemen = 0;
		playerPosition = 0;
		
		if (isThief) {

			gatheredItems = new ArrayList<ObjectOnMap>();
			robbedBanks = new ArrayList<ObjectOnMap>();
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

				HttpHelper.sendGcmMessage(GCM_CANSTART_TAG, registrationId,
						defaultReceivers());
				Toast.makeText(
						getBaseContext(),
						"Igra moze da pocne, idite do svoje startne lokacije.",
						Toast.LENGTH_LONG).show();
			}
			
			ArrayList<String> receivers = new ArrayList<String>();
			for (int i = 0; i < players.size(); i++) {
				String id = players.get(i).getId();
				if (!id.equals(registrationId))
					receivers.add(id);
				else
					playerPosition = i;
			}
			HttpHelper.sendGcmMessage(GCM_ANNOUNCE_TAG, registrationId,
					receivers);
		}
		ArrayList<ObjectOnMap> allObjects = HttpHelper.getObjectsList(String
				.valueOf(mapId));
		for (ObjectOnMap ob : allObjects) {
			if (ob.getType() == "building")
				buildings.add(ob);
			else
				items.add(ob);
		}

		HttpHelper.flushParameters();
		HttpHelper.addParameter("game_id", String.valueOf(gameId));
		HttpHelper.addParameter("player_id", registrationId);
		HttpHelper.sendValuesToUrl(HttpHelper.ADD_NEW_PLAYER_LOC_URL);

		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		provider = LocationManager.GPS_PROVIDER;
		locationManager.requestLocationUpdates(provider, 1000, 0, this);

		return START_STICKY;
	}

	public void startGame() {
		gameTimer = new CountDownTimer(GAME_DURATION, LOCATION_UPDATE_PERIOD) {
			@Override
			public void onTick(long millisUntilFinished) {
				for (ObjectOnMap p : players) {
					updateMapObject(p);
				}
				if (!jammerActive) {
					HttpHelper.sendGcmMessage(GCM_SHOW_THIEF_TAG,
							registrationId, defaultReceivers());
				} else
					jammerActive = false;
				Log.v("TICK", String.valueOf((test++)));
			}

			@Override
			public void onFinish() {
				HttpHelper.sendGcmMessage(GCM_TIMEISUP_TAG, registrationId,
						defaultReceivers());
				Intent victory = new Intent("DISPLAY_DIALOG");
				victory.putExtra("title",
						"Vreme je isteklo, uspesno ste pobegli policiji!");
				sendBroadcast(victory);
			}
		}.start();
	}

	@Override
	public void onLocationChanged(Location location) {
		LatLng newCoordinates;
		newCoordinates = new LatLng(location.getLatitude(),
				location.getLongitude());

		checkAndProcessColision(newCoordinates);

		if (TIME_DIFFERENCE < (location.getTime() - timeOfLastLocation)) {
			updateMapView(newCoordinates);
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
		for (ObjectOnMap object : buildings) {
			if (calculateDistance(newCoordinates, object.getLatlng()) < PICKUP_DISTANCE) {
				if (object.isPoliceStation()) {
					if (!isThief)
						refillAmmo();
				} else if (object.isSafeHouse()) {
					// Nista se ne desava
				} else if (object.isBank() && (object.getValue() > 0)) {
					if (isThief && (!gatheredItems.contains(object))) {
						int bankId = Integer.valueOf(object.getId());
						int numOfNecessaryItems = 0, numOfGatheredNecessaryItems = 0;
						for (int j = 0; j < items.size(); j++) {
							if (bankId == items.get(j).getBankId())
								numOfNecessaryItems++;
						}
						for (int j = 0; j < gatheredItems.size(); j++) {
							if (bankId == gatheredItems.get(j).getBankId())
								numOfGatheredNecessaryItems++;
						}

						if (numOfNecessaryItems == numOfGatheredNecessaryItems) {
							Log.v("BANK ROBED!", String.valueOf(bankId));

							robbedBanks.add(object);

							Intent intent = new Intent("BANK_ROBBED_UPDATE_MAP");
							intent.putExtra("bank", object);
							sendBroadcast(intent);

							Toast.makeText(
									this,
									"Banka je opljackana. Prikupljeno je jos "
											+ object.getValue() + "$ !",
									Toast.LENGTH_LONG).show();

							moneyGathered += object.getValue();

							buildings.remove(object);
							object.setValue(object.getValue() * (-1));
							buildings.add(object);

							if (moneyGathered >= MONEY_LIMIT) {
								HttpHelper.sendGcmMessage(GCM_THIEF_WIN_TAG,
										String.valueOf(moneyGathered),
										defaultReceivers());

								Intent victory = new Intent("DISPLAY_DIALOG");
								victory.putExtra("title",
										"Cestitamo! Pobedili ste!");
								sendBroadcast(victory);
								gameTimer.cancel();
							} else {
								HttpHelper.sendGcmMessage(
										GCM_BANK_ROBBED_UPDATE_MAP,
										object.getId(), defaultReceivers());
							}
						}
					}
				}
			}
		}
		if (isThief 
				&& gameStarted
				) {
			for (ObjectOnMap object : items) {
				if (calculateDistance(newCoordinates, object.getLatlng()) < PICKUP_DISTANCE) {
					if (!gatheredItems.contains(object)) {
						gatheredItems.add(object);
						removeMapObject(object);
						if (object.getName().contains("Pancir")) {
							vestAvailable = true;
							Intent intent = new Intent("ENABLE_VEST_BUTTON_TAG");
							sendBroadcast(intent);
						} else if (object.getName().contains("Ometac")) {
							jammerAvailable = true;
							Intent intent = new Intent(
									"ENABLE_JAMMER_BUTTON_TAG");
							sendBroadcast(intent);
						}
					}
				}
			}
		}
	}

	private void becameBulletproof() {
		bulletproofActive = true;
		vestHandler = new Handler();
		HttpHelper.sendGcmMessage(GCM_BULLETPROOF_VALUE_TAG, "true", defaultReceivers());
		vestHandler.postDelayed(announceBulletproofEnd, VEST_PERIOD);
	}

	private Runnable announceBulletproofEnd = new Runnable() {
		@Override
		public void run() {
			bulletproofActive = false;
			HttpHelper.sendGcmMessage(GCM_BULLETPROOF_VALUE_TAG, "false",
					defaultReceivers());
		}
	};

	private void refillAmmo() {
		if (ammo != MAX_AMMO)
			ammo = MAX_AMMO;
		Intent i = new Intent("BULLETS_UPDATE_TAG");
		i.putExtra("remainingBullets", ammo);
		sendBroadcast(i);
	}

	private void updateMapView(LatLng latLng) {
		Intent i = new Intent("UPDATE_MAP_TAG");
		i.putExtra("thiefDistance", getDistanceFromThief());
		i.putExtra("policemanDistance", getDistanceFromPoliceman());
		i.putExtra("location", latLng);
		sendBroadcast(i);
	}

	private void updateMapObject(ObjectOnMap object) {
		Intent i = new Intent("UPDATE_MAP_OBJECT_TAG");
		i.putExtra("object", object);
		sendBroadcast(i);
	}

	private void removeMapObject(ObjectOnMap object) {
		Intent i = new Intent("REMOVE_MAP_OBJECT_TAG");
		i.putExtra("object", object);
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
		HttpHelper.addParameter("place", String.valueOf(playerPosition));
		HttpHelper.sendValuesToUrl(HttpHelper.EXIT_GAME);
		if (gameTimer != null)
			gameTimer.cancel();
	}

	public void onLowMemory() {
		Log.v("Service killed!", "Reason: Low memory");
	}

	// broadcast receiver that handles messages from GCM
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (action.equals(GCMBaseIntentService.TAG)) {
				processGcmEvent(intent, context);

			} else if (action.equals("REQ_INITIALISE_DATA")) {

				updateMapView(mapCenter);
				Intent j = new Intent("INITIALISE_MAP");
				ArrayList<ObjectOnMap> visibleItems = new ArrayList<ObjectOnMap>(
						items);
				if (isThief) {
					visibleItems.removeAll(gatheredItems);
					j.putExtra("vestAvailable", vestAvailable);
					j.putExtra("jammerAvailable", jammerAvailable);
				} else
					visibleItems = new ArrayList<ObjectOnMap>();

				j.putExtra("items", visibleItems);
				j.putExtra("mapCenter", mapCenter);
				j.putExtra("buildings", buildings);

				sendBroadcast(j);

			} else if (action.equals("SHOT_IS_FIRED")) {
				ammo--;
				if (!bulletproofActive && gameStarted) {
					ArrayList<Double> distance = getDistanceFromThief();
					if (distance.get(0) <= KILL_DISTANCE) {
						HttpHelper.sendGcmMessage(GCM_POLICEWIN_TAG,
								registrationId, defaultReceivers());
						Intent i = new Intent("DISPLAY_DIALOG");
						i.putExtra("title",
								"Bravo! Policija je pobedila, lopov je uspesno uhvacen!");
						sendBroadcast(i);
					}
				} else if (!gameStarted)
					Log.v("SHOT_IS_FIRED", "Igra nije startovana");
				else
					Log.v("SHOT_IS_FIRED", "Aktiviran je pancir");
				// nastavak
				Intent i = new Intent("BULLETS_UPDATE_TAG");
				i.putExtra("remainingBullets", ammo);
				sendBroadcast(i);
			} else if (action.equals("BECAME_BULLETPROOF")) {
				becameBulletproof();
				vestAvailable = false;
			} else if (action.equals("ACTIVATE_JAMMER")) {
				jammerActive = true;
				jammerAvailable = false;
			} else if (action.equals("GAME_RESTART")) {
				gameStarted = false;
				jammerActive = false;
				bulletproofActive = false;
				jammerAvailable = false;
				vestAvailable = false;
				moneyGathered = 0;
				ammo = MAX_AMMO;

				for (ObjectOnMap object : buildings) {
					if (object.isBank())
						if (object.getValue() < 0)
							object.setValue(object.getValue() * (-1));
				}
				if (isThief) {
					if (gameTimer != null)
						gameTimer.cancel();
					if (vestHandler != null)
						vestHandler.removeCallbacks(announceBulletproofEnd);
					gatheredItems = new ArrayList<ObjectOnMap>();
				}
				Toast.makeText(
						getBaseContext(),
						"Igra moze da pocne, idite do svoje startne lokacije.",
						Toast.LENGTH_LONG).show();

				updateMapView(mapCenter);
				Intent j = new Intent("INITIALISE_MAP");
				ArrayList<ObjectOnMap> visibleItems = new ArrayList<ObjectOnMap>(
						items);
				if (isThief) {
					visibleItems.removeAll(gatheredItems);
					j.putExtra("vestAvailable", vestAvailable);
					j.putExtra("jammerAvailable", jammerAvailable);
				} else
					visibleItems = new ArrayList<ObjectOnMap>();
				j.putExtra("items", visibleItems);
				j.putExtra("mapCenter", mapCenter);
				j.putExtra("buildings", buildings);
				sendBroadcast(j);
			}
		}
	};

	protected void processGcmEvent(Intent intent, Context context) {
		{
			Bundle message = intent.getExtras();
			if (message.containsKey(GCM_ANNOUNCE_TAG)) {
				numberOfPolicemen++;
				players.add(new ObjectOnMap(0, 0, message
						.getString(GCM_ANNOUNCE_TAG), "policeman"
						+ String.valueOf(numberOfPolicemen), 0, "player"));

			} else if (message.containsKey(GCM_BANK_ROBBED_UPDATE_MAP)) {
				ObjectOnMap object = null;
				for (int i = 0; i < buildings.size(); i++) {
					if (buildings.get(i).getId()
							.equals(message.get(GCM_BANK_ROBBED_UPDATE_MAP))) {
						buildings.get(i).setValue(
								buildings.get(i).getValue() * (-1));
						object = buildings.get(i);
					}
				}
				Intent in = new Intent("BANK_ROBBED_UPDATE_MAP");
				in.putExtra("bank", object);
				sendBroadcast(in);
			} else if (message.containsKey(GCM_BULLETPROOF_VALUE_TAG)) {
				if (message.getString(GCM_BULLETPROOF_VALUE_TAG).equals("true")) {
					bulletproofActive = true;
				} else
					bulletproofActive = false;
				Log.v("Pancir", message.getString(GCM_BULLETPROOF_VALUE_TAG));

			} else if (message.containsKey(GCM_POLICEWIN_TAG)) {
				Intent i = new Intent("DISPLAY_DIALOG");
				i.putExtra("title",
						"Policija je pobedila, lopov je uspesno uhvacen!");
				sendBroadcast(i);
				if (isThief && gameTimer != null)
					gameTimer.cancel();

			} else if (message.containsKey(GCM_SHOW_THIEF_TAG)) {
				updateMapObject(players.get(0));
			} else if (message.containsKey(GCM_THIEF_WIN_TAG)) {
				Intent i = new Intent("DISPLAY_DIALOG");
				i.putExtra("title", "Lopov je pobedio!");
				sendBroadcast(i);
			} else if (message.containsKey(GCM_CANSTART_TAG)) {
				Toast.makeText(getBaseContext(),
						"Igra moze da pocne, idite do svoje startne lokacije.",
						Toast.LENGTH_LONG).show();
				Log.v("IGRA MOZE DA POCNE", "POCNI");
			} else if (message.containsKey(GCM_TIMEISUP_TAG)) {
				Intent i = new Intent("DISPLAY_DIALOG");
				i.putExtra("title",
						"Vreme je isteklo, lopov je uspesno pobegao!");
				sendBroadcast(i);
			} else if (message.containsKey(GCM_START_TAG)) {
				Toast.makeText(getBaseContext(), "Igra je pocela.",
						Toast.LENGTH_LONG).show();
				gameStarted = true;
			} else if (message.containsKey("player_exited")) {
				Log.v("exited", message.toString());
				String playerId = message.getString("player_exited");
				if (playerId.equals(players.get(0).getId())) {
					Intent i = new Intent("DISPLAY_DIALOG");
					i.putExtra("title", "Lopov je napustio igru!");
					i.putExtra("norestart", true);
					sendBroadcast(i);
				} else {
					Toast.makeText(getBaseContext(),
							"Policajac je napustio igru.", Toast.LENGTH_LONG).show();
					numberOfPolicemen--;
					Intent i = new Intent("REMOVE_PLAYER_TAG");
					i.putExtra("playerID", playerId);
					sendBroadcast(i);
					for (int j = 0; j < players.size(); j++) {
						String id = players.get(j).getId();
						if (id.equals(playerId)) {
							players.remove(j);
							Log.v("BRISANJE", "OBRISAO JE ");
							for (int k = 0; k < players.size(); k++) {
								String idP = players.get(k).getId();
								if (idP.equals(registrationId))
									playerPosition = k;
							}
						}
					}
				}

			} else if (message.containsKey("player_locations")) {
				String playerId = null;
				LatLng newLocation = null;
				ArrayList<String> player_ids = new ArrayList<String>();
				try {
					JSONArray jsonArray = new JSONArray(
							message.getString("player_locations"));
					JSONObject jsonObject;
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
								if (isThief && j == 0) {
									updateMapObject(players.get(j));
								} else if (!isThief && j != 0) {
									updateMapObject(players.get(j));
								}
							}
						}
					}
					if (length < players.size()) {
						if (playerAbsent++ > 4) {
							for (int i = 0; i < players.size(); i++) {
								if (!player_ids
										.contains(players.get(i).getId())) {
									players.remove(i);
									Log.v("BRISANJE", "OBRISAO JE ");
								}
							}
							for (int i = 0; i < players.size(); i++) {
								String id = players.get(i).getId();
								if (id.equals(registrationId))
									playerPosition = i;
							}
						} else {
							playerAbsent = 0;
						}
					} else {
						if (isThief && gameCanStartCheck() && !gameStarted) {
							Toast.makeText(getBaseContext(), "Igra je pocela.",
									Toast.LENGTH_LONG).show();
							startGame();
							HttpHelper.sendGcmMessage(GCM_START_TAG,
									registrationId, defaultReceivers());
							gameStarted = true;
						}
					}
				} catch (JSONException e) {
				}
			}
		}

	}

	public boolean gameCanStartCheck() {
		if (players.size() != 4) {
			return false;
		}
		LatLng policeLoc = new LatLng(0, 0);
		LatLng safehouseLoc = new LatLng(0, 0);
		for (int i = 0; i < buildings.size(); i++) {
			if (buildings.get(i).isPoliceStation())
				policeLoc = buildings.get(i).getLatlng();
			else if (buildings.get(i).isSafeHouse())
				safehouseLoc = buildings.get(i).getLatlng();
		}
		if (calculateDistance(players.get(0).getLatlng(), safehouseLoc) <= PICKUP_DISTANCE) {
			for (int i = 1; i < players.size(); i++) {
				if (calculateDistance(players.get(i).getLatlng(), policeLoc) > PICKUP_DISTANCE)
					return false;
			}
			return true;
		}
		return false;
	}
	
	ArrayList<String> defaultReceivers(){
		ArrayList<String> receivers = new ArrayList<String>();
		for (int i = 0; i < players.size(); i++) {
			String id = players.get(i).getId();
			if (!id.equals(registrationId))
				receivers.add(id);
		}
		return receivers;
	}

	public static double calculateDistance(LatLng p1, LatLng p2) {
		double theta = p1.longitude - p2.longitude;
		double dist = Math.sin(deg2rad(p1.latitude))
				* Math.sin(deg2rad(p2.latitude))
				+ Math.cos(deg2rad(p1.latitude))
				* Math.cos(deg2rad(p2.latitude)) * Math.cos(deg2rad(theta));
		return (rad2deg(Math.acos(dist)) * 111189.57696);
	}

	public ArrayList<Double> getDistanceFromPoliceman() {
		ArrayList<Double> distance = new ArrayList<Double>();
		LatLng myLocation = players.get(playerPosition).getLatlng();
		boolean found = false;
		for (ObjectOnMap p : players) {
			if (myLocation != p.getLatlng()	&& !p.getName().equals("thief")) {
				distance.add(calculateDistance(myLocation, p.getLatlng()));
				found = true;
			}
		}
		if (!found)
			distance.add(0.0);
		return distance;
	}

	public ArrayList<Double> getDistanceFromThief() {
		ArrayList<Double> distance = new ArrayList<Double>();
		if (players.get(playerPosition).getName().equals("thief")) {
			distance.add(Double.valueOf(0.0));
		} else {
			LatLng myLocation = players.get(playerPosition).getLatlng();
			boolean found = false;
			for (ObjectOnMap p : players) {
				if (myLocation != p.getLatlng()	&& !p.getName().equals("thief")) {
					distance.add(calculateDistance(myLocation, p.getLatlng()));
					found = true;
				}
			}
			if (!found)
				distance.add(0.0);
		}
		return distance;
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
