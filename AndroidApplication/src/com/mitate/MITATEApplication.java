package com.mitate;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.NeighboringCellInfo;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

public class MITATEApplication extends Application {
	
    static Context cContext;
    static TelephonyManager tmTelephonyManager;
    
    public static boolean bDebug;
    static String TAG = "MITATEApplication";
    
    // for debugging
    static {
    	bDebug = true;
    }
    
    public void onCreate(){
    	cContext = getApplicationContext();	
    	tmTelephonyManager = (TelephonyManager)cContext.getSystemService(Context.TELEPHONY_SERVICE);
    }

    // return current application context
    public static Context getCustomAppContext(){
      return cContext;
    } 
    
    // return telephony manager system service
    public static TelephonyManager getTelephonyManager() {
    	return tmTelephonyManager;
    }

    // return device model name
    public static String getDeviceModel() {
    	return Build.MODEL.replaceAll("\\s", "%20");
    }
    
    // return the wetwork carrier
    public static String getNetworkCarrierName() {
    	return tmTelephonyManager.getNetworkOperatorName().replaceAll("\\s", "%20");
	}
    
    // check of user is on call
    public static int isCallActive(){
	   AudioManager manager = (AudioManager)cContext.getSystemService(Context.AUDIO_SERVICE);
	   if(manager.getMode()==AudioManager.MODE_IN_CALL){
	         return 1;
	   }
	   else{
	       return 0;
	   }
	}  
    
    // return signal strength - mobile / wifi
    @SuppressLint("NewApi")
	public static int getSignalStrength() {
    	
    	NetworkInfo userNetwork = ((ConnectivityManager)cContext.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
    	if(userNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {

    		if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                CellInfoLte cellinfolte = (CellInfoLte)tmTelephonyManager.getAllCellInfo().get(0);
                CellSignalStrengthLte cellSignalStrengthlte = cellinfolte.getCellSignalStrength();
                return(cellSignalStrengthlte.getAsuLevel());     			
    		} else {
    			return(999);
    		}
   		
    	} else { // (userNetwork.getType() == ConnectivityManager.TYPE_WIFI)
    		WifiManager wifiManager = (WifiManager)cContext.getSystemService(Context.WIFI_SERVICE);
    		return(wifiManager.getConnectionInfo().getRssi());    		
    	}
    }

    // return the battery power
    public static int getBatteryPower() {
	    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	    Intent batteryStatus = MITATEApplication.getCustomAppContext().registerReceiver(null, ifilter);
	    return(batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));    	
    }
}
