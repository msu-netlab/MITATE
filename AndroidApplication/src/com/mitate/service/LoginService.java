package com.mitate.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.mitate.MITATEApplication;
import com.mitate.measurement.Measurement;
import com.mitate.utilities.MITATELocation;
import com.mitate.utilities.MITATEUtilities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class LoginService extends Service {

	String TAG = "LoginService";
	
	public static String sWebServerName = "192.168.1.4";
	public static String sUserName;
	public static String sPassword;
	public static String sDeviceId;
	public static Transfer tPendingTransfers[];
	
	public static final String sPreferenceName = "MNEP_Preferences";	
    public static SharedPreferences spMNEPPreference;
    public static SharedPreferences.Editor editor;
    public static long lPollInterval;
    int iConnectionTimeout = 5000;
    
    Thread tMeasurement;
    Thread tLogin;
    
	@Override
	public IBinder onBind(Intent intent) {
		if(MITATEApplication.bDebug) Log.i(TAG, "@onBind - start");		
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		if(MITATEApplication.bDebug) Log.i(TAG, "@onCreate - start");
		if(MITATEApplication.bDebug) Log.i(TAG, "@onClick - login button - calling @executelogisn");		
		
		spMNEPPreference = MITATEApplication.getCustomAppContext().getSharedPreferences(sPreferenceName, 0);	
		lPollInterval = spMNEPPreference.getLong("pollinginterval", 100000);
		
		Thread tLogin =  new Thread(new Runnable() {
			public void run() {
				Log.i(TAG, "Active Thread Count = "+Thread.activeCount());
				if(Thread.activeCount() > 10) {
					Log.i(TAG, "Active Thread Count = "+Thread.activeCount());
					return;
				}

				if(MITATEApplication.bDebug) Log.i(TAG, "@onClick - login button - calling @executelogin");
				executeLogin(getApplicationContext());
				
				/* if(!tPendingTransfers[0].getsContent().equals("NoPendingTransactions")) {
					tMeasurement = new Measurement();		            
					tMeasurement.start();
		            try {
		            	tMeasurement.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} */				
				
			}
		});
		tLogin.start();			
	}

	public String getDeviceId(String sUsername, String sPassword, String sPhoneNumber, String sDeviceName) throws Exception {

		   	String sURL = "http://"+sWebServerName+"/setup_deviceid.php?username=" + sUsername + "&password=" + sPassword + "&phone_number=" + sPhoneNumber + "&device_name=" + sDeviceName;
		   	HttpClient hcHttpClient = new DefaultHttpClient();	
		   	HttpConnectionParams.setConnectionTimeout(hcHttpClient.getParams(), iConnectionTimeout);
		   	HttpResponse hrHttpResponse = hcHttpClient.execute(new HttpPost(sURL));        
			HttpEntity entity = hrHttpResponse.getEntity();
		    
			BufferedReader brReader = new BufferedReader(new InputStreamReader(entity.getContent(),"iso-8859-1"),8);
		    String sLine = brReader.readLine();;
	        return sLine;  
	}
	
	// login to web server, database server
	public synchronized boolean executeLogin(Context cContext) {
		if(MITATEApplication.bDebug)  Log.i(TAG, "@executeLogin() : start");
	       String sResult = "";
	       
	       spMNEPPreference = MITATEApplication.getCustomAppContext().getSharedPreferences(sPreferenceName, 0);	
	       sUserName = spMNEPPreference.getString("username", "");
	       sPassword = spMNEPPreference.getString("password", "");
	       
	       try {
	    	   String sPhoneNumber = MITATEApplication.getTelephonyManager().getLine1Number();
	    	   String sDeviseModel = Build.MODEL.replaceAll("\\s", "");
	    	   if((sDeviceId = getDeviceId(sUserName, sPassword, sPhoneNumber, sDeviseModel)).equals("InvalidLogin")) {
            	   if(MITATEApplication.bDebug) Log.v(TAG, "@executeLogin() : invalid login credentials");
            	   tPendingTransfers = new Transfer[1];
            	   tPendingTransfers[0] = new Transfer();
            	   tPendingTransfers[0].setsContent("InvalidLogin");
            	   return false;	    		   
	    	   }
		   
	    	   MITATELocation mLocation = new MITATELocation();
	    	   
	    	   String sCoordinates = mLocation.getCoordinates(MITATEApplication.getCustomAppContext());
	    	   
    	   	   /// String sURL = "http://54.243.186.107/mobilelogin.php?" +
    	   	   String sURL = "http://"+sWebServerName+"/mobilelogin.php?" +
    	   			// "http://172.17.5.69/mnep/mobilelogin.php?" +
    	   			// "http://10.0.2.2/mnep/mobilelogin.php?" +
       	   	   		"username="+sUserName+ //
   	    	   	    "&password="+sPassword+ //
   	    	   	    "&time="+(new Timestamp(System.currentTimeMillis())).toString().substring(10, 19).replaceAll(":","").trim()+ 
   	    	   	    // "&networktype="+MITATEUtilities.getNetworkType(cContext)+"&city="+mLocation.getCity(cContext);
   	    	   	    "&networktype=wifi"+ //+MITATEUtilities.getNetworkType(cContext)+
   	    	   	    "&deviceid="+sDeviceId+"&latitude="+sCoordinates.split(":")[0]+"&longitude="+sCoordinates.split(":")[1];
    	   	   
    	   	   HttpClient hcHttpClient = new DefaultHttpClient();	
   		   	   HttpConnectionParams.setConnectionTimeout(hcHttpClient.getParams(), iConnectionTimeout);
               HttpPost hpHttpPost = new HttpPost(sURL);                           
               // httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
               if(MITATEApplication.bDebug) Log.i(TAG, "@executeLogin() : request - "+hpHttpPost.getRequestLine().toString());
               
               HttpResponse hrHttpResponse = hcHttpClient.execute(hpHttpPost);
               HttpEntity entity = hrHttpResponse.getEntity();

               BufferedReader brReader = new BufferedReader(new InputStreamReader(entity.getContent(),"iso-8859-1"),8);
               StringBuilder sbTemp = new StringBuilder();
               String sLine = null;
               while ((sLine = brReader.readLine()) != null) {
            	   sbTemp.append(sLine + "\n");
               }
	        
               sResult = sbTemp.toString();		               
               if(MITATEApplication.bDebug) Log.i(TAG,"@executeLogin() : got transactions - ");

               JSONArray jaPendingTransfers = new JSONArray(sResult);
               tPendingTransfers = new Transfer[jaPendingTransfers.length()];
               
               System.out.println("_------------->"+(jaPendingTransfers.getJSONObject(0)).getString("content"));
               
               // set invalid login to content attributes
               if((jaPendingTransfers.getJSONObject(0)).getString("content").equals("InvalidLogin")) {
            	   if(MITATEApplication.bDebug) Log.v(TAG, "@executeLogin() : invalid login credentials");
            	   tPendingTransfers[0] = new Transfer();
            	   tPendingTransfers[0].setsContent("InvalidLogin");
            	   return false;
               }	               
               else if((jaPendingTransfers.getJSONObject(0)).getString("content").equals("NoPendingTransactions")) {
            	   if(MITATEApplication.bDebug) Log.v(TAG, "@executeLogin() : no pending transactions");
            	   tPendingTransfers[0] = new Transfer();
            	   tPendingTransfers[0].setsContent("NoPendingTransactions");
            	   return true;
               }
               else {
            	   if(MITATEApplication.bDebug) Log.v(TAG, "@executeLogin() : number of pending transactions - "+jaPendingTransfers.length());
	               for(int i=0;i<jaPendingTransfers.length();i++) {
                       JSONObject json_data = jaPendingTransfers.getJSONObject(i);
                       tPendingTransfers[i] = new Transfer();
                       tPendingTransfers[i].setiBytes(json_data.getInt("bytes"));
                       tPendingTransfers[i].setsServerIP(json_data.getString("destinationip"));
                       tPendingTransfers[i].setsSourceIP(json_data.getString("sourceip"));
                       tPendingTransfers[i].setiTransactionid(json_data.getInt("transactionid"));
                       tPendingTransfers[i].setiTransferid(json_data.getInt("transferid"));
                       tPendingTransfers[i].setiPacketType(json_data.getInt("type"));
                       tPendingTransfers[i].setiPacketDelay(json_data.getInt("packetdelay"));
                       tPendingTransfers[i].setiExplicit(json_data.getInt("explicit"));
                       tPendingTransfers[i].setsContent(json_data.getString("content"));                       
                       tPendingTransfers[i].setiNoOfPackets(json_data.getInt("noofpackets"));
                       tPendingTransfers[i].setsPortNumber(json_data.getString("portnumber"));
                       tPendingTransfers[i].setsContentType(json_data.getString("contenttype"));
                       tPendingTransfers[i].setiResponse(json_data.getInt("response"));
                       if(tPendingTransfers[i].getsContent().trim().length() == 0) {
                    	   tPendingTransfers[i].setsContent("null");
                       } else {
                    	   tPendingTransfers[i].setsContent(tPendingTransfers[i].getsContent().replaceAll("\t", " ").trim());
                       }
	               }
               }
	       } 
	       catch(Exception e){
	    	   Log.e(TAG, "@executeLogin() : error - "+e.getMessage());
	    	   if(MITATEApplication.bDebug) e.printStackTrace(); 
	    	   tPendingTransfers = new Transfer[1];
         	   tPendingTransfers[0] = new Transfer();
         	   tPendingTransfers[0].setsContent(e.getClass().toString().substring(e.getClass().toString().lastIndexOf(".") + 1).replace("Exception", "").replaceAll("(.)([A-Z])", "$1 $2"));
	           return false;
	       }	       
	       
	       
	       if(!tPendingTransfers[0].getsContent().equals("NoPendingTransactions")) {
	    	   tMeasurement = new Measurement();		            
	    	   tMeasurement.start();
	           try {
	           	tMeasurement.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
	       } 	
	       
	       if(MITATEApplication.bDebug) Log.i(TAG, "@executeLogin() : end");
	       return true;
	} 	
	
}