package edu.elfak.chasegame;

import java.util.ArrayList;
import java.util.List;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.os.IBinder;
import android.util.Log;

public class GameService extends Service {
	
		private ArrayList<ObjectOnMap> buildings;
		private ArrayList<ObjectOnMap> items;
		private ArrayList<ObjectOnMap> players;
		private int gameId;
		private int mapId;
		private String gameName;		

		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			
			buildings = new ArrayList<ObjectOnMap>();
			items = new ArrayList<ObjectOnMap>();
			players = new ArrayList<ObjectOnMap>();
			
			
			mapId = intent.getExtras().getInt("mapId");
			
			gameName = intent.getExtras().getString("gameName");
			
			String role = intent.getExtras().getString("role");
			
			players.add(new ObjectOnMap(0,0,LoginActivity.registrationId,role,"player"));	
			
			if(role.compareTo("thief")==0)
			{
				
				ArrayList<String> parameters = new ArrayList<String>();
				ArrayList<String> values = new ArrayList<String>();
				parameters.add("name");
				parameters.add("mapId");
				parameters.add("thief");
				values.add(gameName);
				values.add(String.valueOf(mapId));
				values.add(LoginActivity.registrationId);
				String result = "empty";
				result = HTTPHelper.sendValuesToUrl(parameters, values, HTTPHelper.CREATE_GAME_URL);
				Log.v("thief Game id: ",result);
				gameId = Integer.valueOf(result);
			}
			else
			{
				int whoAmI = 1;
				gameId = intent.getExtras().getInt("game_id");
				String thiefId = intent.getExtras().getString("thief");
				players.add(new ObjectOnMap(0,0,thiefId,"thief","player"));
				
				String cop_1Id = intent.getExtras().getString("cop_1");
				players.add(new ObjectOnMap(0,0,cop_1Id,"policeman1","player"));	
				String cop_2Id = intent.getExtras().getString("cop_2");
				if(cop_2Id.compareTo("empty")!=0)
				{
					players.add(new ObjectOnMap(0,0,cop_2Id,"policeman2","player"));	
					whoAmI = 2;
				}
				String cop_3Id = intent.getExtras().getString("cop_3");
				if(cop_3Id.compareTo("empty")!=0)
				{
					players.add(new ObjectOnMap(0,0,cop_3Id,"policeman3","player"));
					whoAmI = 3;
				}	
				
			}
			
			
			// populate items and buildings from server
			ArrayList<ObjectOnMap> allObjects = HTTPHelper.getBuildingAndItemList(String.valueOf(mapId));
			ObjectOnMap ob = null;
			for(int i = 0; i<allObjects.size(); i++)
			{
				ob = allObjects.get(i);
				if(ob.getType()=="building")
					buildings.add(ob);
				else
					items.add(ob);	
			}			
			
			registerReceiver(broadcastReceiver, new IntentFilter(GCMIntentService.TAG));
		    return START_STICKY;
		}
		
		private void announceNewPlayer(String newPlayerId)
		{			
			ArrayList<String> parameters = new ArrayList<String>();
			ArrayList<String> values = new ArrayList<String>();
		
			for(int i = 0; i<players.size(); i++){
				String id = players.get(i).getId();
				if(id.compareTo(newPlayerId)!=0){
					parameters.add(players.get(i).getName());
					values.add(id);
				}
			}
			parameters.add("new_player_id");
			values.add(newPlayerId);
			HTTPHelper.sendValuesToUrl(parameters, values, HTTPHelper.ANNOUNCE_NEW_PLAYER_URL);	
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
		
		public void OnDestroy(){
			unregisterReceiver(broadcastReceiver);
		}
		
		// broadcast receiver that handles messages from GCM
		private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
					 
			@Override public void onReceive(Context context, Intent intent) { 
				String message = intent.getExtras().getString("message");
				Log.v("GCM","Message received"+ message); // Do sth with message
			}
		
		};
}		 
