package com.mitate;

import java.sql.Timestamp;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.telephony.TelephonyManager;

// mitate application instance, get current context and services like telephonymanager
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
    }

    // return current application context
    public static Context getCustomAppContext(){
      return cContext;
    } 
    
    // return telephony manager system service
    public static TelephonyManager getTelephonyManager() {
    	tmTelephonyManager = (TelephonyManager)cContext.getSystemService(Context.TELEPHONY_SERVICE);
    	return tmTelephonyManager;
    }

    public static String getDeviceModel() {
    	return Build.MODEL.replaceAll("\\s", "%20");
    }
    
    public static String getNetworkCarrierName() {
    	tmTelephonyManager = (TelephonyManager)cContext.getSystemService(Context.TELEPHONY_SERVICE);
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
}
