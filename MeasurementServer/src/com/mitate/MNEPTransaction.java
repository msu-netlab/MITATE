/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
//package com.mitate;

import java.sql.Timestamp;

/**
 *
 * @author ajay
 */
public class MNEPTransaction {
    Timestamp[] tsaPacketReceivedTimes_Dest; //tsaTCPPacketReceivedTimes
    String tsaPacketBeforeSendTimes_Src; //tsaTCPPacketBeforeSendTimes_Client

    String sBytesSent_Src; //TCPBytesSent_Client
    int iBytesReceived_Dest; // iTCPTotalBytesReceived

    int[] iLatency; //iTCPUpLatency
    int iNoPackets; //iTCPPackets		
    int iBytes; //iTCPBytes

    public int getiBytes() {
        return iBytes;
    }

    public void setiBytes(int iBytes) {
        this.iBytes = iBytes;
    }

    public int getiBytesReceived_Dest() {
        return iBytesReceived_Dest;
    }

    public void setiBytesReceived_Dest(int iBytesReceived_Dest) {
        this.iBytesReceived_Dest = iBytesReceived_Dest;
    }

    public int[] getiLatency() {
        return iLatency;
    }

    public void setiLatency(int[] iLatency) {
        this.iLatency = iLatency;
    }

    public int getiNoPackets() {
        return iNoPackets;
    }

    public void setiNoPackets(int iNoPackets) {
        this.iNoPackets = iNoPackets;
    }

    public String getsBytesSent_Src() {
        return sBytesSent_Src;
    }

    public void setsBytesSent_Src(String sBytesSent_Src) {
        this.sBytesSent_Src = sBytesSent_Src;
    }

    public String getTsaPacketBeforeSendTimes_Src() {
        return tsaPacketBeforeSendTimes_Src;
    }

    public void setTsaPacketBeforeSendTimes_Src(String tsaPacketBeforeSendTimes_Src) {
        this.tsaPacketBeforeSendTimes_Src = tsaPacketBeforeSendTimes_Src;
    }

    public Timestamp[] getTsaPacketReceivedTimes_Dest() {
        return tsaPacketReceivedTimes_Dest;
    }

    public void setTsaPacketReceivedTimes_Dest(Timestamp[] tsaPacketReceivedTimes_Dest) {
        this.tsaPacketReceivedTimes_Dest = tsaPacketReceivedTimes_Dest;
    }
   
    
}        