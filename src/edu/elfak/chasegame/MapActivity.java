package edu.elfak.chasegame;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;



public class MapActivity extends android.support.v4.app.FragmentActivity{

	
	protected void onCreate(Bundle savedInstanceState){
		 super.onCreate(savedInstanceState);
		 setContentView(R.layout.map);
	}
	// broadcast receiver that handles messages from GCM
	
	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        	String message = intent.getExtras().getString("message");
        	Log.v("GCM","Message received"+ message);
        	// Do sth with message
        	Toast.makeText(context, "Received: " + message, Toast.LENGTH_LONG).show();

        }
    };
    
    @Override
	public void onResume() {
		super.onResume();		
		registerReceiver(broadcastReceiver, new IntentFilter(GCMIntentService.TAG));
	}
 
	//@Override
	/*public void onPause() {
		super.onPause();
		unregisterReceiver(broadcastReceiver);
	}*/

}
