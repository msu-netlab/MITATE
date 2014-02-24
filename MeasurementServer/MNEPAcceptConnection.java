import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class MNEPAcceptConnection {
    static String TAG = "MNEPAcceptConnection";
    int iTCPPort;
    ServerSocket ssTCPServerSocket;
	static long lServerOffsetFromNTPFromMain;
    
    MNEPAcceptConnection() {
        iTCPPort = 32165;
    }
    
    public static void main(String arg[]) {
        MNEPAcceptConnection macClientConnection = new MNEPAcceptConnection();
            try {
                macClientConnection.ssTCPServerSocket = new ServerSocket(macClientConnection.iTCPPort);
                macClientConnection.ssTCPServerSocket.setSoTimeout(0);
				//lServerOffsetFromNTPFromMain = MNEPUtilities.calculateTimeDifferenceBetweenNTPAndLocal();
                for(;;) {		
                System.out.println("Server socket created on port - "+macClientConnection.iTCPPort+", waiting for connection...");
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
    
}
