package com.mitate.service;

import java.sql.Timestamp;
import java.util.Date;
import com.mitate.MITATEApplication;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MITATEActivity extends Activity implements OnClickListener {

	String TAG = "MITATEActivity";
	public static boolean bStopTransactionExecution = false;
	long iPollingInterval = 180000;
	
	TextView tvUsername = null;
	TextView tvPassword = null;
	TextView tvHeader = null;
	TextView tvSeparator = null;
	TextView tvStatus = null;
	
	EditText etUsername = null;
	EditText etPassword = null;
	
	Button btStartService = null;
	Button btStopService = null;
	
	public static final String sPreferenceName = "MNEP_Preferences";	
    public static SharedPreferences spMNEPPreference = null;
    public static SharedPreferences.Editor editor = null;
    
    Intent intent;
    AlarmManager amCheckPendingTests;
    PendingIntent piCheckPendingTests;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_login);

		if(MITATEApplication.bDebug) Log.i(TAG, "@onCreate() - start");
		
		spMNEPPreference = getSharedPreferences(sPreferenceName, 0);		
	    editor = spMNEPPreference.edit();
		
		tvUsername = (TextView)findViewById(R.id.am_tv_username);
		tvPassword = (TextView)findViewById(R.id.am_tv_password);
		tvHeader = (TextView) findViewById(R.id.am_tv_header1);
		tvStatus = (TextView)findViewById(R.id.al_tv_status);
		
		etUsername = (EditText)findViewById(R.id.am_et_username);
		etPassword = (EditText)findViewById(R.id.am_et_password);
		
		tvSeparator =  (TextView)findViewById(R.id.am_tv_separator1);	
		
		btStartService = (Button)findViewById(R.id.am_bt_startservice);
		btStartService.setOnClickListener(this);
		
		btStopService = (Button)findViewById(R.id.am_bt_stopservice);
		btStopService.setOnClickListener(this);
		btStopService.setEnabled(false);
		
	    intent = new Intent(getApplicationContext(), MITATEReceiver.class);
	    amCheckPendingTests = (AlarmManager) getSystemService(Context.ALARM_SERVICE);		
		
	    if(MITATEApplication.bDebug) Log.i(TAG, "@onCreate() : end");
	    
	}

	@Override
	public void onResume() {
		super.onResume();
		setComponentsWidth();
		initializeComponents();
		tvStatus.setText("");
		if(spMNEPPreference.getString("startbutton", "").equals("disabled")) {
			btStartService.setEnabled(false);
			btStopService.setEnabled(true);
		} else {
			btStartService.setEnabled(true);
			btStopService.setEnabled(false);			
		}
		

	}
	
	public void setComponentsWidth() {

		tvSeparator.setMaxHeight(((int)(0.15*getWindowManager().getDefaultDisplay().getHeight())));
		tvSeparator.setMinimumHeight(((int)(0.15*getWindowManager().getDefaultDisplay().getHeight())));
		
		tvHeader.setMaxWidth((int)(getWindowManager().getDefaultDisplay().getWidth()));
		tvHeader.setMinimumWidth((int)(getWindowManager().getDefaultDisplay().getWidth()));
		
        tvUsername.setMaxWidth((int)(0.40*getWindowManager().getDefaultDisplay().getWidth()));
        tvUsername.setMinimumWidth((int)(0.40*getWindowManager().getDefaultDisplay().getWidth()));

        tvPassword.setMaxWidth((int)(0.40*getWindowManager().getDefaultDisplay().getWidth()));
        tvPassword.setMinimumWidth((int)(0.40*getWindowManager().getDefaultDisplay().getWidth()));

        btStartService.setMaxWidth((int)(0.40*getWindowManager().getDefaultDisplay().getWidth()));
        btStartService.setMinimumWidth((int)(0.40*getWindowManager().getDefaultDisplay().getWidth()));

        btStopService.setMaxWidth((int)(0.40*getWindowManager().getDefaultDisplay().getWidth()));
        btStopService.setMinimumWidth((int)(0.40*getWindowManager().getDefaultDisplay().getWidth()));
        
        etUsername.setMaxWidth((int)(0.60*getWindowManager().getDefaultDisplay().getWidth()));
        etUsername.setMinimumWidth((int)(0.60*getWindowManager().getDefaultDisplay().getWidth()));

        etPassword.setMaxWidth((int)(0.60*getWindowManager().getDefaultDisplay().getWidth()));
        etPassword.setMinimumWidth((int)(0.60*getWindowManager().getDefaultDisplay().getWidth()));
       
	}
	
	public void initializeComponents() {
		etUsername.setText(spMNEPPreference.getString("username", ""));
        etPassword.setText(spMNEPPreference.getString("password", ""));

        Intent checkIntent = new Intent(getApplicationContext(),MITATEReceiver.class);
        checkIntent.setAction("MNEPPending_Intent_Received");
        checkIntent.putExtra("username", spMNEPPreference.getString("username", ""));
        checkIntent.putExtra("password", spMNEPPreference.getString("password", ""));
        boolean alarmUp = (PendingIntent.getBroadcast(getApplicationContext(), 12345, checkIntent, PendingIntent.FLAG_NO_CREATE) != null);
        
        if(alarmUp) {
        	btStopService.setEnabled(true);
        	btStartService.setEnabled(false);
        } else {
        	btStartService.setEnabled(true);
        	btStopService.setEnabled(false);
        }
	}
	
	@Override
	public void onClick(View v) {
	    etUsername.setError(null);
	    etPassword.setError(null);
	    
		switch(v.getId()) {
		case R.id.am_bt_startservice:
			
			bStopTransactionExecution = false;
			
			tvStatus.setText("");
			
			if(etUsername.getText().toString().trim().length()==0) {
				etUsername.setError("Username cannot be empty");
				return;
			}
			else if(etPassword.getText().toString().trim().length()==0) {
				etPassword.setError("Password cannot be empty");
				return;
			}
			
			editor.putString("username", etUsername.getText().toString().trim());
			editor.putString("password", etPassword.getText().toString().trim());

			
	    	editor.putString("startbutton", "disabled");
	    	editor.putString("stopbutton", "enabled");
			editor.commit();
		    btStartService.setEnabled(false);
		    btStopService.setEnabled(true);
			
			new ExecuteLogin().execute();
			break;
		case R.id.am_bt_stopservice:
			
			bStopTransactionExecution = true;
			
	    	editor.putString("startbutton", "enabled");	
	    	editor.putString("stopbutton", "disabled");
	    	editor.commit();
	    	
			intent = new Intent(getApplicationContext(), MITATEReceiver.class);
			intent.setAction("MNEPPending_Intent_Received");
			intent.putExtra("username", etUsername.getText().toString().trim());
			intent.putExtra("password", etPassword.getText().toString().trim());
			
			piCheckPendingTests = PendingIntent.getBroadcast(getApplicationContext(), 12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
			amCheckPendingTests.cancel(piCheckPendingTests);
			piCheckPendingTests.cancel();
	    	
		    btStartService.setEnabled(true);
		    btStopService.setEnabled(false);
			break;
			
		}
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_mnep, menu);
		return true;
	}
	
	
	public class ExecuteLogin extends AsyncTask<Void, Integer, Void>
	{
		boolean bLoginStatus;
		protected void onPreExecute() {
			super.onPreExecute();
		}
		
    	@Override
	    protected Void doInBackground(Void... params) {
    		bLoginStatus = new LoginService().executeLogin(getApplicationContext());
    		return null;
    	}
		
		@Override
		protected void onPostExecute(Void result) {
			
			if(!bLoginStatus) {
				tvStatus.setText(LoginService.tPendingTransfers[0].getsContent());
				return;
			}
			
			editor.putString("username", etUsername.getText().toString().trim());
			editor.putString("password", etPassword.getText().toString().trim());

	    	editor.commit();
	    	
			Intent intent = new Intent(getApplicationContext(), MITATEReceiver.class);
			intent.setAction("MNEPPending_Intent_Received");
			intent.putExtra("username", etUsername.getText().toString().trim());
			intent.putExtra("password", etPassword.getText().toString().trim());
					
			
			// ajay@thinkpadmsu:~/software/technical/android/android-sdk-linux/platform-tools$ ./adb shell dumpsys alarm | grep -A 2 com.mitate.service
			piCheckPendingTests = PendingIntent.getBroadcast(getApplicationContext(), 12345, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	        amCheckPendingTests.cancel(piCheckPendingTests);
	        amCheckPendingTests.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+iPollingInterval, iPollingInterval, piCheckPendingTests);

	        editor.putLong("pollinginterval", iPollingInterval);
	        editor.commit();
	        
	        // amCheckPendingTests.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis()+10000, 9000000, piCheckPendingTests);
	        if(MITATEApplication.bDebug) Log.i(TAG, "@onCreate() : pending intentt created - "+new Timestamp(System.currentTimeMillis()+10000).getMinutes()+", "+new Date(System.currentTimeMillis()+60000+30000).getMinutes());
	       
		    
		}
		
	    @Override
		protected void onProgressUpdate(Integer... values) {

		}
	}
}