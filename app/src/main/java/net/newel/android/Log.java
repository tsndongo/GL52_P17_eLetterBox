package net.newel.android;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Calendar;
import java.util.Date;

import android.os.Environment;

/***** ATTENTION ****************
 * This class required usage permission : android.permission.WRITE_EXTERNAL_STORAGE
 *
 */



public class Log{
	public static boolean DEBUG=true;
	public static String LOG_DIRECTORY="/log_ecare";
	private static FileOutputStream file;
	private static Date last_modified=new Date();
	
	private static final String LEVEL_DEBUG="D";
	private static final String LEVEL_VERBOSE="V";
	private static final String LEVEL_ERROR="E";
	private static final String LEVEL_INFO="I";
	private static final String LEVEL_WARN="W";
	
	
	
	public static void d(String tag, String message){
		if(DEBUG){
			android.util.Log.d(tag,message);
			logToFile(LEVEL_DEBUG, tag, message);
		}
	}
	public static void d(String tag, Exception e){
		if(DEBUG){
			e.printStackTrace();
			appendToFile(LEVEL_DEBUG, tag, e);
		}
	}
	public static void d(String tag, String message, Throwable tr){
		if(DEBUG){
			android.util.Log.d(tag,message, tr);
			logToFile(LEVEL_DEBUG, tag, message+" / "+tr.getMessage());
		}
	}
	
	public static void e(String tag, String message){
		if(DEBUG){
			android.util.Log.e(tag,message);
			logToFile(LEVEL_ERROR, tag, message);
		}
	}
	public static void e(String tag, Exception e){
		if(DEBUG){
			e.printStackTrace();
			appendToFile(LEVEL_ERROR, tag, e);
		}
	}
	public static void e(String tag, String message, Throwable tr){
		if(DEBUG){
			android.util.Log.e(tag,message, tr);
			logToFile(LEVEL_ERROR, tag, message+" / "+tr.getMessage());
		}
	}
	
	public static void i(String tag, String message){
		if(DEBUG){
			android.util.Log.i(tag,message);
			logToFile(LEVEL_INFO, tag, message);
		}
	}
	public static void i(String tag, Exception e){
		if(DEBUG){
			e.printStackTrace();
			appendToFile(LEVEL_INFO, tag, e);
		}
	}
	public static void i(String tag, String message, Throwable tr){
		if(DEBUG){
			android.util.Log.i(tag,message, tr);
			logToFile(LEVEL_INFO, tag, message+" / "+tr.getMessage());
		}
	}
	
	public static void v(String tag, String message){
		if(DEBUG){
			android.util.Log.v(tag,message);
			logToFile(LEVEL_VERBOSE, tag, message);
		}
	}
	public static void v(String tag, Exception e){
		if(DEBUG){
			e.printStackTrace();
			appendToFile(LEVEL_VERBOSE, tag, e);
		}
	}
	public static void v(String tag, String message, Throwable tr){
		if(DEBUG){
			android.util.Log.v(tag,message, tr);
			logToFile(LEVEL_VERBOSE, tag, message+" / "+tr.getMessage());
		}
	}
	
	public static void w(String tag, String message){
		if(DEBUG){
			android.util.Log.w(tag,message);
			logToFile(LEVEL_WARN, tag, message);
		}
	}
	public static void w(String tag, Exception e){
		if(DEBUG){
			e.printStackTrace();
			appendToFile(LEVEL_WARN, tag, e);
		}
	}
	public static void w(String tag, Throwable tr){
		if(DEBUG){
			android.util.Log.w(tag, tr);
			logToFile(LEVEL_WARN, tag, tr.getMessage());
		}
	}
	public static void w(String tag, String message, Throwable tr){
		if(DEBUG){
			android.util.Log.w(tag,message, tr);
			logToFile(LEVEL_WARN, tag, message+" / "+tr.getMessage());
		}
	}
	
	
	

	
	
	
	
	
	
	//methods required by writing log into file
	
	private static String getLogUri(){
		String filePathString=Environment.getExternalStorageDirectory()+LOG_DIRECTORY+"/";
		File filePath = new File(filePathString);
		if(!filePath.isDirectory()){
        	filePath.mkdirs();
        }
		
        return filePathString;
	}
	
	private static String getFileName(){
		int day,month,year,hour,minute,second;
		
		String day_str, month_str;
		Calendar cal = Calendar.getInstance();
		day = cal.get(Calendar.DAY_OF_MONTH);
		if(day<10){day_str="0"+day;}else{day_str=String.valueOf(day);}
		month = 1+cal.get(Calendar.MONTH);
		if(month<10){month_str="0"+month;}else{month_str=String.valueOf(month);}
		year = cal.get(Calendar.YEAR);
		hour = cal.get(Calendar.HOUR_OF_DAY);
		minute = cal.get(Calendar.MINUTE);
		second = cal.get(Calendar.SECOND);
		
		String hour_str, minute_str, second_str;
		if(hour<10){hour_str="0"+hour;}else{hour_str=String.valueOf(hour);}
		if(minute<10){minute_str="0"+minute;}else{minute_str=String.valueOf(minute);}
		if(second<10){second_str="0"+second;}else{second_str=String.valueOf(second);}
		
		return year + "_" + month_str + "_" + day_str + "-" + hour_str + "_" + minute_str + "_" + second_str + ".txt";
	}
	
	private static void openFile(){
		//creating and opening file
		String filePath=getLogUri();//getString FilePath (and create the directory if not created)
		String fileName=getFileName();
		
		try {
			file = new FileOutputStream(new File(filePath,fileName),false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		
	}
	private static void logToFile(String level, String tag, String message){
		//open file if not opened
		if(!isLastModifiedToday()){
			restartNewLog();
		}
		if(file==null){
			openFile();
		}
		String date = getNow();
		String dataLine = level + "\t" + date + "\t\t" + tag + "\t\t" + message + "\n";
		
		try {
			file.write(dataLine.getBytes());
			file.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NullPointerException e) {
			e.printStackTrace();
		}
	}

	
	
	private static void appendToFile(String level, String tag, Exception e) {
		//open file if not opened
		if(file==null){
			openFile();
		}
		String date = getNow();
		String dataLine = level + "\t" + date + "\t\t" + tag + "\n";
		
		try {
			file.write(dataLine.getBytes());
			file.flush();
		} catch (IOException exc) {
			exc.printStackTrace();
		}
		
		e.printStackTrace(new PrintStream(file));
		
		
		
	}
	
	private static String getNow() {

		//Appel à ce fichier : Mémorisation de cette date comme la dernière action effectuée sur ce fichier

		
		int day,month,year,hour,minute,second,milisecond;
		String day_str, month_str, hour_str, minute_str, second_str, milisecond_str;
		
		Calendar cal = Calendar.getInstance();
		day = cal.get(Calendar.DAY_OF_MONTH);
		if(day<10){day_str="0"+day;}else{day_str=String.valueOf(day);}
		month = 1+cal.get(Calendar.MONTH);
		if(month<10){month_str="0"+month;}else{month_str=String.valueOf(month);}
		year = cal.get(Calendar.YEAR);
		hour = cal.get(Calendar.HOUR_OF_DAY);
		minute = cal.get(Calendar.MINUTE);
		second = cal.get(Calendar.SECOND);
		milisecond = cal.get(Calendar.MILLISECOND);
		
		if(hour<10){hour_str="0"+hour;}else{hour_str=String.valueOf(hour);}
		if(minute<10){minute_str="0"+minute;}else{minute_str=String.valueOf(minute);}
		if(second<10){second_str="0"+second;}else{second_str=String.valueOf(second);}
		if(milisecond<10){milisecond_str="00"+milisecond;}else if(milisecond<100){milisecond_str="0"+milisecond;}else{milisecond_str=String.valueOf(milisecond);}
		
		last_modified = cal.getTime();
		
		return day_str+"/"+month_str+"/"+year + " " + hour_str + ":" + minute_str + ":" + second_str + "." + milisecond_str;
	}
	
	
	public static void restartNewLog(){
		closeFile();
		openFile();
		
	}
	
	public static void closeFile(){
		//closing file
		
    	try {
			file.close();
			file=null;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private static boolean isLastModifiedToday(){
		Date now = new Date();
        Calendar cal = Calendar.getInstance();
        cal.setTime(now);
        int now_day = cal.get(Calendar.DAY_OF_YEAR);
        
        cal.setTime(last_modified);
        int last_day = cal.get(Calendar.DAY_OF_YEAR);

		return now_day==last_day;
	}
}
