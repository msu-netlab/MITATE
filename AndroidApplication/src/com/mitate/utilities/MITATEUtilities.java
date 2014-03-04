package com.mitate.utilities;

import com.mitate.MITATEApplication;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

public class MITATEUtilities extends PhoneStateListener {
	
	public static long lTimeDifference = 0;
	public static int iSignalStrength;
	
    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength)
    {
       super.onSignalStrengthsChanged(signalStrength);
       iSignalStrength = signalStrength.getGsmSignalStrength();
    }	
	
	// convert hex into string
	public static String parseHexString(String sHexString) {
		  StringBuilder sb = new StringBuilder();
		  for( int i=0; i<sHexString.length(); i+=2 ){
		      String output = sHexString.substring(i, (i + 2));
		      int decimal = Integer.parseInt(output, 16);
		      sb.append((char)decimal);
		  }
		  return sb.toString();
	}
	
	// convert hex into string
	public static String parseBinaryString(String sBinaryString) {
		  StringBuilder sb = new StringBuilder();
		  for( int i=0; i<sBinaryString.length(); i+=8 ){
		      String output = sBinaryString.substring(i, (i + 8));
		      int decimal = Integer.parseInt(output, 2);
		      sb.append((char)decimal);
		  }
		  return sb.toString();
	}	
	
	// get network type
	public static String getNetworkType(Context cContext) {
		if(MITATEApplication.bDebug) Log.i("MITATEUtilities","@getNetworkType Start");

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
			else if (netType == ConnectivityManager.TYPE_MOBILE) {
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
		while(true) {
	 	    SNTPClient client = new SNTPClient();
	    	if (client.requestTime("time.nist.gov", 5000)) { 
	    		lNTPTime = client.getNtpTime() + SystemClock.elapsedRealtime() - client.getNtpTimeReference();
	    		if((lNTPTime+"").length() == 13) {
	    			lTimeDifference = System.currentTimeMillis() - lNTPTime;
	    			break;
	    		} 
	    	} 			
		}

    	return lTimeDifference;	    
	} 
	
	/*
	public static long calculateTimeDifferenceBetweenNTPAndLocal() {
        long lNTPTime = 0;
		String sNTPServer = "us.pool.ntp.org";
		int ntpSubDomain = 0;
        SNTPClient client = new SNTPClient();   
		while((lNTPTime + "").length() < 13 && ntpSubDomain <= 4 ) {
			if(ntpSubDomain > 3) 
				ntpSubDomain = 0;
			if (client.requestTime(ntpSubDomain + "." + sNTPServer, 4000)) {
				lNTPTime = client.getNtpTime() + ((long)Math.ceil(System.nanoTime() * Math.pow(10, -6))) - client.getNtpTimeReference();
				System.out.println(lNTPTime + "****" + System.currentTimeMillis());
				if((lNTPTime+"").length() == 13) {
	    			lTimeDifference = System.currentTimeMillis() - lNTPTime;
	    			break;
	    		} 
			}
			else {
				ntpSubDomain = ntpSubDomain + 1;
			}
		}
        return lTimeDifference;
    }
	*/
	
}