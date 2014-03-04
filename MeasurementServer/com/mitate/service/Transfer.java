package com.mitate.service;

import java.io.Serializable;

public class Transfer implements Serializable {
    String sUsername;
    String sSourceIP;
    String sServerIP;
    String sContent;
    int iBytes;
    int iPacketType; 
    int iTransactionid;
    int iTransferid;
    int iPacketDelay;
    int iExplicit;
    int iNoOfPackets;
    String sPortNumber;
    String sContentType;
    int iResponse;
    int iTransferDelay;

    String sDeviceName;
    String sDeviceId;
    String sNetworkCarrier;
    long lClientOffsetFromNTP;
    int iDirection;
    
    int iUDPHexBytes;
    

    public int getiUDPHexBytes() {
        return iUDPHexBytes;
    }
    public void setiUDPHexBytes(int iUDPHexBytes) {
        this.iUDPHexBytes = iUDPHexBytes;
    }
    public String getsServerIP() {
        return sServerIP;
    }
    public void setsServerIP(String sServerIP) {
        this.sServerIP = sServerIP;
    }
    public String getsDeviceName() {
        return sDeviceName;
    }
    public void setsDeviceName(String sDeviceName) {
        this.sDeviceName = sDeviceName;
    }
    public String getsDeviceId() {
        return sDeviceId;
    }
    public void setsDeviceId(String sDeviceId) {
        this.sDeviceId = sDeviceId;
    }
    public String getsNetworkCarrier() {
        return sNetworkCarrier;
    }
    public void setsNetworkCarrier(String sNetWorkCarrier) {
        this.sNetworkCarrier = sNetWorkCarrier;
    }
    public long getlClientOffsetFromNTP() {
        return lClientOffsetFromNTP;
    }
    public void setlClientOffsetFromNTP(long lClientOffsetFromNTP) {
        this.lClientOffsetFromNTP = lClientOffsetFromNTP;
    }
    public int getiDirection() {
        return iDirection;
    }
    public void setiDirection(int iDirection) {
        this.iDirection = iDirection;
    }
    
    
    public String getsUsername() {
        return sUsername;
    }
    public void setsUsername(String sUsername) {
        this.sUsername = sUsername;
    }
    public int getiTransferDelay() {
        return iTransferDelay;
    }
    public void setiTransferDelay(int iTransferDelay) {
        this.iTransferDelay = iTransferDelay;
    }
    public int getiResponse() {
        return iResponse;
    }
    public void setiResponse(int iResponse) {
        this.iResponse = iResponse;
    }
    public String getsContentType() {
        return sContentType;
    }
    public void setsContentType(String sContentType) {
        this.sContentType = sContentType;
    }
    public String getsPortNumber() {
        return sPortNumber;
    }
    public void setsPortNumber(String sPortNumber) {
        this.sPortNumber = sPortNumber;
    }
    public int getiNoOfPackets() {
        return iNoOfPackets;
    }
    public void setiNoOfPackets(int iNoOfPackets) {
        this.iNoOfPackets = iNoOfPackets;
    }
    public String getsContent() {
        return sContent;
    }
    public void setsContent(String sContent) {
        this.sContent = sContent;
    }
    public int getiExplicit() {
        return iExplicit;
    }
    public void setiExplicit(int iExplicit) {
        this.iExplicit = iExplicit;
    }
    public int getiPacketDelay() {
        return iPacketDelay;
    }
    public void setiPacketDelay(int iPacketDelay) {
        this.iPacketDelay = iPacketDelay;
    }
    public int getiPacketType() {
        return iPacketType;
    }
    public void setiPacketType(int iPacketType) {
        this.iPacketType = iPacketType;
    }
    public String getsSourceIP() {
        return sSourceIP;
    }
    public void setsSourceIP(String sSourceIP) {
        this.sSourceIP = sSourceIP;
    }
    public String getsDestinationIP() {
        return sServerIP;
    }
    public void setsDestinationIP(String sServerIP) {
        this.sServerIP = sServerIP;
    }
    public int getiBytes() {
        return iBytes;
    }
    public void setiBytes(int iBytes) {
        this.iBytes = iBytes;
    }
    public int getiTransferid() {
        return iTransferid;
    }
    public void setiTransferid(int iTransferid) {
        this.iTransferid = iTransferid;
    }
    public int getiTransactionid() {
        return iTransactionid;
    }
    public void setiTransactionid(int iTransactionid) {
        this.iTransactionid = iTransactionid;
    }
}