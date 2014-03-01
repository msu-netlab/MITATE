package com.mitate.utilities;

import java.io.Serializable;

public class MeasurementLogs implements Serializable  {
	
	int iTransferId;
	int iDeviceId;
	String sUsername;
	String sMessage;
	
	public int getiTransferId() {
		return iTransferId; 
	}
	public void setiTransferId(int iTransferId) {
		this.iTransferId = iTransferId;
	}
	public int getiDeviceId() {
		return iDeviceId;
	}
	public void setiDeviceId(int iDeviceId) {
		this.iDeviceId = iDeviceId;
	}
	public String getsUsername() {
		return sUsername;
	}
	public void setsUsername(String sUsername) {
		this.sUsername = sUsername;
	}
	public String getsMessage() {
		return sMessage;
	}
	public void setsMessage(String sMessage) {
		this.sMessage = sMessage;
	}
	
}
