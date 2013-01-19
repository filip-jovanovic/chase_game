package edu.elfak.chasegame;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeActivity extends Activity implements OnClickListener {
	
	private Bundle dataBundle;
	
	public void onCreate(Bundle savedInstanceState ){
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.home); 
        
        dataBundle = getIntent().getExtras().getBundle("dataBundle");
    	
        View but = findViewById(R.id.about_button);
        but.setOnClickListener(this);
        but = findViewById(R.id.create_game_button);
        but.setOnClickListener(this);
        but = findViewById(R.id.exit_button);
        but.setOnClickListener(this);
        but = findViewById(R.id.stop_game_button);
        but.setOnClickListener(this);
	}
	

	public void onResume(){
		
        Button button = (Button)findViewById(R.id.create_game_button);
        Button buttonStop = (Button)findViewById(R.id.stop_game_button);
		if(GameService.isRuning){
			button.setText("Resume");
			buttonStop.setVisibility(View.VISIBLE);
		}
	    else
	    {
	    	button.setText("New Game");
	    	buttonStop.setVisibility(View.GONE);
	    }
		super.onResume();
		
	}
	
	public void onClick(View v) {
		Intent i;
		switch(v.getId()){
			case(R.id.about_button):
				i = new Intent(this, AboutActivity.class);
				startActivity(i);
				break;
			case(R.id.create_game_button):
				if(GameService.isRuning){
					i = new Intent(this, MapActivity.class);
					//i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(i);
				}
				else{
					i = new Intent(this, JoinGameActivity.class);
					i.putExtra("dataBundle", dataBundle);
					startActivity(i);
				}
				
				break;
			case(R.id.stop_game_button):
				
				stopService(new Intent(this, GameService.class));
				Button buttonStop = (Button)findViewById(R.id.stop_game_button);
				Button button = (Button)findViewById(R.id.create_game_button);
				button.setText("New Game");
				buttonStop.setVisibility(View.GONE);
				//obrisi igrace iz baze i game tabele
				if(GameService.isRuning){
					ArrayList<String> parameters = new ArrayList<String>();
					ArrayList<String> values = new ArrayList<String>();
					parameters.add("game_id");
					parameters.add("player_id");
					parameters.add("place");
					values.add(String.valueOf(GameService.gameId));
					values.add(String.valueOf(GameService.registrationId));
					values.add(String.valueOf(GameService.numberOfPolicemen));
					String result = HTTPHelper.sendValuesToUrl(parameters, values, HTTPHelper.EXIT_GAME);
				}
				break;
			case(R.id.exit_button):
				stopService(new Intent(this, GameService.class));
				finish();
		}
	}

}
