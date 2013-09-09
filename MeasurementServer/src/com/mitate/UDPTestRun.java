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
    }
    
    public boolean runUDPTest(int iUplinkOrDownlink, int iExplicit, String sContent, String sContentType) {        
        System.out.println(TAG+" : @runUDPTest : Begin");
        try {
            baReceivedData = new byte[iUDPBytes < 27 ? 27 : iUDPBytes];
            dsUDPSocket = new DatagramSocket(iUDPPort);
            dsUDPSocket.setSoTimeout(40000);
            dpUDPRecvPacket = new DatagramPacket(baReceivedData, baReceivedData.length);
            for (int i=0; i<5; i++){
                dsUDPSocket.receive(dpUDPRecvPacket);
                System.out.println(TAG+" : @runUDPTest : Receiving demo packet - "+i+ "--" + dpUDPRecvPacket.getSocketAddress());
            }
            saClientAddress = dpUDPRecvPacket.getSocketAddress();                     
            int iTimeOutPackets = 0;
            
            byte[] bExtraBytes;
            if(iUDPBytes-(":;:11111:;:"+System.currentTimeMillis()+":;:").getBytes().length > 0)
                bExtraBytes = new byte[iUDPBytes-(":;:11111:;:"+System.currentTimeMillis()+":;:").getBytes().length];
            else 
                bExtraBytes = new byte[1];
            
            String sData = "";
            if(sContentType.equals("HEX")){
            	if(sContent.length()%2 != 0)
            		sContent += "0";
            	sContent = new String(DatatypeConverter.parseHexBinary(sContent));
            }
            else if(sContentType.equals("BINARY")){
            	int end = sContent.length()%8;
            	if(end != 0){
            		for(int istart = 0; istart<end; istart++)
            		sContent += "0";
            	}
            	String s2 = "";   
            	char nextChar;
            	for(int iParse = 0; iParse < sContent.length()-8; iParse += 9)
            	{
            	     nextChar = (char)Integer.parseInt(sContent.substring(iParse, iParse+7), 2);
            	     s2 += nextChar;
            	}
            	
            	sContent = s2;
            }
           
            for (int i = 0; i < iUDPPackets; i++){
                try {
                    
                if(iUplinkOrDownlink == 1) {         
					long lServerTime = System.currentTimeMillis()- MNEPServer.lServerOffsetFromNTP;                
					if(iExplicit == 0) {
						sData = Arrays.toString(bExtraBytes).replace('[', (char)32).replace(']', (char)32).replaceAll(",", "").replaceAll("(\\s)", "") + ":;:" + String.valueOf(i)+":;:"+lServerTime+":;:";
					}
					else if(iExplicit == 1)	{
						sData = sContent + ":;:" + String.valueOf(i)+":;:"+lServerTime+":;:";
					}					
					baSendData = sData.getBytes();
					iUDPTotalBytesSentToClient += baSendData.length;						
					dpUDPSendPacket = new DatagramPacket(baSendData, baSendData.length, saClientAddress);                           
					dsUDPSocket.send(dpUDPSendPacket);
					System.out.println(TAG+": @UDPTest : Packet- " + i + " sent, Total bytes sennt - "+iUDPTotalBytesSentToClient);
                    Thread.sleep(MNEPServer.iPacketDelay);
                        
                    }
                    if(iUplinkOrDownlink == 0) {		
                        baReceivedData = new byte[iUDPBytes < 27 ? 27 : iUDPBytes];
                        dpUDPRecvPacket = new DatagramPacket(baReceivedData, baReceivedData.length);
                        dsUDPSocket.receive(dpUDPRecvPacket);
			   // System.out.println("Packet Add: " + dpUDPRecvPacket.getSocketAddress() + "---Remote Add: " + dsUDPSocket.getRemoteSocketAddress() + "----Local Add: " + dsUDPSocket.getLocalSocketAddress() );               
			   long lTimeOnServer = System.currentTimeMillis();
                        int iNoOfBytesReceived = dpUDPRecvPacket.getLength();
                        iUDPTotalBytesReceivedFromClient += iNoOfBytesReceived;
                        int iPacketNumber = Integer.parseInt(new String(dpUDPRecvPacket.getData()).split(":;:")[1]);
                        long lTimeOnClient = Long.parseLong(new String(dpUDPRecvPacket.getData()).split(":;:")[2]);
                        long lLatencyDownLink = lTimeOnServer - MNEPServer.lServerOffsetFromNTP - lTimeOnClient; 
                        laUDPPacketReceivedTimestamps[i] = lLatencyDownLink;
                        iaUDPBytes[i] = iNoOfBytesReceived;
                        i = iPacketNumber;
                        System.out.println(TAG+": @UDPTest : Packet " + iPacketNumber  + " received, Total bytes received - "+iUDPTotalBytesReceivedFromClient+", ctime - "+lTimeOnClient ); 
                    }
                    
                } catch (Exception e){
                    if(++iTimeOutPackets > 3) {
                        return false;
                    }
                    System.out.println(TAG+" : @runUDPTest - " + e.getMessage());      
                    e.printStackTrace();
                    // return false;
                } 
            } 

            System.out.println(TAG+" : @runUDPTest : UDP Test Completed");
            dsUDPSocket.close();
            return true;

        } catch (Exception e){
            System.out.println(TAG+" : @runUDPTest : error - " + e.getMessage()); 
            e.printStackTrace();
            return false;
        }
    }
}
