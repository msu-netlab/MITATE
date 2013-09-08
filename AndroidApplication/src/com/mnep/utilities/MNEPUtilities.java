package com.mnep.utilities;

import com.mnep.MNEPApplication;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.util.Log;

public class MNEPUtilities {
	
	public static long lTimeDifference = 0;
	
	public MNEPUtilities() {
		
	}
	
	public static String getNetworkType(Context cContext) {
		if(MNEPApplication.bDebug) Log.i("MNEPUtilities","@getNetworkType Start");

		ConnectivityManager mConnectivity = (ConnectivityManager) cContext.getSystemService(Context.CONNECTIVITY_SERVICE);
		TelephonyManager mTelephony = (TelephonyManager) cContext.getSystemService(Context.TELEPHONY_SERVICE);
		NetworkInfo info = mConnectivity.getActiveNetworkInfo();

		if (info == null || !mConnectivity.getBackgroundDataSetting()) {
			return "";
		}
		try {
			int netType = info.getType();
			int netSubtype = info.getSubtype();
			if (netType == ConnectivityManager.TYPE_WIFI) {
				return "wifi";
			} 
			else if (netType == ConnectivityManager.TYPE_MOBILE) { //  && netSubtype == TelephonyManager.NETWORK_TYPE_UMTS && !mTelephony.isNetworkRoaming()) {
				return "cellular";
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return "";
	}
	
	public static String getCity(Context cContext) {
		String sLatLong = getLatLong(cContext);
		return "sLatLong";		
	}
	
	public static String getLatLong(Context cContext) {
 	   LocationManager lm = (LocationManager) cContext.getSystemService(Context.LOCATION_SERVICE);
 	   Criteria crit = new Criteria();
 	   crit.setAccuracy(Criteria.ACCURACY_FINE);
 	   String provider = lm.getBestProvider(crit, false);
 	   Location loc = lm.getLastKnownLocation(provider);

		return loc.getLatitude()+""+loc.getLongitude();
	}
	
	// calculate time difference with NTP server
	public static long calculateTimeDifferenceBetweenNTPAndLocal() {
		long lNTPTime = 0;
 	    SNTPClient client = new SNTPClient();
		if (client.requestTime("us.pool.ntp.org",5000)) {
		   lNTPTime = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
		}
 	  
 	    long lSystemTime = System.currentTimeMillis();
 	    lTimeDifference = lSystemTime - lNTPTime;
 	    return lTimeDifference;
	}
}