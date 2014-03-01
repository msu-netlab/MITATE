import java.io.ObjectInputStream;
import java.io.ByteArrayInputStream;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.Arrays;
import java.io.Serializable;
import java.sql.*;

class TransferMetrics implements Serializable {

	public static final long serialVersionUID = -4426938724709265922L;
	long[] laLatencies;
	int[] iaNoOfBytes;
	float[] faThroughput;	
    float fLatencyConfInterval;
    float fThroughpuConfInterval;
    
    TransferMetrics() {}
	
	TransferMetrics(long[] laLatencies, int[] iaNoOfBytes, float[] faThroughput) {
		this.laLatencies = laLatencies;
		this.iaNoOfBytes =  iaNoOfBytes;
		this.faThroughput = faThroughput;		
	}
}

public class MITATE_Per_Packet_Network_Metrics {
	
	static String sUsername = "m_user";;
	static String sPassword = "";
	static String sDatabaseServerName = "localhost";
	static String sDatabaseName = "m_schema";
	static String sDatabaseURL = "jdbc:mysql:/"+"/"+sDatabaseServerName+":3306/"+sDatabaseName;
	static Connection cDatabaseConnection = null;

	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException{
		try {
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
		} 
		catch (Exception e) {
           System.err.println("error - "+e.getMessage());
           e.printStackTrace();
		}
		cDatabaseConnection = getDBConnection();
		Statement readTransferObject, packetMetrics, getPacketCount = null;
		readTransferObject = packetMetrics = cDatabaseConnection.createStatement();
		ResultSet rs = readTransferObject.executeQuery("SELECT tm.transferid, tm.udppacketmetrics, tm.tcppacketmetrics, tr.type FROM transfermetrics tm, transfer tr where tm.transferid = tr.transferid;");
		while(rs.next()){
			int transferid = rs.getInt("transferid");
			String getTCPTransferObjects = rs.getString("tcppacketmetrics");
			String getUDPTransferObjects = rs.getString("udppacketmetrics");
			int transferType = rs.getInt("type");
			String ObjectToDecode = null;
			if(transferType == 1)
				ObjectToDecode = getUDPTransferObjects;
			else if(transferType == 2)
				ObjectToDecode = getTCPTransferObjects;
			TransferMetrics t = (TransferMetrics)new ObjectInputStream(new ByteArrayInputStream(DatatypeConverter.parseBase64Binary(getTCPTransferObjects))).readObject();
			long[] laTransferLatency = t.laLatencies;
			float[] faTransferThroughput = t.faThroughput;
			for(int i = 1; i <= laTransferLatency.length; i++  ) {
				packetMetrics.addBatch("replace into packetmetrics (transferid, packetid, throughput, latency) values (" + transferid + ", " + i + ", " + faTransferThroughput[i -1] + ", " + laTransferLatency[i - 1] + ")");
			}
		}
		packetMetrics.executeBatch();
		cDatabaseConnection.close();
		System.out.println("The table 'packetmetrics' has been now populated with per packet network metrics.");
	}
	
	public static Connection getDBConnection() {
		try {
			cDatabaseConnection = DriverManager.getConnection(sDatabaseURL, sUsername, sPassword);
			return cDatabaseConnection;
		} catch(Exception e) {
			System.err.println("error - "+e.getMessage());
			e.printStackTrace();
		}
		return null;
	}
	
	public static void closeDBConnection() {
		if (cDatabaseConnection != null) {
			try {
				cDatabaseConnection.close ();
			}
			catch (Exception e) { 
				System.err.println("error - "+e.getMessage());
				e.printStackTrace();
			}
		}    
	}
}