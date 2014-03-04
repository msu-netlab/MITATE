package com.mitate.utilities;

import java.util.List;
import java.util.Locale;

import com.mitate.MITATEApplication;

import android.content.Context;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class MITATELocation implements LocationListener {
	private static final String TAG = "LocationDemo";
	private LocationManager locationManager;
	private String bestProvider;
	
	public String getCity(Context mContext) {
		try {
		Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
		
		List<Address> addresses = gcd.getFromLocation(getLatLong(mContext).getLatitude(), getLatLong(mContext).getLongitude(), 1);
		if (addresses.size() > 0) 
			if(MITATEApplication.bDebug) System.out.println(addresses.get(0).getLocality());	
			return addresses.get(0).getLocality().toString();
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return "";
	}
	
	public String getCoordinates(Context mContext) {
		
		LocationManager lm = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE); 
		Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(location == null) {
			return 45.66962856799364+":"+(-111.06049848720431);
		}
		double longitude = location.getLongitude();
		double latitude = location.getLatitude();
		
		return latitude+":"+longitude;
	}
	
	public Location getLatLong(Context mContext) {
		try {
		Log.i("TAG", "@getLatLong - start");
		// Get the location manager
		locationManager = (LocationManager) mContext.getSystemService(mContext.LOCATION_SERVICE);

		// List all providers:
		List<String> providers = locationManager.getAllProviders();
		for (String provider : providers) {
			printProvider(provider);
		}

		Criteria criteria = new Criteria();
		bestProvider = locationManager.getBestProvider(criteria, false);

		Location location = locationManager.getLastKnownLocation(bestProvider);
		
		return location;
		}
		catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void onLocationChanged(Location location) {
		// printLocation(location);
	}

	public void onProviderDisabled(String provider) {
		// let okProvider be bestProvider
		// re-register for updates
		// output.append("\n\nProvider Disabled: " + provider);
	}

	public void onProviderEnabled(String provider) {
		// is provider better than bestProvider?
		// is yes, bestProvider = provider
		// output.append("\n\nProvider Enabled:: " + provider);
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// output.append("\n\nProvider Status Changed: " + provider + ", Status="
			// 	+ S[status] + ", Extras=" + extras);
	}

	private void printProvider(String provider) {
		LocationProvider info = locationManager.getProvider(provider);
		// output.append(info.toString() + "\n\n");
	}
	
	

	private void printLocation(Location location, Context mContext) {
		if (location == null)
			// output.append("\nLocation[unknown]\n\n");
			Log.i(TAG, "@getLatLong1 - start");
		else {
			if(MITATEApplication.bDebug) Log.i(TAG, "\n\n" + location.getLatitude()+"--"+location.getLongitude());
			try {
				Geocoder gcd = new Geocoder(mContext, Locale.getDefault());
				List<Address> addresses = gcd.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
				if (addresses.size() > 0) 
					if(MITATEApplication.bDebug) System.out.println("-----------------"+addresses.get(0).getLocality());	
			}
			catch(Exception e) {
				e.printStackTrace();
			}
			
		}
	}

}