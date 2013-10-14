package com.mitate;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;

@TargetApi(17)
public class MITATEApplication extends Application {
	
    static Context cContext;
    public static boolean bDebug;
    static TelephonyManager tmTelephonyManager;
    
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

    public static String getDeviceModel() {
    	return Build.MODEL.replaceAll("\\s", "%20");
    }
    
    public static String getNetworkCarrierName() {
    	return tmTelephonyManager.getNetworkOperatorName().replaceAll("\\s", "%20");
	}
    
    public static int isCallActive(){
	   AudioManager manager = (AudioManager)cContext.getSystemService(Context.AUDIO_SERVICE);
	   if(manager.getMode()==AudioManager.MODE_IN_CALL){
	         return 1;
	   }
	   else{
	       return 0;
	   }
	}  
    
    public static int getSignalStrength() {
        // API Min 17
        CellInfoLte cellinfolte = (CellInfoLte)tmTelephonyManager.getAllCellInfo().get(0);
        CellSignalStrengthLte cellSignalStrengthlte = cellinfolte.getCellSignalStrength();
        // System.out.println("------------>///`"+cellSignalStrengthlte.getDbm()+"-"+cellSignalStrengthlte.getLevel()+"-"+cellSignalStrengthlte.getAsuLevel());		    	   
        return(cellSignalStrengthlte.getAsuLevel());    	
    }
    
    public static int getBatteryPower() {
	    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
	    Intent batteryStatus = MITATEApplication.getCustomAppContext().registerReceiver(null, ifilter);
	    return(batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1));    	
    }
}
