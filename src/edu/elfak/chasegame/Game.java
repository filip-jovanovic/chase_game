package edu.elfak.chasegame;

import java.util.List;

import edu.elfak.chasegame.Player.Role;

import android.location.Location;

public class Game {
	
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

		public Game(int gameId, int mapId, String gameName) {
			
			players.add(new Player(LoginActivity.registrationId,Role.thief));
			
			this.gameId = gameId;
			this.mapId = mapId;
			this.gameName = gameName;
			
			this.buildings = buildings;
			this.items = items;
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
		
}
