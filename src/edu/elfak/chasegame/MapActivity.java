package edu.elfak.chasegame;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
//import android.util.Log;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
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
	private HashMap<String, Marker> allMarkers;
	IntentFilter intentFilter;
	private ToggleButton screenLockButton;

	ImageView bullet1;
	ImageView bullet2;
	ImageView bullet3;
	private ImageButton shootButton;
	private ImageView radarThiefIcon;
	private ArrayList<ImageView> radarCopIcons;
	private TextView gatheredMoneyTextView;
	private TextView gatheredMoneyTextView2;

	private View jammerButton;
	private View vestButton;

	private int moneyGathered;
	private int remainingBullets = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		moneyGathered = 0;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.map);

		mMap = ((SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map)).getMap();

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
		SnippetAdapter sa = new SnippetAdapter(getLayoutInflater());
		mMap.setInfoWindowAdapter(sa);
		playerMarkers = new HashMap<String, Marker>();
		allMarkers = new HashMap<String, Marker>();

		screenLockButton = (ToggleButton) findViewById(R.id.screenLockButton);
		jammerButton = findViewById(R.id.jammerButton);
		vestButton = findViewById(R.id.vestButton);

		jammerButton.setOnClickListener(this);
		vestButton.setOnClickListener(this);

		gatheredMoneyTextView = (TextView) findViewById(R.id.textViewNovac1);
		gatheredMoneyTextView2 = (TextView) findViewById(R.id.textViewNovac2);

		radarThiefIcon = (ImageView) findViewById(R.id.radarThief);
		radarCopIcons = new ArrayList<ImageView>();
		radarCopIcons.add((ImageView) findViewById(R.id.radarCop1));
		radarCopIcons.add((ImageView) findViewById(R.id.radarCop2));
		radarCopIcons.add((ImageView) findViewById(R.id.radarCop3));

		bullet1 = (ImageView) findViewById(R.id.bullet1);
		bullet2 = (ImageView) findViewById(R.id.bullet2);
		bullet3 = (ImageView) findViewById(R.id.bullet3);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case (R.id.shootButton):
			sendBroadcast(new Intent("SHOT_IS_FIRED"));
			break;
		case (R.id.vestButton):
			vestButton.setEnabled(false);
			sendBroadcast(new Intent("BECAME_BULLETPROOF"));
			break;
		case (R.id.jammerButton):
			jammerButton.setEnabled(false);
			sendBroadcast(new Intent("ACTIVATE_JAMMER"));
		}
	}

	private void drawMarkers(ArrayList<ObjectOnMap> buildings,
			ArrayList<ObjectOnMap> items) {
		ArrayList<ObjectOnMap> allObjects = new ArrayList<ObjectOnMap>(
				buildings);
		allObjects.addAll(items);

		for (ObjectOnMap object : allObjects) {
			MarkerOptions markerOptions = new MarkerOptions();
			markerOptions.visible(true);
			markerOptions.position(object.getLatlng());
			markerOptions.title(object.getName());

			if (object.getType().equals("item")) {
				if (object.getName().equals("Pancir"))
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.vest_icon));
				else if (object.getName().equals("Ometac"))
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.jammer_icon));
				else
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.wooden_crate));
			}

			else if (object.isBank()) {
				if (GameService.isThief) {
					String snippet = "";
					for (ObjectOnMap nesessaryItem : items) {
						if (nesessaryItem.getBankId() == Integer.valueOf(object
								.getId()))
							snippet += nesessaryItem.getName() + " \n";
					}
					snippet += "novac" + String.valueOf(object.getValue()) + "";
					markerOptions.snippet(snippet);
				}

				if (object.getValue() > 0)
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.dollar_icon));
				else
					markerOptions.icon(BitmapDescriptorFactory
							.fromResource(R.drawable.bank_robed));

			} else if (object.isPoliceStation())
				markerOptions.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.policestation));
			else if (object.isSafeHouse() && GameService.isThief) {
				markerOptions.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.safehouse));
				Marker marker = mMap.addMarker(markerOptions);
				allMarkers.put(object.getName(), marker);
			}

			if (!(object.isSafeHouse() && !GameService.isThief)) {
				Marker marker = mMap.addMarker(markerOptions);
				allMarkers.put(object.getName(), marker);
			}
		}
	}

	@Override
	public void onResume() {
		if (dataUpdateReceiver == null)
			dataUpdateReceiver = new MapUpdateReceiver();
		intentFilter = new IntentFilter();
		intentFilter.addAction("UPDATE_MAP_OBJECT_TAG");
		intentFilter.addAction("REMOVE_MAP_OBJECT_TAG");
		intentFilter.addAction("UPDATE_MAP_TAG");
		intentFilter.addAction("INITIALISE_MAP");
		intentFilter.addAction("BULLETS_UPDATE_TAG");
		intentFilter.addAction("ENABLE_VEST_BUTTON_TAG");
		intentFilter.addAction("ENABLE_JAMMER_BUTTON_TAG");
		intentFilter.addAction("BANK_ROBBED_UPDATE_MAP");
		intentFilter.addAction("DISPLAY_DIALOG");
		intentFilter.addAction("REMOVE_PLAYER_TAG");
		registerReceiver(dataUpdateReceiver, intentFilter);

		View imBut;
		if (allMarkers.size() == 0)
			sendBroadcast(new Intent("REQ_INITIALISE_DATA"));

		if (GameService.isThief) {
			jammerButton.setVisibility(View.VISIBLE);
			vestButton.setVisibility(View.VISIBLE);
			gatheredMoneyTextView.setText(String
					.valueOf(GameService.moneyGathered));
			gatheredMoneyTextView.setVisibility(View.VISIBLE);
			gatheredMoneyTextView2.setVisibility(View.VISIBLE);
		} else {
			shootButton = (ImageButton) findViewById(R.id.shootButton);
			shootButton.setOnClickListener(this);
			shootButton.setVisibility(View.VISIBLE);
			imBut = findViewById(R.id.bulletsLayout);
			imBut.setVisibility(View.VISIBLE);
			setRemainingBullets(GameService.ammo);
			gatheredMoneyTextView.setVisibility(View.GONE);
			gatheredMoneyTextView2.setVisibility(View.GONE);
		}
		super.onResume();

	}

	@Override
	public void onPause() {
		if (dataUpdateReceiver != null)
			unregisterReceiver(dataUpdateReceiver);

		super.onPause();
	}

	private Polygon drawBoundaries(LatLng location, GoogleMap myMap) {
		PolygonOptions rectOptions = new PolygonOptions();
		double radius = 0.01;
		int numPoints = 30;
		double phase = 2 * Math.PI / numPoints;
		for (int i = 0; i <= numPoints; i++) {
			rectOptions.add(new LatLng(location.latitude + radius
					* Math.sin(i * phase), location.longitude + radius
					* Math.cos(i * phase) * 1.4));
		}
		rectOptions.strokeColor(Color.RED);
		rectOptions.geodesic(true);
		rectOptions.strokeWidth(3);
		return myMap.addPolygon(rectOptions);

	}

	private void endGameFromMap() {
		stopService(new Intent(this, GameService.class));
		finish();
	}

	private class MapUpdateReceiver extends BroadcastReceiver {
		public void endGameDialog(String title, boolean noRestart) {
			AlertDialog alertDialog = new AlertDialog.Builder(MapActivity.this)
					.create();
			alertDialog.setTitle("Igra je zavrsena!");
			alertDialog.setMessage(title);
			alertDialog.setButton("Izadji",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							endGameFromMap();
						}
					});
			if (!noRestart) {
				alertDialog.setButton2("Restartuj igru",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								sendBroadcast(new Intent("GAME_RESTART"));
							}
						});
			}
			alertDialog.show();
		}

		public void updateRadar(Context context,
				ArrayList<Double> thiefDistance,
				ArrayList<Double> policemanDistance) {
			DisplayMetrics displayMetrics = context.getResources()
					.getDisplayMetrics();
			int padding = (int) ((12 * displayMetrics.density) + 0.5);
			int x;
			double density = 0.62 * displayMetrics.density;
			int displacement = (int) (12 * displayMetrics.density);
			if (thiefDistance.get(0) < 200)
				x = (int) (thiefDistance.get(0) * density);
			else
				x = (int) (200 * density);
			radarThiefIcon.setPadding(x + displacement, padding, 0, 0);
			for (int j = 0; j < policemanDistance.size(); j++) {
				if (policemanDistance.get(j) < 200)
					x = (int) (policemanDistance.get(j) * density);
				else
					x = (int) (200 * density);
				radarCopIcons.get(j)
						.setPadding(x + displacement, padding, 0, 0);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals("UPDATE_MAP_TAG")) {
				if ((screenLockButton.isChecked())) {
					LatLng latLng = (LatLng) intent.getExtras().get("location");
					mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,
							15));
				}
				updateRadar(context, (ArrayList<Double>) intent.getExtras()
						.get("thiefDistance"), (ArrayList<Double>) intent
						.getExtras().get("policemanDistance"));
			} else if (action.equals("UPDATE_MAP_OBJECT_TAG")) {
				ObjectOnMap player = (ObjectOnMap) intent.getExtras().get(
						"object");
				Marker marker = playerMarkers.get(player.getId());
				if (marker == null) {
					MarkerOptions markerOptions = new MarkerOptions();
					markerOptions.position(player.getLatlng());
					if (player.getName().compareTo("thief") == 0)
						markerOptions.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.thief));
					else
						markerOptions.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.policeman));
					markerOptions.title(player.getName());
					marker = mMap.addMarker(markerOptions);
					playerMarkers.put(player.getId(), marker);
				} else {
					marker.setPosition(player.getLatlng());
				}
			} else if (action.equals("REMOVE_MAP_OBJECT_TAG")) {
				ObjectOnMap item = (ObjectOnMap) intent.getExtras().get(
						"object");
				Marker m = allMarkers.get(item.getName());
				m.setVisible(false);
				m.remove();

				allMarkers.remove(item.getName());

			} else if (action.equals("ENABLE_VEST_BUTTON_TAG")) {
				vestButton.setEnabled(true);
			} else if (action.equals("DISPLAY_DIALOG")) {
				String title = (String) intent.getExtras().getString("title");
				boolean noRestart = false;
				if (intent.getExtras().containsKey("norestart"))
					noRestart = true;
				endGameDialog(title, noRestart);
			} else if (action.equals("ENABLE_JAMMER_BUTTON_TAG")) {
				jammerButton.setEnabled(true);
			} else if (action.equals("REMOVE_PLAYER_TAG")) {
				String playerID = (String) intent.getExtras().getString(
						"playerID");
				Marker m = playerMarkers.get(playerID);
				if (m != null) {
					m.setVisible(false);
					m.remove();
					playerMarkers.remove(playerID);
				}
			} else if (action.equals("BANK_ROBBED_UPDATE_MAP")) {
				ObjectOnMap bank = (ObjectOnMap) intent.getExtras().get("bank");
				Marker m = allMarkers.get(bank.getName());
				m.setVisible(false);
				m.remove();

				MarkerOptions markerOptions = new MarkerOptions();
				markerOptions.visible(true);
				markerOptions.position(bank.getLatlng());
				markerOptions.title(bank.getName());
				markerOptions.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.bank_robed));
				markerOptions.snippet(null);
				allMarkers.put(bank.getName(), mMap.addMarker(markerOptions));
				// alert dialog
				if (!GameService.isThief) {
					AlertDialog alertDialog = new AlertDialog.Builder(
							MapActivity.this).create();
					alertDialog.setTitle("Obavestenje");
					alertDialog.setMessage("Opljackana je banka!");
					alertDialog.setButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									Toast.makeText(getApplicationContext(),
											"Uhvatite lopova :)",
											Toast.LENGTH_SHORT).show();
								}
							});
					alertDialog.show();
				} else {
					moneyGathered = (GameService.moneyGathered);
					gatheredMoneyTextView
							.setText(String.valueOf(moneyGathered));
				}
			} else if (action.equals("INITIALISE_MAP")) {

				moneyGathered = (GameService.moneyGathered);
				ArrayList<ObjectOnMap> items = intent.getExtras()
						.getParcelableArrayList("items");
				ArrayList<ObjectOnMap> buildings = intent.getExtras()
						.getParcelableArrayList("buildings");
				if (!allMarkers.isEmpty()) {
					for (ObjectOnMap object : buildings) {
						if (allMarkers.containsKey(object.getName())) {
							allMarkers.get(object.getName()).setVisible(false);
							allMarkers.get(object.getName()).remove();
						}
					}
					for (ObjectOnMap object : items) {
						if (allMarkers.containsKey(object.getName())) {
							allMarkers.get(object.getName()).setVisible(false);
							allMarkers.get(object.getName()).remove();
						}
					}
				}
				allMarkers = new HashMap<String, Marker>();

				drawMarkers(buildings, items);
				drawBoundaries(
						(LatLng) intent.getExtras().get("mapCenter"), mMap);
				if (GameService.isThief) {
					vestButton.setEnabled(intent.getExtras().getBoolean(
							"vestAvailable"));
					jammerButton.setEnabled(intent.getExtras().getBoolean(
							"jammerAvailable"));

					View imBut;
					imBut = findViewById(R.id.shootButton);
					imBut.setVisibility(View.GONE);
					imBut = findViewById(R.id.bullet1);
					imBut.setVisibility(View.GONE);
					imBut = findViewById(R.id.bullet2);
					imBut.setVisibility(View.GONE);
					imBut = findViewById(R.id.bullet3);
					imBut.setVisibility(View.GONE);
				} else {
					remainingBullets = GameService.ammo;
					setRemainingBullets(remainingBullets);
				}

			} else if (action.equals("BULLETS_UPDATE_TAG")) {
				setRemainingBullets(intent.getExtras().getInt(
						"remainingBullets"));
				remainingBullets = GameService.ammo;
			}
		}
	}

	private void setRemainingBullets(int remainingBullets) {

		switch (remainingBullets) {
		case 3:
			bullet1.setAlpha(255);
			bullet2.setAlpha(255);
			bullet3.setAlpha(255);
			shootButton.setClickable(true);
			shootButton.setEnabled(true);
			break;
		case 2:
			bullet1.setAlpha(255);
			bullet2.setAlpha(255);
			bullet3.setAlpha(100);
			break;
		case 1:
			bullet1.setAlpha(255);
			bullet2.setAlpha(100);
			bullet3.setAlpha(100);
			break;

		default:
			bullet1.setAlpha(100);
			bullet2.setAlpha(100);
			bullet3.setAlpha(100);
			shootButton.setClickable(false);
			shootButton.setEnabled(false);
			break;
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {
		return true;
	}

	public boolean onPrepareOptionsMenu(Menu menu) {

		if (GameService.isThief) {
			List<String> listOfItems = new ArrayList<String>();
			for (ObjectOnMap ob : GameService.getGatheredItems()) {
				String itemName = ob.getName();
				if (!itemName.equals("pancir") && !itemName.equals("ometac"))
					listOfItems.add(itemName);
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);

			if (listOfItems.isEmpty()) {
				builder.setTitle("Niste pokupili nijedan predmet.");
			} else {
				CharSequence[] items = listOfItems
						.toArray(new CharSequence[listOfItems.size()]);

				builder.setTitle("Prikupljeni predmeti:");
				builder.setItems(items, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
					}
				});
			}
			AlertDialog alert = builder.create();
			alert.show();
		}
		return true;
	}

}
