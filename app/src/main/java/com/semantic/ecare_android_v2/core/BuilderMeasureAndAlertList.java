package com.semantic.ecare_android_v2.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import net.newel.android.Log;
import android.app.Service;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;

import com.semantic.ecare_android_v2.core.listener.BuilderMeasureAndAlertListListener;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.DataBaseConnector;
import com.semantic.ecare_android_v2.util.NetworkStatus;

/**!
 * Classe Permettant d'aller chercher dans lA BDD les information sur les mesure de façon asyncrone
 */
public class BuilderMeasureAndAlertList {

	private Handler handler;
	private final Collection<BuilderMeasureAndAlertListListener> builderMeasureAndAlertListListeners = new ArrayList<BuilderMeasureAndAlertListListener>();
	private Service service;
	private NetworkStatus networkStatus;
	private ArrayList<Measure> measures = new ArrayList<Measure>();
	private ArrayList<Alert> alerts = new ArrayList<Alert>();
	private String CLASSNAME=this.getClass().getName();

	private Runnable runFireGetMeasureAndAlertListFromInternet = new Runnable(){
		@Override
		public void run(){
			fireRunFireGetMeasureAndAlertListFromInternet();
		}
	};

	private Runnable runFireEndOfUpdate = new Runnable(){  
		@Override
		public void run(){
			fireEndOfUpdate(measures, alerts);
		}  
	}; 

	private Runnable runFireEndOfGetListFromLocal = new Runnable(){  
		@Override
		public void run(){
			fireEndOfGetListFromLocal(measures, alerts);
		}  
	};  

	private Runnable runFireErrorNodata = new Runnable(){  
		@Override
		public void run(){
			fireErrorNoData();
		}  
	};  

	public BuilderMeasureAndAlertList(Service service) {
		handler = new Handler();
		this.service=service;
	}

	public void get(){
		//get list from Internet or from local sqLite database, and returns it with listener once done.
		Log.i(Constants.TAG,CLASSNAME+" Build (update from internet or get from local SQLite DB) Measure and Alert list");

		new Thread(){
			public void run(){
				Log.i(Constants.TAG,CLASSNAME+" Internet Nok, searching data in local SQLite DB");
				//Check if there is data from local DB

				getMeasureAndAlertFromDataBase();
			}
		}.start();
	}

	private void getMeasureAndAlertFromDataBase(){
		networkStatus = new NetworkStatus(service);
		int networkState = networkStatus.checkNetworkStatus();

		DataBaseConnector dbc = new DataBaseConnector(service);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			Cursor cursor_measure = db.query(Constants.TABLE_MESURE, new String[] { "id", "id_patient", "sensor", "date", "valeur", "synchronized", "note", "noteDate" }, null, null, null, null, null);
			Cursor cursor_alert = db.query(Constants.TABLE_ALERTE, new String[] { "id", "id_patient", "date", "mesure", "niveau", "message", "note", "noteDate" }, null, null, null, null, null);
			if (cursor_measure != null){
				if(cursor_measure.getCount()>=0){
					Log.i(Constants.TAG, CLASSNAME + "pas de problem, on charge les mesures depuis la base");

					measures = buildMeasuresFromDBD(cursor_measure);
				}
				cursor_measure.close();
			}
			if (cursor_alert != null){
				if(cursor_alert.getCount()>=0){
					Log.i(Constants.TAG, CLASSNAME + "pas de problem, on charge les alertes depuis la base");

					alerts = buildAlertsFromDBD(cursor_alert);
				}
				cursor_alert.close();
			}
			db.close();
		}
		dbc.close();

		if(measures.size()>0 && alerts.size()>0){
			handler.post(runFireEndOfGetListFromLocal);
		}else if ((networkState == NetworkStatus.WIFI) || (networkState == NetworkStatus.MOBILE)) {

			Log.i(Constants.TAG,CLASSNAME+" Internet OK, initialisation de la liste des mesures et des alertes");
			handler.post(runFireGetMeasureAndAlertListFromInternet);

		}
		else
		{
			// non initialise, et pas d'internet
			Log.i(Constants.TAG,CLASSNAME+" Internet NOK, Telechargment de la liste des patients non possible");
			//handler.post(runFireErrorNoInternet);
		}
	}

	public ArrayList<Measure> buildMeasuresFromDBD(Cursor cursor)
	{
		Log.i(Constants.TAG, CLASSNAME + " build measure list from dbd");
		ArrayList<Measure> measureList = new ArrayList<Measure>();
		if(cursor.getCount()>0){
			cursor.moveToFirst();
			do{
				Measure measure = new Measure(cursor.getInt(0),cursor.getInt(2), new Date(cursor.getLong(3)), cursor.getString(1), cursor.getDouble(4), (cursor.getInt(5)==0)?false:true, cursor.getString(6), cursor.getString(7));
				measureList.add(measure);
			} while(cursor.moveToNext());
		}
		return measureList;
	}

	public ArrayList<Alert> buildAlertsFromDBD(Cursor cursor)
	{
		Log.i(Constants.TAG, CLASSNAME + "build alert list from dbd");
		ArrayList<Alert> alertList = new ArrayList<Alert>();
		if(cursor.getCount()>0){
			cursor.moveToFirst();
			do{
				Alert alert = new Alert(cursor.getInt(0), cursor.getString(1), new Date(cursor.getLong(2)) ,cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6), cursor.getString(7));
				alertList.add(alert);
			} while(cursor.moveToNext());
		}
		return alertList;
	}

	public void addListener(BuilderMeasureAndAlertListListener listener){
		builderMeasureAndAlertListListeners.add(listener);
	}

	private void fireEndOfUpdate(ArrayList<Measure> measureList, ArrayList<Alert> alertList) {
		for (BuilderMeasureAndAlertListListener listener : builderMeasureAndAlertListListeners) {
			listener.endOfUpdateMeasureAndAlertList(measureList, alertList);
		}
	}
	private void fireEndOfGetListFromLocal(ArrayList<Measure> measureList, ArrayList<Alert> alertList) {
		for (BuilderMeasureAndAlertListListener listener : builderMeasureAndAlertListListeners) {
			listener.endOfGetMeasureAndAlertListFromLocal(measureList, alertList);
		}
	}
	private void fireErrorNoData() {
		for (BuilderMeasureAndAlertListListener listener : builderMeasureAndAlertListListeners) {
			listener.errorNoMeasureAndAlertData();
		}
	}

	private void fireRunFireGetMeasureAndAlertListFromInternet()
	{
		for (BuilderMeasureAndAlertListListener listener : builderMeasureAndAlertListListeners) {
			listener.runFireGetMeasureAndAlertListFromInternet();
		}
	}

}
