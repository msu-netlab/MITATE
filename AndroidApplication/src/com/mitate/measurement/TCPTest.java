package com.mitate.measurement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;
import com.mitate.MITATEApplication;
import com.mitate.service.LoginService;
import com.mitate.utilities.MITATEUtilities;

import android.util.Log;

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
	Scanner scReadFromServer;
	
	String sLog = "SUCCESS";
	
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
		this.sLog = "SUCCESS";
	}
	 
	public boolean runTCPTest() {

		if(MITATEApplication.bDebug) Log.d(TAG, "@runTCPTest : Connecting to server ..");
		
		try {

	        int iTCPConnectionRetryCount = 0;
			while(++iTCPConnectionRetryCount < 6) {
				try {
					Thread.sleep(1000);
					sConnectionSocket = new Socket(sServerIP, iServerTCPPortNo);
					sConnectionSocket.setSoTimeout(10000);
					if(sConnectionSocket != null) {
						if(MITATEApplication.bDebug) Log.d(TAG, "@runTCPTest : Connected to Server : "+sConnectionSocket.getRemoteSocketAddress());
						break; 
					}
				} catch(Exception e) {						
					Log.e(TAG, "@TCPTest : retry - "+iTCPConnectionRetryCount+", error - "+e.getMessage());
					if(iTCPConnectionRetryCount > 0) {
						Log.e(TAG, "@sendtimes : connection failed");
						sLog = e.getMessage();
						return false;
					}	
					Thread.sleep(10000);
				}
			}

			brReadFromServer = new BufferedReader(new InputStreamReader(sConnectionSocket.getInputStream()));
			scReadFromServer = new Scanner(new BufferedReader(new InputStreamReader(sConnectionSocket.getInputStream())));
			
			laTCPPacketReceivedTimes = new long[iTCPPackets];
			iaTCPBytes = new int[iTCPPackets];

			String sBuffer = "";						
			byte[] baExtraBytes = null;
			
			if(iExplicit == 0) {
				if(iTCPBytes-(":;:1111:;:"+System.currentTimeMillis()+":::").getBytes().length > 0)
					baExtraBytes = new byte[iTCPBytes-(":;:1111:;:"+System.currentTimeMillis()+":::").getBytes().length];				
			} else {

			}
			
			if(MITATEApplication.bDebug) Log.i(TAG, "@runTCPTest : TCP Transactionn - " + iTCPPackets + " Packets.");
			
			for (int i = 0; i <iTCPPackets; i++){
				try{
					if(iDirection == 0) {
						if(iExplicit == 0) {
							sBuffer = Arrays.toString(baExtraBytes).replace('[', (char)32).replace(']', (char)32).replaceAll(",", "").replaceAll("(\\s)", "")+":;:"+String.format("%4s", i).replaceAll("\\s", "0")+":;:";							
						} else {
							sBuffer = sContent+":;:"+String.format("%4s", i).replaceAll("\\s", "0")+":;:";
						}
						
						System.out.print("offset - "+LoginService.lClientTimeOffset); 
						bwWriteToServer = new BufferedWriter(new OutputStreamWriter(sConnectionSocket.getOutputStream()));
						 
						bwWriteToServer.write(sBuffer+(System.currentTimeMillis() - LoginService.lClientTimeOffset)+":::"); 
						bwWriteToServer.flush();
					
						int iTCPBytesSent = sBuffer.getBytes().length;
						iTCPBytesSentToServer += iTCPBytesSent;							
						
						Thread.sleep(iPacketDelay);
					}
					if(iDirection == 1) {

						scReadFromServer.useDelimiter(":::");						
						String sFromServer = scReadFromServer.next();
												
						long lTimeOnClient = System.currentTimeMillis();
						int iNumberOfBytesReceived = 0;
						
						iNumberOfBytesReceived = sFromServer.getBytes().length + ":::".getBytes().length;
						iTCPBytesReadFromServer += iNumberOfBytesReceived;
						
						int iTCPPacketNumber = Integer.parseInt(sFromServer.split(":;:")[1]);
						long lTimeOnServer = Long.parseLong(sFromServer.split(":;:")[2]);

						long lLatencyDownLink = lTimeOnClient - LoginService.lClientTimeOffset - lTimeOnServer; // MITATEUtilities.lTimeDifference) - (lTimeOnServer + lOffsetDifferenceClientAndServer - Measurement.lOffsetServerAndNTP);
						
						i= iTCPPacketNumber;
						laTCPPacketReceivedTimes[i] = lLatencyDownLink;
						iaTCPBytes[i] = iNumberOfBytesReceived;
					}
				}
				catch(Exception e) {
					Log.e(TAG, "@runTCPTest : error - "+e.getMessage());
					sLog = e.getClass()+"";
					if(MITATEApplication.bDebug) e.printStackTrace();
				}
			}
			
			Thread.sleep(iPacketDelay);
			
			Log.i(TAG, "@runTCPTest : TCP Completed");			
			
		} catch (Exception e) {
			Log.e(TAG, "@runTCPTest : error - "+e.getMessage());
			if(MITATEApplication.bDebug) e.printStackTrace();
			sLog = e.getMessage();
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
	
		return true;
	}
}