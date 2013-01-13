package edu.elfak.chasegame;

import java.util.List;

import edu.elfak.chasegame.Player.Role;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

public class GameService extends Service {
	
		private List<Location> buildings;
		private List<Location> items;
		private List<Player> players;
		private int gameId;
		private int mapId;
		private String gameName;		

		public List<Location> getBuildings() {
			return buildings;
		}

		public void setBuildings(List<Location> buildings) {
			this.buildings = buildings;
		}

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			Log.v("GameService","Game initialisation..."); 

			mapId = intent.getExtras().getInt("mapid");
			gameName = intent.getExtras().getString("gameName");
			Role role = (Role)intent.getExtras().get("role");
			players.add(new Player(LoginActivity.registrationId,role));
			
			if(role==Role.thief)
			{
				// create the game on server!
			}
			else
			{
				// find the game on server and pull info about players
			}
			
			// populate items and buildings from server
			
			
			
			/*
			this.buildings = buildings;
			this.items = items;
			*/
			
			registerReceiver(broadcastReceiver, new IntentFilter(GCMIntentService.TAG));
		    return START_STICKY;
		}
		
		
		
		public List<Location> getItems() {
			return items;
		}

		public void setItems(List<Location> items) {
			this.items = items;
		}

		public int getGameId() {
			return gameId;
		}

		public void setGameId(int gameId) {
			this.gameId = gameId;
		}

		@Override
		public IBinder onBind(Intent arg0) {
			// TODO Auto-generated method stub
			return null;
		}
		
		// broadcast receiver that handles messages from GCM
		private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
					 
			@Override public void onReceive(Context context, Intent intent) { 
				String message = intent.getExtras().getString("message");
				Log.v("GCM","Message received"+ message); // Do sth with message
			}
		
		};
}		 
