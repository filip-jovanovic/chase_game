package edu.elfak.chasegame;

import com.google.android.gms.maps.model.LatLng;

public class ObjectOnMap {

	private LatLng latlng;
	private String id;
	private String name;
	private String type;
	
	public LatLng getLatlng() {
		return latlng;
	}

	public void setLatlng(LatLng latlng) {
		this.latlng = latlng;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public ObjectOnMap(double lat, double lon, String id, String name, String type){
		this.latlng = new LatLng(lat,lon);
		this.id = id;
		this.name = name;
		this.type = type;
	}
}
