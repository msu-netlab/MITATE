package com.mnep.service;

import com.mnep.MNEPApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class MNEPReceiver extends BroadcastReceiver {

	String TAG = "MNEPReceiver";
	
	String sUsername = "";
	String sPassword = "";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		
		if(MNEPApplication.bDebug) Log.i(TAG, "@onReceive -  start");
		
		try {
			
			if(intent.getAction().equals("MNEPPending_Intent_Received")) {
				if(MNEPApplication.bDebug) Log.i(TAG, "@intent action -  MNEPPending_Intent_Received1");
			    Bundle bundle = intent.getExtras();
			    sUsername = bundle.getString("username");
			    sPassword = bundle.getString("password");
			    
				Intent intent1 = new Intent(context, LoginService.class);
				intent1.putExtra("username", sUsername);
				intent1.putExtra("password", sPassword);
				context.stopService(intent1);
				context.startService(intent1);					
			}
		 } catch (Exception e) {
			 Log.e(TAG, "@onReceive : error occurred - "+e.getMessage());
			 if(MNEPApplication.bDebug) e.printStackTrace();
		 }
		
		if(MNEPApplication.bDebug) Log.i(TAG, "@onReceive -  end");
	}
}