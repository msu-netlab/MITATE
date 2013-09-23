package com.mitate.measurement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Arrays;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.BatteryManager;
import android.os.Build;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import com.mitate.MITATEApplication;
import com.mitate.service.LoginService;
import com.mitate.service.MITATEActivity;
import com.mitate.utilities.MITATELocation;
import com.mitate.utilities.MITATEUtilities;

@TargetApi(17)
public class Measurement extends Thread implements SensorEventListener {
	
	static int iUDPBytes, iUDPPackets, iUDPPort;
 	static int iTCPBytes, iTCPPackets, iTCPPort;
 	static int iPacketDelay, iExplicit;
 	static int iDirection = 0;
 	static String sContent;
 	static String sContentType;
 	static String sPhoneNumber;
 	String sAfterExecCoordinates;
 	String sBeforeExecCoordinates;
 	
 	String sAccelerometerReading;
 	String sSignalStrength;
 	
	Sensor sSensor;
	SensorManager smManager;
 	
 	long lStartTime;

 	static long lServerOffsetFromNTP;
 	static long lClientOffsetFromNTP;
 	
	Socket sConnectionSocket;
	BufferedWriter bwWriteToClient;
	BufferedReader brReadFromServer;

	UDPTest utUDPTest;
	TCPTest ttTCPTest;
	CDNTest ctCDNTest;
 	
	public static String sServerIP = "";
	public static String sServerTCPPort = "32165";
	
	String TAG = "Measurement";
		
	public Measurement() {
		smManager = (SensorManager)MITATEApplication.getCustomAppContext().getSystemService(Context.SENSOR_SERVICE);
		sSensor = smManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		smManager.registerListener(this, sSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
		private boolean sendAndReceiveParameters(String serverip, int packetype, String sUserName, int bytes, int transferid, int transactionid, 
				int direction, int packetdelay, int noofpackets, int explicit, String content, String portnumber, String contenttype) {
			
			boolean done = false;
			sServerIP = serverip;
			
			try{
			    String sCarrierName = (MITATEApplication.getTelephonyManager()).getNetworkOperatorName();
			    
			    IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
			    Intent batteryStatus = MITATEApplication.getCustomAppContext().registerReceiver(null, ifilter);
			    int status = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
			    System.out.println("battery power - "+status);
				
				if(contenttype.equalsIgnoreCase("hex")) {
					/* Log.v(TAG, content+"<<<--------------->"+content.length());
					content = new String(Base64.decode(content, Base64.DEFAULT));
					Log.v(TAG, content+"<<<<<<<--------------->"+content.length()); */
					
					String sTempContent = "";
					// Log.v(TAG, "-------++++-------->"+content);
					for(int k=0; k<content.length(); k+=2) {
						sTempContent += (char)(Integer.parseInt(content.substring(k,k+2), 16));
					}
					
					content = sTempContent;

					Log.v(TAG, "hex---------------->"+content+">>"+bytes+">>"+content.length()+">>"+content.getBytes().length);
				}
			    
				
		        int iTCPConnectionRetryCount = 0;
		        
				while(++iTCPConnectionRetryCount < 6) {
					try {
						Thread.sleep(1000);
						sConnectionSocket = new Socket(sServerIP, Integer.parseInt(sServerTCPPort));
						sConnectionSocket.setSoTimeout(8000);
						if(sConnectionSocket != null) {
							Log.d(TAG, "@sendAndReceiveParameters1 : connected to server - "+sConnectionSocket.getRemoteSocketAddress());
							break;
						}
					} catch(Exception e) {						
						Log.e(TAG, "@sendAndReceiveParameters : retry - "+iTCPConnectionRetryCount+", error - "+e.getMessage());
						if(iTCPConnectionRetryCount >= 5) {
							Log.e(TAG, "@sendAndReceiveParameters : connection failed1");
							return false;
						}	
					}
				}
				
				lClientOffsetFromNTP = MITATEUtilities.calculateTimeDifferenceBetweenNTPAndLocal();
				bwWriteToClient = new BufferedWriter(new OutputStreamWriter(sConnectionSocket.getOutputStream()));
				
				// String newcontent = content.replaceAll("\\r\\n","newlinecharacter");	
				// newcontent = content.replaceAll("\\n","newlinecharacter");
				
				/* bwWriteToClient.write(sUserName+":;:"+packetype+":;:"+bytes+":;:"+transferid+":;:"+transactionid+":;:"+
				direction+":;:"+lClientOffsetFromNTP+":;:"+p						bwWriteToServer.write()acketdelay+":;:"+sCarrierName+":;:"+noofpackets+":;:"+explicit+
				":;:"+content+":;:"+portnumber+":;:"+contenttype+"\n"); //+":"+MITATEUtilities.lTimeDifference+"\n"); */ 
				
				
				bwWriteToClient.write(sUserName+":;:"+packetype+":;:"+bytes+":;:"+transferid+":;:"+transactionid+":;:"+
						direction+":;:"+lClientOffsetFromNTP+":;:"+packetdelay+":;:"+sCarrierName+":;:"+noofpackets+":;:"+explicit+
						":;:"+portnumber+":;:"+contenttype+":;:"+LoginService.sDeviceId+":;:"+Build.MODEL+":;:"+(content.getBytes().length+26)+":;:"+content+":;:"); 
				
				bwWriteToClient.flush();
				
				iPacketDelay = packetdelay;
				iExplicit = explicit;
				sContent = content;
				sContentType = contenttype;				
				
				if(MITATEApplication.bDebug) Log.d(TAG, "@sendAndReceiveParameters : parameters sent - " + sUserName+":"+packetype+":"
				+bytes+":"+transferid+":"+transactionid+":"+direction+":"+lClientOffsetFromNTP+":"+packetdelay+":"+sCarrierName+":"
						+noofpackets+":"+explicit+"->:port-"+portnumber+":"+content+":"+contenttype+"\n"); //+":"+MITATEUtilities.lTimeDifference+"\n");
				
				brReadFromServer = new BufferedReader(new InputStreamReader(sConnectionSocket.getInputStream(), "UTF-8"));
				String sParameters = brReadFromServer.readLine();
				if(MITATEApplication.bDebug) Log.d(TAG, "@sendAndReceiveParameters : parameters read - " + sParameters);

				String[] saParameters = sParameters.split(":;:");
				iUDPBytes = Integer.parseInt(saParameters[0].trim());
				iUDPPackets = Integer.parseInt(saParameters[1].trim());
				iUDPPort = Integer.parseInt(saParameters[2].trim());
				iTCPBytes = Integer.parseInt(saParameters[3].trim());
				iTCPPackets = Integer.parseInt(saParameters[4].trim());
				iTCPPort = Integer.parseInt(saParameters[5].trim());				
				lServerOffsetFromNTP = Long.parseLong(saParameters[6].trim());
				
				// Log.v(TAG, "port num from server - udp : "+iUDPPort+", tcp : "+iTCPPort);

				done = true;
				sConnectionSocket.close();
				brReadFromServer.close();
				bwWriteToClient.close();

			} catch(Exception e) {
				done = false;
				Log.e(TAG, "error - "+e.getMessage());
				e.printStackTrace();
			}
			return done;
		}

		
		int sendtimes() {
			try {

		        int iTCPConnectionRetryCount = 0;
				while(++iTCPConnectionRetryCount < 6) {
					try {
						Thread.sleep(1000);
						sConnectionSocket = new Socket(sServerIP, iTCPPort);						
						sConnectionSocket.setSoTimeout(iPacketDelay + 8000);
						
						if(sConnectionSocket != null) {
							Log.d(TAG, "@sendtimes : connected to server - "+sConnectionSocket.getRemoteSocketAddress()+", local - "+sConnectionSocket.getLocalSocketAddress());							
							break;
						}						
					} catch(Exception e) {						
						Log.e(TAG, "@sendtimes : retry - "+iTCPConnectionRetryCount+", error - "+e.getMessage());
						if(iTCPConnectionRetryCount == 5) {
							Log.e(TAG, "@sendtimes : connection failed");   
							return 0;
						}	
					}
				}

				if(ttTCPTest == null || utUDPTest == null) {
					if(ttTCPTest == null)
						ttTCPTest = new TCPTest("",0,0,0,0,0,0,0,"","");
					if(utUDPTest == null)
						utUDPTest = new UDPTest("",0,0,0,0,0,0,0,"",""); 
				}
				
				
				bwWriteToClient = new BufferedWriter(new OutputStreamWriter(sConnectionSocket.getOutputStream()));
				bwWriteToClient.write(
						Arrays.toString(ttTCPTest.laTCPPacketReceivedTimes)  + "\n"+
						Arrays.toString(ttTCPTest.iaTCPBytes)  + "\n"+
						ttTCPTest.iTCPBytesReadFromServer+"\n"+
						ttTCPTest.iTCPBytesSentToServer+"\n"+
						Arrays.toString(utUDPTest.lUDPPacketReceivedTimes)+"\n"+
						Arrays.toString(utUDPTest.iaUDPBytes)+"\n"+
						utUDPTest.iUDPBytesReceivedFromServer+"\n"+
						(new Timestamp(System.currentTimeMillis())).toString()+"\n"+
						sBeforeExecCoordinates.split(":")[0]+"\n"+
						sBeforeExecCoordinates.split(":")[1]+"\n"+
						sAfterExecCoordinates.split(":")[0]+"\n"+
						sAfterExecCoordinates.split(":")[1]+"\n"+
						sSignalStrength+"\n"+
						sAccelerometerReading+"\n"+
						MITATEApplication.isCallActive()+"\n"
						);
				bwWriteToClient.flush();
				
				/* if(MITATEApplication.bDebug) {dsjysd
						System.out.println("client times sent to server - \n"+
						Arrays.toString(ttTCPTest.laTCPPacketReceivedTimes)  + "\n"+
						Arrays.toString(ttTCPTest.iaTCPBytes)  + "\n"+								
						ttTCPTest.iTCPBytesReadFromServer+"\n"+
						ttTCPTest.iTCPBytesSentToServer+"\n"+
						Arrays.toString(utUDPTest.lUDPPacketReceivedTimes)+"\n"+
						Arrays.toString(utUDPTest.iaUDPBytes)+"\n"+
						utUDPTest.iUDPBytesReceivedFromServer+"\n"+
						(new Timestamp(System.currentTimeMillis())).toString()+"\n");
				} */
				
				brReadFromServer = new BufferedReader(new InputStreamReader(sConnectionSocket.getInputStream()));
				int iTemp = Integer.parseInt(brReadFromServer.readLine());  // -- just commented to check

				if(MITATEApplication.bDebug) Log.d(TAG, "@sendtimes : client times sent"); 
				
				bwWriteToClient.close();
				brReadFromServer.close();
				sConnectionSocket.close();				
				return iTemp;
			} catch (Exception e) {
				Log.e(TAG,"@sendtimes : error - "+e.getMessage());
				e.printStackTrace();
				return 0;
			}
		}
		
		public void run() {
			
		 	lStartTime = System.currentTimeMillis();

		 	TelephonyManager tmTelephoneManager = (TelephonyManager)(MITATEApplication.getCustomAppContext()).getSystemService(Context.TELEPHONY_SERVICE);
		 	sPhoneNumber = tmTelephoneManager.getLine1Number();

			for(int j=0; j<LoginService.tPendingTransfers.length && !MITATEActivity.bStopTransactionExecution; j++) {
				if(MITATEApplication.bDebug)  Log.d(TAG, "@run : request parameters");
				
				if(LoginService.tPendingTransfers[j].getiResponse() == 1) {
					System.out.println("response = 1"); 
					ctCDNTest = new CDNTest(LoginService.tPendingTransfers[j].getiTransferid(), LoginService.tPendingTransfers[j].getiTransactionid(), LoginService.tPendingTransfers[j].getsServerIP(),
							LoginService.tPendingTransfers[j].getsPortNumber(), LoginService.tPendingTransfers[j].getsContent(), LoginService.tPendingTransfers[j].getiNoOfPackets());
					ctCDNTest.runCDNTest();
				} else { 
				
			// if(!LoginService.tPendingTransfers[j].getsSourceIP().trim().toLowerCase().equalsIgnoreCase("client") &&
				// 	LoginService.tPendingTransfers[j].getsSourceIP().trim().toLowerCase().equalsIgnoreCase("54.241.6.5")) {
				
				MITATELocation mLocation = new MITATELocation();
				sBeforeExecCoordinates = mLocation.getCoordinates(MITATEApplication.getCustomAppContext());
				
				boolean bGotVars = sendAndReceiveParameters (
						LoginService.tPendingTransfers[j].getsSourceIP().trim().toLowerCase().equalsIgnoreCase("client") ? LoginService.tPendingTransfers[j].getsServerIP().trim().toLowerCase() : LoginService.tPendingTransfers[j].getsSourceIP().trim().toLowerCase(),
						LoginService.tPendingTransfers[j].getiPacketType(), 
						LoginService.sUserName, 
						LoginService.tPendingTransfers[j].getiBytes(), 
						LoginService.tPendingTransfers[j].getiTransferid(), 
						LoginService.tPendingTransfers[j].getiTransactionid(), 
						(iDirection = LoginService.tPendingTransfers[j].getsSourceIP().equals("client") ? 0 : 1),
						LoginService.tPendingTransfers[j].getiPacketDelay(),
						LoginService.tPendingTransfers[j].getiNoOfPackets(),
						LoginService.tPendingTransfers[j].getiExplicit(),
						LoginService.tPendingTransfers[j].getsContent(),
						LoginService.tPendingTransfers[j].getsPortNumber(),						
						LoginService.tPendingTransfers[j].getsContentType()						
					);
		
 
		           TelephonyManager telephonyManager = MITATEApplication.getTelephonyManager();
			        // for example value of first element
		           
		           // API Min 17
			        CellInfoLte cellinfolte = (CellInfoLte)telephonyManager.getAllCellInfo().get(0);
			        CellSignalStrengthLte cellSignalStrengthlte = cellinfolte.getCellSignalStrength();
			        // System.out.println("------------>///`"+cellSignalStrengthlte.getDbm()+"-"+cellSignalStrengthlte.getLevel()+"-"+cellSignalStrengthlte.getAsuLevel());		    	   
			        sSignalStrength = ""+cellSignalStrengthlte.getAsuLevel();
				
				
				// System.out.println("----------------?"+LoginService.tPendingTransfers[j].getsContent());
				// System.out.println("current- "+System.currentTimeMillis()+", start- "+lStartTime+", poll- "+LoginService.lPollInterval+", calc- "+(System.currentTimeMillis() - lStartTime + 15000));
				if(bGotVars) { // && (System.currentTimeMillis() - lStartTime + 15000) < LoginService.lPollInterval) {
					
					ttTCPTest =  null;
					utUDPTest = null;
					
						if(LoginService.tPendingTransfers[j].getiPacketType() == 1 || LoginService.tPendingTransfers[j].getiPacketType() == 0) {
							utUDPTest = new UDPTest(sServerIP, iUDPPort, iUDPBytes, iUDPPackets, iDirection, lServerOffsetFromNTP, iPacketDelay, iExplicit, sContent, sContentType);
							utUDPTest.runUDPTest();  // if test fails do something
						}
						
						if(LoginService.tPendingTransfers[j].getiPacketType() == 2 || LoginService.tPendingTransfers[j].getiPacketType() == 0) {
							ttTCPTest = new TCPTest(sServerIP, iTCPPort, iTCPBytes, iTCPPackets, iDirection, lServerOffsetFromNTP, iPacketDelay, iExplicit, sContent, sContentType);
							ttTCPTest.runTCPTest();  // if test fails do something1
						}
						
						sAfterExecCoordinates = mLocation.getCoordinates(MITATEApplication.getCustomAppContext());
						
						int iTemp = sendtimes();
							if(iTemp == 0) {
								// j--; // to repeat transfer if not successful
							}
					
				} 
				/* else if((System.currentTimeMillis() - lStartTime + 15000) > LoginService.lPollInterval) {
					System.out.println("Stopping thread --- time for another thread : "+((System.currentTimeMillis() - lStartTime + 15000) < LoginService.lPollInterval));
					break;
				} */
			// }
			}
		}
	}


		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// TODO Auto-generated method stub
			
		}


		@Override
		public void onSensorChanged(SensorEvent event) {
			sAccelerometerReading = event.values[0]+":"+event.values[1]+":"+event.values[2];
			// System.out.println("---------------->"+event.values[0]+", ====="+event.values[1]+", ==="+event.values[2]);
		}
}