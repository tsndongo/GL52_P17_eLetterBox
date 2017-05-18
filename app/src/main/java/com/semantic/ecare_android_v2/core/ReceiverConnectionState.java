package com.semantic.ecare_android_v2.core;

import com.semantic.ecare_android_v2.ui.SplashScreen;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.NetworkStatus;

import net.newel.android.Log;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReceiverConnectionState extends BroadcastReceiver {
	private NetworkStatus networkStatus;
	private String CLASSNAME=this.getClass().getName();
	private ServiceEcare service;
	private static boolean firstTime = true;
	
	public ReceiverConnectionState(ServiceEcare service){
		this.service=service;
		Log.i(Constants.TAG, CLASSNAME+" Initialisation ConnectivityChangeReceiver");
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		
		networkStatus = new NetworkStatus(context);
		int networkState = networkStatus.checkNetworkStatus();
		Log.i(Constants.TAG, CLASSNAME+" Changement connectivity");
        SplashScreen splashScreen = new SplashScreen();
		if (((networkState == NetworkStatus.WIFI) || (networkState == NetworkStatus.MOBILE)) && firstTime == false) {
			//Connexion au service pour lancer l'update
			Log.i(Constants.TAG, CLASSNAME+" Pr�sence de connexion");
        	
			Log.e(Constants.TAG, CLASSNAME + " Connection internet detect� !!!!!!!!");
            service.buildConfigurationList(); 

  	        if(service.getLaunchComplete()){
  	        	Log.i(Constants.TAG, CLASSNAME+" Lancement de la synchronisation sur changement de statut r�seau");
  	        	//service.synchronize();
  	        }else{
  	        	Log.i(Constants.TAG, CLASSNAME+" Lancement de l'application non termin�e !");
  	        }
		}
		else
		{
			firstTime = false;//Sinon : pas de connexion
			Log.e(Constants.TAG, CLASSNAME + "First time !");
		}
	}


}
