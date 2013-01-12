package edu.elfak.chasegame;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class LocationSenderService extends Service implements LocationListener {

	public class LocalBinder extends Binder {
		LocationSenderService getService() {
        return LocationSenderService.this;
        }
    }

	private static final String TAG = "SERVICE!!";

	private LocationManager locationManager;
	private String provider;
	private long timeOfLastLocation;
	private static long TIME_DIFFERENCE = 2000;
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate(){
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		provider = locationManager.NETWORK_PROVIDER;
		Location lastKnownLocation = locationManager.getLastKnownLocation(provider);
		timeOfLastLocation = lastKnownLocation.getTime();
		
		provider = locationManager.GPS_PROVIDER;
		
		LatLng latLng = new LatLng(lastKnownLocation.getLatitude(),
				lastKnownLocation.getLongitude());
		
		locationManager.requestLocationUpdates(provider, 1000, 5, this);
		
	}
	
	@Override
	public void onDestroy(){
	    Log.v("SERVICE","Service killed");
	    locationManager.removeUpdates(this);
	    super.onDestroy();  
	}
	
	public void onLocationChanged(Location location) {
		LatLng latLng;
		if(TIME_DIFFERENCE<(location.getTime()-timeOfLastLocation))
		{	
			// this location is 10s "away" from last one
			latLng = new LatLng(location.getLatitude(),location.getLongitude());
			timeOfLastLocation = location.getTime();
			
			// TODO : send it to others
			Log.d(TAG, location.toString());
		}
	}

	public void onProviderDisabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onProviderEnabled(String provider) {
		// TODO Auto-generated method stub
		
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub
		
	}
	

}
