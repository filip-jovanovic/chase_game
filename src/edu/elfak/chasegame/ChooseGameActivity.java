package edu.elfak.chasegame;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;


public class ChooseGameActivity extends Activity implements OnClickListener {

	/**
	 * 
	 */
	public void onCreate(Bundle savedInstanceState ){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.choosegame); 
        
        View but = findViewById(R.id.enter_game_button);
        but.setOnClickListener(this);
        
	}
	public void onClick(View v) {
		
		if(v.getId() == R.id.enter_game_button);
		{
			Intent i = new Intent(this, ChaseGameMapActivity.class);
			startActivity(i);
		}
		
	}

}
