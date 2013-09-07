package com.mnep.service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.mnep.MNEPApplication;
import com.mnep.measurement.Measurement;
import com.mnep.utilities.MNEPLocation;
import com.mnep.utilities.MNEPUtilities;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;

public class LoginService extends Service {

	String TAG = "LoginService";
	
	public static String sUserName = "demo";
	public static String sPassword = "demo";
	public static Transfer tPendingTransfers[] = null;
	
	public static final String sPreferenceName = "MNEP_Preferences";	
    public static SharedPreferences spMNEPPreference = null;
    public static SharedPreferences.Editor editor = null;
    
    public static long lPollInterval;
    
    Thread tMeasurement;
    Thread tLogin;
    
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		if(MNEPApplication.bDebug) Log.i(TAG, "@onBind - start");		
		return null;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		if(MNEPApplication.bDebug)  Log.i(TAG, "@onCreate - start");
		if(MNEPApplication.bDebug) Log.i(TAG, "@onClick - login button - calling @executelogin");
		
		
		spMNEPPreference = MNEPApplication.getCustomAppContext().getSharedPreferences(sPreferenceName, 0);	
		lPollInterval = spMNEPPreference.getLong("pollinginterval", 100000);
		
		Thread tLogin =  new Thread(new Runnable() {
			public void run() {
				if(MNEPApplication.bDebug) Log.i(TAG, "@onClick - login button - calling @executelogin");
					// if(tMeasurement.isAlive()) {
						executeLogin(getApplicationContext());
						
						if(!tPendingTransfers[0].getSLocation().equals("NoPendingTransactions")) {
							tMeasurement = new Measurement();		            
							tMeasurement.start();
				            try {
				            	tMeasurement.join();
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					// }
			}
		});
		tLogin.start();			
	}

	// login to web server, database server
	public boolean executeLogin(Context cContext) {
		if(MNEPApplication.bDebug)  Log.i(TAG, "@executeLogin() : start");
	       String sResult = "";
	       InputStream is = null;
	       
	       spMNEPPreference = MNEPApplication.getCustomAppContext().getSharedPreferences(sPreferenceName, 0);	
	       sUserName = spMNEPPreference.getString("username", "mwittie");
	       sPassword = spMNEPPreference.getString("password", "mwittie");
	       
	       long now = 0;
	       try {
	    	   MNEPLocation mLocation = new MNEPLocation();
	    	   
	    	   String sCoordinates = mLocation.getCoordinates(MNEPApplication.getCustomAppContext());
	    	   
	    	   TelephonyManager tmTelephoneManager = (TelephonyManager)getSystemService(getApplicationContext().TELEPHONY_SERVICE);
	    	   String sPhoneNumber = tmTelephoneManager.getLine1Number();
	    	   
    	   	   // String sURL = "http://54.243.186.107/mobilelogin.php?" +
    	   		String sURL = "http://192.168.1.12/mobilelogin.php?" +
    	   			// "http://172.17.5.69/mnep/mobilelogin.php?" +
    	   			// "http://10.0.2.2/mnep/mobilelogin.php?" +
       	   	   		"username="+sUserName+ //
   	    	   	    "&password="+sPassword+ //
   	    	   	    "&time="+(new Timestamp(System.currentTimeMillis())).toString().substring(10, 19).replaceAll(":","").trim()+ 
   	    	   	    // "&networktype="+MNEPUtilities.getNetworkType(cContext)+"&city="+mLocation.getCity(cContext);
   	    	   	    "&networktype=wifi"+ //+MNEPUtilities.getNetworkType(cContext)+
   	    	   	    "&city=Bozeman&deviceid="+sPhoneNumber+"&latitude="+sCoordinates.split(":")[0]+"&longitude="+sCoordinates.split(":")[1];
    	   	   
    	   	   HttpClient hcHttpClient = new DefaultHttpClient();		    
               HttpPost hpHttpPost = new HttpPost(sURL);                           
               // httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
               if(MNEPApplication.bDebug) Log.i(TAG, "@executeLogin() : request - "+hpHttpPost.getRequestLine().toString());
               
               HttpResponse hrHttpResponse = hcHttpClient.execute(hpHttpPost);
               HttpEntity entity = hrHttpResponse.getEntity();
               is = entity.getContent();

               BufferedReader brReader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8);
               StringBuilder sbTemp = new StringBuilder();
               String sLine = null;
               while ((sLine = brReader.readLine()) != null) {
            	   sbTemp.append(sLine + "\n");
               }
               is.close();		        
               sResult = sbTemp.toString();		               
               if(MNEPApplication.bDebug) Log.i(TAG,"@executeLogin() : got transactions - ");

               JSONArray jaPendingTransfers = new JSONArray(sResult);
               tPendingTransfers = new Transfer[jaPendingTransfers.length()];

               if((jaPendingTransfers.getJSONObject(0)).getString("location").equals("InvalidLogin")) {
            	   if(MNEPApplication.bDebug) Log.v(TAG, "@executeLogin() : invalid login credentials");
            	   tPendingTransfers[0] = new Transfer();
            	   tPendingTransfers[0].setsLocation("InvalidLogin");
            	   return false;
               }	               
               else if((jaPendingTransfers.getJSONObject(0)).getString("location").equals("NoPendingTransactions")) {
            	   if(MNEPApplication.bDebug) Log.v(TAG, "@executeLogin() : no pending transactions");
            	   tPendingTransfers[0] = new Transfer();
            	   tPendingTransfers[0].setsLocation("NoPendingTransactions");
            	   return true;
               }
               else {
            	   if(MNEPApplication.bDebug) Log.v(TAG, "@executeLogin() : number of pending transactions - "+jaPendingTransfers.length());
	               for(int i=0;i<jaPendingTransfers.length();i++){
                       JSONObject json_data = jaPendingTransfers.getJSONObject(i);
                       tPendingTransfers[i] = new Transfer();
                       tPendingTransfers[i].setsLocation(json_data.getString("location"));
                       tPendingTransfers[i].setiBytes(json_data.getInt("bytes"));
                       tPendingTransfers[i].setsServerIP(json_data.getString("destinationip"));
                       tPendingTransfers[i].setsSourceIP(json_data.getString("sourceip"));
                       tPendingTransfers[i].setsEndTime(json_data.getString("endtime"));
                       tPendingTransfers[i].setsStartTime(json_data.getString("starttime"));
                       tPendingTransfers[i].setsNetworkType(json_data.getString("networktype"));		                       
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
                    	   // System.out.println("befeore - "+tPendingTransfers[i].getsContent());
                    	   tPendingTransfers[i].setsContent(tPendingTransfers[i].getsContent().replaceAll("\t", " ").trim());
                    	   // System.out.println("after - "+tPendingTransfers[i].getsContent());
                       }
	               }
               }
	       } 
	       catch(JSONException e){
	    	   Log.e(TAG, "@executeLogin() : error - "+e.toString());
	    	   if(MNEPApplication.bDebug) e.printStackTrace();
               return false;
	       }
	       catch(org.apache.http.conn.HttpHostConnectException e) {
	    	   Log.e(TAG, "@executeLogin() : error - "+e.getMessage());
	    	   if(MNEPApplication.bDebug) e.printStackTrace();
	    	   tPendingTransfers = new Transfer[1];
         	   tPendingTransfers[0] = new Transfer();
         	   tPendingTransfers[0].setsLocation("Cannot connect to server. Try again later.");
         	   return false;
	       }
	       catch(Exception e){
	    	   Log.e(TAG, "@executeLogin() : error - "+e.getMessage());
	    	   if(MNEPApplication.bDebug) e.printStackTrace(); 
	    	   tPendingTransfers = new Transfer[1];
         	   tPendingTransfers[0] = new Transfer();
         	   tPendingTransfers[0].setsLocation(""+e.getClass());
	           return false;
	       }	       
	       if(MNEPApplication.bDebug) Log.i(TAG, "@executeLogin() : end");
	       return true;
	} 	
	
}