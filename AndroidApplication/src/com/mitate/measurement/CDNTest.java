package com.mitate.measurement;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import org.apache.http.HttpResponse;
import org.apache.http.MethodNotSupportedException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Build;

import com.mitate.MITATEApplication;
import com.mitate.service.LoginService;

public class CDNTest {

	PrintWriter pwWriteRequest;
	BufferedReader brReadResponse;
	
	// String sServerName = "192.168.1.4";
	
	String sServerName = "mitate.cs.montana.edu";
	String sResponse;	
	Socket sConnectionSocket;
	
	int iTransferId;
	int iTransacationId;
	String sCDNName;
	String iPortNo;
	String sHTTPRequest;
	int iRepeat;
	
	public CDNTest(int iTransferId, int iTransactionId, String sCDNName, String iPortNo, String sHTTPRequest, int iRepeat) {
		this.iTransferId = iTransferId;
		this.iTransacationId = iTransactionId;
		this.sCDNName= sCDNName;
		this.iPortNo = iPortNo;
		this.sHTTPRequest = sHTTPRequest;
		this.iRepeat = iRepeat;
	} 
	
	
	public void runCDNTest() {
	
		ArrayList<Long> time = new ArrayList<Long>();	
		int iBytesRead = 0;
		long start = 0;
		long finish = 0;
		
		try {
			
			sHTTPRequest = sHTTPRequest+"\r\n";
			sHTTPRequest = sHTTPRequest.replaceAll("\\/","aabbcc").replaceAll("aabbcc","/"); //.replaceAll("\r\n", "====").replaceAll("====", "\r\n");
			
			System.out.println(sHTTPRequest.toString()+"------i-->"+sCDNName+"--"+iPortNo);
			
			sConnectionSocket = new Socket(sCDNName, Integer.parseInt(iPortNo));
			pwWriteRequest = new PrintWriter(sConnectionSocket.getOutputStream(), true);
			brReadResponse = new BufferedReader(new InputStreamReader(sConnectionSocket.getInputStream()));		

			
			
			start = System.currentTimeMillis();
			
			for(int i=0; i<iRepeat; i++) {
				pwWriteRequest.write(sHTTPRequest+"\n"); //.substring(0, sHTTPRequest.length()-1));
				pwWriteRequest.flush();
			}
				
		
			while((sResponse = brReadResponse.readLine())!=null) {
				time.add(System.currentTimeMillis());
				iBytesRead += sResponse.getBytes().length;
				if(System.currentTimeMillis() - time.get(time.size()-1) > 1000)
				 	break;
			}
			
			finish = System.currentTimeMillis();
			
			brReadResponse.close();
			pwWriteRequest.close();
			sConnectionSocket.close();
				 
			sendMetrics(iBytesRead, (time.get(time.size() - 2) - time.get(0)), (finish-start), new Timestamp(System.currentTimeMillis()).toString());
			
		}
		catch(Exception e){
			e.printStackTrace();
		}

	}
	
	public void sendMetrics(int iBytesRead, long Oneway, long RTT, String sClientTime ) {
		try {
			
		   sClientTime = sClientTime.substring(0, sClientTime.indexOf(46)).replaceAll("(\\s)", "T");
	   	   String sURL = "http://"+sServerName+"/cdn.php?" +
	   			"username="+LoginService.sUserName+"&mobilecarrier="+(MITATEApplication.getTelephonyManager().getNetworkOperatorName().replaceAll("\\s", ""))+
	   			"&transferid="+iTransferId+"&transactionid="+iTransacationId+"&size="+iBytesRead+"&oneway="+Oneway+
	   			"&rtt="+RTT+"&time="+sClientTime+"&deviceid="+LoginService.sDeviceId+"&devicename="+MITATEApplication.getDeviceModel();
   	   
	   	   System.out.println(sURL);
	   	   
	   	   HttpClient hcHttpClient = new DefaultHttpClient();		    
	   	   HttpGet hpHttpGet = new HttpGet(sURL);    
	   	   HttpResponse hrHttpResponse = hcHttpClient.execute(hpHttpGet);
	   	   
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
