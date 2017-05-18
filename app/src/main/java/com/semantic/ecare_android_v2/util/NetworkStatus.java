package com.semantic.ecare_android_v2.util;

import net.newel.android.Log;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/*
 * ATTENTION CETTE CLASSE REQUIERT L'UTILISATION
 * DE LA PERMISSION android.permission.ACCESS_NETWORK_STATE
 */


public class NetworkStatus {

	
	public static int NO_NETWORK = 1;
	public static int WIFI = 2;
	public static int MOBILE = 3;

	private Context context;

	public NetworkStatus(Context ctx) {
		this.context = ctx;
	}

	public int checkNetworkStatus() {
		int result = 0;
		try{
			ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo mobile = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				
			if (wifi!=null && wifi.isConnected()) {
				result = WIFI;
			} else if (mobile!=null && mobile.isAvailable()) {
				result = MOBILE;
			} else {
				result = NO_NETWORK;
			}
	
			return result;
		}catch(Exception e){
			Log.e(Constants.TAG, e);
			return NO_NETWORK;
		}
	}

}
