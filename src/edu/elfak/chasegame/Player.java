package edu.elfak.chasegame;

import edu.elfak.chasegame.Player.Role;
import android.location.Location;

public class Player extends Location{
	
	public static String player_id;
	public enum Role { thief,policeman }

	private Role role;
	
	public Player(String registrationId, Role role ) {
		super("");
		player_id = registrationId;
		this.role = role;
	}


	
	public static void setPlayerID(String id){
		
	}
}
