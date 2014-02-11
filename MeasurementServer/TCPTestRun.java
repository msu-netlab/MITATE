//package com.mitate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.util.Arrays;
import java.util.Scanner;
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
	String sLog;
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
		sLog = "";
    }
    
    public boolean runTCPTest(int iUplinkOrDownlink, int iExplicit, String sContent, String sContentType) {       
        System.out.println(TAG+" : @runTCPTest : begin");
        try {
            ssServerSocket = new ServerSocket(iTCPPort);
            ssServerSocket.setSoTimeout(10000);
            sSocket = ssServerSocket.accept();
            brReadFromClient = new BufferedReader(new InputStreamReader(sSocket.getInputStream(), "UTF-8"));
            Scanner scReadFromClient = new Scanner(new BufferedReader(new InputStreamReader(sSocket.getInputStream())));
            
            int iTimeOutPackets = 0;
            String sBuffer = "";
            byte[] baExtraBytes = null;
            if(iExplicit == 0) {
            	if(iTCPBytes-(":;:1111:;:"+System.currentTimeMillis()+":::").getBytes().length > 0)
            		baExtraBytes = new byte[iTCPBytes-(":;:1111:;:"+System.currentTimeMillis()+":::").getBytes().length];
            }
            for (int i = 0; i < iTCPPackets; i++) {
                try{
                    if(iUplinkOrDownlink == 1) {
                    	bwWriteToClient = new BufferedWriter(new OutputStreamWriter(sSocket.getOutputStream()));
                        if(iExplicit == 0)
						{
                        	sBuffer = Arrays.toString(baExtraBytes).replace('[', (char)32).replace(']', (char)32).replaceAll(",", "").replaceAll("(\\s)", "") + ":;:" + String.format("%4s", i).replaceAll("\\s", "0") +":;:";
                        }
						else if(iExplicit == 1)
						{
							sBuffer = sContent+":;:"+String.format("%4s", i).replaceAll("\\s", "0")+":;:";
						}
						bwWriteToClient.write(sBuffer + (System.currentTimeMillis() - MNEPServer.lServerOffsetFromNTP) + ":::");
                        bwWriteToClient.flush();
						iTCPTotalBytesSentToClient += sBuffer.getBytes().length;
                        System.out.println(TAG+"@runTCPTest : Sent - " + i +", Bytes count -- "+sBuffer.getBytes().length+", Total bytes sent - "+iTCPTotalBytesSentToClient);	
                        Thread.sleep(MNEPServer.iPacketDelay);
                    }
                    if(iUplinkOrDownlink == 0) {                   	
                        scReadFromClient.useDelimiter(":::");
                        String sFromClient = scReadFromClient.next();
                        long lTimeOnServer = System.currentTimeMillis();
                        int iNoOfBytesReceived = sFromClient.getBytes().length + ":::".length();
                        iTCPTotalBytesReceivedFromClient += iNoOfBytesReceived;
                        int iPacketNumber = Integer.parseInt(sFromClient.split(":;:")[1]);
                        long lTimeOnClient = Long.parseLong(sFromClient.split(":;:")[2]);
                        long lLatencyDownLink = lTimeOnServer - MNEPServer.lServerOffsetFromNTP - lTimeOnClient;
                        System.out.println(TAG+" : @runTCPTest : Received - " + iPacketNumber +" Total bytes received - "+iTCPTotalBytesReceivedFromClient);
                        laTCPPacketReceivedTimestamps[i] = lLatencyDownLink;
                        iaTCPBytes[i] = iNoOfBytesReceived;
                    }
                }
                catch(Exception e) {
                    System.out.println(TAG+" : @runTCPTest : error - "+e.getMessage());
                    e.printStackTrace();
					return false;
                }
            }
            scReadFromClient.close();             
            System.out.println(TAG+" : @runTCPTest : TCP test completed");  
			sLog = "SUCCESS";			
            return true;
        } catch (Exception e) {
            System.out.println(TAG+" : @runTCPTest - " + e.getMessage()); 
            e.printStackTrace();
			sLog = "TCP SERVER SIDE ERROR - " + e.getClass() + "";
            return false;
        }
		finally {
			try {
			sSocket.close();
            ssServerSocket.close(); 
			}
			catch(Exception e) {}
		}
    }
}