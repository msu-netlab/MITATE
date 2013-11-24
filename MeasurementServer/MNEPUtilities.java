//package com.mitate;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.util.Arrays;

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
       sUsername = "mitate";
       sPassword = "Database4Mitate";
       sDatabaseServerName = "nl.cs.montana.edu";
      
       sDatabaseName = "mitate";
       
       sDatabaseURL = "jdbc:mysql:/"+"/"+sDatabaseServerName+":3306/"+sDatabaseName;
       try {
            Class.forName ("com.mysql.jdbc.Driver").newInstance ();
       } catch (Exception e) {
           System.err.println(TAG+" : @static : error - "+e.getMessage());
           e.printStackTrace();
       }
    }
    
    // to create a database connection
    public static Connection getDBConnection() {
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
    public static void closeDBConnection() {
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
    public static float toKBps(int bytes, int msecs){
        float result = 0;
        try {
            result = (float)((toKB(bytes)/(msecs/1000.0))*8);
        } catch (Exception e) {
              System.err.println(TAG+" : @toKBps : error - "+e.getMessage());
              e.printStackTrace();
        }
        return result;
    } 
    
    // to convert bytes to kilobytes
    public static float toKB(long bytes){
        return (float)(bytes/1024.0);
    }
 
    //creates a Timestamp array from a String array of long values
    public static long[] toTimeArray(String timesStr){
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
    
    public static int[] toNumberOfBytesArray(String timesStr){
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
    
    public static float[] calculateThroughput(long[] laLatencies, int[] iaBytes) {
    	float[] faThroughput = new float[laLatencies.length];
    	for(int i=0; i<laLatencies.length; i++) {
    		try {
    			faThroughput[i] = (float)iaBytes[i] / (float)laLatencies[i];
    		} catch(Exception e) {
    			System.out.println(TAG+" : error : "+e.getMessage());
    		}
    	}
    	return faThroughput;
    }
    
    public static int getSum(long[] values){
        int total = 0;
        for (long value: values) {
            total += Math.abs(value);
        }
        return total;
    }   
    
    public static float getSumThroughput(float[] values){
        int total = 0;
        for (float value: values) {
            total += value;
        }
        return total;
    }  
    
    public static long calculateTimeDifferenceBetweenNTPAndLocal(String sNTPServer) {
        long lNTPTime = 0;
        SNTPClient client = new SNTPClient();   
		try {
			while((lNTPTime + "").length() < 13) {
				if (client.requestTime(sNTPServer, 4000)) {
					lNTPTime = client.getNtpTime() + ((long)Math.ceil(System.nanoTime() * Math.pow(10, -6))) - client.getNtpTimeReference();
				}
				else
					Thread.sleep(5000);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
        long lSystemTime = System.currentTimeMillis();
        lServerOffsetWithNTP = lSystemTime - lNTPTime;
        return lServerOffsetWithNTP;
    }
}