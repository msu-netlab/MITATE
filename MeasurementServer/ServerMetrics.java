import java.io.Serializable;

public class ServerMetrics {
	
	int iTransferId;
	long[] laUDPPacketReceivedTimestamps;
    int[] iaUDPBytes;
    int iUDPTotalBytesReceivedFromClient;
    long[] laTCPPacketReceivedTimestamps;
    int[] iaTCPBytes;
    int iTCPTotalBytesReceivedFromClient;
    int iTCPTotalBytesSentToClient;
    int iTCPBytes;
    int iUDPBytes;
    int iUplinkOrDownlink;
    int iTransactionId;
	public int getiTransactionId() {
		return iTransactionId;
	}
	public void setiTransactionId(int iTransactionId) {
		this.iTransactionId = iTransactionId;
	}
	public int getiUplinkOrDownlink() {
		return iUplinkOrDownlink;
	}
	public void setiUplinkOrDownlink(int iUplinkOrDownlink) {
		this.iUplinkOrDownlink = iUplinkOrDownlink;
	}
	public int getiTCPBytes() {
		return iTCPBytes;
	}
	public void setiTCPBytes(int iTCPBytes) {
		this.iTCPBytes = iTCPBytes;
	}
	public int getiUDPBytes() {
		return iUDPBytes;
	}
	public void setiUDPBytes(int iUDPBytes) {
		this.iUDPBytes = iUDPBytes;
	}
	public int getiTransferId() {
		return iTransferId;
	}
	public void setiTransferId(int iTransferId) {
		this.iTransferId = iTransferId;
	}
	public long[] getLaUDPPacketReceivedTimestamps() {
		return laUDPPacketReceivedTimestamps;
	}
	public void setLaUDPPacketReceivedTimestamps(
			long[] laUDPPacketReceivedTimestamps) {
		this.laUDPPacketReceivedTimestamps = laUDPPacketReceivedTimestamps;
	}
	public int[] getIaUDPBytes() {
		return iaUDPBytes;
	}
	public void setIaUDPBytes(int[] iaUDPBytes) {
		this.iaUDPBytes = iaUDPBytes;
	}
	public int getiUDPTotalBytesReceivedFromClient() {
		return iUDPTotalBytesReceivedFromClient;
	}
	public void setiUDPTotalBytesReceivedFromClient(
			int iUDPTotalBytesReceivedFromClient) {
		this.iUDPTotalBytesReceivedFromClient = iUDPTotalBytesReceivedFromClient;
	}
	public long[] getLaTCPPacketReceivedTimestamps() {
		return laTCPPacketReceivedTimestamps;
	}
	public void setLaTCPPacketReceivedTimestamps(
			long[] laTCPPacketReceivedTimestamps) {
		this.laTCPPacketReceivedTimestamps = laTCPPacketReceivedTimestamps;
	}
	public int[] getIaTCPBytes() {
		return iaTCPBytes;
	}
	public void setIaTCPBytes(int[] iaTCPBytes) {
		this.iaTCPBytes = iaTCPBytes;
	}
	public int getiTCPTotalBytesReceivedFromClient() {
		return iTCPTotalBytesReceivedFromClient;
	}
	public void setiTCPTotalBytesReceivedFromClient(
			int iTCPTotalBytesReceivedFromClient) {
		this.iTCPTotalBytesReceivedFromClient = iTCPTotalBytesReceivedFromClient;
	}
	public int getiTCPTotalBytesSentToClient() {
		return iTCPTotalBytesSentToClient;
	}
	public void setiTCPTotalBytesSentToClient(int iTCPTotalBytesSentToClient) {
		this.iTCPTotalBytesSentToClient = iTCPTotalBytesSentToClient;
	}
    
}
