package edu.elfak.chasegame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HomeActivity extends Activity implements OnClickListener {
	
	private Bundle dataBundle;
	
	@Override
	public void onCreate(Bundle savedInstanceState ){
		
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
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
	

	@Override
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
	
	@Override
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
				break;
			case(R.id.exit_button):
				stopService(new Intent(this, GameService.class));
				finish();
		}
	}

}
