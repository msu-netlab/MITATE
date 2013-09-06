/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mitate;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MNEPAcceptConnection {
    
    static String TAG = "MNEPAcceptConnection";
    
    int iTCPPort;
    
    ServerSocket ssTCPServerSocket;
    // Socket sTCPConnectionSocket;
    
    MNEPAcceptConnection() {
        iTCPPort = 32165;
    }
    
    public static void main(String arg[]) {
        MNEPAcceptConnection macClientConnection = new MNEPAcceptConnection();

            try {
                macClientConnection.ssTCPServerSocket = new ServerSocket(macClientConnection.iTCPPort);
                macClientConnection.ssTCPServerSocket.setSoTimeout(108000000);
                                        for(;;) {
                System.out.println("Server socket created on port - "+macClientConnection.iTCPPort+", waiting for connections ..");

                final Socket sTCPConnectionSocket = macClientConnection.ssTCPServerSocket.accept();
                System.out.println("Connection accepted from "+sTCPConnectionSocket.getRemoteSocketAddress());
                new Thread(new Runnable() {

                    @Override
                    public void run() {
                        MNEPServer msServer = new MNEPServer();
                        msServer.main1(sTCPConnectionSocket);
                    }
                }).start(); 
                
                        } 
            } catch (IOException ex) {
                System.out.println(TAG+" : @main : error - "+ex.getMessage());
                ex.printStackTrace();
            }
       
    }
    
    private double distFrom(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
               Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
               Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return   earthRadius * c;
      }
    
}
