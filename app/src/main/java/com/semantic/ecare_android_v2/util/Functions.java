package com.semantic.ecare_android_v2.util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Locale;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.SensorState;

import net.newel.android.Log;
import android.content.Context;
import android.provider.Settings.Secure;
/*!
Classe gérant l'accé au numéro de de device
Toute les methode sont statique.
 */
public class Functions {
	private static String CLASSNAME="fr.semantic.ecare.android.util.Functions";
	
	
	public static int getDeviceName(SensorState state){
		int deviceName;
		if(ServiceEcare.SENSOR_TYPE.containsKey(state.getMacAddr().toUpperCase(Locale.getDefault())))
		{
			int deviceType = ServiceEcare.SENSOR_TYPE.get(state.getMacAddr().toUpperCase(Locale.getDefault()));
			deviceName=Constants.SENSOR_NAME.get(deviceType);
		}
		else
		{
			deviceName=R.string.sensor_name_undefined;
		}
		return deviceName;
	}	

	public static String getDeviceUniqId(Context context) {
		return Secure.getString(context.getContentResolver(),Secure.ANDROID_ID);
	}
	

	public static Boolean execCommand(String command) {
		Log.i(Constants.TAG, CLASSNAME+" Execution de la commande "+command);
        try {
            Runtime rt = Runtime.getRuntime();
            Process process = rt.exec("su"); 
            DataOutputStream os = new DataOutputStream(process.getOutputStream()); 
            os.writeBytes(command + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (IOException e) {
        	Log.e(Constants.TAG,e);
            return false;
        } catch (InterruptedException e) {
        	Log.e(Constants.TAG,e);
            return false;
        }
        return true; 
    }
	
	
    public static double round(double value, int decimalPlace){
      double power_of_ten = 1;
      double fudge_factor = 0.05;
      while (decimalPlace-- > 0) {
         power_of_ten *= 10.0d;
         fudge_factor /= 10.0d;
      }
      return Math.round((value + fudge_factor)* power_of_ten)  / power_of_ten;
    }
}
