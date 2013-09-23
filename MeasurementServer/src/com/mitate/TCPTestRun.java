//package com.mitate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Arrays;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import javax.xml.bind.DatatypeConverter;

public class TCPTestRun {
    String TAG = "TCPTestRun";  
    public static ServerSocket ssServerSocket;
    public static Socket sSocket;
    int iTCPPackets, iTCPBytes, iTCPPort;    
    int iTCPTotalBytesSentToClient, iTCPTotalBytesReceivedFromClient;
    long[] laTCPPacketReceivedTimestamps;
	int[] iaTCPBytes;
    BufferedReader brReadFromClient;
    BufferedWriter bwWriteToClient;
    
    public TCPTestRun() {
        
    }
    
    public TCPTestRun(int iTCPBytes, int iTCPPackets, int iTCPPort) {
        this.iTCPBytes = iTCPBytes;
        this.iTCPPackets = iTCPPackets;
        this.iTCPPort = iTCPPort;
        iTCPTotalBytesSentToClient = 0;
        iTCPTotalBytesReceivedFromClient = 0;
        laTCPPacketReceivedTimestamps = new long[iTCPPackets];
        iaTCPBytes = new int[iTCPPackets];
    }
    
    public boolean runTCPTest(int iUplinkOrDownlink, int iExplicit, String sContent, String sContentType) {       
        System.out.println(TAG+" : @runTCPTest : begin");
        try {
            ssServerSocket = new ServerSocket(iTCPPort);
            ssServerSocket.setSoTimeout(40000);
            sSocket = ssServerSocket.accept();
            brReadFromClient = new BufferedReader(new InputStreamReader(sSocket.getInputStream(), "UTF-8"));
            
            int iTimeOutPackets = 0;
            String sBuffer = "";
            byte[] baExtraBytes = null;
            if(iExplicit == 0) {
            	if(iTCPBytes-(":;:1111:;:"+System.currentTimeMillis()+":;:").getBytes().length > 0)
            		baExtraBytes = new byte[iTCPBytes-(":;:1111:;:"+System.currentTimeMillis()+":;:").getBytes().length];
            }
			/*
            if(sContentType.equals("HEX")){
            	if(sContent.length()%2 != 0)
            		sContent.concat("0");
            	sContent = new String(DatatypeConverter.parseHexBinary(sContent));
            }
            else if(sContentType.equals("BINARY")){
            	int end = sContent.length()%8;
            	if(end != 0){
            		for(int istart = 0; istart<end; istart++)
            		sContent.concat("0");
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
*/
            for (int i = 0; i < iTCPPackets; i++) {
                try{
                    if(iUplinkOrDownlink == 1) {
                    	bwWriteToClient = new BufferedWriter(new OutputStreamWriter(sSocket.getOutputStream()));
                    	long lServerTime =  System.currentTimeMillis() - MNEPServer.lServerOffsetFromNTP;
                        if(iExplicit == 0)
						{
                        	sBuffer = Arrays.toString(baExtraBytes).replace('[', (char)32).replace(']', (char)32).replaceAll(",", "").replaceAll("(\\s)", "") + ":;:" + String.format("%4s", i).replaceAll("\\s", "0") +":;:"+lServerTime+":;:";
                        }
						else if(iExplicit == 1)
						{
							sBuffer = sContent+":;:"+String.format("%4s", i).replaceAll("\\s", "0")+":;:"+lServerTime+":;:";
						}
						System.out.println(sBuffer);
						bwWriteToClient.write(sBuffer);
                        bwWriteToClient.flush();
						if(sContentType.equalsIgnoreCase("HEX"))
							iTCPTotalBytesSentToClient += sBuffer.length();
						else
							iTCPTotalBytesSentToClient += sBuffer.getBytes().length;
                        System.out.println(TAG+"@runTCPTest : Sent - " + i +", Bytes count -- "+sBuffer.getBytes().length+", Total bytes sent - "+iTCPTotalBytesSentToClient);	
                        Thread.sleep(MNEPServer.iPacketDelay);
                    }
                    if(iUplinkOrDownlink == 0) {                   	
                    	char[] buf = new char[iTCPBytes < 27 ? 27 : iTCPBytes];
                        brReadFromClient.read(buf);
                        String sFromClient = new String(buf);
						System.out.println(sFromClient);
                        long lTimeOnServer = System.currentTimeMillis();
                        int iNoOfBytesReceived = sFromClient.getBytes().length;
                        iTCPTotalBytesReceivedFromClient += iNoOfBytesReceived;
                        int iPacketNumber = Integer.parseInt(sFromClient.split(":;:")[1]);
                        long lTimeOnClient = Long.parseLong(sFromClient.split(":;:")[2]);
                        long lLatencyDownLink = lTimeOnServer - MNEPServer.lServerOffsetFromNTP - lTimeOnClient;
                        System.out.println(TAG+" : @runTCPTest : Received - " + iPacketNumber +" Total bytes received - "+iTCPTotalBytesReceivedFromClient /*+", stime - "+lTimeOnServer+", ctime - "+lTimeOnClient+", diff - "+(lTimeOnClient-lTimeOnServer)+", ntpd - "+MNEPServer.lServerOffsetFromNTP */);
                        laTCPPacketReceivedTimestamps[i] = lLatencyDownLink;
                        iaTCPBytes[i] = iNoOfBytesReceived;
                    }
                }
                catch(Exception e) {
                    if(++iTimeOutPackets > 3) {
                        return false;
                    }
                    System.out.println(TAG+" : @runTCPTest : error - "+e.getMessage());
                    e.printStackTrace();
                }
            }
                
            System.out.println(TAG+" : @runTCPTest : TCP test completed");
            sSocket.close();
            ssServerSocket.close();            
            return true;
        } catch (Exception e) {
            System.out.println(TAG+" : @runTCPTest - " + e.getMessage()); 
            e.printStackTrace();
            return false;
        }        
    }
}