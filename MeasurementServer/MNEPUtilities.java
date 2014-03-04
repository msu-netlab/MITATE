import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.Arrays;
import java.lang.reflect.Array;

public class MNEPUtilities {
    
    private static String TAG = "MNEPUtilities";
    
    private static String sUsername;
    private static String sPassword;
    private static String sDatabaseURL;
    private static String sDatabaseName;
    private static String sDatabaseServerName;
    public static Connection cDatabaseConnection = null;
    
    public static long lServerOffsetWithNTP = 0;
	
    static {
       sUsername = "";
       sPassword = "";
       sDatabaseServerName = "";
      
       sDatabaseName = "";
       
       sDatabaseURL = "jdbc:mysql:/"+"/"+sDatabaseServerName+":3306/"+sDatabaseName;
       try {
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
       } catch (Exception e) {
           System.err.println(TAG+" : @static : error - "+e.getMessage());
           e.printStackTrace();
       }
    }
    
    // to create a database connection
    public Connection getDBConnection() {
        try {
            cDatabaseConnection = DriverManager.getConnection(sDatabaseURL, sUsername, sPassword);
            System.out.println ("Database connection created");
            return cDatabaseConnection;
        } catch(Exception e) {
           System.err.println(TAG+" : @getConnection : error - "+e.getMessage());
           e.printStackTrace();
        }
        return null;
    }
	
    // to close a database connection
    public void closeDBConnection() {
        if (cDatabaseConnection != null) {
           try {
               cDatabaseConnection.close ();
               System.out.println ("Database connection closed");
           }
           catch (Exception e) { 
              System.err.println(TAG+" : @closeDBConnection : error - "+e.getMessage());
              e.printStackTrace();
           }
       }
    
    }
    
    // to calculate throughtput in kilobytes per second
    public float toKbps(int bytes, int msecs){
        float result = 0;
        try {
            result = (float)((toKb(bytes))/(msecs/1000.0));
        } catch (Exception e) {
              System.err.println(TAG+" : @toKbps : error - "+e.getMessage());
              e.printStackTrace();
        }
        return result;
    } 
    
    // to convert bytes to kilobytes
    public float toKb(long bytes){
        return (float)((bytes * 8)/1000.0);
    }
 
    //creates a Timestamp array from a String array of long values
    public long[] toTimeArray(String timesStr){
        //remove extra characters and split into array
        timesStr = timesStr.trim().substring(1, timesStr.length()-1);
        String[] timestamps = timesStr.split(",");

        long[] times = new long[timestamps.length];
        int i = 0;

        for (String timeStr: timestamps){

            times[i] = Long.parseLong(timeStr.trim());
            i++;
        }       
        return times;
    }
    
    public int[] toNumberOfBytesArray(String timesStr){
        //remove extra characters and split into array
        timesStr = timesStr.trim().substring(1, timesStr.length()-1);
        String[] timestamps = timesStr.split(",");

        int[] times = new int[timestamps.length];
        int i = 0;

        for (String timeStr: timestamps){

            times[i] = Integer.parseInt(timeStr.trim());
            i++;
        }       
        return times;
    }
    
    public float[] calculateThroughput(long[] laLatencies, int[] iaBytes, int iPacketDelay) {
    	float[] faThroughput = new float[laLatencies.length];
    	for(int i=0; i<laLatencies.length; i++) {
    		try {
    			faThroughput[i] = (float)((iaBytes[i]/1000.0) * 8) / (float)((laLatencies[i] + iPacketDelay)/1000.0);
    		} catch(Exception e) {
    			System.out.println(TAG+" : error : "+e.getMessage());
    		}
    	}
    	return faThroughput;
    }
    
    public int getSum(long[] values){
        int total = 0;
        for (long value: values) {
            total += Math.abs(value);
        }
        return total;
    }   
    
    public float getSumThroughput(float[] values){
        int total = 0;
        for (float value: values) {
            total += value;
        }
        return total;
    }  
    
    public long calculateTimeDifferenceBetweenNTPAndLocal() {
        long lNTPTime = 0;
		String sNTPServer = "us.pool.ntp.org";
		int ntpSubDomain = 0;
        SNTPClient client = new SNTPClient();   
		while((lNTPTime + "").length() < 13 && ntpSubDomain <= 4 ) {
			if(ntpSubDomain > 3) 
				ntpSubDomain = 0;
			try {
				if (client.requestTime(ntpSubDomain + "." + sNTPServer, 4000)) {
					lNTPTime = client.getNtpTime() + ((long)Math.ceil(System.nanoTime() * Math.pow(10, -6))) - client.getNtpTimeReference();
					System.out.println(lNTPTime + "****" + System.currentTimeMillis());
				}
			}
			catch (Exception e) {
				e.printStackTrace();
				ntpSubDomain = ntpSubDomain + 1;
			}
		}
        long lSystemTime = System.currentTimeMillis();
        lServerOffsetWithNTP = lSystemTime - lNTPTime;
        return lServerOffsetWithNTP;
    }
	
	public float calculateConfInterval(long[] laLatencies, float[] faThroughput, String tag) {
		float fStandardDeviation = 0.0f;
		float fValueToReturn = 0.0f;
		float fSum;
		if(tag.equals("latencyConfInterval")) {
			float fLatencyConfInterval;
			long[] templatencies = laLatencies.clone();
			Arrays.sort(templatencies);
			int elim = 0;
			while(elim < templatencies.length) {
				if(templatencies[elim] == 0)
					elim = elim + 1;
				else
					break;
			}
			float fLatencyMean = getSum(laLatencies) / (float)(laLatencies.length - elim + 1);
			fSum = 0.0f;
			for(int i=elim; i<templatencies.length; i++) {
				fSum += (float)Math.pow((templatencies[i] - fLatencyMean), 2);
			}
			fStandardDeviation = (float)Math.sqrt(fSum / (laLatencies.length - elim + 1));
			fLatencyConfInterval = (float)(1.96 * fStandardDeviation / (Math.sqrt(laLatencies.length - elim + 1)));	
			fValueToReturn = fLatencyConfInterval;
		}
		if(tag.equals("throughputConfInterval")) {
			float fThroughpuConfInterval;
			float[] tempthroughputs = faThroughput.clone();
			Arrays.sort(tempthroughputs);
			int elim = 0;
			while(elim < tempthroughputs.length) {
				if(tempthroughputs[elim] == 0)
					elim = elim + 1;
				else
					break;
			}
			float fThroughputMean = getSumThroughput(faThroughput) / (float)(faThroughput.length - elim + 1);
			fSum = 0.0f;
			for(int i=elim; i<tempthroughputs.length; i++) {
				fSum += (float)Math.pow((tempthroughputs[i] - fThroughputMean), 2);
			}
			fStandardDeviation= (float)Math.sqrt(fSum / (faThroughput.length - elim + 1));
			fThroughpuConfInterval = (float)(1.96 * fStandardDeviation / (Math.sqrt(faThroughput.length - elim + 1)));
			fValueToReturn = fThroughpuConfInterval;
		}
		return fValueToReturn;
	}
}
