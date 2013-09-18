//package com.mitate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.CharBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Random;
        
public class MNEPServer {
	static String TAG = "MNEPServer";   
    static String sNTPServer = "us.pool.ntp.org"; 
    public int iUplinkOrDownlink;
    public int iUDPPackets, iUDPBytes, iUDPPort; 
    public int iTCPPackets, iTCPBytes, iTCPPort;
    public int iTransferId, iTransactionId, iExplicit;
    public static int iPacketDelay = 300;
    public String sMobileNetworkCarrier = "", sUsername = "", sDeviceName = "", sContent = "", sContentType = "", sDeviceId = "";
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

    private boolean receiveAndSendConnectionParameters() {     
        boolean bParametersReceivedSent = false;
        String sClientParameters = "";
        int iPacketType = 0, iNumberOfBytes = 0;      
        try {            
            brReadFromClient = new BufferedReader(new InputStreamReader(sTCPConnectionSocket.getInputStream(), "UTF-8"));                       
            char[] buf = new char[2000];
            brReadFromClient.read(buf);
            sClientParameters = new String(buf);
            System.out.println(sClientParameters);
            String[] saClientParameters = sClientParameters.trim().split(":;:");
            sUsername = saClientParameters[0];
            iPacketType = Integer.parseInt(saClientParameters[1]);
            iNumberOfBytes = Integer.parseInt(saClientParameters[2]);                       
            iTransferId = Integer.parseInt(saClientParameters[3]);
            iTransactionId = Integer.parseInt(saClientParameters[4]);
            iUplinkOrDownlink = Integer.parseInt(saClientParameters[5]);
            lClientOffsetFromNTP = Long.parseLong(saClientParameters[6]);
            iPacketDelay = Integer.parseInt(saClientParameters[7]);
            sMobileNetworkCarrier = saClientParameters[8];
            iUDPPackets = iTCPPackets = Integer.parseInt(saClientParameters[9]);
            iExplicit = Integer.parseInt(saClientParameters[10]); 
            if(!saClientParameters[11].equals("null")) {      
            	iUDPPort = iTCPPort = Integer.parseInt(saClientParameters[11]);            
            }
            else if(saClientParameters[11].equals("null")) {           
            	iUDPPort = new Random().nextInt(20000) + 20000;
            	iTCPPort = new Random().nextInt(20000) + 40000; // change required, port number changed after connection accepted
            }
            sContentType = saClientParameters[12];
			sDeviceId = saClientParameters[13];
			sDeviceName = saClientParameters[14];
            sContent = saClientParameters[15];           
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
			lServerOffsetFromNTP = MNEPUtilities.calculateTimeDifferenceBetweenNTPAndLocal("us.pool.ntp.org");             
            brWriteToClient = new BufferedWriter(new OutputStreamWriter(sTCPConnectionSocket.getOutputStream()));
            brWriteToClient.write(iUDPBytes+":;:"+iUDPPackets+":;:"+iUDPPort+":;:"+iTCPBytes+":;:"+iTCPPackets+":;:"+iTCPPort+":;:"+lServerOffsetFromNTP);
            brWriteToClient.flush();
            brReadFromClient.close();
            brWriteToClient.close();
            sTCPConnectionSocket.close();
            bParametersReceivedSent = true;          
        } 
        catch(Exception e) {
            bParametersReceivedSent = false;
            System.out.println(TAG+" : @sendConnectionParameters--- : error occurred - "+e.getMessage());
            e.printStackTrace();
        }
        return bParametersReceivedSent;
    }
	
    public void receiveTimes() {
        try {
            System.out.println("Receiving client times...");
            ssTCPServerSocket = new ServerSocket(iTCPPort);
			ssTCPServerSocket.setSoTimeout(0);
			System.out.println(ssTCPServerSocket.getLocalSocketAddress());
            Socket sTCPConnectionSocket = ssTCPServerSocket.accept();
            System.out.println(sTCPConnectionSocket.getRemoteSocketAddress());		
            brReadFromClient = new BufferedReader(new InputStreamReader(sTCPConnectionSocket.getInputStream(), "UTF-8"));
            String sDataFromClient = "";           
            for (int i=0; i<14; i++){
                sDataFromClient = brReadFromClient.readLine();
                switch(i) {
                    case 0: tsaTCPPacketReceivedTimes_Client = (sDataFromClient == null ? new String() : sDataFromClient);
                            //System.out.println("tts"+tsaTCPPacketReceivedTimes_Client);
                            break;
                    case 1: iaTCPBytes_Client = (sDataFromClient == null ? new String() : sDataFromClient);
                    		//System.out.println("tbytes-"+iaTCPBytes_Client);
                    		break;				
                    case 2: sTCPBytesReceived_Client = (sDataFromClient == null ? new String() : sDataFromClient);
                            //System.out.println("tbyterec"+sTCPBytesReceived_Client);
                            break;
                    case 3: sTCPBytesSent_Client = (sDataFromClient == null ? new String() : sDataFromClient);
                            //System.out.println("tbytesent"+sTCPBytesSent_Client);
                            break;
                    case 4: tsaUDPPacketReceivedTimes_Client = (sDataFromClient == null ? new String() : sDataFromClient);
                            //System.out.println("uts"+tsaUDPPacketReceivedTimes_Client);
                            break;
                    case 5: iaUDPBytes_Client = (sDataFromClient == null ? new String() : sDataFromClient);
                    		//System.out.println("tbytes"+iaUDPBytes_Client);
                    		break;				
                    case 6: sUDPBytesReceived_Client = (sDataFromClient == null ? new String() : sDataFromClient);
                            //System.out.println("tbyterec"+sUDPBytesReceived_Client);
                            break;
                    case 7: sClientTime = (sDataFromClient == null ? new String() : sDataFromClient);
                            sClientTime = sClientTime.substring(0, sClientTime.indexOf("."));
                            //System.out.println(sClientTime);
                            break;	
					case 8: sLatitudeBeforeTransferExecution = (sDataFromClient == null ? new String() : sDataFromClient);
							//System.out.println("sLatitudeBeforeTransferExecution" + sLatitudeBeforeTransferExecution);
							break;
					case 9: sLongitudeBeforeTransferExecution = (sDataFromClient == null ? new String() : sDataFromClient);
							//System.out.println("sLongitudeBeforeTransferExecution" + sLongitudeBeforeTransferExecution);
							break;
					case 10: sLatitudeAfterTransferExecution = (sDataFromClient == null ? new String() : sDataFromClient);
							//System.out.println("sLatitudeAfterTransferExecution" + sLatitudeAfterTransferExecution);
							break;
					case 11: sLongitudeAfterTransferExecution = (sDataFromClient == null ? new String() : sDataFromClient);
							//System.out.println("sLongitudeAfterTransferExecution" + sLongitudeAfterTransferExecution);
							break;
					case 12: sMobileSignalStrength = (sDataFromClient == null ? new String() : sDataFromClient);
							//System.out.println("sMobileSignalStrength" + sMobileSignalStrength);
							break;
					case 13: sAccelerometerReading = (sDataFromClient == null ? new String() : sDataFromClient);
							//System.out.println("sAccelerometerReading" + sAccelerometerReading);
							break;
                }
            }
            
            brWriteToClient = new BufferedWriter(new OutputStreamWriter(sTCPConnectionSocket.getOutputStream()));
            brWriteToClient.write(1+"\n");
            brWriteToClient.flush();
            brReadFromClient.close();
            brWriteToClient.close();
            sTCPConnectionSocket.close();
            ssTCPServerSocket.close();
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
				//s.execute("delete from metricdata where metricid != 9999 and transactionid = "+iTransactionId+" and transferid = "+iTransferId);
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
				
        	   //s.execute("update metricdata set value = 1 where metricid = 9999 and transferid = " + iTransferId + " and transactionid = " + iTransactionId);
        	   //s.execute("update metricdata set transferfinished = '" + sClientTime + "' where transferid = " + iTransferId + " and transactionid = " + iTransactionId);
			   s.execute("insert into metricdata values(10030, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sLatitudeBeforeTransferExecution) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10031, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sLongitudeBeforeTransferExecution) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10032, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sLatitudeAfterTransferExecution) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10033, " + iTransferId + ", " + iTransactionId + ", '" + Double.parseDouble(sLongitudeAfterTransferExecution) + "', '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10034, " + iTransferId + ", " + iTransactionId + ", " + dDeviceTravelSpeedInMeterPerSecond + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10035, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sMobileSignalStrength) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10036, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sAccelerometerReading.split(":")[0]) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10037, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sAccelerometerReading.split(":")[1]) + ", '" + sClientTime + "', '" + sDeviceId + "')");
			   s.execute("insert into metricdata values(10038, " + iTransferId + ", " + iTransactionId + ", " + Double.parseDouble(sAccelerometerReading.split(":")[2]) + ", '" + sClientTime + "', '" + sDeviceId + "')");
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
    
    public void main1(Socket sConnectionSocket) {
        sTCPConnectionSocket = sConnectionSocket;           
        System.out.println("*****************MITATE Measurement Server Started*********************");
        System.out.println("Variables sent: " + receiveAndSendConnectionParameters());
        if(iUDPBytes > 0) {
            uTestRun = new UDPTestRun(iUDPBytes, iUDPPackets, iUDPPort);                 
            uTestRun.runUDPTest(iUplinkOrDownlink, iExplicit, sContent, sContentType);
        }
        if(iTCPBytes > 0) {
            tTestRun = new TCPTestRun(iTCPBytes, iTCPPackets, iTCPPort);
            tTestRun.runTCPTest(iUplinkOrDownlink, iExplicit, sContent, sContentType);
        }
        receiveTimes();       
        if(iTCPBytes > 0)
        {			
            if(iUplinkOrDownlink == 0) {
                laTCPUplinkLatencies = tTestRun.laTCPPacketReceivedTimestamps;               
                iaTCPUpBytes = tTestRun.iaTCPBytes;
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
                fTCPUplinkThroughput = MNEPUtilities.toKBps(tTestRun.iTCPTotalBytesReceivedFromClient, MNEPUtilities.getSum(laTCPUplinkLatencies));
                fTCPUplinkJitter = fTCPUplinkMaxLatency - fTCPUplinkMinLatency;
                fTCPUplinkPacketLoss = (Integer.parseInt(sTCPBytesSent_Client)/(float)iTCPBytes - tTestRun.iTCPTotalBytesReceivedFromClient/(float)iTCPBytes) * 100 / (Integer.parseInt(sTCPBytesSent_Client)/(float)iTCPBytes);
               
                System.out.println(iTCPBytes + "-----" + sTCPBytesSent_Client);
                
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
            if(iUplinkOrDownlink == 1) {
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
                
                fTCPDownlinkPacketLoss = ((((float)tTestRun.iTCPTotalBytesSentToClient/iTCPBytes) - ((float)Integer.parseInt(sTCPBytesReceived_Client)/iTCPBytes)) * 100) / ((float)tTestRun.iTCPTotalBytesSentToClient/iTCPBytes);
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

            if(iUDPBytes>0)
            {                
                if(iUplinkOrDownlink == 0){
                    laUDPUplinkLatencies = uTestRun.laUDPPacketReceivedTimestamps;                 
                    iaUDPUpBytes = uTestRun.iaUDPBytes;
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
                    fUDPUplinkThroughput = MNEPUtilities.toKBps(uTestRun.iUDPTotalBytesReceivedFromClient, MNEPUtilities.getSum(laUDPUplinkLatencies));
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
                if(iUplinkOrDownlink == 1){
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
            insertIntoDatabase();
    	}
	}
