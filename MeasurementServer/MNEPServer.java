//package com.mitate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import java.io.ObjectInputStream;

import com.mitate.measurement.ClientTimes;
import com.mitate.service.Transfer;
        
public class MNEPServer {
	static String TAG = "MNEPServer";   
    static String sNTPServer = "us.pool.ntp.org"; 
    public int iUplinkOrDownlink;
    public int iUDPPackets, iUDPBytes, iUDPPort, iUdpHexBytes; 
    public int iTCPPackets, iTCPBytes, iTCPPort;
    public int iTransferId, iTransactionId, iExplicit;
    public static int iPacketDelay = 300;
    public String sMobileNetworkCarrier = "", sUsername = "", sDeviceName = "", sContent = "", sContentType = "", sDeviceId = "", isDeviceInCall = "";
    public static long lServerOffsetFromNTP;
    public long lClientOffsetFromNTP;
    
    ServerSocket ssTCPServerSocket;
    Socket sTCPConnectionSocket;
    BufferedReader brReadFromClient = null;
    BufferedWriter brWriteToClient = null;
    UDPTestRun uTestRun;
    TCPTestRun tTestRun;
    String sMeasurements;
    
    float fTCPUplinkMeanLatency, fTCPUplinkMaxLatency, fTCPUplinkMinLatency, fTCPUplinkMedianLatency, fTCPUplinkThroughput, fTCPUplinkJitter, fTCPUplinkPacketLoss;
    long[] laTCPUplinkLatencies;
    int[] iaTCPUpBytes;
    float[] faTCPUpThroughput;
    float fTCPDownlinkMeanLatency, fTCPDownlinkMaxLatency, fTCPDownlinkMinLatency, fTCPDownlinkMedianLatency, fTCPDownlinkThroughput, fTCPDownlinkJitter, fTCPDownlinkPacketLoss;
    long[] laTCPDownlinkLatencies;
    int[] iaTCPDownBytes;
    float[] faTCPDownThroughput;
    float fUDPUplinkMeanLatency, fUDPUplinkMaxLatency, fUDPUplinkMinLatency, fUDPUplinkMedianLatency, fUDPUplinkThroughput, fUDPUplinkJitter;;
    long[] laUDPUplinkLatencies;
    int[] iaUDPUpBytes;
    float[] faUDPUpThroughput;
    float fUDPDownlinkMeanLatency, fUDPDownlinkMaxLatency, fUDPDownlinkMinLatency, fUDPDownlinkMedianLatency, fUDPDownlinkThroughput, fUDPDownlinkJitter;
    long[] laUDPDownlinkLatencies;
    int[] iaUDPDownBytes;
    float[] faUDPDownThroughput;
    
    TransferMetrics tmTCPTransferMetrics;
    TransferMetrics tmUDPTransferMetrics;
    
    String tsaTCPPacketReceivedTimes_Client = "", iaTCPBytes_Client = "", sTCPBytesReceived_Client = "", sTCPBytesSent_Client = "";
    String tsaUDPPacketReceivedTimes_Client = "", iaUDPBytes_Client = "", sUDPBytesReceived_Client;
    String sClientTime="", sLatitudeBeforeTransferExecution = "", sLongitudeBeforeTransferExecution = "", sLatitudeAfterTransferExecution = "", sLongitudeAfterTransferExecution = "", sMobileSignalStrength = "", sAccelerometerReading = "";    
    HashMap<Integer, ServerMetrics> hmServerMetrics;
    
    public void main1(Socket sConnectionSocket) {
        sTCPConnectionSocket = sConnectionSocket;           
        System.out.println("*****************MITATE Measurement Server Started*********************");
		try{
			ObjectInputStream ois = new ObjectInputStream(sTCPConnectionSocket.getInputStream());
			ObjectOutputStream oos = new ObjectOutputStream(sTCPConnectionSocket.getOutputStream());
			lServerOffsetFromNTP = MNEPUtilities.calculateTimeDifferenceBetweenNTPAndLocal("us.pool.ntp.org");
			Transfer[] convertReceivedObjectFromClientIntoServerObject = (Transfer[])ois.readObject();
			oos.writeObject(lServerOffsetFromNTP);
			sTCPConnectionSocket.close();
			hmServerMetrics = new HashMap<Integer, ServerMetrics>();
			for(int iLoopAllTransfers = 0; iLoopAllTransfers< convertReceivedObjectFromClientIntoServerObject.length; iLoopAllTransfers++) {
				Thread.sleep(convertReceivedObjectFromClientIntoServerObject[iLoopAllTransfers].getiTransferDelay());
				boolean iParameterStatus = receiveAndSendConnectionParameters(convertReceivedObjectFromClientIntoServerObject[iLoopAllTransfers]);
				System.out.println("\nVariables sent Status: " + iParameterStatus);
				if(iParameterStatus) {
					if(iUDPBytes > 0) {
						uTestRun = new UDPTestRun(iUDPBytes, iUDPPackets, iUDPPort);                 
						uTestRun.runUDPTest(iUplinkOrDownlink, iExplicit, sContent, sContentType, iUdpHexBytes);
					}
					else {
						iUDPBytes = 0;
						uTestRun = new UDPTestRun(iUDPBytes, 0, 0);
					}
					if(iTCPBytes > 0) {
						tTestRun = new TCPTestRun(iTCPBytes, iTCPPackets, iTCPPort);
						tTestRun.runTCPTest(iUplinkOrDownlink, iExplicit, sContent, sContentType);
					}
					else {
						iTCPBytes = 0;
						tTestRun = new TCPTestRun(iTCPBytes, 0, 0);
					}				
					ServerMetrics smServerMetrics = new ServerMetrics();
					smServerMetrics.iTransferId = convertReceivedObjectFromClientIntoServerObject[iLoopAllTransfers].getiTransferid();
					smServerMetrics.laUDPPacketReceivedTimestamps = uTestRun.laUDPPacketReceivedTimestamps;
					smServerMetrics.iaUDPBytes = uTestRun.iaUDPBytes;
					smServerMetrics.iUDPTotalBytesReceivedFromClient = uTestRun.iUDPTotalBytesReceivedFromClient;
					smServerMetrics.laTCPPacketReceivedTimestamps = tTestRun.laTCPPacketReceivedTimestamps;
					smServerMetrics.iaTCPBytes = tTestRun.iaTCPBytes;
					smServerMetrics.iTCPTotalBytesReceivedFromClient = tTestRun.iTCPTotalBytesReceivedFromClient;
					smServerMetrics.iTCPTotalBytesSentToClient = tTestRun.iTCPTotalBytesSentToClient;
					smServerMetrics.iTCPBytes = iTCPBytes;
					smServerMetrics.iUDPBytes = iUDPBytes;
					smServerMetrics.iUplinkOrDownlink = convertReceivedObjectFromClientIntoServerObject[iLoopAllTransfers].getiDirection();
					smServerMetrics.iTransactionId = convertReceivedObjectFromClientIntoServerObject[iLoopAllTransfers].getiTransactionid();
					hmServerMetrics.put(smServerMetrics.iTransferId, smServerMetrics);
				}
				else
					System.out.println("Error in initializing parameters");			
			}
			receiveTimes();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
    	}
    
    private boolean receiveAndSendConnectionParameters(Transfer convertReceivedObjectFromClientIntoServerObject) {     
        boolean bParametersReceivedSent = false;
        String sClientParameters = "";
        int iPacketType = 0, iNumberOfBytes = 0;      
        try {            
            sUsername = convertReceivedObjectFromClientIntoServerObject.getsUsername();
            iPacketType = convertReceivedObjectFromClientIntoServerObject.getiPacketType();
            iNumberOfBytes = convertReceivedObjectFromClientIntoServerObject.getiBytes();                       
            iTransferId = convertReceivedObjectFromClientIntoServerObject.getiTransferid();
            iTransactionId = convertReceivedObjectFromClientIntoServerObject.getiTransactionid();
            iUplinkOrDownlink = convertReceivedObjectFromClientIntoServerObject.getiDirection();
            lClientOffsetFromNTP = convertReceivedObjectFromClientIntoServerObject.getlClientOffsetFromNTP();
            iPacketDelay = convertReceivedObjectFromClientIntoServerObject.getiPacketDelay();
            sMobileNetworkCarrier = convertReceivedObjectFromClientIntoServerObject.getsNetworkCarrier();
            iUDPPackets = iTCPPackets = convertReceivedObjectFromClientIntoServerObject.getiNoOfPackets();
            iExplicit = convertReceivedObjectFromClientIntoServerObject.getiExplicit(); 
            iUDPPort = iTCPPort = Integer.parseInt(convertReceivedObjectFromClientIntoServerObject.getsPortNumber());            
            sContentType = convertReceivedObjectFromClientIntoServerObject.getsContentType();
			sDeviceId = convertReceivedObjectFromClientIntoServerObject.getsDeviceId();
			sDeviceName = convertReceivedObjectFromClientIntoServerObject.getsDeviceName();
			iUdpHexBytes = convertReceivedObjectFromClientIntoServerObject.getiUDPHexBytes();
            sContent = convertReceivedObjectFromClientIntoServerObject.getsContent();           
            if(iExplicit == 0) {
            	if(iPacketType == 0 ) {
            			iUDPBytes = iNumberOfBytes/iUDPPackets;
            			iTCPBytes = iNumberOfBytes/iTCPPackets;
            	}
            	else if(iPacketType == 1 ) {
                	iUDPBytes = iNumberOfBytes/iUDPPackets;
                	iTCPBytes = 0;
            	}
            	else if(iPacketType == 2 ) {
              	iUDPBytes = 0;
                	iTCPBytes = iNumberOfBytes/iTCPPackets;
            	}
            }
			else if(iExplicit == 1) {	     
				if(iPacketType == 0 ) {
					iUDPBytes = iNumberOfBytes;
					iTCPBytes = iNumberOfBytes;
				}
				else if(iPacketType == 1 ) {
					iUDPBytes = iNumberOfBytes;
					iTCPBytes = 0;
				}
				else if(iPacketType == 2 ) {
					iUDPBytes = 0;
					iTCPBytes = iNumberOfBytes;
				}
			}
            bParametersReceivedSent = true;          
            return bParametersReceivedSent;
        }
        catch(Exception e) {
            bParametersReceivedSent = false;
            System.out.println(TAG+" : @sendConnectionParameters--- : error occurred - "+e.getMessage());
            e.printStackTrace();
            return bParametersReceivedSent;
        }
    }
	
    public void receiveTimes() {
        try {
            System.out.println("Receiving client times...");
            ssTCPServerSocket = new ServerSocket(32166);
			ssTCPServerSocket.setSoTimeout(0);
            Socket sTCPConnectionSocket = ssTCPServerSocket.accept();
            try {
    			ObjectInputStream ois = new ObjectInputStream(sTCPConnectionSocket.getInputStream());
    			ClientTimes[] convertReceivedClientTimesObject = (ClientTimes[])ois.readObject();
    			ObjectOutputStream oos = new ObjectOutputStream(sTCPConnectionSocket.getOutputStream());
    			oos.writeObject(1);
                sTCPConnectionSocket.close();
                ssTCPServerSocket.close();
    			for(int iLoopAllClientTimes = 0; iLoopAllClientTimes < convertReceivedClientTimesObject.length; iLoopAllClientTimes++) {
    				tsaTCPPacketReceivedTimes_Client = Arrays.toString(convertReceivedClientTimesObject[iLoopAllClientTimes].getLaTCPPacketReceivedTimes());
                    iaTCPBytes_Client = Arrays.toString(convertReceivedClientTimesObject[iLoopAllClientTimes].getIaTCPBytes());	
                    sTCPBytesReceived_Client = convertReceivedClientTimesObject[iLoopAllClientTimes].getiTCPBytesReadFromServer() + "";
                    sTCPBytesSent_Client = convertReceivedClientTimesObject[iLoopAllClientTimes].getiTCPBytesSentToServer() + "";
                    tsaUDPPacketReceivedTimes_Client = Arrays.toString(convertReceivedClientTimesObject[iLoopAllClientTimes].getlUDPPacketReceivedTimes());
                    iaUDPBytes_Client = Arrays.toString(convertReceivedClientTimesObject[iLoopAllClientTimes].getIaUDPBytes());		
                    sUDPBytesReceived_Client = convertReceivedClientTimesObject[iLoopAllClientTimes].getiUDPBytesReceivedFromServer() + "";
                    sClientTime = convertReceivedClientTimesObject[iLoopAllClientTimes].getsClientTime();
                    sClientTime = sClientTime.substring(0, sClientTime.indexOf("."));
					sLatitudeBeforeTransferExecution = convertReceivedClientTimesObject[iLoopAllClientTimes].getsBeforeExecCoordinates().split(":")[0];
					sLongitudeBeforeTransferExecution = convertReceivedClientTimesObject[iLoopAllClientTimes].getsBeforeExecCoordinates().split(":")[1];
					sLatitudeAfterTransferExecution = convertReceivedClientTimesObject[iLoopAllClientTimes].getsAfterExecCoordinates().split(":")[0];
					sLongitudeAfterTransferExecution = convertReceivedClientTimesObject[iLoopAllClientTimes].getsAfterExecCoordinates().split(":")[1];
					sMobileSignalStrength = convertReceivedClientTimesObject[iLoopAllClientTimes].getsSignalStrength();
					sAccelerometerReading = convertReceivedClientTimesObject[iLoopAllClientTimes].getsAccelerometerReading();
					isDeviceInCall = convertReceivedClientTimesObject[iLoopAllClientTimes].getIsCallActive() + "";
					ServerMetrics currentTransferServerMetrics = hmServerMetrics.get(convertReceivedClientTimesObject[iLoopAllClientTimes].getiTransferId());
					 if(currentTransferServerMetrics.iTCPBytes > 0)
				        {			
				            if(currentTransferServerMetrics.iUplinkOrDownlink == 0) {
				                laTCPUplinkLatencies = currentTransferServerMetrics.laTCPPacketReceivedTimestamps;               
				                iaTCPUpBytes = currentTransferServerMetrics.iaTCPBytes;
				                faTCPUpThroughput = MNEPUtilities.calculateThroughput(laTCPUplinkLatencies, iaTCPUpBytes); 
				                tmTCPTransferMetrics = new TransferMetrics(laTCPUplinkLatencies.clone(), iaTCPUpBytes, faTCPUpThroughput);
				                tmTCPTransferMetrics.calculateConfInterval();                
				                Arrays.sort(laTCPUplinkLatencies);
				                fTCPUplinkMeanLatency = MNEPUtilities.getSum(laTCPUplinkLatencies)/laTCPUplinkLatencies.length;
				                fTCPUplinkMaxLatency = laTCPUplinkLatencies[laTCPUplinkLatencies.length-1];
								int elim = 0;
								while(elim < laTCPUplinkLatencies.length)
								{
									if(laTCPUplinkLatencies[elim] == 0)
										elim = elim + 1;
									else
										break;
								}
				                fTCPUplinkMinLatency = laTCPUplinkLatencies[elim];
				                fTCPUplinkMedianLatency = laTCPUplinkLatencies[laTCPUplinkLatencies.length/2];
				                fTCPUplinkThroughput = MNEPUtilities.toKBps(currentTransferServerMetrics.iTCPTotalBytesReceivedFromClient, MNEPUtilities.getSum(laTCPUplinkLatencies));
				                fTCPUplinkJitter = fTCPUplinkMaxLatency - fTCPUplinkMinLatency;
				                fTCPUplinkPacketLoss = (float)(((Integer.parseInt(sTCPBytesSent_Client) - currentTransferServerMetrics.iTCPTotalBytesReceivedFromClient) * 100) / (Integer.parseInt(sTCPBytesSent_Client)));				                
				                sMeasurements = String.format(
				                    "----------TCP Network Metrics-------------\n" +
				                    "Uplink TCP throughput:       \t%.2f Kbps \n"+
				                    "Mean uplink latency:         \t%.2f ms \n" +
				                    "Max uplink latency:          \t%.2f ms\n" +
				                    "Min uplink latency:          \t%.2f ms\n" +
				                    "Uplink jitter:               \t%.2f ms \n" +
				                    "Uplink Packet Loss:          \t%f \n\n" +
				                    "Latency Confidence interval:		  \t%.2f ms \n" +
				                    "Throughput Confidence interval:		  \t%.2f ms \n",
				                    fTCPUplinkThroughput,fTCPUplinkMeanLatency,fTCPUplinkMaxLatency,fTCPUplinkMinLatency,fTCPUplinkJitter, fTCPUplinkPacketLoss,
				                    tmTCPTransferMetrics.fLatencyConfInterval, tmTCPTransferMetrics.fThroughpuConfInterval);
				                System.out.println(sMeasurements);
				            }
				            if(currentTransferServerMetrics.iUplinkOrDownlink == 1) {
				                laTCPDownlinkLatencies = MNEPUtilities.toTimeArray(tsaTCPPacketReceivedTimes_Client);               
				                iaTCPDownBytes = MNEPUtilities.toNumberOfBytesArray(iaTCPBytes_Client);
				                faTCPDownThroughput = MNEPUtilities.calculateThroughput(laTCPDownlinkLatencies, iaTCPDownBytes); 
				                tmTCPTransferMetrics = new TransferMetrics(laTCPDownlinkLatencies.clone(), iaTCPDownBytes, faTCPDownThroughput);
				                tmTCPTransferMetrics.calculateConfInterval();               
				                Arrays.sort(laTCPDownlinkLatencies);
				                fTCPDownlinkMeanLatency = MNEPUtilities.getSum(laTCPDownlinkLatencies)/laTCPDownlinkLatencies.length;
				                fTCPDownlinkMaxLatency = laTCPDownlinkLatencies[laTCPDownlinkLatencies.length-1];
				            	int elim = 0;
								while(elim < laTCPDownlinkLatencies.length)
								{
									if(laTCPDownlinkLatencies[elim] == 0)
										elim = elim + 1;
									else
										break;
								}
								fTCPDownlinkMinLatency = laTCPDownlinkLatencies[elim];
				                fTCPDownlinkMedianLatency = laTCPDownlinkLatencies[laTCPDownlinkLatencies.length/2];
				                fTCPDownlinkThroughput = MNEPUtilities.toKBps(Integer.parseInt(sTCPBytesReceived_Client), MNEPUtilities.getSum(laTCPDownlinkLatencies));
				                fTCPDownlinkJitter = fTCPDownlinkMaxLatency - fTCPDownlinkMinLatency;
				                
				                fTCPDownlinkPacketLoss = (float)(((currentTransferServerMetrics.iTCPTotalBytesSentToClient - Integer.parseInt(sTCPBytesReceived_Client)) * 100) / currentTransferServerMetrics.iTCPTotalBytesSentToClient);
				        
				                sMeasurements = String.format(
				                    "----------TCP Network Metrics-------------\n" +
				                    "Downlink TCP throughput:     \t%.2f Kbps \n"+
				                    "Mean downlink latency:       \t%.2f ms \n" +
				                    "Max downlink latency:        \t%.2f ms \n" +
				                    "Min downlink latency:        \t%.2f ms\n" +
				                    "Downlink jitter:             \t%.2f ms \n" +
				                    "Downlink Packet Loss:        \t%f \n\n" +
				                    "Latency Confidence interval:		  \t%.2f ms \n" +
				                    "Throughput Confidence interval:		  \t%.2f ms \n",
				                    fTCPDownlinkThroughput,fTCPDownlinkMeanLatency,fTCPDownlinkMaxLatency,fTCPDownlinkMinLatency,
				                    fTCPDownlinkJitter, fTCPDownlinkPacketLoss, tmTCPTransferMetrics.fLatencyConfInterval, tmTCPTransferMetrics.fThroughpuConfInterval);
				                System.out.println(sMeasurements);
				                }
				            }

				            if(currentTransferServerMetrics.iUDPBytes>0)
				            {                
				                if(currentTransferServerMetrics.iUplinkOrDownlink == 0) {
				                    laUDPUplinkLatencies = currentTransferServerMetrics.laUDPPacketReceivedTimestamps;                 
				                    iaUDPUpBytes = currentTransferServerMetrics.iaUDPBytes;
				                    faUDPUpThroughput = MNEPUtilities.calculateThroughput(laUDPUplinkLatencies, iaUDPUpBytes); 
				                    tmUDPTransferMetrics = new TransferMetrics(laUDPUplinkLatencies.clone(), iaUDPUpBytes, faUDPUpThroughput);
				                    tmUDPTransferMetrics.calculateConfInterval();   
				                    Arrays.sort(laUDPUplinkLatencies);
				                    fUDPUplinkMeanLatency = MNEPUtilities.getSum(laUDPUplinkLatencies)/laUDPUplinkLatencies.length;
				                    fUDPUplinkMaxLatency = laUDPUplinkLatencies[laUDPUplinkLatencies.length-1];
									int elim = 0;
									while(elim < laUDPUplinkLatencies.length)
									{
										if(laUDPUplinkLatencies[elim] == 0)
											elim = elim + 1;
										else
											break;
									}	
									fUDPUplinkMinLatency = laUDPUplinkLatencies[elim];
				                    fUDPUplinkMedianLatency = laUDPUplinkLatencies[laUDPUplinkLatencies.length/2];
				                    fUDPUplinkThroughput = MNEPUtilities.toKBps(currentTransferServerMetrics.iUDPTotalBytesReceivedFromClient, MNEPUtilities.getSum(laUDPUplinkLatencies));
				                    fUDPUplinkJitter = fUDPUplinkMaxLatency - fUDPUplinkMinLatency;
				                    sMeasurements = String.format(
					                    "----------UDP Network Metrics-------------\n" +
					                    "Uplink UDP throughput:       \t%.2f Kbps \n"+
					                    "Mean uplink latency:         \t%.2f ms \n" +
					                    "Max uplink latency:          \t%.2f ms\n" +
					                    "Min uplink latency:          \t%.2f ms\n" +
					                    "Uplink jitter:               \t%.2f ms \n" +
					                    "Latency Confidence interval:		  \t%.2f ms \n" +
					                    "Throughput Confidence interval:		  \t%.2f ms \n",                                    
					                    fUDPUplinkThroughput,fUDPUplinkMeanLatency,fUDPUplinkMaxLatency,fUDPUplinkMinLatency,fUDPUplinkJitter,
					                    tmUDPTransferMetrics.fLatencyConfInterval, tmUDPTransferMetrics.fThroughpuConfInterval);
				                    System.out.println(sMeasurements);
				                }
				                if(currentTransferServerMetrics.iUplinkOrDownlink == 1){
				                    laUDPDownlinkLatencies = MNEPUtilities.toTimeArray(tsaUDPPacketReceivedTimes_Client);		                    
				                    iaUDPDownBytes = MNEPUtilities.toNumberOfBytesArray(iaUDPBytes_Client);
				                    faUDPDownThroughput = MNEPUtilities.calculateThroughput(laUDPDownlinkLatencies, iaUDPDownBytes); 
				                    tmUDPTransferMetrics = new TransferMetrics(laUDPDownlinkLatencies.clone(), iaUDPDownBytes, faUDPDownThroughput);
				                    tmUDPTransferMetrics.calculateConfInterval();                    
				                    Arrays.sort(laUDPDownlinkLatencies);
				                    fUDPDownlinkMeanLatency = MNEPUtilities.getSum(laUDPDownlinkLatencies)/laUDPDownlinkLatencies.length;
				                    fUDPDownlinkMaxLatency = laUDPDownlinkLatencies[laUDPDownlinkLatencies.length-1];
									int elim = 0;
									while(elim < laUDPDownlinkLatencies.length)
									{
										if(laUDPDownlinkLatencies[elim] == 0)
											elim = elim + 1;
										else
											break;
									}
									fUDPDownlinkMinLatency = laUDPDownlinkLatencies[elim];
				                    fUDPDownlinkMedianLatency = laUDPDownlinkLatencies[laUDPDownlinkLatencies.length/2];
				                    fUDPDownlinkThroughput = MNEPUtilities.toKBps(Integer.parseInt(sUDPBytesReceived_Client), MNEPUtilities.getSum(laUDPDownlinkLatencies));
				                    fUDPDownlinkJitter = fUDPDownlinkMaxLatency - fUDPDownlinkMinLatency;
				                    sMeasurements = String.format(
				                        "----------UDP Network Metrics-------------\n" +
				                        "Downlink UDP throughput:     \t%.2f Kbps \n"+
				                        "Mean downlink latency:       \t%.2f ms \n" +
				                        "Max downlink latency:        \t%.2f ms \n" +
				                        "Min downlink latency:        \t%.2f ms\n" +
				                        "Downlink jitter:             \t%.2f ms \n" +
				                        "Latency Confidence interval:		  \t%.2f ms \n" +
				                        "Throughput Confidence interval:		  \t%.2f ms \n",
				                        fUDPDownlinkThroughput,fUDPDownlinkMeanLatency,fUDPDownlinkMaxLatency,
				                        fUDPDownlinkMinLatency,fUDPDownlinkJitter, tmUDPTransferMetrics.fLatencyConfInterval, tmUDPTransferMetrics.fThroughpuConfInterval);
				                    System.out.println(sMeasurements);
				                }
				            }
				            Connection conn = null;
				            try{
				    			conn = MNEPUtilities.getDBConnection();
				    			if(conn != null) {
				    				System.out.println ("Database connection established");  
				    				Statement s;
				    				s = conn.createStatement();
				    				iTransferId = currentTransferServerMetrics.iTransferId;
				    				iTransactionId = currentTransferServerMetrics.iTransactionId;
				    				if(currentTransferServerMetrics.iUDPBytes>0) {
				    					if(currentTransferServerMetrics.iUplinkOrDownlink == 0){
				    						s.execute("insert into metricdata values(10000, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkThroughput + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10004, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkMinLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10006, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkMeanLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10023, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkMedianLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10008, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkMaxLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10016, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkJitter + ", '" + sClientTime + "', '" + sDeviceId + "')");                                 
				    					}
				    					if(currentTransferServerMetrics.iUplinkOrDownlink == 1){
				    						s.execute("insert into metricdata values(10002, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkThroughput + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10010, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkMinLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10012, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkMeanLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10025, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkMedianLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10014, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkMaxLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10018, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkJitter + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    					}
				    				}
				    				if(currentTransferServerMetrics.iTCPBytes>0) {
				    					if(currentTransferServerMetrics.iUplinkOrDownlink == 0){
				    						s.execute("insert into metricdata values(10001, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkThroughput + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10005, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkMinLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10007, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkMeanLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10009, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkMaxLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10024, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkMedianLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10017, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkJitter + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10020, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkPacketLoss + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    					}
				    					if(currentTransferServerMetrics.iUplinkOrDownlink == 1){
				    						s.execute("insert into metricdata values(10003, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkThroughput + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10011, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkMinLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10013, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkMeanLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10015, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkMaxLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10026, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkMedianLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10019, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkJitter + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    						s.execute("insert into metricdata values(10021, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkPacketLoss + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    					}
				    				}         
				    				if(tmUDPTransferMetrics == null) {
				    					tmUDPTransferMetrics = new TransferMetrics();
				    				}
				    				if(tmTCPTransferMetrics == null) {
				    					tmTCPTransferMetrics = new TransferMetrics();
				    				}          
				    				PreparedStatement psInsertStmt = conn.prepareStatement("insert into transfermetrics " + "(transferid, transactionid, udppacketmetrics, tcppacketmetrics, udplatencyconf, udpthroughputconf, tcplatencyconf, tcpthroughputconf, deviceid)" + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
				    				psInsertStmt.setInt(1, iTransferId);
				    				psInsertStmt.setInt(2, iTransactionId);
				    				psInsertStmt.setObject(3, (Object)tmUDPTransferMetrics);
				    				psInsertStmt.setObject(4, (Object)tmTCPTransferMetrics);
				    				psInsertStmt.setFloat(5, tmUDPTransferMetrics.fLatencyConfInterval);
				    				psInsertStmt.setFloat(6, tmUDPTransferMetrics.fThroughpuConfInterval);
				    				psInsertStmt.setFloat(7, tmTCPTransferMetrics.fLatencyConfInterval);
				    				psInsertStmt.setFloat(8, tmTCPTransferMetrics.fThroughpuConfInterval);
				    				psInsertStmt.setString(9, sDeviceId);
				               
				    				int t = psInsertStmt.executeUpdate();
				    				System.out.println("number of records inserted - " + t);
				    				
				    				double dDistanceBetweenTwoGeographicCoordinatesInKilometeres = 6378.137 * Math.acos( Math.cos( Double.parseDouble(sLatitudeBeforeTransferExecution) * (22/(180*7)) ) * Math.cos( Double.parseDouble(sLatitudeAfterTransferExecution) * (22/(180*7))  ) * Math.cos(( Double.parseDouble(sLongitudeAfterTransferExecution) - Double.parseDouble(sLongitudeBeforeTransferExecution)) * (22/(180*7)) ) + Math.sin( Double.parseDouble(sLatitudeBeforeTransferExecution) * (22/(180*7)) ) * Math.sin( Double.parseDouble(sLatitudeAfterTransferExecution) * (22/(180*7)) ));
				    				
				    				float fTotalTimeForTransfer = 0.0f;
				    				if(currentTransferServerMetrics.iUDPBytes > 0 && currentTransferServerMetrics.iUplinkOrDownlink == 0 && currentTransferServerMetrics.iTCPBytes == 0 )
				    					fTotalTimeForTransfer = fUDPUplinkMeanLatency;
				    				if(currentTransferServerMetrics.iUDPBytes > 0 && currentTransferServerMetrics.iUplinkOrDownlink == 1 && currentTransferServerMetrics.iTCPBytes == 0 )
				    					fTotalTimeForTransfer = fUDPDownlinkMeanLatency;
				    				if(currentTransferServerMetrics.iTCPBytes > 0 && currentTransferServerMetrics.iUplinkOrDownlink == 0 && currentTransferServerMetrics.iUDPBytes == 0 )
				    					fTotalTimeForTransfer = fTCPUplinkMeanLatency;
				    				if(currentTransferServerMetrics.iTCPBytes > 0 && currentTransferServerMetrics.iUplinkOrDownlink == 1 && currentTransferServerMetrics.iUDPBytes == 0 )
				    					fTotalTimeForTransfer = fTCPDownlinkMeanLatency;
				    				if(currentTransferServerMetrics.iTCPBytes > 0 && currentTransferServerMetrics.iUDPBytes > 0 && currentTransferServerMetrics.iUplinkOrDownlink == 0 )
				    					fTotalTimeForTransfer = fUDPUplinkMeanLatency + fTCPUplinkMeanLatency;
				    				if(currentTransferServerMetrics.iTCPBytes > 0 && currentTransferServerMetrics.iUDPBytes > 0 && currentTransferServerMetrics.iUplinkOrDownlink == 1 )
				    					fTotalTimeForTransfer = fUDPDownlinkMeanLatency + fTCPDownlinkMeanLatency;
				    				
				    				double dDeviceTravelSpeedInMeterPerSecond = (dDistanceBetweenTwoGeographicCoordinatesInKilometeres * 1000.0) / (fTotalTimeForTransfer / 1000.0);
				    				
				    			   s.execute("insert into metricdata values(10030, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sLatitudeBeforeTransferExecution) + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    			   s.execute("insert into metricdata values(10031, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sLongitudeBeforeTransferExecution) + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    			   s.execute("insert into metricdata values(10032, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sLatitudeAfterTransferExecution) + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    			   s.execute("insert into metricdata values(10033, " + iTransferId + ", " + iTransactionId + ", '" + Double.parseDouble(sLongitudeAfterTransferExecution) + "', '" + sClientTime + "', '" + sDeviceId + "')");
				    			   s.execute("insert into metricdata values(10034, " + iTransferId + ", " + iTransactionId + ", " + dDeviceTravelSpeedInMeterPerSecond + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    			   s.execute("insert into metricdata values(10035, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sMobileSignalStrength) + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    			   s.execute("insert into metricdata values(10036, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sAccelerometerReading.split(":")[0]) + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    			   s.execute("insert into metricdata values(10037, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sAccelerometerReading.split(":")[1]) + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    			   s.execute("insert into metricdata values(10038, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sAccelerometerReading.split(":")[2]) + ", '" + sClientTime + "', '" + sDeviceId + "')");
				    			   s.execute("insert into metricdata values(10039, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(isDeviceInCall) + ", '" + sClientTime + "', '" + sDeviceId + "')");
				            	   s.execute("insert into transferexecutedby values(" + iTransferId + ", '" + sDeviceName + "', '" + sUsername + "', '" + sMobileNetworkCarrier + "', '" + sDeviceId + "')");
				            	   conn.close();
				            	   System.out.println ("Entry made in Database");  
				               	}
				            }
				            catch (SQLException e) {
				            	System.err.println ("Could not make entry to database server");
				            	e.printStackTrace();
				            	try {
				                    if(conn != null)
				                        conn.close();
				            	} 
				            	catch (SQLException ex) {
				                    System.err.println ("Could not make entry to database server");
				                    e.printStackTrace();
				            	}
				            }			            
    			}

            }
            catch (Exception e)
    		{
    			e.printStackTrace();
    		}       
        } 
        catch(Exception e) {
            System.out.println(TAG+" : @receiveTimes : error occurred - "+e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void insertIntoDatabase() {
        Connection conn = null;
        try{
			conn = MNEPUtilities.getDBConnection();
			if(conn != null) {
				System.out.println ("Database connection established");  
				Statement s;
				s = conn.createStatement();
				if(iUDPBytes>0) {
					if(iUplinkOrDownlink == 0){
						s.execute("insert into metricdata values(10000, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkThroughput + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10004, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkMinLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10006, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkMeanLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10023, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkMedianLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10008, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkMaxLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10016, " + iTransferId + ", " + iTransactionId + ", " + fUDPUplinkJitter + ", '" + sClientTime + "', '" + sDeviceId + "')");                                 
					}
					if(iUplinkOrDownlink == 1){
						s.execute("insert into metricdata values(10002, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkThroughput + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10010, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkMinLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10012, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkMeanLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10025, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkMedianLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10014, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkMaxLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10018, " + iTransferId + ", " + iTransactionId + ", " + fUDPDownlinkJitter + ", '" + sClientTime + "', '" + sDeviceId + "')");
					}
				}
				if(iTCPBytes>0) {
					if(iUplinkOrDownlink == 0){
						s.execute("insert into metricdata values(10001, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkThroughput + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10005, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkMinLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10007, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkMeanLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10009, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkMaxLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10024, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkMedianLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10017, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkJitter + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10020, " + iTransferId + ", " + iTransactionId + ", " + fTCPUplinkPacketLoss + ", '" + sClientTime + "', '" + sDeviceId + "')");
					}
					if(iUplinkOrDownlink == 1){
						s.execute("insert into metricdata values(10003, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkThroughput + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10011, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkMinLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10013, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkMeanLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10015, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkMaxLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10026, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkMedianLatency + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10019, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkJitter + ", '" + sClientTime + "', '" + sDeviceId + "')");
						s.execute("insert into metricdata values(10021, " + iTransferId + ", " + iTransactionId + ", " + fTCPDownlinkPacketLoss + ", '" + sClientTime + "', '" + sDeviceId + "')");
					}
				}         
				if(tmUDPTransferMetrics == null) {
					tmUDPTransferMetrics = new TransferMetrics();
				}
				if(tmTCPTransferMetrics == null) {
					tmTCPTransferMetrics = new TransferMetrics();
				}          
				PreparedStatement psInsertStmt = conn.prepareStatement("insert into transfermetrics " + "(transferid, transactionid, udppacketmetrics, tcppacketmetrics, udplatencyconf, udpthroughputconf, tcplatencyconf, tcpthroughputconf, deviceid)" + "values (?, ?, ?, ?, ?, ?, ?, ?, ?)");
				psInsertStmt.setInt(1, iTransferId);
				psInsertStmt.setInt(2, iTransactionId);
				psInsertStmt.setObject(3, (Object)tmUDPTransferMetrics);
				psInsertStmt.setObject(4, (Object)tmTCPTransferMetrics);
				psInsertStmt.setFloat(5, tmUDPTransferMetrics.fLatencyConfInterval);
				psInsertStmt.setFloat(6, tmUDPTransferMetrics.fThroughpuConfInterval);
				psInsertStmt.setFloat(7, tmTCPTransferMetrics.fLatencyConfInterval);
				psInsertStmt.setFloat(8, tmTCPTransferMetrics.fThroughpuConfInterval);
				psInsertStmt.setString(9, sDeviceId);
           
				int t = psInsertStmt.executeUpdate();
				System.out.println("number of records inserted - " + t);
				
				double dDistanceBetweenTwoGeographicCoordinatesInKilometeres = 6378.137 * Math.acos( Math.cos( Double.parseDouble(sLatitudeBeforeTransferExecution) * (22/(180*7)) ) * Math.cos( Double.parseDouble(sLatitudeAfterTransferExecution) * (22/(180*7))  ) * Math.cos(( Double.parseDouble(sLongitudeAfterTransferExecution) - Double.parseDouble(sLongitudeBeforeTransferExecution)) * (22/(180*7)) ) + Math.sin( Double.parseDouble(sLatitudeBeforeTransferExecution) * (22/(180*7)) ) * Math.sin( Double.parseDouble(sLatitudeAfterTransferExecution) * (22/(180*7)) ));
				
				float fTotalTimeForTransfer = 0.0f;
				if(iUDPBytes > 0 && iUplinkOrDownlink == 0 && iTCPBytes == 0 )
					fTotalTimeForTransfer = fUDPUplinkMeanLatency;
				if(iUDPBytes > 0 && iUplinkOrDownlink == 1 && iTCPBytes == 0 )
					fTotalTimeForTransfer = fUDPDownlinkMeanLatency;
				if(iTCPBytes > 0 && iUplinkOrDownlink == 0 && iUDPBytes == 0 )
					fTotalTimeForTransfer = fTCPUplinkMeanLatency;
				if(iTCPBytes > 0 && iUplinkOrDownlink == 1 && iUDPBytes == 0 )
					fTotalTimeForTransfer = fTCPDownlinkMeanLatency;
				if(iTCPBytes > 0 && iUDPBytes > 0 && iUplinkOrDownlink == 0 )
					fTotalTimeForTransfer = fUDPUplinkMeanLatency + fTCPUplinkMeanLatency;
				if(iTCPBytes > 0 && iUDPBytes > 0 && iUplinkOrDownlink == 1 )
					fTotalTimeForTransfer = fUDPDownlinkMeanLatency + fTCPDownlinkMeanLatency;
				
				double dDeviceTravelSpeedInMeterPerSecond = (dDistanceBetweenTwoGeographicCoordinatesInKilometeres * 1000.0) / (fTotalTimeForTransfer / 1000.0);
				
			   s.execute("insert into metricdata values(10030, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sLatitudeBeforeTransferExecution) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10031, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sLongitudeBeforeTransferExecution) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10032, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sLatitudeAfterTransferExecution) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10033, " + iTransferId + ", " + iTransactionId + ", '" + Double.parseDouble(sLongitudeAfterTransferExecution) + "', '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10034, " + iTransferId + ", " + iTransactionId + ", " + dDeviceTravelSpeedInMeterPerSecond + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10035, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sMobileSignalStrength) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10036, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sAccelerometerReading.split(":")[0]) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10037, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sAccelerometerReading.split(":")[1]) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10038, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sAccelerometerReading.split(":")[2]) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10039, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(isDeviceInCall) + ", '" + sClientTime + "', '" + sDeviceId + "')");
        	   s.execute("insert into transferexecutedby values(" + iTransferId + ", '" + sDeviceName + "', '" + sUsername + "', '" + sMobileNetworkCarrier + "', '" + sDeviceId + "')");
        	   conn.close();
        	   System.out.println ("Entry made in Database");  
           	}
        }
        catch (SQLException e) {
        	System.err.println ("Could not make entry to database server");
        	e.printStackTrace();
        	try {
                if(conn != null)
                    conn.close();
        	} 
        	catch (SQLException ex) {
                System.err.println ("Could not make entry to database server");
                e.printStackTrace();
        	}
        }
    }
    
    
	}
