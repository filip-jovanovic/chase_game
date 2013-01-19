package edu.elfak.chasegame;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.gms.maps.model.LatLng;

public class ObjectOnMap implements Parcelable {

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

	public ObjectOnMap(double lat, double lon, String id, String name,
			String type) {
		this.latlng = new LatLng(lat, lon);
		this.id = id;
		this.name = name;
		this.type = type;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(name);
		dest.writeString(type);
		double lat = latlng.latitude;
		dest.writeDouble(lat);
		double lng = latlng.longitude;
		dest.writeDouble(lng);
	}

	public ObjectOnMap(Parcel in) {
		id = in.readString();
		name = in.readString();
		type = in.readString();
		double lat = in.readDouble();
		double lng = in.readDouble();
		latlng = new LatLng(lat, lng);
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public static final Parcelable.Creator<ObjectOnMap> CREATOR = new Parcelable.Creator<ObjectOnMap>() {
		public ObjectOnMap createFromParcel(Parcel in) {
			return new ObjectOnMap(in);
		}

		public ObjectOnMap[] newArray(int size) {
			return new ObjectOnMap[size];
		}
	};
}
