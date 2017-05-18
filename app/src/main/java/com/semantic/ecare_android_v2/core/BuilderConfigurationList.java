package com.semantic.ecare_android_v2.core;

import java.util.ArrayList;
import java.util.Collection;

import net.newel.android.Log;
import android.app.Service;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.semantic.ecare_android_v2.core.listener.BuilderConfigurationListListener;
import com.semantic.ecare_android_v2.object.ConfigurationList;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.DataBaseConnector;
import com.semantic.ecare_android_v2.util.NetworkStatus;

public class BuilderConfigurationList {
	private Handler handler;
	private final Collection<BuilderConfigurationListListener> builderConfigurationListListeners = new ArrayList<BuilderConfigurationListListener>();
	private Service service;
	private NetworkStatus networkStatus;
	private ConfigurationList configurations = new ConfigurationList();
	private String CLASSNAME=this.getClass().getName();
	
	private Runnable runFireEndOfUpdate = new Runnable(){  
		@Override
		public void run(){
			fireEndOfUpdate(configurations);
		}  
	};

	//Method laucnh when it's intaciate
	private Runnable runFireEndOfGetListFromLocal = new Runnable(){  
		@Override
		public void run(){
			fireEndOfGetListFromLocal(configurations);
		}  
	};  
	
	private Runnable runFireGetListFromInternet = new Runnable(){
		@Override
		public void run(){
			fireRunFireGetListFromInternet();
		}
	};
	
	private Runnable runFireErrorNoInternet = new Runnable(){
		@Override
		public void run(){
			fireErrorNoInternet();
		}
	};	
	
	private Runnable runFireErrorNodata = new Runnable(){  
		@Override
		public void run(){
			fireErrorNoData();
		}  
	};  
	

	public BuilderConfigurationList(Service service) {
		handler = new Handler(); // Handler permet de metre a jour l'IHM
		this.service=service;
		
	}
	
	

	public void get(){
		//get list from Internet or from local sqLite database, and returns it with listener once done.
		Log.i(Constants.TAG,CLASSNAME+" Build (update from internet or get from local SQLite DB) configuration list");
		
		new Thread(){
			public void run(){
					//Check if there is data from local DB
					getConfigurationFromDatabase();
			}
		}.start();
	}
	

	
	private void getConfigurationFromDatabase(){
		//Check if there is Internet
		networkStatus = new NetworkStatus(service);
		int networkState = networkStatus.checkNetworkStatus();
				
		DataBaseConnector dbc = new DataBaseConnector(service);
		DataBaseConnector.getInstance(service);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			Cursor cursor = db.query(Constants.TABLE_CONFIGURATION, new String[] { "portWrite", "portHL7", "init", "active", "type", "idUserService","gender", "blocInfo", "nameOrEstablishment" , "surnameOrService", "canUpdate"}, null, null, null, null, null);
			if (cursor != null){
				cursor.moveToFirst();
				configurations= new ConfigurationList();
				configurations.setPortWrite(cursor.getInt(0));
				configurations.setPortHl7(cursor.getInt(1));
				configurations.setInitialise(cursor.getInt(2));
				configurations.setActive(cursor.getInt(3));
				configurations.setType(cursor.getInt(4));
				configurations.setIdPatientOrService(cursor.getString(5));
				configurations.setGender(cursor.getInt(6));
				configurations.setBlocInfo(cursor.getString(7));
				configurations.setNomOuEtablissement(cursor.getString(8));
				configurations.setPrenomOuService(cursor.getString(9));
				configurations.setCanUpdate(cursor.getInt(10));
				if(configurations!=null){

					if(configurations.getInitialise()==0)
					{
						// la tablette na pas encore ete initialisee

						if ((networkState == NetworkStatus.WIFI) || (networkState == NetworkStatus.MOBILE)) {

							Log.i(Constants.TAG,CLASSNAME+" Internet OK, initialisation");
							handler.post(runFireGetListFromInternet);
							//init usine
						}
						else
						{
							// non initialise, et pas dinternet
							Log.i(Constants.TAG,CLASSNAME+" Internet NOK, initialisation non possible");
							handler.post(runFireErrorNoInternet);
					
						}
					}
					else{
						// tablette deja initialisee
						//faire un connect
						if ((networkState == NetworkStatus.WIFI) || (networkState == NetworkStatus.MOBILE)) {

							Log.i(Constants.TAG,CLASSNAME+" Internet OK, mise a jour");
							handler.post(runFireEndOfGetListFromLocal);
							//connect
						}
						else
						{
							// pas dinternet, lancer lapplication en locale
							handler.post(runFireEndOfGetListFromLocal);
						}
					}

				}else{
					// probleme contacter le service technique, base de donnees defectueuse
					handler.post(runFireErrorNodata);
				}
				cursor.close();
				//Once finishing, all configurations are loaded memory
			}
			db.close();
		}
		dbc.close();

	}
	
	public void addListener(BuilderConfigurationListListener listener){
		builderConfigurationListListeners.add(listener);
	}
	
	private void fireRunFireGetListFromInternet()
	{
		for (BuilderConfigurationListListener listener : builderConfigurationListListeners) {
			listener.runFireGetConfigListFromInternet();
		}
	}
	
	private void fireErrorNoInternet()
	{
		for (BuilderConfigurationListListener listener : builderConfigurationListListeners) {
			listener.runErrorNoInternet();
		}
	}
	
	private void fireEndOfUpdate(ConfigurationList list) {
		for (BuilderConfigurationListListener listener : builderConfigurationListListeners) {
			listener.endOfUpdateConfigurationList(list);
		}
	}
	private void fireEndOfGetListFromLocal(ConfigurationList list) {
		for (BuilderConfigurationListListener listener : builderConfigurationListListeners) {
			listener.endOfGetConfigurationListFromLocal(list);
		}
	}
	private void fireErrorNoData() {
		for (BuilderConfigurationListListener listener : builderConfigurationListListeners) {
			listener.errorNoConfigurationData();
		}
	}
}
