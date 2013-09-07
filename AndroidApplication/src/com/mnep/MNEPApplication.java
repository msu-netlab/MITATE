package com.mnep;

import android.app.Application;
import android.content.Context;

public class MNEPApplication extends Application {
    static Context cContext;
    public static boolean bDebug;
    
    static {
    	bDebug = true;
    }
    
    public void onCreate(){
    	cContext = getApplicationContext();
    }

    public static Context getCustomAppContext(){
      return cContext;
    } 
}
