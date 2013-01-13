package edu.elfak.chasegame;

import com.google.android.gms.maps.model.LatLng;

public class ObjectOnMap {

	private LatLng latlng;
	private String id;
	private String name;
	private String type;
	
	public ObjectOnMap(double lat, double lon, String id, String name, String type){
		this.latlng = new LatLng(lat,lon);
		this.id = id;
		this.name = name;
		this.type = type;
	}
}
