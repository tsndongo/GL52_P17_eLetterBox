package com.semantic.ecare_android_v2.core;

import android.app.Service;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Handler;

import com.semantic.ecare_android_v2.core.listener.BuilderPatientListListener;
import com.semantic.ecare_android_v2.object.Patient;
import com.semantic.ecare_android_v2.object.UserConstant;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.DataBaseConnector;
import com.semantic.ecare_android_v2.util.NetworkStatus;

import net.newel.android.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BuilderPatientList {
	private Handler handler;
	private final Collection<BuilderPatientListListener> builderPatientListListeners = new ArrayList<BuilderPatientListListener>();
	private Service service;
	private NetworkStatus networkStatus;
	private ArrayList<Patient> patients = new ArrayList<Patient>();
	private String CLASSNAME=this.getClass().getName();
	private String serveurAdresse;
	private Runnable runFireGetPatientListFromInternet = new Runnable(){
		@Override
		public void run(){
			fireRunFireGetPatientListFromInternet();
		}
	};
	
	private Runnable runFireEndOfUpdate = new Runnable(){  
		@Override
		public void run(){
			fireEndOfUpdate(patients);
		}  
	}; 
	private Runnable runFireEndOfGetListFromLocal = new Runnable(){  
		@Override
		public void run(){
			fireEndOfGetListFromLocal(patients);
		}  
	};  
	private Runnable runFireErrorNodata = new Runnable(){  
		@Override
		public void run(){
			fireErrorNoData();
		}  
	};  
	

	public BuilderPatientList(Service service,String serveurAdresse) {
		handler = new Handler();
		this.service=service;
		this.serveurAdresse=serveurAdresse;
	}

	public void get(){
		//get list from Internet or from local sqLite database, and returns it with listener once done.
		Log.i(Constants.TAG,CLASSNAME+" Build (update from internet or get from local SQLite DB) patient list");
		// Create a thread who will check on the database
		new Thread(){
			public void run(){
				Log.i(Constants.TAG,CLASSNAME+" Internet Nok, searching data in local SQLite DB");
				//Check if there is data from local DB

				getPatientFromDataBase();
			}
		}.start();
	}
	
	private void getPatientFromDataBase(){
		networkStatus = new NetworkStatus(service);
		int networkState = networkStatus.checkNetworkStatus();
		
		DataBaseConnector dbc = new DataBaseConnector(service);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			Cursor cursor_patient = db.query(Constants.TABLE_PATIENT, new String[] { "id", "idUser", "gender", "name", "surname", "symptome", "note", "noteDate" }, null, null, null, null, "name ASC");
			if (cursor_patient != null){
				if(cursor_patient.getCount()>=0){
					Log.i(Constants.TAG, CLASSNAME + " pas de problem, on charge depuis la base");
					// on creer une liste de patint a partir de la BDD
					patients = buildFromDBD(cursor_patient);
				}
				cursor_patient.close();
			}
			db.close();
		}
		dbc.close();
        new GetElements().execute();
	}
	
	
	private void fireRunFireGetPatientListFromInternet()
	{
		for (BuilderPatientListListener listener : builderPatientListListeners) {
			listener.runFireGetPatientListFromInternet();
		}
	}
	
	private ArrayList<Patient> buildFromDBD(Cursor cursor){
		Log.i(Constants.TAG, CLASSNAME + " build from dbd");
		ArrayList<Patient> patientList = new ArrayList<Patient>();
		if(cursor.getCount()>0){
			cursor.moveToFirst();
		    do{
		    	Patient patient = new Patient(cursor.getInt(0),cursor.getString(1), cursor.getInt(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7));
		    	patientList.add(patient);
		    } while(cursor.moveToNext());
		}
	    return patientList;
	}
	
	
	private void addUserConstantToPatient(Patient patient, Cursor cursor){
		//For this patient, adding all the user constants contained by this cursor
		if(cursor.moveToFirst()){
			do{
				UserConstant userConstant = new UserConstant(cursor.getString(0),cursor.getDouble(1),cursor.getInt(2));
				int sensor = cursor.getInt(1);
				
				if(patient.getUserConstants().get(sensor)!=null){
					//getting the user constant List for this key and add the current user constant to it
					patient.getUserConstants().get(sensor).add(userConstant);
				}else{
					//else, creating a user constant list, adding this user constant, and inserting to the MainList
					ArrayList<UserConstant> listeUserConstants = new ArrayList<UserConstant>();
					listeUserConstants.add(userConstant);
					
					patient.getUserConstants().put(sensor, listeUserConstants);
				}
			} while(cursor.moveToNext());
		}
	}	
	
	public void addListener(BuilderPatientListListener listener){
		builderPatientListListeners.add(listener);
	}
	
	private void fireEndOfUpdate(ArrayList<Patient> list) {
		for (BuilderPatientListListener listener : builderPatientListListeners) {
			listener.endOfUpdatePatientList(list);
		}
	}
	private void fireEndOfGetListFromLocal(ArrayList<Patient> list) {
		for (BuilderPatientListListener listener : builderPatientListListeners) {
			listener.endOfGetPatientListFromLocal(list);
		}
	}
	private void fireErrorNoData() {
		for (BuilderPatientListListener listener : builderPatientListListeners) {
			listener.errorNoPatientData();
		}
	}

	private void retourWebService(JSONObject retour){
			if (retour != null) {
				try {
				Log.w(Constants.TAG, "retourWebService");
				JSONArray personne = retour.getJSONArray("reservatiopn");
				for (int i = 0; i < personne.length(); i++) {
					JSONObject getJSonObj = (JSONObject) personne.get(i);
					String personneNOm = getJSonObj.getString("nom");
					String prenom = getJSonObj.getString("prenom");

					String symptome = getJSonObj.getString("symptome");
					String note = getJSonObj.getString("note");
					String genre = getJSonObj.getString("genre");
					String noteDate = getJSonObj.getString("noteDate");
				int type;
					if(genre.equals("FEMMININ")){
						type=0;}
					else{
							type=1;
						}
					Patient patient = new Patient(patients.get(patients.size()-1).getId()-1, "", type,prenom, personneNOm, symptome,note,noteDate);
					Log.w(Constants.TAG,patient.toString());

					patients.add(patient);
				}
				handler.post(runFireEndOfGetListFromLocal);
				//handler.post(runFireGetPatientListFromInternet);
			}catch(JSONException e){
				e.printStackTrace();
			}
			}
			else{
				handler.post(runFireEndOfGetListFromLocal);
			}


	}




	class GetElements extends AsyncTask<Void, Void, JSONObject> {
		JSONObject jObj = null;
		///TODO rentrer ici l'adresse du serveur

		String token;


		@Override
		protected void onPreExecute() {

			String adresse= new String("http://"+serveurAdresse+":9191/uaa/oauth/token");

			HttpURLConnection myURLConnection = null;

			Log.w(Constants.TAG," async  Internet OK, initialisation de la liste des patients "+adresse);



			OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(10, TimeUnit.SECONDS)
					.writeTimeout(10, TimeUnit.SECONDS)
					.readTimeout(30, TimeUnit.SECONDS)
					.build();

			MediaType mediaType = MediaType.parse("multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
			RequestBody body = RequestBody.create(mediaType, "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"password\"\r\n\r\nflorian\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"username\"\r\n\r\nflorian\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"grant_type\"\r\n\r\npassword\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"scope\"\r\n\r\nopenid\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"client_secret\"\r\n\r\nacmesecret\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW\r\nContent-Disposition: form-data; name=\"client_id\"\r\n\r\nacme\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--");
			Request request = new Request.Builder()
					.url("http://"+serveurAdresse+":9191/uaa/oauth/token")
					.post(body)
					.addHeader("content-type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW")
					.addHeader("accept", "application/json")
					.addHeader("authorization", "Basic YWNtZTphY21lc2VjcmV0")
					.addHeader("cache-control", "no-cache")
					.addHeader("postman-token", "8155402f-c27d-defb-2bb1-3161f80b0cf1")
					.build();

			try {
				Response response = client.newCall(request).execute();

				try {
					String test=response.body().string();
					Log.w(Constants.TAG,test);
					jObj= new JSONObject(test);
					token=jObj.getString("access_token");
					Log.w(Constants.TAG,token);
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} catch (IOException e) {
				Log.w(Constants.TAG,"jdnzkjdnein");
				e.printStackTrace();
			}

		}
		/**
		 * Fonction in background responsable to find the RestApi
		 *
		 * @param str The url in which find the JSON
		 * @return The JSON in the URL
		 */
		@Override
		protected JSONObject doInBackground(Void... str) {
			jObj=null;
			OkHttpClient client = new OkHttpClient.Builder()
					.connectTimeout(10, TimeUnit.SECONDS)
					.writeTimeout(10, TimeUnit.SECONDS)
					.readTimeout(10, TimeUnit.SECONDS)
					.build();

			Log.w(Constants.TAG," lacement tache recup serveur");

			Request request = new Request.Builder()
					.url("http://"+serveurAdresse+":9999/reservations/names")
					.get()
					.addHeader("authorization", "bearer "+token)
					.addHeader("cache-control", "no-cache")
					.addHeader("postman-token", "d0bccf7e-1949-4a49-2d68-105046dcc235")
					.build();

			try {
				Response response = client.newCall(request).execute();
				String test=response.body().string();
				jObj= new JSONObject(test);
				Log.w(Constants.TAG,test);
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}

			retourWebService(jObj);
			return jObj;
		}


	}

}

