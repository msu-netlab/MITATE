package com.mitate.measurement;

import java.io.Serializable;

public class ClientTimes implements Serializable {
    
    long[] laTCPPacketReceivedTimes;
    int[] iaTCPBytes;
    int iTCPBytesReadFromServer;
    int iTCPBytesSentToServer;
    long[] lUDPPacketReceivedTimes;
    int[] iaUDPBytes;
    int iUDPBytesReceivedFromServer;
    String sClientTime;
    String sBeforeExecCoordinates;
    String sAfterExecCoordinates;
    String sSignalStrength;
    String sAccelerometerReading;
    int isCallActive;
    int iTransferId;
    String sUDPLog;
    String sTCPLog;
	int iUDPBytesSentToServer;
    
    public int getiUDPBytesSentToServer() {
        return iUDPBytesSentToServer;
    }
    public void setiUDPBytesSentToServer(int iUDPBytesSentToServer) {
        this.iUDPBytesSentToServer = iUDPBytesSentToServer;
    }
    public String getsUDPLog() {
        return sUDPLog;
    }
    public void setsUDPLog(String sUDPLog) {
        this.sUDPLog = sUDPLog;
    }
    public String getsTCPLog() {
        return sTCPLog;
    }
    public void setsTCPLog(String sTCPLog) {
        this.sTCPLog = sTCPLog;
    }
    public int getiTransferId() {
        return iTransferId;
    }
    public void setiTransferId(int iTransferId) {
        this.iTransferId = iTransferId;
    }
    public long[] getLaTCPPacketReceivedTimes() {
        return laTCPPacketReceivedTimes;
    }
    public void setLaTCPPacketReceivedTimes(long[] laTCPPacketReceivedTimes) {
        this.laTCPPacketReceivedTimes = laTCPPacketReceivedTimes;
    }
    public int[] getIaTCPBytes() {
        return iaTCPBytes;
    }
    public void setIaTCPBytes(int[] iaTCPBytes) {
        this.iaTCPBytes = iaTCPBytes;
    }
    public int getiTCPBytesReadFromServer() {
        return iTCPBytesReadFromServer;
    }
    public void setiTCPBytesReadFromServer(int iTCPBytesReadFromServer) {
        this.iTCPBytesReadFromServer = iTCPBytesReadFromServer;
    }
    public int getiTCPBytesSentToServer() {
        return iTCPBytesSentToServer;
    }
    public void setiTCPBytesSentToServer(int iTCPBytesSentToServer) {
        this.iTCPBytesSentToServer = iTCPBytesSentToServer;
    }
    public long[] getlUDPPacketReceivedTimes() {
        return lUDPPacketReceivedTimes;
    }
    public void setlUDPPacketReceivedTimes(long[] lUDPPacketReceivedTimes) {
        this.lUDPPacketReceivedTimes = lUDPPacketReceivedTimes;
    }
    public int[] getIaUDPBytes() {
        return iaUDPBytes;
    }
    public void setIaUDPBytes(int[] iaUDPBytes) {
        this.iaUDPBytes = iaUDPBytes;
    }
    public int getiUDPBytesReceivedFromServer() {
        return iUDPBytesReceivedFromServer;
    }
    public void setiUDPBytesReceivedFromServer(int iUDPBytesReceivedFromServer) {
        this.iUDPBytesReceivedFromServer = iUDPBytesReceivedFromServer;
    }
    public String getsClientTime() {
        return sClientTime;
    }
    public void setsClientTime(String sClientTime) {
        this.sClientTime = sClientTime;
    }
    public String getsBeforeExecCoordinates() {
        return sBeforeExecCoordinates;
    }
    public void setsBeforeExecCoordinates(String sBeforeExecCoordinates) {
        this.sBeforeExecCoordinates = sBeforeExecCoordinates;
    }
    public String getsAfterExecCoordinates() {
        return sAfterExecCoordinates;
    }
    public void setsAfterExecCoordinates(String sAfterExecCoordinates) {
        this.sAfterExecCoordinates = sAfterExecCoordinates;
    }
    public String getsSignalStrength() {
        return sSignalStrength;
    }
    public void setsSignalStrength(String sSignalStrength) {
        this.sSignalStrength = sSignalStrength;
    }
    public String getsAccelerometerReading() {
        return sAccelerometerReading;
    }
    public void setsAccelerometerReading(String sAccelerometerReading) {
        this.sAccelerometerReading = sAccelerometerReading;
    }
    public int getIsCallActive() {
        return isCallActive;
    }
    public void setIsCallActive(int isCallActive) {
        this.isCallActive = isCallActive;
    }
}