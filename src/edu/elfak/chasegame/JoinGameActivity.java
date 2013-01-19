package edu.elfak.chasegame;

import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.maps.model.LatLng;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class JoinGameActivity extends Activity implements OnClickListener, OnItemClickListener{

	private HashMap<String, String> gamesHashMap;
	private Bundle dataBundle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.joingame);
		
		dataBundle = getIntent().getExtras().getBundle("dataBundle");
		
		gamesHashMap = HTTPHelper.getGameList();
		ArrayList<String> listForAdapter = new ArrayList<String>();
		Object[] games = gamesHashMap.keySet().toArray();
		for(int i = 0; i< gamesHashMap.size(); i++){
			listForAdapter.add((String)games[i]);
		}
		
		ListView list = (ListView) findViewById(R.id.gameList);
		
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
						(this,android.R.layout.simple_list_item_1, listForAdapter);
		list.setAdapter(arrayAdapter);
		list.setOnItemClickListener(this);
		
		/*new OnItemClickListener() {
			
            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
            	
                String item = ((TextView)v).getText().toString();
                
                Toast.makeText(getBaseContext(), item, Toast.LENGTH_SHORT).show();
            }
        });*/
		View but = findViewById(R.id.createGameButton);
        but.setOnClickListener(this);
	}

	public void onClick(View arg0) {
		Intent i = new Intent(this, CreateGameActivity.class);
		i.putExtra("dataBundle",dataBundle);
		startActivity(i);	
		finish();
	}
	
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		
		String gameId =  gamesHashMap.get(((TextView)arg1).getText().toString());

		ArrayList<String> parameters = new ArrayList<String>();
		ArrayList<String> values =  new ArrayList<String>();
		parameters.add("game_id");
		parameters.add("player_id");
		values.add(gameId);
		values.add(dataBundle.getString("registrationId"));
		String res = HTTPHelper.sendValuesToUrl(parameters, values, HTTPHelper.UPDATE_GAME_URL);
		
		Intent gameIntent = new Intent(this, GameService.class);
		
		try {
			Log.v("res",res);
			
			JSONObject jsonGame = new JSONObject(res);
			
			gameIntent.putExtra("gameName",jsonGame.getString("game_name"));
			gameIntent.putExtra("mapId",jsonGame.getInt("map_id"));
			
			gameIntent.putExtra("mapCenter", new LatLng(jsonGame.getDouble("map_latitude"), 
					jsonGame.getDouble("map_longitude")));
			
			gameIntent.putExtra("thief",jsonGame.getString("thief"));
			gameIntent.putExtra("cop_1",jsonGame.getString("cop_1"));
			gameIntent.putExtra("cop_2",jsonGame.getString("cop_2"));
			gameIntent.putExtra("cop_3",jsonGame.getString("cop_3"));			
			gameIntent.putExtra("game_id",Integer.valueOf(gameId));
	    	gameIntent.putExtra("role","policeman");
	    	gameIntent.putExtra("dataBundle",dataBundle);
	    	
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
    	startService(gameIntent);

    	Intent i = new Intent(this, MapActivity.class);
    	//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    	startActivity(i);	
    	finish();
	}
}
