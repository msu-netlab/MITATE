package com.mitate.measurement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import javax.xml.bind.DatatypeConverter;

import com.mitate.MITATEApplication;

import android.util.Log;

/**
 * 
 * @author ajay
 * 
 */
public class TCPTest {
	
	String TAG = "TCPTest";
	
	String sServerIP;
	int iServerTCPPortNo;

	int iTCPBytes, iTCPPackets;		
	long[] laTCPPacketReceivedTimes;
	int[] iaTCPBytes;
	int iTCPBytesReadFromServer;
	int iTCPBytesSentToServer;
	int iDirection;
	int iPacketDelay;
	int iExplicit;
	String sContent;
	String sContentType;
	
	Socket sConnectionSocket;
	BufferedWriter bwWriteToServer;
	BufferedReader brReadFromServer;
	
	public TCPTest(String sServerIP, int iServerTCPPortNo, int iTCPBytes, int iTCPPackets, int iDirection, long lOffsetDifferenceClientAndServer, int iPacketDelay, int iExplicit, String sContent, String sContentType) {
		this.sServerIP = sServerIP;
		this.iServerTCPPortNo = iServerTCPPortNo;
		this.iTCPBytes = iTCPBytes;
		this.iTCPPackets = iTCPPackets;
		this.laTCPPacketReceivedTimes = new long[iTCPPackets];
		this.iaTCPBytes = new int[iTCPPackets];
		this.iTCPBytesReadFromServer = 0;
		this.iTCPBytesSentToServer = 0;
		this.iDirection = iDirection;
		this.iPacketDelay = iPacketDelay;
		this.iExplicit = iExplicit;
		this.sContent = sContent;
		this.sContentType = sContentType;
	}
	 
	public boolean runTCPTest() {

		if(MITATEApplication.bDebug) Log.d(TAG, "@runTCPTest : Connecting to server ..");
		
		try {

	        int iTCPConnectionRetryCount = 0;
			while(++iTCPConnectionRetryCount < 6) {
				try {
					Thread.sleep(1000);
					sConnectionSocket = new Socket(sServerIP, iServerTCPPortNo);
					sConnectionSocket.setSoTimeout(iPacketDelay + 8000);
					if(sConnectionSocket != null) {
						if(MITATEApplication.bDebug) Log.d(TAG, "@runTCPTest : Connected to Server : "+sConnectionSocket.getRemoteSocketAddress());
						break;
					}
				} catch(Exception e) {						
					Log.e(TAG, "@sendtimes : retry - "+iTCPConnectionRetryCount+", error - "+e.getMessage());
					if(iTCPConnectionRetryCount > 5) {
						Log.e(TAG, "@sendtimes : connection failed");
						return false;
					}	
				}
			}

			brReadFromServer = new BufferedReader(new InputStreamReader(sConnectionSocket.getInputStream()));
			
			
			laTCPPacketReceivedTimes = new long[iTCPPackets];
			iaTCPBytes = new int[iTCPPackets];

			String sBuffer = "";						
			byte[] baExtraBytes = null;
			
			if(iExplicit == 0) {
				if(iTCPBytes-(":;:11111:;:"+System.currentTimeMillis()+":;:").getBytes().length > 0)
					baExtraBytes = new byte[iTCPBytes-(":;:11111:;:"+System.currentTimeMillis()+":;:").getBytes().length];
				else {
					baExtraBytes = new byte[iTCPBytes - (sContent+":;:"+String.valueOf(1)+":;:"+System.currentTimeMillis()+":;:\n").length()];
				}
				
			} else {
				
				if(sContentType.equals("HEX")) {
					if(sContent.length() % 2 != 0) {
						sContent = sContent+"0";
					}
					
					sContent = new String(DatatypeConverter.parseHexBinary(sContent));

					System.out.println("tcp -hex hex --- >"+sContent);
				} else if(sContentType.equals("BINARY")) {
					
				} else {
					
				}
			}
			
			if(MITATEApplication.bDebug) Log.i(TAG, "@runTCPTest : TCP Transactionn - " + iTCPPackets + " Packets.");
			
			for (int i = 0; i <iTCPPackets; i++){
				try{
					if(iDirection == 0) {
						long lClientTime = System.currentTimeMillis() - Measurement.lClientOffsetFromNTP;
						if(iExplicit == 0) {
							sBuffer = Arrays.toString(baExtraBytes).replace('[', (char)32).replace(']', (char)32).replaceAll(",", "").replaceAll("(\\s)", "")+":;:"+i+":;:"+lClientTime+":;:"+"\n";							
						} else {
							// _sBuffer = sContent+(new char[5 - (i+"").length()]).length+":;:"+String.valueOf(i)+":;:"+lClientTime+":;:\n";
							sBuffer = sContent+Arrays.toString(baExtraBytes).replace('[', (char)32).replace(']', (char)32).replaceAll(",", "").replaceAll("(\\s)", "")+":;:"+i+":;:"+lClientTime+":;:\n";
									
							// sBuffer=  sContent+":;:"+String.valueOf(i)+":;:"+lClientTime+":;:\n";
						}

						bwWriteToServer = new BufferedWriter(new OutputStreamWriter(sConnectionSocket.getOutputStream()));
						bwWriteToServer.write(sBuffer); 		
						bwWriteToServer.flush();
						iTCPBytesSentToServer += sBuffer.getBytes().length + 1;
						// if(MITATEApplication.bDebug) Log.d(TAG, "@runTCPTest : +"+", packet delay - "+iPacketDelay+", Sent - " + i+", Bytes count -- "+sBuffer.getBytes().length+", Total bytes sent - "+iTCPBytesSentToServer);

						// if(MITATEApplication.bDebug) Log.d(TAG, "@TCP : C2S "+i+);
						Thread.sleep(iPacketDelay);
						System.out.println("sent-"+i+", chars - "+sBuffer.length()+", bytes - "+sBuffer.getBytes().length);
						// bwWriteToServer.close();
					}
					if(iDirection == 1) {

						char[] buf = new char[iTCPBytes < 27 ? 27 : iTCPBytes + 12];
						
						brReadFromServer.read(buf);
						String sFromServer = new String(buf);
						
						// String sFromServer = brReadFromServer.readLine();

						long lTimeOnClient = System.currentTimeMillis();
						int iNumberOfBytesReceived = sFromServer.getBytes().length;
						iTCPBytesReadFromServer += iNumberOfBytesReceived;

						int iTCPPacketNumber = Integer.parseInt(sFromServer.split(":;:")[1]);
						long lTimeOnServer = Long.parseLong(sFromServer.split(":;:")[2]);

						// long lLatencyDownLink = (lTimeOnClient-MITATEUtilities.lTimeDifference) - (lTimeOnServer + lOffsetDifferenceClientAndServer - Measurement.lOffsetServerAndNTP);
						long lLatencyDownLink = lTimeOnClient - Measurement.lClientOffsetFromNTP - lTimeOnServer; // MITATEUtilities.lTimeDifference) - (lTimeOnServer + lOffsetDifferenceClientAndServer - Measurement.lOffsetServerAndNTP);
						
						i= iTCPPacketNumber;

						laTCPPacketReceivedTimes[i] = lLatencyDownLink;
						iaTCPBytes[i] = iNumberOfBytesReceived;
						// brReadFromServer.close();
					}
				}
				catch(Exception e) {
					Log.e(TAG, "@runTCPTest : error - "+e.getMessage());
					if(MITATEApplication.bDebug) e.printStackTrace();
				}
			}
			
			Thread.sleep(iPacketDelay);
			
			Log.i(TAG, "@runTCPTest : TCP Completed");			
			return true;
			
		} catch (Exception e) {
			Log.e(TAG, "@runTCPTest : error - "+e.getMessage());
			if(MITATEApplication.bDebug) e.printStackTrace();
			return false;
		} finally {
			try {
				brReadFromServer.close();
				bwWriteToServer.close();
				sConnectionSocket.close();
			} catch(Exception e) {
				Log.e(TAG, "@runTCPTest : error - closing streams - "+e.getMessage());
			}
		}
	}
}