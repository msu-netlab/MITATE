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
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

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
	String sContentTypeEncoding;
	
	// initialize CDN test parameters
	public CDNTest(int iTransferId, int iTransactionId, String sCDNName, String iPortNo, String sHTTPRequest, int iRepeat, int iPacketType, String sContentType) {
		this.iTransferId = iTransferId;
		this.iTransactionId = iTransactionId;
		this.sCDNName= sCDNName;
		this.iPortNo = iPortNo;
		this.sHTTPRequest = sHTTPRequest;
		this.iRepeat = iRepeat;
		this.iPacketType = iPacketType;
		this.sContentTypeEncoding = sContentType;
	} 
	
	// run the cdn test
	public void runCDNTest() {
	
		ArrayList<Long> time = new ArrayList<Long>();	
		int iBytesRead = 0;
		long startTime = 0;
		long finishTime = 0;
		
		if(sContentTypeEncoding.equals("ASCII")) {
			sHTTPRequest = sHTTPRequest+"\r\n";
			sHTTPRequest = sHTTPRequest.replaceAll("\\/","aabbcc").replaceAll("aabbcc","/"); //.replaceAll("\r\n", "====").replaceAll("====", "\r\n");
		}
		else if(sContentTypeEncoding.equals("HEX")) {  
			StringBuilder output = new StringBuilder();
			for (int i = 0; i < sHTTPRequest.length() - 1; i+=2) {
				String str = sHTTPRequest.substring(i, i+2);
				output.append((char)Integer.parseInt(str, 16));
			}
			sHTTPRequest = new String(output);
		}
			
			
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
				System.out.println(System.currentTimeMillis());
				iBytesRead += sResponse.getBytes().length;
				sResponseFromServer = sResponseFromServer + sResponse;
				if(System.currentTimeMillis() - time.get(time.size()-1) > 5000)
				 	break;
			}
			
			finishTime = System.currentTimeMillis();
			
			brReadResponse.close();
			pwWriteRequest.close();
			sConnectionSocket.close();
			if(time.size() >= 2)	 
				sendMetrics(iBytesRead, (time.get(time.size() - 2) - time.get(0)), (time.get(0)-startTime), new Timestamp(System.currentTimeMillis()).toString(), sResponseFromServer.substring(0, sResponseFromServer.length() < 512 ? sResponseFromServer.length() : 512));
			else
				sendMetrics(0, 0, 0, new Timestamp(System.currentTimeMillis()).toString(), "Response not captured at the application layer");
		}
		catch(Exception e) {
			e.printStackTrace();
			sendMetrics(0, 0, 0, new Timestamp(System.currentTimeMillis()).toString(), e.getClass()+"");
		}
		}
		else if(iPacketType == 1) {
			try {
			dsUDPSocket = new DatagramSocket(10000);
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
				sResponseFromServer = sResponseFromServer + new String(dpUDPRecvPacket.getData());
				if(System.currentTimeMillis() - time.get(time.size()-1) > 1000)
					break;
			}
			finishTime = System.currentTimeMillis();
			
			if(time.size() >= 2)	 
				sendMetrics(iBytesRead, (time.get(time.size() - 2) - time.get(0)), (time.get(0)-startTime), new Timestamp(System.currentTimeMillis()).toString(), sResponseFromServer.substring(0, sResponseFromServer.length() < 512 ? sResponseFromServer.length() : 512));
			else
				sendMetrics(0, 0, 0, new Timestamp(System.currentTimeMillis()).toString(), "Response not captured at the application layer");
		
			dsUDPSocket.close();
			}
			catch(Exception e) {
				dsUDPSocket.close();
				//e.printStackTrace();
				if(time.size() > 0) {
					if(time.get(time.size() - 1) == time.get(0)) {
						sendMetrics(iBytesRead, (time.get(0)-startTime), (time.get(0)-startTime), new Timestamp(System.currentTimeMillis()).toString(), sResponseFromServer.substring(0, sResponseFromServer.length() < 512 ? sResponseFromServer.length() : 512));
					}
					else
						sendMetrics(iBytesRead, (time.get(time.size() - 1) - time.get(0)), (time.get(0)-startTime), new Timestamp(System.currentTimeMillis()).toString(), sResponseFromServer.substring(0, sResponseFromServer.length() < 512 ? sResponseFromServer.length() : 512));
				}
				else {
					sendMetrics(0, 0, 0, new Timestamp(System.currentTimeMillis()).toString(), e.getClass()+"");
				}
			}
		}
	}
	
	// save results to database
	public void sendMetrics(int iBytesRead, long oneway, long RTT, String sClientTime, String sLog) {
		try {
			
		   sClientTime = sClientTime.substring(0, sClientTime.indexOf(46)).replaceAll("(\\s)", "T");
	   	   
	   	   HttpClient hcHttpClient = new DefaultHttpClient();	
	   	   HttpPost hpHttpPost = new HttpPost("http://"+sServerName+"/cdn.php");  
	   	   ArrayList<NameValuePair> params = new ArrayList<NameValuePair>(2);
	   	   params.add(new BasicNameValuePair("username", LoginService.sUserName));
	   	   params.add(new BasicNameValuePair("mobilecarrier", (MITATEApplication.getTelephonyManager().getNetworkOperatorName().replaceAll("\\s", ""))));
	   	   params.add(new BasicNameValuePair("transferid", iTransferId + ""));
	   	   params.add(new BasicNameValuePair("transactionid", iTransactionId + ""));
	   	   params.add(new BasicNameValuePair("size", iBytesRead + ""));
	   	   params.add(new BasicNameValuePair("oneway", oneway + ""));
	   	   params.add(new BasicNameValuePair("rtt", RTT + ""));
	   	   params.add(new BasicNameValuePair("time", sClientTime));
	   	   params.add(new BasicNameValuePair("deviceid", LoginService.sDeviceId));
	   	   params.add(new BasicNameValuePair("devicename", MITATEApplication.getDeviceModel()));
	   	   params.add(new BasicNameValuePair("log", sLog));
	   	   hpHttpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

	   	   HttpResponse response = hcHttpClient.execute(hpHttpPost);
	   	   
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
