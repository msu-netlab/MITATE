package com.mitate;

import android.app.Application;
import android.content.Context;
import android.telephony.TelephonyManager;

// mitate application instance, get current context and services like telephonymanager
public class MITATEApplication extends Application {
	
    static Context cContext;
    public static boolean bDebug;
    
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
    	TelephonyManager tmTelephonyManager = (TelephonyManager)cContext.getSystemService(Context.TELEPHONY_SERVICE);
    	return tmTelephonyManager;
    }
}
