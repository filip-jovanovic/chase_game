package edu.elfak.chasegame;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;
import android.widget.EditText;

public class CreateGameActivity extends Activity implements OnClickListener {
	private EditText gameName;
	private Spinner listOfMaps;
	private Bundle dataBundle;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.creategame);
		
		dataBundle = getIntent().getExtras().getBundle("dataBundle");
		
		ArrayList<String> maps = HTTPHelper.getMapList();
		listOfMaps = (Spinner) findViewById(R.id.mapList);
		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>
						(this,android.R.layout.simple_spinner_dropdown_item, maps);
		listOfMaps.setAdapter(arrayAdapter);
		View button = findViewById(R.id.startGame);
		button.setOnClickListener(this);
		
		gameName = (EditText) findViewById(R.id.gameName);
	}
	
	public void onClick(View arg0) {
	    if(gameName.getText().toString().compareTo("")==0)
	    	Toast.makeText(getBaseContext(), "Unesi tekst", Toast.LENGTH_SHORT).show();
	    else{
	    	
	    	Intent gameIntent = new Intent(this, GameService.class);
	    	gameIntent.putExtra("gameName",gameName.getText().toString());
	    	gameIntent.putExtra("mapId",Integer.parseInt(((String)listOfMaps.getSelectedItem()).split("\\.")[0]));
	    	gameIntent.putExtra("role","thief");
	    	gameIntent.putExtra("dataBundle",dataBundle);
	    	startService(gameIntent);

	    	Intent i = new Intent(this, MapActivity.class);
	    	//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	    	startActivity(i);	
	    	finish();
	    }
	}
}
