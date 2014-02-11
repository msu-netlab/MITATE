//package com.mitate;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Arrays;

import javax.xml.bind.DatatypeConverter;

public class UDPTestRun {
    
    String TAG = "UDPTestRun";

    DatagramPacket tsUDPPacketBeforeSendTimePacket;        
    SocketAddress saClientAddress;
    
    int iUDPBytes, iUDPPackets, iUDPPort;
    int iUDPTotalBytesSentToClient, iUDPTotalBytesReceivedFromClient;  
    
    byte[] baSendData;
    byte[] baReceivedData;
    
	String sLog;
	
    DatagramSocket dsUDPSocket;
    DatagramPacket dpUDPSendPacket;
    DatagramPacket dpUDPRecvPacket;
    
    long[] laUDPPacketReceivedTimestamps;
    int[] iaUDPBytes;
    
    public UDPTestRun() {
        
    }
    
    public UDPTestRun(int iUDPBytes, int iUDPPackets, int iUDPPort) {
        this.iUDPBytes = iUDPBytes;
        this.iUDPPackets = iUDPPackets;
        this.iUDPPort = iUDPPort;
        laUDPPacketReceivedTimestamps = new long[iUDPPackets];
        iaUDPBytes = new int[iUDPPackets]; 
		sLog = "";
    }
    
    public boolean runUDPTest(int iUplinkOrDownlink, int iExplicit, String sContent, String sContentType, int iUdpHexBytes) {        
        System.out.println(TAG+" : @runUDPTest : Begin");
        try {
            baReceivedData = new byte[iUDPBytes < 27 ? 27 : iUDPBytes];
            dsUDPSocket = new DatagramSocket(iUDPPort);
            dsUDPSocket.setSoTimeout(10000);
            dpUDPRecvPacket = new DatagramPacket(baReceivedData, baReceivedData.length);
            for (int i=0; i<5; i++){
                dsUDPSocket.receive(dpUDPRecvPacket);
                //System.out.println(TAG+" : @runUDPTest : Receiving demo packet - "+i+ "--" + dpUDPRecvPacket.getSocketAddress());
            }
            saClientAddress = dpUDPRecvPacket.getSocketAddress();                     
            int iTimeOutPackets = 0;
            
            byte[] bExtraBytes;
            if(iUDPBytes-(":;:1111:;:"+System.currentTimeMillis()+":;:").getBytes().length > 0)
                bExtraBytes = new byte[iUDPBytes-(":;:1111:;:"+System.currentTimeMillis()+":;:").getBytes().length];
            else 
                bExtraBytes = new byte[1];
            
            String sData = "";
            for (int i = 0; i < iUDPPackets; i++){
                try {                
                if(iUplinkOrDownlink == 1) {                      
					if(iExplicit == 0) {
						sData = Arrays.toString(bExtraBytes).replace('[', (char)32).replace(']', (char)32).replaceAll(",", "").replaceAll("(\\s)", "") + ":;:" + String.format("%4s", i).replaceAll("\\s", "0") +":;:";
					}
					else if(iExplicit == 1)	{
						sData = sContent + ":;:" + String.format("%4s", i).replaceAll("\\s", "0")+":;:";
					}					
					baSendData = (sData + (System.currentTimeMillis()- MNEPServer.lServerOffsetFromNTP) + ":;:").getBytes();
					iUDPTotalBytesSentToClient += baSendData.length;						
					dpUDPSendPacket = new DatagramPacket(baSendData, baSendData.length, saClientAddress);                           
					dsUDPSocket.send(dpUDPSendPacket);
					System.out.println(TAG+": @UDPTest : Packet- " + i + " sent, Total bytes sent - "+iUDPTotalBytesSentToClient);
                    Thread.sleep(MNEPServer.iPacketDelay);               
                    }
                    if(iUplinkOrDownlink == 0) {		
						if(sContentType.equals("HEX")) {
							baReceivedData = new byte[iUdpHexBytes < 27 ? 27 : iUdpHexBytes];
						}
						else
							baReceivedData = new byte[iUDPBytes < 27 ? 27 : iUDPBytes];
                        dpUDPRecvPacket = new DatagramPacket(baReceivedData, baReceivedData.length);
                        dsUDPSocket.receive(dpUDPRecvPacket);            
						long lTimeOnServer = System.currentTimeMillis();
                        int iNoOfBytesReceived = dpUDPRecvPacket.getLength();
                        iUDPTotalBytesReceivedFromClient += iNoOfBytesReceived;
                        int iPacketNumber = Integer.parseInt(new String(dpUDPRecvPacket.getData()).split(":;:")[1]);
                        long lTimeOnClient = Long.parseLong(new String(dpUDPRecvPacket.getData()).split(":;:")[2]);
                        long lLatencyDownLink = lTimeOnServer - MNEPServer.lServerOffsetFromNTP - lTimeOnClient; 
                        laUDPPacketReceivedTimestamps[i] = lLatencyDownLink;
                        iaUDPBytes[i] = iNoOfBytesReceived;
                        i = iPacketNumber;
                        System.out.println(TAG+": @UDPTest : Packet " + iPacketNumber  + " received, Total bytes received - "+iUDPTotalBytesReceivedFromClient); 
                    }
                    
                } catch (Exception e){
                    System.out.println(TAG+" : @runUDPTest - " + e.getMessage());      
                    e.printStackTrace();
					return false;
                } 
            } 
            System.out.println(TAG+" : @runUDPTest : UDP Test Completed");
            dsUDPSocket.close();
			sLog = "SUCCESS";
            return true;

        } catch (Exception e){
            System.out.println(TAG+" : @runUDPTest : error - " + e.getMessage()); 
			dsUDPSocket.close();
            e.printStackTrace();
			sLog = "UDP SERVER SIDE ERROR - " + e.getClass() + "";
            return false;
        }
    }
}
