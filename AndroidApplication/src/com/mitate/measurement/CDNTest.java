package com.mitate.measurement;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import java.net.DatagramPacket;
import java.net.DatagramSocket;                                  

import android.util.Log;

import com.mitate.MITATEApplication;
import com.mitate.service.LoginService;

public class CDNTest {
	
	String TAG = "CDNTest";
	
	String sServerName = "mitate.cs.montana.edu";
	String sCDNName;
	String iPortNo;
	String sHTTPRequest;
	String sResponse;	
	String sResponseFromServer = "";

	Socket sConnectionSocket;
	PrintWriter pwWriteRequest;
	BufferedReader brReadResponse;
	
	DatagramSocket dsUDPSocket;
	DatagramPacket dpUDPSendPacket;
	DatagramPacket dpUDPRecvPacket;
	
	byte[] baReceivedData;
	
	int iTransactionId;
	int iTransferId;
	int iRepeat;
	int iPacketType;
	
	// initialize CDN test parameters
	public CDNTest(int iTransferId, int iTransactionId, String sCDNName, String iPortNo, String sHTTPRequest, int iRepeat, int iPacketType) {
		this.iTransferId = iTransferId;
		this.iTransactionId = iTransactionId;
		this.sCDNName= sCDNName;
		this.iPortNo = iPortNo;
		this.sHTTPRequest = sHTTPRequest;
		this.iRepeat = iRepeat;
		this.iPacketType = iPacketType;
	} 
	
	// run the cdn test
	public void runCDNTest() {
	
		ArrayList<Long> time = new ArrayList<Long>();	
		int iBytesRead = 0;
		long startTime = 0;
		long finishTime = 0;
		
		sHTTPRequest = sHTTPRequest+"\r\n";
		sHTTPRequest = sHTTPRequest.replaceAll("\\/","aabbcc").replaceAll("aabbcc","/"); //.replaceAll("\r\n", "====").replaceAll("====", "\r\n");
		
		if(MITATEApplication.bDebug) Log.v(TAG, sHTTPRequest.toString()+"-"+sCDNName+":"+iPortNo);
		
		if(iPacketType == 2){
		try {
			sConnectionSocket = new Socket(sCDNName, Integer.parseInt(iPortNo));
			sConnectionSocket.setSoTimeout(10000);
			pwWriteRequest = new PrintWriter(sConnectionSocket.getOutputStream(), true);
			brReadResponse = new BufferedReader(new InputStreamReader(sConnectionSocket.getInputStream()));					
			
			startTime = System.currentTimeMillis();
			
			for(int i=0; i<iRepeat; i++) {
				pwWriteRequest.write(sHTTPRequest+"\n");
				pwWriteRequest.flush();
			}				
			while((sResponse = brReadResponse.readLine())!=null) {
				time.add(System.currentTimeMillis());
				iBytesRead += sResponse.getBytes().length;
				System.out.println("----" + sResponse);
				sResponseFromServer = sResponseFromServer + sResponse;
				if(System.currentTimeMillis() - time.get(time.size()-1) > 1000)
				 	break;
			}
			
			finishTime = System.currentTimeMillis();
			
			brReadResponse.close();
			pwWriteRequest.close();
			sConnectionSocket.close();
			if(time.size() >= 2)	 
				sendMetrics(iBytesRead, (time.get(time.size() - 2) - time.get(0)), (time.get(0)-startTime), new Timestamp(System.currentTimeMillis()).toString(), sResponseFromServer.substring(0, sResponseFromServer.length() < 512 ? sResponseFromServer.length() : 512));
			else
				sendMetrics(0, 0, 0, new Timestamp(System.currentTimeMillis()).toString(), sResponseFromServer.substring(0, sResponseFromServer.length() < 512 ? sResponseFromServer.length() : 512));
		}
		catch(Exception e) {
			e.printStackTrace();
			sendMetrics(0, 0, 0, new Timestamp(System.currentTimeMillis()).toString(), "UnableToResolveTheHost");
		}
		}
		else if(iPacketType == 1) {
			try {
			dsUDPSocket = new DatagramSocket(3333);
			dsUDPSocket.setSoTimeout(10000);
			InetAddress iaServerAddress = InetAddress.getByName(sCDNName);
			dpUDPSendPacket = new DatagramPacket(sHTTPRequest.getBytes(), sHTTPRequest.getBytes().length, iaServerAddress, Integer.parseInt(iPortNo));
			
			startTime = System.currentTimeMillis();
			dsUDPSocket.send(dpUDPSendPacket);	
			
			baReceivedData = new byte[1000];
			dpUDPRecvPacket = new DatagramPacket(baReceivedData, baReceivedData.length, iaServerAddress, Integer.parseInt(iPortNo));
			
			while(true) {
				dsUDPSocket.receive(dpUDPRecvPacket);
				time.add(System.currentTimeMillis());
				iBytesRead += dpUDPRecvPacket.getData().length;
				System.out.println("+++++" + new String(dpUDPRecvPacket.getData()));
				sResponseFromServer = sResponseFromServer + new String(dpUDPRecvPacket.getData());
				if(dpUDPRecvPacket.getData().length <= 0 && (System.currentTimeMillis() - time.get(time.size()-1) > 1000))
					break;
			}
			finishTime = System.currentTimeMillis();
			
			if(time.size() >= 2)	 
				sendMetrics(iBytesRead, (time.get(time.size() - 2) - time.get(0)), (time.get(0)-startTime), new Timestamp(System.currentTimeMillis()).toString(), sResponseFromServer.substring(0, sResponseFromServer.length() < 512 ? sResponseFromServer.length() : 512));
			else
				sendMetrics(0, 0, 0, new Timestamp(System.currentTimeMillis()).toString(), sResponseFromServer.substring(0, sResponseFromServer.length() < 512 ? sResponseFromServer.length() : 512));
		
			dsUDPSocket.close();
			}
			catch(Exception e) {
				dsUDPSocket.close();
				e.printStackTrace();
				sendMetrics(0, 0, 0, new Timestamp(System.currentTimeMillis()).toString(), "UnableToResolveTheHost");
			}
		}
	}
	
	// save results to database
	public void sendMetrics(int iBytesRead, long oneway, long RTT, String sClientTime, String sLog) {
		try {
			
		   sClientTime = sClientTime.substring(0, sClientTime.indexOf(46)).replaceAll("(\\s)", "T");
	   	   String sURL = "http://"+sServerName+"/cdn.php?" +
	   			"username="+LoginService.sUserName+"&mobilecarrier="+(MITATEApplication.getTelephonyManager().getNetworkOperatorName().replaceAll("\\s", ""))+
	   			"&transferid="+iTransferId+"&transactionid="+iTransactionId+"&size="+iBytesRead+"&oneway="+oneway+
	   			"&rtt="+RTT+"&time="+sClientTime+"&deviceid="+LoginService.sDeviceId+"&devicename="+MITATEApplication.getDeviceModel()+"&log="+sLog;
   	   
	   	   Log.v(TAG, ""+sURL);
	   	   
	   	   HttpClient hcHttpClient = new DefaultHttpClient();		    
	   	   HttpGet hpHttpGet = new HttpGet(sURL);    
	   	   HttpResponse hrHttpResponse = hcHttpClient.execute(hpHttpGet);
	   	   
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
