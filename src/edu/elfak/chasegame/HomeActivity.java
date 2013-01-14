package edu.elfak.chasegame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class HomeActivity extends Activity implements OnClickListener {

	public void onCreate(Bundle savedInstanceState ){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.home); 
        
        View but = findViewById(R.id.about_button);
        but.setOnClickListener(this);
        but = findViewById(R.id.create_game_button);
        but.setOnClickListener(this);
        but = findViewById(R.id.exit_button);
        but.setOnClickListener(this);
        but = findViewById(R.id.debug_button);
        but.setOnClickListener(this);
	}
	
	public void onClick(View v) {
		Intent i;
		switch(v.getId()){
			case(R.id.about_button):
				i = new Intent(this, AboutActivity.class);
				startActivity(i);
				break;
			case(R.id.create_game_button):
				i = new Intent(this, JoinGameActivity.class);
				startActivity(i);
				break;
			case(R.id.debug_button):
				i = new Intent(this, DebugActivity.class);
				startActivity(i);
				break;
			case(R.id.exit_button):
				stopService(new Intent(this, GameService.class));
				finish();
		}
	}

}
