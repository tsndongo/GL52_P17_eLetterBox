package com.semantic.ecare_android_v2.util;


import java.util.Locale;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.object.SensorState;


public class FunctionsUI {
	//private static String CLASSNAME="fr.semantic.ecare.android.util.FunctionsUI";
	
	public static String genderIntToString(int gender){
		return gender==1 ? "M." : "Mme.";
	}
	
//	public static int getDeviceName(SensorState state){
//		int deviceName;
//		if(Constants.SENSOR_TYPE.containsKey(state.getMacAddr().toUpperCase(Locale.getDefault()))){
//			int deviceType = Constants.SENSOR_TYPE.get(state.getMacAddr().toUpperCase(Locale.getDefault()));
//			deviceName=Constants.SENSOR_NAME.get(deviceType);
//		}else{
//			deviceName=R.string.sensor_name_undefined;
//		}
//		return deviceName;
//	}
	
	public static int getDayModulo(int dayNum){
		if(dayNum>7){
			while(dayNum>7){
				dayNum=dayNum-7;
			}
		}else if(dayNum<1){
			while(dayNum<1){
				dayNum=dayNum+7;
			}
		}
		return dayNum;
	}
}
