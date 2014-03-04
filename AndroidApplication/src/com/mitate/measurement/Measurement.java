package com.mitate.measurement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.apache.http.MethodNotSupportedException;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import com.mitate.MITATEApplication;
import com.mitate.service.LoginService;
import com.mitate.service.MITATEActivity;
import com.mitate.service.Transfer;
import com.mitate.utilities.MITATELocation;
import com.mitate.utilities.MITATEUtilities;
import com.mitate.utilities.MeasurementLogs;

public class Measurement extends Thread implements SensorEventListener {

	String TAG = "Measurement";

	public static String sServerIP = "";
	public static String sServerTCPPort = "32165";
	
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
	
 	static long lServerOffsetFromNTP;
	
 	long lStartTime;
 	
	Socket sConnectionSocket;
	BufferedWriter bwWriteToClient;
	BufferedReader brReadFromServer;

	UDPTest utUDPTest;
	TCPTest ttTCPTest;
	CDNTest ctCDNTest; 

	ClientTimes[] ctTimes;
	MeasurementLogs[] mlLogs;
		
	public Measurement() {
		smManager = (SensorManager)MITATEApplication.getCustomAppContext().getSystemService(Context.SENSOR_SERVICE);
		sSensor = smManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		smManager.registerListener(this, sSensor, SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	// send transfer descriptions to measurement servers
	private boolean sendAllTransferDetails(Transfer[] tForServer) {
		
		boolean bTransferDetailsSent = false;
		
		sServerIP = tForServer[0].getsDestinationIP().equalsIgnoreCase("client") ? tForServer[0].getsSourceIP() : tForServer[0].getsDestinationIP();
		try {
			sConnectionSocket = new Socket(sServerIP, Integer.parseInt(sServerTCPPort));
			ObjectOutputStream oosWriteToServer = new ObjectOutputStream(sConnectionSocket.getOutputStream());
			oosWriteToServer.writeObject(tForServer);
			oosWriteToServer.flush();
			
			ObjectInputStream oisReadFromServer = new ObjectInputStream(sConnectionSocket.getInputStream());
			lServerOffsetFromNTP = Long.parseLong(oisReadFromServer.readObject().toString());
			
			bTransferDetailsSent =  true;
		} catch(Exception e) {
			// e.printStackTrace();
			Log.e(TAG, "Error sending transfer list to server - "+sServerIP+":"+sServerTCPPort+"-"+e.getMessage());
			bTransferDetailsSent = false;
		} 
		
		return bTransferDetailsSent;
	}
	
	// initialize parameters 
	private boolean sendAndReceiveParameters(
			String serverip, int packetype, String sUserName, int bytes, int transferid, int transactionid, 
			int direction, int packetdelay, int noofpackets, int explicit, String content, String portnumber, 
			String contenttype, int transferdelay) {
		
		boolean done = false;
		sServerIP = serverip;
		
		try {
			
			iPacketDelay = packetdelay;
			iExplicit = explicit;
			sContent = content;
			sContentType = contenttype;				
			
			if(iExplicit == 0) {
	            if(packetype == 0 ) {
	            iUDPBytes = bytes/noofpackets;
	            iTCPBytes = bytes/noofpackets;
	            }
	            else if(packetype == 1 ) {
	                iUDPBytes = bytes/noofpackets;
	                iTCPBytes = 0;
	            }
	            else if(packetype == 2 ) {
	              iUDPBytes = 0;
	                iTCPBytes = bytes/noofpackets;
	            }
            }
			else if(iExplicit == 1) {	    
				if(packetype == 0 ) {
				iUDPBytes = bytes;
				iTCPBytes = bytes;
				}
				else if(packetype == 1 ) {
				iUDPBytes = bytes;
				iTCPBytes = 0;
				}
				else if(packetype == 2 ) {
				iUDPBytes = 0;
				iTCPBytes = bytes;
				}
			}								

			iUDPPackets = noofpackets;
			iUDPPort = Integer.parseInt(portnumber);
			iTCPPackets = noofpackets;
			iTCPPort = Integer.parseInt(portnumber);				

			done = true;
			
		} catch(Exception e) {
			done = false;
			Log.e(TAG, "error - "+e.getMessage());
			e.printStackTrace();
		}
		return done;
	}

	// store metrics after each transfer into client times object	
	int saveMetrics(int index, int iTransferId) {
		int iMetricsSaved = 0;
		try {

			if(ttTCPTest == null || utUDPTest == null) {
				if(ttTCPTest == null)
					ttTCPTest = new TCPTest("",0,0,0,0,0,0,0,"","");
				if(utUDPTest == null)
					utUDPTest = new UDPTest("",0,0,0,0,0,0,0,"",""); 
			}
			
			ctTimes[index] = new ClientTimes();
			
			ctTimes[index].laTCPPacketReceivedTimes = ttTCPTest.laTCPPacketReceivedTimes;
			ctTimes[index].iaTCPBytes = ttTCPTest.iaTCPBytes;
			ctTimes[index].iTCPBytesReadFromServer = ttTCPTest.iTCPBytesReadFromServer;
			ctTimes[index].iTCPBytesSentToServer = ttTCPTest.iTCPBytesSentToServer;
			ctTimes[index].lUDPPacketReceivedTimes = utUDPTest.lUDPPacketReceivedTimes;
			ctTimes[index].iaUDPBytes = utUDPTest.iaUDPBytes;
			ctTimes[index].iUDPBytesReceivedFromServer = utUDPTest.iUDPBytesReceivedFromServer;				
			ctTimes[index].sClientTime = (new Timestamp(System.currentTimeMillis())).toString();   				
			ctTimes[index].sBeforeExecCoordinates = sBeforeExecCoordinates;
			ctTimes[index].sAfterExecCoordinates = sAfterExecCoordinates; 
			ctTimes[index].sSignalStrength = MITATEApplication.getSignalStrength()+"";
			ctTimes[index].sAccelerometerReading = sAccelerometerReading;
			ctTimes[index].isCallActive = MITATEApplication.isCallActive();
			ctTimes[index].iTransferId = iTransferId;
			ctTimes[index].sUDPLog = utUDPTest.sLog;
			ctTimes[index].sTCPLog = ttTCPTest.sLog;
			ctTimes[index].iUDPBytesSentToServer = utUDPTest.iUDPBytesSentToServer;
			
			if(MITATEApplication.bDebug) Log.d(TAG, "@saveMetrics : saved metrics"); 
		
			iMetricsSaved = 1;
		} catch (Exception e) {
			Log.e(TAG,"@saveMetrics : error - "+e.getMessage());
			e.printStackTrace();
			iMetricsSaved = 0;
		}
		return iMetricsSaved;
	}
		
	public void run() {
			
		lStartTime = System.currentTimeMillis();
 		TelephonyManager tmTelephoneManager = (TelephonyManager)(MITATEApplication.getCustomAppContext()).getSystemService(Context.TELEPHONY_SERVICE);
 		sPhoneNumber = tmTelephoneManager.getLine1Number();   
  
 		for(int j=0; j<LoginService.tPendingTransfers.length && !MITATEActivity.bStopTransactionExecution; j++) {
			if(MITATEApplication.bDebug)  Log.d(TAG, "@run : request parameters");
		
			if(LoginService.tPendingTransfers[j].getiResponse() == 1) { 
				ctCDNTest = new CDNTest(
						LoginService.tPendingTransfers[j].getiTransferid(), LoginService.tPendingTransfers[j].getiTransactionid(), 
						LoginService.tPendingTransfers[j].getsDestinationIP(), LoginService.tPendingTransfers[j].getsPortNumber(), 
						LoginService.tPendingTransfers[j].getsContent(), LoginService.tPendingTransfers[j].getiNoOfPackets(),
						LoginService.tPendingTransfers[j].getiPacketType(), LoginService.tPendingTransfers[j].getsContentType());
				ctCDNTest.runCDNTest(); 
			} else { 
			
				Set<String> s = new HashSet<String>(); 	   
				int i=j;

				for(;j<LoginService.tPendingTransfers.length && !MITATEActivity.bStopTransactionExecution;j++) {
					
					s.add(LoginService.tPendingTransfers[j].getsSourceIP());
					s.add(LoginService.tPendingTransfers[j].getsDestinationIP());
					if(s.size() > 2) {
						j--;
						break;
					}								
			
					if(LoginService.tPendingTransfers[j].getsContentType().equalsIgnoreCase("hex")) { 
						String sTempContent = "";
						for(int k=0; k<LoginService.tPendingTransfers[j].getsContent().length(); k+=2) {
							sTempContent += (char)(Integer.parseInt(LoginService.tPendingTransfers[j].getsContent().substring(k,k+2), 16));
						}
						LoginService.tPendingTransfers[j].setsContent(sTempContent); 
						LoginService.tPendingTransfers[j].setiBytes(sTempContent.getBytes().length + 26);
					}	
				}
			
				s.removeAll(s);
				
				Transfer[] temp = Arrays.copyOfRange(LoginService.tPendingTransfers, i, j+1 > LoginService.tPendingTransfers.length ? LoginService.tPendingTransfers.length : j+1);	
				boolean bsent = sendAllTransferDetails(temp);
				ctTimes = new ClientTimes[temp.length];
				
				for(int k=0; k<temp.length && !MITATEActivity.bStopTransactionExecution; k++) {
				
					MITATELocation mLocation = new MITATELocation();
					sBeforeExecCoordinates = mLocation.getCoordinates(MITATEApplication.getCustomAppContext());
				 
					boolean bGotVars = sendAndReceiveParameters (
						temp[k].getsSourceIP().trim().toLowerCase().equalsIgnoreCase("client") ? temp[k].getsDestinationIP().trim().toLowerCase() : temp[k].getsSourceIP().trim().toLowerCase(),
						temp[k].getiPacketType(), 
						LoginService.sUserName, 
						temp[k].getiBytes(), 
						temp[k].getiTransferid(), 
						temp[k].getiTransactionid(), 
						(iDirection = temp[k].getsSourceIP().equals("client") ? 0 : 1),
						temp[k].getiPacketDelay(),
						temp[k].getiNoOfPackets(),
						temp[k].getiExplicit(),
						temp[k].getsContent(),
						temp[k].getsPortNumber(),						
						temp[k].getsContentType(),
						temp[k].getiTransferDelay() 
					);
		
				if(bGotVars) { // && (System.currentTimeMillis() - lStartTime + 15000) < LoginService.lPollInterval) {
					
					ttTCPTest =  null;
					utUDPTest = null;
					try {
						Thread.sleep(temp[k].getiTransferDelay()); 
					} catch(Exception e) {
						
					}
						if(iUDPBytes > 0 && (temp[k].getiPacketType() == 1 || temp[k].getiPacketType() == 0)) {
							utUDPTest = new UDPTest(sServerIP, iUDPPort, iUDPBytes, iUDPPackets, iDirection, lServerOffsetFromNTP, iPacketDelay, iExplicit, sContent, sContentType);
							utUDPTest.runUDPTest();  // if test fails do something
						}
						 
						if(iTCPBytes >  0 && (temp[k].getiPacketType() == 2 || temp[k].getiPacketType() == 0)) {
							ttTCPTest = new TCPTest(sServerIP, iTCPPort, iTCPBytes, iTCPPackets, iDirection, lServerOffsetFromNTP, iPacketDelay, iExplicit, sContent, sContentType);
							ttTCPTest.runTCPTest();  // if test fails do something1 
						}
						
						sAfterExecCoordinates = mLocation.getCoordinates(MITATEApplication.getCustomAppContext());
						
						saveMetrics(k, temp[k].getiTransferid());
				
				} 
				/* else if((System.currentTimeMillis() - lStartTime + 15000) > LoginService.lPollInterval) {
					System.out.println("Stopping thread --- time for another thread : "+((System.currentTimeMillis() - lStartTime + 15000) < LoginService.lPollInterval));
					break;
				} */

			}
	        int iTCPConnectionRetryCount = 0;
			while(++iTCPConnectionRetryCount < 10) {
				try {
					Thread.sleep(10000);
					iTCPPort = 32166;
					sConnectionSocket = new Socket(sServerIP, iTCPPort);						
					sConnectionSocket.setSoTimeout(10000);
					
					if(sConnectionSocket != null) {
						Log.d(TAG, "@clienttimesending : connected to server - "+sConnectionSocket.getRemoteSocketAddress()+", local - "+sConnectionSocket.getLocalSocketAddress());							
						break;
					}						
				} catch(Exception e) {						
					Log.e(TAG, "@clienttimesending : retry - "+iTCPConnectionRetryCount+", error - "+e.getMessage());
					if(iTCPConnectionRetryCount == 5) {
						Log.e(TAG, "@clienttimesending : connection failed");   
						return;
					}	
				}
			} 
			
			
			try {		
				ObjectOutputStream oosClientTimesWriteToServer = new ObjectOutputStream(sConnectionSocket.getOutputStream());
				oosClientTimesWriteToServer.writeObject(ctTimes);
				ObjectInputStream oisReadFromServer = new ObjectInputStream(sConnectionSocket.getInputStream());
				if(Integer.parseInt(oisReadFromServer.readObject().toString()) == 1) {
					Log.v(TAG, "success to send client times");
				}
				else { 
					Log.v(TAG, "failed to send client times");
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}				
			
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
		}
}