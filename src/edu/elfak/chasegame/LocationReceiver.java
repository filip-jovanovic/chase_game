package edu.elfak.chasegame;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;

public class LocationReceiver extends Activity implements OnClickListener {

	private static final String TAG = "LOCRECEIVER";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location_receiver);
		Log.d(TAG, "STARTED LOC RECEIVER");
		View but = findViewById(R.id.buttonStartService);
        but.setOnClickListener(this);
        but = findViewById(R.id.buttonStopService);
        but.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_location_receiver, menu);
		return true;
	}

	public void onClick(View v) {
		Intent i;
		 switch (v.getId()) {
		    case R.id.buttonStartService:
		      Log.d(TAG, "onClick: starting srvice");
		      startService(new Intent(this, LocationSenderService.class));
		      break;
		    case R.id.buttonStopService:
		      Log.d(TAG, "onClick: stopping srvice");
		      stopService(new Intent(this, LocationSenderService.class));
		      break;
		    }
		 
		
	}

}
