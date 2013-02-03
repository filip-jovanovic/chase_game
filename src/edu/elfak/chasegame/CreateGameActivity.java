package edu.elfak.chasegame;

import java.util.HashMap;
import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.EditText;

public class CreateGameActivity extends Activity implements OnClickListener {
	private EditText gameName;
	private Spinner listOfMaps;
	Button createGameBbutton;
	private Bundle dataBundle;
	private HashMap<String, LatLng> result;
	private Context context;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.creategame);

		dataBundle = getIntent().getExtras().getBundle("dataBundle");

		result = new HashMap<String, LatLng>();
		result = HttpHelper.getMapList();

		listOfMaps = (Spinner) findViewById(R.id.mapList);

		String[] maps = new String[result.size()];
		result.keySet().toArray(maps);
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this,
				//android.R.layout.simple_spinner_item, maps);
				R.layout.spinner_item,maps);
		listOfMaps.setAdapter(arrayAdapter);
		createGameBbutton = (Button) findViewById(R.id.startGame);
		createGameBbutton.setOnClickListener(this);

		gameName = (EditText) findViewById(R.id.gameName);
		context = this;
	}

	@Override
	public void onResume(){
		createGameBbutton.setOnClickListener(this);
		super.onResume();
	}

	@Override
	public void onClick(View arg0) {
		if (gameName.getText().toString().length() == 0)
			Toast.makeText(getBaseContext(), "Unesi tekst", Toast.LENGTH_SHORT)
					.show();
		else {		
			Toast.makeText(getBaseContext(), "Ucitavanje mape u toku ...",
					Toast.LENGTH_LONG).show();
			
			String selected = (String) listOfMaps.getSelectedItem();
			
			HttpHelper.flushParameters();
			HttpHelper.addParameter("game_name", gameName.getText().toString());
			String returned = HttpHelper.sendValuesToUrl(HttpHelper.CHECK_EXISTING_GAMES);
			Log.v("Create Game", "Vratio je: "+returned);
			if(returned.contains("not")){
				LatLng mapCenter = result.get(selected);
			Intent gameIntent = new Intent(context, GameService.class);
				gameIntent.putExtra("gameName", gameName.getText().toString());
			gameIntent.putExtra("mapId", Integer.parseInt(selected.split("\\.")[0]));
				gameIntent.putExtra("mapCenter", mapCenter);
				gameIntent.putExtra("role", "thief");
				gameIntent.putExtra("dataBundle", dataBundle);
				startService(gameIntent);
			
			Intent i = new Intent(context, MapActivity.class);
				startActivity(i);
				finish();
			
			}
			else if(returned.contains("exists")){
					Toast.makeText(getBaseContext(), "Igra sa tim imenom vec postoji, unesite drugo ime.",
						Toast.LENGTH_SHORT).show();
			} else 
				Toast.makeText(getBaseContext(), "Greska na na serveru pokusajte kasnije.",
						Toast.LENGTH_SHORT).show();
		}
	}
}
