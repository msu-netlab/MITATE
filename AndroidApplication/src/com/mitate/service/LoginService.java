package com.mitate.service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONArray;
import org.json.JSONObject;
import com.mitate.MITATEApplication;
import com.mitate.measurement.Measurement;
import com.mitate.utilities.MITATELocation;
import com.mitate.utilities.MITATEUtilities;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

public class LoginService extends Service {

	String TAG = "LoginService";
	
	public static long lClientTimeOffset;
	
	public static String sWebServerName = "mitate.cs.montana.edu";
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
		if(MITATEApplication.bDebug) Log.i(TAG, "@onBind - start1");		
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
			}
		});
		tLogin.start();			
	}

	public String getDeviceId(String sUsername, String sPassword, String sPhoneNumber, String sDeviceName) throws Exception {

		// String s = Base64.encodeToString(sPassword.getBytes(), Base64.DEFAULT);
	
	   	String sURL = "http://"+sWebServerName+"/setup.php?username=" + sUsername + "&password=" + (Base64.encodeToString(sPassword.getBytes(), Base64.DEFAULT).replaceAll("[\\n]","")) + "&phone_number=" + sPhoneNumber + "&device_name=" + sDeviceName;
	   	
	   	HttpClient hcHttpClient = new DefaultHttpClient();	
	   	HttpConnectionParams.setConnectionTimeout(hcHttpClient.getParams(), iConnectionTimeout);
	   	HttpResponse hrHttpResponse = hcHttpClient.execute(new HttpPost(sURL));        
		HttpEntity entity = hrHttpResponse.getEntity();
	    
		BufferedReader brReader = new BufferedReader(new InputStreamReader(entity.getContent(),"iso-8859-1"),8);
	    String sLine = brReader.readLine();
	    
	    MITATEActivity.iPollingInterval = Integer.parseInt(sLine.split(":")[1]);
	    
        return sLine.split(":")[0];  
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
            	   tPendingTransfers[0].setsContent("Invalid Login");
            	   return false;	    		   
	    	   }
		   
	    	   MITATELocation mLocation = new MITATELocation();
	    	   
	    	   String sCoordinates = mLocation.getCoordinates(MITATEApplication.getCustomAppContext());    	   
	    	   lClientTimeOffset = MITATEUtilities.calculateTimeDifferenceBetweenNTPAndLocal();
	    	   
    	   	   String sURL = "http://"+sWebServerName+"/mobilelogin.php?" +
       	   	   		"username="+sUserName+ //
   	    	   	    "&password="+Base64.encodeToString(sPassword.getBytes(), Base64.DEFAULT).replaceAll("[\\n]","")+ //
   	    	   	    "&time="+(new Timestamp(System.currentTimeMillis())).toString().substring(10, 19).replaceAll(":","").trim()+ 
   	    	   	    "&networktype="+MITATEUtilities.getNetworkType(cContext)+// +"&city="+mLocation.getCity(cContext);
   	    	   	    // "&networktype=wifi"+ //+MITATEUtilities.getNetworkType(cContext)+
   	    	   	    "&deviceid="+sDeviceId+"&latitude="+sCoordinates.split(":")[0]+"&longitude="+sCoordinates.split(":")[1]+
   	    	   	    "&batterypower="+MITATEApplication.getBatteryPower()+"&signalstrength="+MITATEApplication.getSignalStrength()+
   	    	   	    "&networkcarrier="+MITATEApplication.getNetworkCarrierName()+"&devicemodelname="+MITATEApplication.getDeviceModel(); 
    	   	   
    	   	   
    	   	   /* HttpClient hcHttpClient = new DefaultHttpClient();	
    	   	   HttpPost hpHttpPost = new HttpPost("http://"+sWebServerName+"/mobilelogin.php");  
    	   	   
    	   	   ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    	   	   params.add(new BasicNameValuePair("username", sUserName));
    	   	   params.add(new BasicNameValuePair("password", Base64.encodeToString(sPassword.getBytes(), Base64.DEFAULT).replaceAll("[\\n]","")));
    	   	   params.add(new BasicNameValuePair("time", (new Timestamp(System.currentTimeMillis())).toString().substring(10, 19).replaceAll(":","").trim()));
    	   	   params.add(new BasicNameValuePair("networktype", MITATEUtilities.getNetworkType(cContext)));
    	   	   params.add(new BasicNameValuePair("deviceid", sDeviceId));
    	   	   params.add(new BasicNameValuePair("latitude", sCoordinates.split(":")[0]));
    	   	   params.add(new BasicNameValuePair("longitude", sCoordinates.split(":")[1]));
    	   	   params.add(new BasicNameValuePair("batterypower", MITATEApplication.getBatteryPower()+""));
    	   	   params.add(new BasicNameValuePair("signalstrength", MITATEApplication.getSignalStrength()+""));
    	   	   params.add(new BasicNameValuePair("networkcarrier", MITATEApplication.getNetworkCarrierName()));
    	   	   params.add(new BasicNameValuePair("devicemodelname", MITATEApplication.getDeviceModel()));
    	   	   hpHttpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));     	   
    	   	   */
    	   	   
    	   	   HttpClient hcHttpClient = new DefaultHttpClient();	
   		   	   HttpConnectionParams.setConnectionTimeout(hcHttpClient.getParams(), iConnectionTimeout);
               HttpPost hpHttpPost = new HttpPost(sURL);     

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
               if(MITATEApplication.bDebug) Log.i(TAG,"@executeLogin() : got transactions "+sResult);

               JSONArray jaPendingTransfers = new JSONArray(sResult);
               tPendingTransfers = new Transfer[jaPendingTransfers.length()];
               
               // set invalid login to content attributes
               if((jaPendingTransfers.getJSONObject(0)).getString("content").equals("InvalidLogin")) {
            	   if(MITATEApplication.bDebug) Log.v(TAG, "@executeLogin() : invalid login credentials");
            	   tPendingTransfers[0] = new Transfer();
            	   tPendingTransfers[0].setsContent("Invalid Login");
            	   return false;
               }	               
               else if((jaPendingTransfers.getJSONObject(0)).getString("content").equals("NoPendingTransactions")) {
            	   if(MITATEApplication.bDebug) Log.v(TAG, "@executeLogin() : no pending transactions");
            	   tPendingTransfers[0] = new Transfer();
            	   tPendingTransfers[0].setsContent("No Pending Transactions");
            	   return true;
               }
               else {
            	   if(MITATEApplication.bDebug) Log.v(TAG, "@executeLogin() : number of pending transactions - "+jaPendingTransfers.length());
	               
            	   for(int i=0;i<jaPendingTransfers.length();i++) {
                       JSONObject json_data = jaPendingTransfers.getJSONObject(i);
                       tPendingTransfers[i] = new Transfer();
                       tPendingTransfers[i].setiBytes(json_data.getInt("bytes"));
                       tPendingTransfers[i].setsDestinationIP(json_data.getString("destinationip"));
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
                       tPendingTransfers[i].setiTransferDelay(json_data.getInt("transferdelay"));
                       tPendingTransfers[i].setsUsername(LoginService.sUserName);
                       tPendingTransfers[i].setiDirection(tPendingTransfers[i].getsSourceIP().equals("client") ? 0 : 1);
                       tPendingTransfers[i].setsDeviceName(MITATEApplication.getDeviceModel());
                       tPendingTransfers[i].setsNetworkCarrier(MITATEApplication.getNetworkCarrierName());
                       tPendingTransfers[i].setsDeviceId(LoginService.sDeviceId);
                       tPendingTransfers[i].setlClientOffsetFromNTP(lClientTimeOffset);
                       tPendingTransfers[i].setiUDPHexBytes(tPendingTransfers[i].getsContent().getBytes().length + 26);
                       
                       if(tPendingTransfers[i].getsContent().trim().length() == 0) {
                    	   tPendingTransfers[i].setsContent("null");
                       } else {
                    	   tPendingTransfers[i].setsContent(tPendingTransfers[i].getsContent().replaceAll("\t", " "));
                       }
	               } 
               }
	       } 
	       catch(Exception e){
	    	   Log.e(TAG, "@executeLogin() : error1 - "+e.getMessage());
	    	   if(MITATEApplication.bDebug) e.printStackTrace(); 
	    	   tPendingTransfers = new Transfer[1];
         	   tPendingTransfers[0] = new Transfer();
         	   // tPendingTransfers[0].setsContent(e.getClass().toString().substring(e.getClass().toString().lastIndexOf(".") + 1).replace("Exception", "").replaceAll("(.)([A-Z])", "$1 $2"));
         	  tPendingTransfers[0].setsContent("Cannot connect to server");
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