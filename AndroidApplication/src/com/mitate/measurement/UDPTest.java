package com.mitate.measurement;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;

import com.mitate.MITATEApplication;
import com.mitate.service.LoginService;
 
import android.telephony.TelephonyManager;
import android.util.Log;

public class UDPTest {

	String TAG = "UDPTest";
	
	int iUDPBytes, iUDPPackets, iUDPPort, iUDPBytesSentToServer, iUDPBytesReceivedFromServer;  
	int iServerUDPPortNo;
	String sServerIP;

	long[] lUDPPacketReceivedTimes;
	int[] iaUDPBytes;
	int iDirection = 0;
	int iPacketDelay, iExplicit;
	String sContent;
	String sContentType;
	
	DatagramSocket dsUDPSocket;
	DatagramPacket dpUDPSendPacket;
	DatagramPacket dpUDPRecvPacket;
	
	byte[] baSendData;
	byte[] baReceivedData;
	
	TelephonyManager mTelephonyManager;
	
	String sLog = "SUCCESS";

	public UDPTest() {

	}
	
	public UDPTest(String sServerIP, int iUDPPort, int iUDPBytes, int iUDPPackets, int iDirection, long lOffsetDifferenceClientAndServer, int iPacketDelay, int iExplicit, String sContent, String sContentType) {
		this.sServerIP = sServerIP;			
		this.iUDPPackets = iUDPPackets;
		this.iUDPPort = iUDPPort;
		this.iUDPBytesSentToServer = 0;
		this.iUDPBytesReceivedFromServer = 0;
		this.lUDPPacketReceivedTimes = new long[iUDPPackets];
		this.iUDPBytes = iUDPBytes;			
		this.iaUDPBytes = new int[iUDPPackets];
		this.iDirection = iDirection;
		this.iPacketDelay = iPacketDelay;
		this.iExplicit = iExplicit;
		this.sContent = sContent;
		this.sContentType = sContentType;
		this.sLog = "SUCCESS";
	}

	public boolean runUDPTest() {
		try { 

			lUDPPacketReceivedTimes = new long[iUDPPackets];
			iaUDPBytes = new int[iUDPPackets];
			
			InetAddress iaServerAddress = InetAddress.getByName(sServerIP);
			dsUDPSocket = new DatagramSocket(iUDPPort);
			dsUDPSocket.setSoTimeout(10000);
			dpUDPSendPacket = new DatagramPacket(new byte[10], 1, iaServerAddress, iUDPPort);
			
			for(int k=0;k <5; k++) {
				dsUDPSocket.send(dpUDPSendPacket);	
			}
			
			if(MITATEApplication.bDebug) Log.i(TAG, "@runUDPTest : UDP Transactions " + iUDPPackets + " packets");
			
			int iPacketNumber=0;
			int iTimeOutPackets = 0;
			
			byte[] bExtraBytes = null;
			
			if(iExplicit == 0) {
				if(iUDPBytes-(":;:1111:;:"+System.currentTimeMillis()+":;:").getBytes().length > 0)
					bExtraBytes = new byte[iUDPBytes-(":;:1111:;:"+System.currentTimeMillis()+":;:").getBytes().length];
			} else {
				
			}

			String sData = "";
			for (int i = 0; i < iUDPPackets; i++) {
				try {
					if(iDirection == 0) {
						if(iExplicit == 0) {
							sData = Arrays.toString(bExtraBytes).replace('[', (char)32).replace(']', (char)32).replaceAll(",", "").replaceAll("(\\s)", "")+":;:"+String.format("%4s", i).replaceAll("\\s", "0")+":;:"; //+lClientTime+":;:";						
						} else {
							sData = sContent+":;:"+String.format("%4s", i).replaceAll("\\s", "0")+":;:"; //+lClientTime+":;:";
						}

						baSendData = (sData+(System.currentTimeMillis() - LoginService.lClientTimeOffset)+":;:").getBytes();
						iUDPBytesSentToServer += sData.getBytes().length;						
						dpUDPSendPacket = new DatagramPacket(baSendData, baSendData.length, iaServerAddress, iUDPPort);
						dsUDPSocket.send(dpUDPSendPacket);					

						Thread.sleep(iPacketDelay);
					}
					if(iDirection == 1) {		
						baReceivedData = new byte[iUDPBytes < 27 ? 27 : iUDPBytes];
						dpUDPRecvPacket = new DatagramPacket(baReceivedData, baReceivedData.length);
						
						dsUDPSocket.receive(dpUDPRecvPacket);
						long lTimeOnClient = System.currentTimeMillis();
						
						int iUDPBytesReceived = dpUDPRecvPacket.getData().length;
						iUDPBytesReceivedFromServer += iUDPBytesReceived;
						
						iPacketNumber = Integer.parseInt(new String(dpUDPRecvPacket.getData()).split(":;:")[1]);
						long lTimeOnServer = Long.parseLong(new String(dpUDPRecvPacket.getData()).split(":;:")[2]);

						long lLatencyDownLink = lTimeOnClient - LoginService.lClientTimeOffset - lTimeOnServer; // (lTimeOnClient-MITATEUtilities.lTimeDifference) - (lTimeOnServer + lOffsetDifferenceClientAndServer - Measurement.lOffsetServerAndNTP);
						lUDPPacketReceivedTimes[i] = lLatencyDownLink;
						iaUDPBytes[i] = iUDPBytesReceived;

						i = iPacketNumber;

					}
				} catch(Exception e){
					if(++iTimeOutPackets > 1) break;
					Log.e("UDP", "error udp - "+e.getMessage());
					sLog = e.getClass()+"";
					e.printStackTrace();
				} 
			}
			
			Thread.sleep(iPacketDelay);			
			Log.i(TAG, "@UDPTest : UDP Completed");

		} catch (Exception e) {
			Log.e(TAG, "@UDPTest : error - "+e.getMessage());
			e.printStackTrace();
			sLog = e.getMessage();
			return false;
		} finally {
			dsUDPSocket.close();
		}
		return true;		
	}
}