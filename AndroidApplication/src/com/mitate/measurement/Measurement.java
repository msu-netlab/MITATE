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

import android.annotation.TargetApi;
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

@TargetApi(17)
public class Measurement extends Thread implements SensorEventListener {
	
	// String[] saResult;
	ClientTimes[] ctTimes;
	
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
	
	
	private boolean sendAllTransferDetails(Transfer[] tForServer) {
		sServerIP = tForServer[0].getsDestinationIP();
		try {
			System.out.println(sServerIP+"-"+Integer.parseInt(sServerTCPPort));
			sConnectionSocket = new Socket(sServerIP, Integer.parseInt(sServerTCPPort));
			ObjectOutputStream oosWriteToServer = new ObjectOutputStream(sConnectionSocket.getOutputStream());
			oosWriteToServer.writeObject(tForServer);
			oosWriteToServer.flush();
			// oosWriteToServer.close();
			
			ObjectInputStream oisReadFromServer = new ObjectInputStream(sConnectionSocket.getInputStream());
			lServerOffsetFromNTP = Long.parseLong(oisReadFromServer.readObject().toString());
			// oisReadFromServer.close();		
			
			System.out.println("complted "+lServerOffsetFromNTP);
			
			for(int i=0; i<tForServer.length; i++)
				System.out.println("--------->"+tForServer[i].getsPortNumber()+"--"+tForServer[i].getsDestinationIP()+"-"+tForServer[i].getiTransferDelay()+"="+tForServer[i].getiPacketType());
			
		} catch(Exception e) {
			// System.out.println(e.getMessage()+"->error");
			e.printStackTrace();
		} 
		
		return false;
	}
	
		private boolean sendAndReceiveParameters(String serverip, int packetype, String sUserName, int bytes, int transferid, int transactionid, 
				int direction, int packetdelay, int noofpackets, int explicit, String content, String portnumber, 
				String contenttype, int transferdelay) {
			boolean done = true;
			sServerIP = serverip;
			
			try{
				lClientOffsetFromNTP = MITATEUtilities.calculateTimeDifferenceBetweenNTPAndLocal();
				
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
				// lServerOffsetFromNTP = Long.parseLong(saParameters[6].trim());

				done = true;
				
				
				System.out.println("itcpbytes - "+iTCPBytes+",itcpPackets - "+iTCPPackets+",iudpbytes - "+iUDPBytes+",iucppackets - "+iUDPPackets);

			} catch(Exception e) {
				done = false;
				Log.e(TAG, "error - "+e.getMessage());
				e.printStackTrace();
			}
			return done;
		}

		
		int sendtimes(int index, int iTransferId) {
			try {

		        /* int iTCPConnectionRetryCount = 0;
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
				} */

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
				ctTimes[index].iaTCPBytes = ttTCPTest.iaTCPBytes;
				ctTimes[index].sBeforeExecCoordinates = sBeforeExecCoordinates;
				ctTimes[index].sAfterExecCoordinates = sAfterExecCoordinates;
				ctTimes[index].sSignalStrength = MITATEApplication.getSignalStrength()+"";
				ctTimes[index].sAccelerometerReading = sAccelerometerReading;
				ctTimes[index].isCallActive = MITATEApplication.isCallActive();
				ctTimes[index].iTransferId = iTransferId;
				
				
				/*saResult[index] = new String( 				
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
					MITATEApplication.isCallActive()+"\n" ); */
				
				
				/* bwWriteToClient = new BufferedWriter(new OutputStreamWriter(sConnectionSocket.getOutputStream()));
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
				bwWriteToClient.flush(); */
				
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
				
				// brReadFromServer = new BufferedReader(new InputStreamReader(sConnectionSocket.getInputStream()));
				// int iTemp = Integer.parseInt(brReadFromServer.readLine());  // -- just commented to check

				if(MITATEApplication.bDebug) Log.d(TAG, "@sendtimes : client times sent"); 
				
				// bwWriteToClient.close();
				// brReadFromServer.close();
				// sConnectionSocket.close();				
				return 1;
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
					ctCDNTest = new CDNTest(LoginService.tPendingTransfers[j].getiTransferid(), LoginService.tPendingTransfers[j].getiTransactionid(), LoginService.tPendingTransfers[j].getsDestinationIP(),
							LoginService.tPendingTransfers[j].getsPortNumber(), LoginService.tPendingTransfers[j].getsContent(), LoginService.tPendingTransfers[j].getiNoOfPackets());
					ctCDNTest.runCDNTest();
				} else { 
				
			Set<String> s = new HashSet<String>();	   
			int i=j;
			for(;j<LoginService.tPendingTransfers.length && !MITATEActivity.bStopTransactionExecution;j++) {
				
				if(LoginService.tPendingTransfers[j].getsContentType().equalsIgnoreCase("hex")) { 
					String sTempContent = "";
					for(int k=0; k<LoginService.tPendingTransfers[j].getsContent().length(); k+=2) {
						sTempContent += (char)(Integer.parseInt(LoginService.tPendingTransfers[j].getsContent().substring(k,k+2), 16));
					}
					LoginService.tPendingTransfers[j].setsContent(sTempContent); 
					LoginService.tPendingTransfers[j].setiBytes(sTempContent.getBytes().length + 26);
				}				
				
				s.add(LoginService.tPendingTransfers[j].getsSourceIP());
				s.add(LoginService.tPendingTransfers[j].getsDestinationIP());
				if(s.size() > 2) {
					j--;
					break;
				}
				System.out.println("yep"+j);
			}
			
			Transfer[] temp = Arrays.copyOfRange(LoginService.tPendingTransfers, i, j);	
			boolean bsent = sendAllTransferDetails(temp);
			
			System.out.println("test completed ---_________>");
			
			// saResult = new String[temp.length];
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
		
				System.out.println("-------------->"+temp.length+"->"+LoginService.tPendingTransfers[k].getsPortNumber());
 
				if(bGotVars) { // && (System.currentTimeMillis() - lStartTime + 15000) < LoginService.lPollInterval) {
					
					ttTCPTest =  null;
					utUDPTest = null;
					try {
						System.out.println("----------->"+temp[k].getiTransferDelay());
						Thread.sleep(temp[k].getiTransferDelay()); 
					} catch(Exception e) {
						
					}
						if(iUDPBytes > 0 && (LoginService.tPendingTransfers[k].getiPacketType() == 1 || LoginService.tPendingTransfers[k].getiPacketType() == 0)) {
							utUDPTest = new UDPTest(sServerIP, iUDPPort, iUDPBytes, iUDPPackets, iDirection, lServerOffsetFromNTP, iPacketDelay, iExplicit, sContent, sContentType);
							utUDPTest.runUDPTest();  // if test fails do something
						}
						 
						if(iTCPBytes >  0 && (LoginService.tPendingTransfers[k].getiPacketType() == 2 || LoginService.tPendingTransfers[k].getiPacketType() == 0)) {
							ttTCPTest = new TCPTest(sServerIP, iTCPPort, iTCPBytes, iTCPPackets, iDirection, lServerOffsetFromNTP, iPacketDelay, iExplicit, sContent, sContentType);
							ttTCPTest.runTCPTest();  // if test fails do something1 
						}
						
						sAfterExecCoordinates = mLocation.getCoordinates(MITATEApplication.getCustomAppContext());
						
						System.out.println("completed -------->>>>>>>>>>>>"+k);
						int iTemp = sendtimes(k, LoginService.tPendingTransfers[k].getiTransferid());
						/*	if(iTemp == 0) {
								// j--; // to repeat transfer if not successful
							}*/
					
				} 
				/* else if((System.currentTimeMillis() - lStartTime + 15000) > LoginService.lPollInterval) {
					System.out.println("Stopping thread --- time for another thread : "+((System.currentTimeMillis() - lStartTime + 15000) < LoginService.lPollInterval));
					break;
				} */
			// }
			}
			}
				
		        int iTCPConnectionRetryCount = 0;
				while(++iTCPConnectionRetryCount < 6) {
					try {
						Thread.sleep(1000);
						iTCPPort = 32166;
						sConnectionSocket = new Socket(sServerIP, iTCPPort);						
						// sConnectionSocket.setSoTimeout(iPacketDelay + 8000);
						
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