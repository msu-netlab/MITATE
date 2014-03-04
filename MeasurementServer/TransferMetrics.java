import java.io.Serializable;

public class TransferMetrics implements Serializable {
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