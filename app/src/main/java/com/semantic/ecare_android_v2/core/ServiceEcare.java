package com.semantic.ecare_android_v2.core;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.SparseArray;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.listener.BuilderConfigurationListListener;
import com.semantic.ecare_android_v2.core.listener.BuilderMeasureAndAlertListListener;
import com.semantic.ecare_android_v2.core.listener.BuilderPatientListListener;
import com.semantic.ecare_android_v2.core.listener.ServiceAntidoteClientListener;
import com.semantic.ecare_android_v2.core.listener.ServiceEcareListener;
import com.semantic.ecare_android_v2.core.listener.ServiceEcareUpdatingListener;
import com.semantic.ecare_android_v2.core.listener.SessionListener;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.ConfigurationList;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.NoteModel;
import com.semantic.ecare_android_v2.object.NoteModelListComparator;
import com.semantic.ecare_android_v2.object.Patient;
import com.semantic.ecare_android_v2.object.PatientListComparator;
import com.semantic.ecare_android_v2.object.SensorState;
import com.semantic.ecare_android_v2.ui.ExceptActivity;
import com.semantic.ecare_android_v2.ui.MainActivity;
import com.semantic.ecare_android_v2.ui.SplashScreen;
import com.semantic.ecare_android_v2.ui.WaitActivity;
import com.semantic.ecare_android_v2.ui.WaitActivityPatient;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.DataBaseConnector;
import com.semantic.ecare_android_v2.util.Functions;
import com.semantic.ecare_android_v2.util.FunctionsPropertiesFile;

import net.newel.android.Log;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;


/**!
 * Classe charger de demander l'accé au divers BDD pour les charger de façon asyncrone
 * Cette classe lance la fonction demandant la liste de patient, lorsque cette liste est terminé (pattern observer) alors la fonction lance l'activité main.
 */
public class ServiceEcare extends Service implements BuilderPatientListListener, BuilderConfigurationListListener, BuilderMeasureAndAlertListListener, ServiceAntidoteClientListener, SessionListener{

	private String CLASSNAME=this.getClass().getName();

	// variable concernant Antidote
	//private boolean mIsAntidoteClientBound;
	//private ServiceAntidoteClient serviceAntidoteClient;
	//private Intent serviceAntidoteClientIntent;
	private SharedPreferences preferences;
	private Session currentSession=null;
	private String addresseServeur;
	// liste des patients, builder variables
	private ArrayList<Patient> patientList = new ArrayList<Patient>(); 	// liste des patients dans le service
	private Patient selectedPatient=null; //CONTEXTE, patient selectionn�
	private BuilderPatientList patientListBuilder=null;/** Service permettant de creer la liste des patient*/
	private BuilderConfigurationList configurationListBuilder=null;
	private BuilderMeasureAndAlertList measureAndAlertListBuilder=null;

	// variable concernant les listeners
	private Collection<ServiceEcareUpdatingListener> serviceEcareUpdaterListeners = new ArrayList<ServiceEcareUpdatingListener>();
	private ServiceEcareListener serviceEcareListener;
	
	// variable concernant les bluetooth et l'internet
	private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
	private ReceiverConnectionState receiverConnectivityChange;

	// objet configuration contenant les diff�rentes param�tres
	private static ConfigurationList configurationList = new ConfigurationList();

	//Construction de la liste des mesures d'UNE recup de capteur
	private ArrayList<CompoundMeasure> measuresListContext; //CONTEXTE : Liste ordonn�e de mesures : pour les retrouver à l'affichage
	//Construction de la liste des mesures (ordonn�es delon l'ordre d'importance des capteurs)
	private SparseArray<CompoundMeasure> measuresList; //Tampon : Liste de mesures incr�ment� à chaque newmeasure, et vid� lorsque le contexte est rempli
	private ArrayList<Measure> measureList = new ArrayList<Measure>(); // liste des mesures
	private ArrayList<Alert> alertList = new ArrayList<Alert>(); // liste des alertes
		
	// variables concernant la synchronization
	private Timer timerMeasures = null;
	private TimerTask timerTaskMeasure=null;
	
	private ArrayList<String> updateType = new ArrayList<String>(); // contient les types de MAJ � appliquer
	private ArrayList<String> patientsId = new ArrayList<String>(); // liste des identifiants des patients qu'il faut demander leurs mesures et alerts

	private SplashScreen splashScreen = new SplashScreen();

	// variable boolean
	private static boolean antidote = true;
	private static boolean saveMeasure = true; // false pour le mode test
	private static boolean needToUpdate = false; // true pour installer la nouvelle version
	private boolean launchComplete=false;

	private String versionName, newVersionName, macAddress;
	private static String host;
	private int versionCode;

	// variable pour sauvegarder l'adresse mac du capteur et l'identifiant <macAddress,idType>
	public static final HashMap<String, Integer> SENSOR_TYPE = new HashMap<String, Integer>();
	private HashMap<String,SensorState> sensorStateList;

	private final IBinder mBinder = new LocalBinder();
	public class LocalBinder extends Binder {
		public ServiceEcare getService() {
			return ServiceEcare.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(Constants.TAG,CLASSNAME+" OnBind ServiceEcare");
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(Constants.TAG,CLASSNAME+" On create ServiceEcare");
		preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		WifiInfo wInfo= wifiManager.getConnectionInfo();
		macAddress = wInfo.getMacAddress();

		//First : adding in log system the version of the application, the device id ...
		Log.i(Constants.TAG, CLASSNAME+" Device ID = "+Functions.getDeviceUniqId(getApplicationContext()));

		try {
			versionName = getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName;
			versionCode = getPackageManager().getPackageInfo(getPackageName(), 0 ).versionCode;
			String svnVersion = getResources().getString(R.string.svn_version);
			newVersionName = new String(versionName);
			Log.i(Constants.TAG, CLASSNAME + "new version name : " + newVersionName);
			Log.i(Constants.TAG, CLASSNAME+" Application Version : "+versionName+" (version_int: "+versionCode+" rev_SVN: "+svnVersion+")");
		} catch (NameNotFoundException e) {
			Log.i(Constants.TAG, e);
			e.printStackTrace();
		}

		//Begin to Receive ConnectionChange
		IntentFilter connectionChangeFilter = new IntentFilter("android.net.conn.CONNECTIVITY_CHANGE");
		receiverConnectivityChange = new ReceiverConnectionState(this);
		// If there is no connection say it to receiverConnectionState
		registerReceiver(receiverConnectivityChange, connectionChangeFilter);

		// charger les parametre depuis le fichier
		getPamameters();
		
		// detection de l'evenement d'extinction de la tablette pour arreter le service antidote
		IntentFilter filterShutdown = new IntentFilter(Intent.ACTION_SHUTDOWN);
		this.registerReceiver(shutdownReceiver, filterShutdown);
//
//		IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
//		this.registerReceiver(mReceiver, filter);
//
//		if (bluetoothAdapter.isEnabled())
//		{
//			bluetoothAdapter.disable();
//		}
//		else
//		{
//			bluetoothAdapter.enable();
//		}
		// Create thread to find all the data then launch the main activity
		configurationListBuilder = new BuilderConfigurationList(this);
		configurationListBuilder.addListener(this);
		//get the configuration list
		buildConfigurationList();

		
		//si aucune mesure n'est d�tect�e dans la table MESURE, la pr�f�rence "LastSynchronizedMeasureIndex" = 0
		if(getAllMeasuresNumber()==0){//Attention =-1 si pas de connexion à la base de donn�es !
			Log.i(Constants.TAG, CLASSNAME+" Aucune mesure détectée dans la base locale : réinitialisation préférence \"LastSynchronizedMeasureDate\" = 0");
			SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
			Editor editor = preferences.edit();

			editor.putLong("LastSynchronizedMeasureDate", 0);
			editor.commit();
		}
//		synchronizer = Synchronizer.getInstance(this);
//		synchronizer.startSynchronize();
	}

	//detection de l'evenement d'extinction de la tablette pour arreter le service antidote
	private final BroadcastReceiver shutdownReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) 
		{
//			Log.i(Constants.TAG, CLASSNAME+"stop service antidote turning OFFFF");
//			stopService(serviceAntidoteClientIntent);
		}
	};

	public void getPamameters()
	{
		File property_file = new File(Constants.PROPERTY_FILE_LOCATION,Constants.PROPERTY_FILE);
		if(!property_file.exists()){
			//Cr�er le fichier � partir des assets dans la carte SD
			Log.w(Constants.TAG, CLASSNAME+" Le fichier de propriété "+Constants.PROPERTY_FILE_LOCATION+"/"+Constants.PROPERTY_FILE+" n'existe pas, il va etre copié depuis les assets");
			FunctionsPropertiesFile.copyFileFromAppToPath(getApplicationContext(),Constants.PROPERTY_FILE_LOCATION,Constants.PROPERTY_FILE);
		}
		host=FunctionsPropertiesFile.getProperty(Constants.PROPERTY_FILE_LOCATION+Constants.PROPERTY_FILE, "constant.url.host", "localhost");
		setHost(host);
	}

	// fonction appell�e pour l'initialisation de l'application, 1- supprimer toute la BDD, 2- appeler le SplashScreen pour re-demander la synchronisation
	public void init()
	{
		clearDatabase();
		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openWrite();
		String request="INSERT OR REPLACE INTO " + Constants.TABLE_CONFIGURATION + " VALUES(1 ," + Constants.PORT_WRITE + "," + Constants.PORT_HL7 + ", 0, 0, 0, \"\",\"\",\"\",0, \"\",0)";
		db.execSQL(request);
		
		configurationList = new ConfigurationList();
		dbc = new DataBaseConnector(this);
		db = dbc.openRead();
		if(db!=null){
			Cursor cursor = db.query(Constants.TABLE_CONFIGURATION, new String[] { "portWrite", "portHL7", "init", "active", "type", "idUserService","gender", "blocInfo", "nameOrEstablishment" , "surnameOrService", "canUpdate"}, null, null, null, null, null);
			if (cursor != null){
				cursor.moveToFirst();
				configurationList= new ConfigurationList();
				configurationList.setPortWrite(cursor.getInt(0));
				configurationList.setPortHl7(cursor.getInt(1));
				configurationList.setInitialise(cursor.getInt(2));
				configurationList.setActive(cursor.getInt(3));
				configurationList.setType(cursor.getInt(4));
				configurationList.setIdPatientOrService(cursor.getString(5));
				configurationList.setGender(cursor.getInt(6));
				configurationList.setBlocInfo(cursor.getString(7));
				configurationList.setNomOuEtablissement(cursor.getString(8));
				configurationList.setPrenomOuService(cursor.getString(9));
				configurationList.setCanUpdate(cursor.getInt(10));
			}
		}
		db.close();
		dbc.close();
		selectedPatient = null;
		Intent intentWait = new Intent(getApplicationContext(), SplashScreen.class);
		intentWait.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intentWait);
	}

//	// pour savoir l'�tat du bluetooth
//	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			final String action = intent.getAction();
//
//			if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
//				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
//				switch (state) {
//				case BluetoothAdapter.STATE_OFF:
//					Log.i(Constants.TAG, CLASSNAME+"State off");
//					stopAntidote();
//					bluetoothAdapter.enable();
//					break;
//				case BluetoothAdapter.STATE_TURNING_OFF:
//					Log.i(Constants.TAG, CLASSNAME+"Turning off");
//					break;
//				case BluetoothAdapter.STATE_ON:
//					Log.i(Constants.TAG, CLASSNAME+"State on");
//					startAntidote();
//					Log.i(Constants.TAG, CLASSNAME + "isAntidote = " + isAntidote());
//
//					break;
//				case BluetoothAdapter.STATE_TURNING_ON:
//					Log.i(Constants.TAG, CLASSNAME+"Turning on");
//					break;
//				}
//			}
//		}
//	};
//
//	//arr�ter Antidote
//	public void stopAntidote()
//	{
//		if(mIsAntidoteClientBound && serviceAntidoteClient != null){
//			serviceAntidoteClient.removeListener(this);
//			doUnbindServiceAntidoteClient();
//			Log.i(Constants.TAG, CLASSNAME+"stop service antidote");
//			stopService(serviceAntidoteClientIntent);
//		}
//	}
//
//	// d�marrer Antidote
//	public void startAntidote() 
//	{
//		Log.i(Constants.TAG, CLASSNAME+"start antidote");
//		//start and bind Service : ServiceAntidoteClient
//		serviceAntidoteClientIntent = new Intent(getApplicationContext(), ServiceAntidoteClient.class);
//		startService(serviceAntidoteClientIntent);
//		if(!mIsAntidoteClientBound){
//			doBindServiceAntidoteClient();
//		}
//
//		sensorStateList = new HashMap<String,SensorState>();
//		measuresList = new SparseArray<CompoundMeasure>();
//		measuresListContext = new ArrayList<CompoundMeasure>();
//
//		//Notification
//		//Lancement une seule fois meme si on reclique ici, car singleTask dans la config AndroidManifest !
//		Intent notificationIntent = new Intent(this, MainActivity.class);		
//		PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,notificationIntent, 0);
//		//FIXME : Utiliser autre chose pour les notifications (deprecated!)
//		Notification notification = new Notification.Builder(this)
//		.setContentTitle("E-Care")
//		.setContentText("Collecte des données active")
//		.setSmallIcon(R.drawable.ic_launcher)
//		.setContentIntent(pendingIntent)
//		.getNotification();
//		startForeground(Notification.FLAG_ONLY_ALERT_ONCE, notification); // FLAG_ONGOING_EVENT // FLAG_FOREGROUND_SERVICE
//	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		//this.unregisterReceiver(mReceiver);
		this.unregisterReceiver(receiverConnectivityChange);
		//if(mIsAntidoteClientBound){
//			serviceAntidoteClient.removeListener(this);
//			doUnbindServiceAntidoteClient();
//			stopService(serviceAntidoteClientIntent);
		//}
		Log.i(Constants.TAG, CLASSNAME + "On destroy");
		stopForeground(true);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We don't want this service to continue running if it is explicitly
		// stopped, so return not sticky.
		return START_NOT_STICKY;
	}

	public void disconnectPatient() {
		Log.i(Constants.TAG, CLASSNAME+" Déconnexion du patient + suppression de la session");
		selectedPatient=null;
		measuresListContext.clear();
		measuresList.clear();
		if(currentSession!=null){
			currentSession.stopAll();
		}
		currentSession=null;
	}

	// Charger la liste des patients
	public void buildPatientList(){
		//first : check if property file exists
		File property_file = new File(Constants.PROPERTY_FILE_LOCATION,Constants.PROPERTY_FILE);
		if(!property_file.exists()){
			//Cr�er le fichier � partir des assets dans la carte SD
			Log.w(Constants.TAG, CLASSNAME+" Le fichier de propriété "+Constants.PROPERTY_FILE_LOCATION+"/"+Constants.PROPERTY_FILE+" n'existe pas, il va etre copié depuis les assets");
			FunctionsPropertiesFile.copyFileFromAppToPath(getApplicationContext(),Constants.PROPERTY_FILE_LOCATION,Constants.PROPERTY_FILE);
		}
		patientListBuilder.get();
	}

	// Charger la liste des mesures et des alertes
	public void buildMeasureAndAlertList(){
		//first : check if property file exists
		File property_file = new File(Constants.PROPERTY_FILE_LOCATION,Constants.PROPERTY_FILE);
		if(!property_file.exists()){
			//Cr�er le fichier � partir des assets dans la carte SD
			Log.w(Constants.TAG, CLASSNAME+" Le fichier de propriété "+Constants.PROPERTY_FILE_LOCATION+"/"+Constants.PROPERTY_FILE+" n'existe pas, il va etre copié depuis les assets");
			FunctionsPropertiesFile.copyFileFromAppToPath(getApplicationContext(),Constants.PROPERTY_FILE_LOCATION,Constants.PROPERTY_FILE);
		}
		measureAndAlertListBuilder.get();
	}

	// charger la configuration
	public void buildConfigurationList(){
		//first : check if property file exists
		File property_file = new File(Constants.PROPERTY_FILE_LOCATION,Constants.PROPERTY_FILE);
			// If the file doesn't exist
			if(!property_file.exists()){
			//Cr�er le fichier � partir des assets dans la carte SD
			Log.w(Constants.TAG, CLASSNAME+" Le fichier de propriété "+Constants.PROPERTY_FILE_LOCATION+"/"+Constants.PROPERTY_FILE+" n'existe pas, il va etre copié depuis les assets");
			FunctionsPropertiesFile.copyFileFromAppToPath(getApplicationContext(),Constants.PROPERTY_FILE_LOCATION,Constants.PROPERTY_FILE);
		}
		configurationListBuilder.get();
	}

	//Binding service AntidoteClient
	/*private ServiceConnection mConnectionAntidoteClient = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			serviceAntidoteClient = ((ServiceAntidoteClient.LocalBinder)service).getService();
			serviceAntidoteClient.addListener(ServiceEcare.this);
			Log.i(Constants.TAG,CLASSNAME+" Connected to service Antidote Client");
		}

		public void onServiceDisconnected(ComponentName className) {
			serviceAntidoteClient = null;
			Log.i(Constants.TAG,CLASSNAME+" Disconnected from service Antidote Client");
		}
	};

	private void doBindServiceAntidoteClient() {
		bindService(new Intent(getApplicationContext(), ServiceAntidoteClient.class), mConnectionAntidoteClient, Context.BIND_AUTO_CREATE);
		mIsAntidoteClientBound = true;
	}

	private void doUnbindServiceAntidoteClient() {
		if (mIsAntidoteClientBound) {
			unbindService(mConnectionAntidoteClient);
			mIsAntidoteClientBound = false;
		}
	}*/

	//Listener only for updating data
	public void addUpdaterListener(ServiceEcareUpdatingListener listener){
		serviceEcareUpdaterListeners.add(listener);
	}

	public void removeUpdaterListener(ServiceEcareUpdatingListener listener) {
		serviceEcareUpdaterListeners.remove(listener);
	}

	private void fireEndOfConfigurationListNoData() {
		for (ServiceEcareUpdatingListener listener : serviceEcareUpdaterListeners) {
			listener.endOfConfigurationListNoData();
		}
	}

	private void fireEndOfPatientListBuild() {
		for (ServiceEcareUpdatingListener listener : serviceEcareUpdaterListeners) {
			listener.endOfPatientListBuild();
		}
	}
	private void fireEndOfPatientListNoData() {
		for (ServiceEcareUpdatingListener listener : serviceEcareUpdaterListeners) {
			listener.endOfPatientListNoData();
		}
	}

	//Listeners ONLY for global serviceEcare Listener methods
	public void addListener(ServiceEcareListener listener){
		Log.i(Constants.TAG, CLASSNAME+" Ajout d'un listener : "+listener.getClass().getName());
		serviceEcareListener=listener;
	}

	private void fireNewSensorMeasure(ArrayList<CompoundMeasure> mesures) {
		if(serviceEcareListener!=null){
			Log.i(Constants.TAG, CLASSNAME+" LISTENER NEWMEASURE : " + serviceEcareListener.getClass().getName());
			serviceEcareListener.newSensorMeasure(mesures);
		}
		playBip();
	}

	private void fireNewSensorState(SensorState state) {
		if(serviceEcareListener!=null)
			serviceEcareListener.newSensorState(state);
	}

	private void fireDisconnect() {
		if(serviceEcareListener!=null)
			serviceEcareListener.disconnect();
	}

	/*Receive listeners from Configuration builder*/
	@Override
	public void endOfUpdateConfigurationList(ConfigurationList list) {
		Log.i(Constants.TAG,CLASSNAME+" Listener, configuration list updating complete from internet");
		configurationList=list;
		launchComplete=true;
	}

	// Cr�er la liste des capteurs en mettant ensemble l'adresse mac avec le type du capteur
	public void setSensorType()
	{
		DataBaseConnector dbc = new DataBaseConnector(this);
		DataBaseConnector.getInstance(this);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			Cursor cursor = db.query(Constants.TABLE_CAPTEUR, new String[] {"id_type", "adresse_mac"}, null, null, null, null, null);
			if (cursor != null){
				if(cursor.moveToFirst()){
					do
					{
						SENSOR_TYPE.put(cursor.getString(1),cursor.getInt(0));
					} while(cursor.moveToNext());
				}
			}
		}
	}

	public void endOfConfigurationListBuild(ConfigurationList list) {
		Log.i(Constants.TAG,CLASSNAME+" Listener configuration list loading complete from local DB");
		configurationList=list;
		setSensorType();
		if(configurationList.getActive() == 1)
		{		
			Log.i(Constants.TAG, CLASSNAME + "Tablette est activée");
			if(configurationList.getType()==1)
			{
				//Add Service As listener of patient list updater
				patientListBuilder = new BuilderPatientList(this,getString(R.string.serverAdresse));
				patientListBuilder.addListener(this);
				buildPatientList();
			}
			else if(configurationList.getType()==2)
			{
				selectedPatient = new Patient(1,
											  configurationList.getIdPatientOrService(),
											  configurationList.getGender(),
											  configurationList.getPrenomOuService(),
											  configurationList.getNomOuEtablissement(), 
											  null,
											  null,
											  null);
				launchComplete=true;
				// get patient mesures and alerts
				measureAndAlertListBuilder = new BuilderMeasureAndAlertList(this);
				measureAndAlertListBuilder.addListener(this);
				buildMeasureAndAlertList();
				Intent intentWait = new Intent(getApplicationContext(), WaitActivityPatient.class);
				intentWait.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intentWait);
			}
		}
		else
		{
			Log.i(Constants.TAG, CLASSNAME + "Tablette n'est pas activée");
		}
	}

	@Override
	public void endOfGetConfigurationListFromLocal(ConfigurationList list) {
		Log.i(Constants.TAG,CLASSNAME+" Listener configuration list loading complete from local DB");
		configurationList=list;

		Log.i(Constants.TAG, CLASSNAME+"patientssss");
		setSensorType();

		if(configurationList.getType()==1)
		{
			//Add Service As listener of patient list updater
			patientListBuilder = new BuilderPatientList(this,getString(R.string.serverAdresse));
			patientListBuilder.addListener(this);
			buildPatientList();
		}
		else if(configurationList.getType()==2)
		{
			selectedPatient = new Patient(1,
					configurationList.getIdPatientOrService(),
					configurationList.getGender(),
					configurationList.getPrenomOuService(),
					configurationList.getNomOuEtablissement(), 
					null,
					null,
					null);
			launchComplete=true;
			Intent intentWait = new Intent(getApplicationContext(), WaitActivityPatient.class);
			intentWait.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intentWait);
		}
	}

	@Override
	public void errorNoConfigurationData() {
		Log.i(Constants.TAG,CLASSNAME+" Listener any configuration data loaded");
		fireEndOfConfigurationListNoData();
	}

	@Override
	public void runFireGetPatientListFromInternet() {
		Log.i(Constants.TAG, CLASSNAME + " get patient list from internet");

		
	}

	@Override
	public void runFireGetConfigListFromInternet() {
		Log.i(Constants.TAG, CLASSNAME + " get list from internet");

		
	}

	@Override
	public void runErrorNoInternet() {
		Log.i(Constants.TAG, CLASSNAME + " Error no internet");

		Intent intentWait = new Intent(getApplicationContext(), ExceptActivity.class);
		intentWait.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intentWait);
	}

	@Override
	public void endOfUpdatePatientList(ArrayList<Patient> list) {
		Log.i(Constants.TAG,CLASSNAME+" Listener, patient list updating complete from internet");
		patientList = list;
		fireEndOfPatientListBuild();
	}

	@Override
	public void endOfUpdatePatientListFromInternet(ArrayList<Patient> list) {
		Log.i(Constants.TAG,CLASSNAME+" Listener patient list loading complete from local DB");
		patientList = list;
		launchComplete= true;
		Intent intentWait = new Intent(getApplicationContext(), SplashScreen.class);
		intentWait.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intentWait);
		fireEndOfPatientListBuild();
	}

	/**
	 * Function call when the BuilderPatientList have found only data in the local BDD(SQLite)
	 * Call by BuilderPatientList when he have finish
	 * This function call the main activity
	 * @param list The list of patient from the database in loca
     */
	@Override
	public void endOfGetPatientListFromLocal(ArrayList<Patient> list) {
		Log.i(Constants.TAG,CLASSNAME+" Listener patient list loading complete from local DB");
		patientList = list;
		launchComplete= true;
		// Quand on a finis de charger tous les patient en local on peut envoyer la main activity
		Intent intentWait = new Intent(getApplicationContext(), MainActivity.class);
		intentWait.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intentWait);
		// Previent tous les listenner que les patients sont build
		fireEndOfPatientListBuild();

		measureAndAlertListBuilder = new BuilderMeasureAndAlertList(this);
		measureAndAlertListBuilder.addListener(this);
		buildMeasureAndAlertList();
	}

	@Override
	public void errorNoPatientData() {
		Log.i(Constants.TAG,CLASSNAME+" Listener any patient data loaded");
		fireEndOfPatientListNoData();
	}

	/*Listening from ServiceAntidoteClient*/
	@Override
	public void newSensorMeasure(CompoundMeasure mesures) {
		//v�rification si la MC envoy�e n'est pas null
		//(principalement pour tester le cas ou le retour bdd g�nère un alerte à la suite d'une mauvaise �criture en bdd)
		if(mesures!=null){
			Log.i(Constants.TAG, CLASSNAME+" Receive new measure From Antidote Client");

			sessionAction();

			//If Connected : save measure, get returned Id, Generate Alerte and Save Alert
			//Else nothing but called when patient is selected !

			boolean measureisSaved = saveMeasureAndSaveAlerte(mesures);
			if(measureisSaved){
				//Reset context ?
				measuresListContext.clear();

				//Ajout dans le tampon
				switch(mesures.get(0).getSensor()){
				case Measure.SENSOR_OXY:
					CompoundMeasure cm1 = new CompoundMeasure();
					cm1.add(mesures.get(0));
					measuresList.put(mesures.get(0).getSensor(),cm1);
					CompoundMeasure cm2 = new CompoundMeasure();
					cm2.add(mesures.get(1));
					measuresList.put(mesures.get(1).getSensor(),cm2);
					break;
				case Measure.SENSOR_TENSION_SYS:
				case Measure.SENSOR_POIDS:
				case Measure.SENSOR_CARDIO:
					measuresList.put(mesures.get(0).getSensor(),mesures);
					break;
				}

				if(timerMeasures!=null){
					timerTaskMeasure.cancel();
					timerMeasures.cancel();
					timerMeasures.purge();
				}
				timerMeasures = new Timer();

				//Lancement du compte à rebours en attente d'autres mesures (ex tension)
				timerTaskMeasure = new TimerTask(){
					@Override
					public void run() {
						timerMeasures.cancel();
						//à la fin du timer, cr�ation du contexte,tri de la liste

						//Ajout dans l'ordre choisi des mesures dispo pour former l'arrayList pour l'affichage des mesures
						if(measuresList.get(Measure.SENSOR_TENSION_SYS)!=null){
							measuresListContext.add(measuresList.get(Measure.SENSOR_TENSION_SYS));
						}
						if(measuresList.get(Measure.SENSOR_OXY)!=null){
							measuresListContext.add(measuresList.get(Measure.SENSOR_OXY));
						}
						if(measuresList.get(Measure.SENSOR_CARDIO)!=null){
							measuresListContext.add(measuresList.get(Measure.SENSOR_CARDIO));
						}
						if(measuresList.get(Measure.SENSOR_POIDS)!=null){
							measuresListContext.add(measuresList.get(Measure.SENSOR_POIDS));
						}

						//measuresListContext=new HashMap<Integer,CompoundMesure>(measuresList);

						//vidage du tampon
						measuresList.clear();

						Log.i(Constants.TAG,CLASSNAME+" génération d'evenement newMeasures avec "+measuresListContext.size()+" groupes de mesures");

						//g�n�ration evenement
						fireNewSensorMeasure(measuresListContext);
					}
				};

				Log.i(Constants.TAG, CLASSNAME+" Lancement du timerMeasures");
				timerMeasures.schedule(timerTaskMeasure, 500);//500 ms pour attendre une 2eme mesure (dans le cas du tensiometre)
			}else{
				fireNewSensorMeasure(null);
			}
		}else{
			fireNewSensorMeasure(null);
		}
	}

	public void playBip() {
		final int soundRaw=preferences.getInt("soundRaw", R.raw.bip);

		new Thread(){
			@Override
			public void run(){
				Log.i(Constants.TAG, CLASSNAME+" Lecture du Bip dans un Thread");

				MediaPlayer mp = MediaPlayer.create(ServiceEcare.this,soundRaw);
				mp.start();
				try {
					Thread.sleep(2500);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				mp.release();

				Log.i(Constants.TAG, CLASSNAME+" End of Thread \"BIP\"");
			}
		}.start();
	}

	private boolean saveMeasureAndSaveAlerte(CompoundMeasure mesures){
		if(saveMeasure)
		{
			if(selectedPatient!=null){
				Iterator<Measure> it = mesures.iterator();
				while(it.hasNext()){
					Measure mesure = it.next();

					mesure.setPatientId(selectedPatient.getUid());

					//Recherche si une mesure du même type a d�ja �t� ajout�e au cours de la meme session
					Measure oldMesure = sessionAddMesure(mesure);

					if(oldMesure!=null){
						mesure.setOverride(true);
						deleteMeasure(oldMesure);
					}

					//g�n�ration de l'alerte
					Alert alerte = generateAlert(mesure);
					mesure.setAlerte(alerte);	

					//Enregistrement de la mesure
					int mesure_id = (int) saveMesure(mesure);
					if(mesure_id==-1){
						//ERROR, Retry once
						mesure_id = (int) saveMesure(mesure);
						if(mesure_id==-1){
							return false;
						}
					}
					if(mesure_id!=-1){
						mesure.setMeasureId(mesure_id);
					}

					if(alerte!=null){
						//Set mesure ID returned By sqLite Inserting !
						alerte.setMeasureId(mesure_id);
						//M�morisation de l'alerte si non nulle
						if(alerte!=null){
							int alert_id = (int) saveAlert(alerte);
							if(alert_id==-1){
								//ERROR, Retry once
								alert_id = (int) saveAlert(alerte);
								if(alert_id==-1){
									return false;
								}
							}
							if(alert_id!=-1){
								alerte.setAlertId(alert_id);
							}
						}
					}
				}

			}
		}
		return true;
	}

	@Override
	public void newSensorState(SensorState state) {
		sessionAction();

		switch(state.getState()){

		case SensorState.STATE_CONNECTED:
			//adding sensor state
			sensorStateList.put(state.getMacAddr(), state);
			break;

		case SensorState.STATE_DISCONNECTED:
			//delete sensor state
			sensorStateList.remove(state.getMacAddr());
			break;

		default :
			//edit sensor state
			sensorStateList.remove(state.getMacAddr());
			sensorStateList.put(state.getMacAddr(), state);
		}
		fireNewSensorState(state); //change current view
	}

	private Alert generateAlert(Measure measure) {
//		Inference inference = new Inference(getApplicationContext());
//		return inference.infere(measure);
		return null;
	}

	//Fonctionnalit�s li�es à la base de donn�es
	
	private long saveAlert(Alert alerte){
		long res=-1;
		if(selectedPatient!=null){
			ContentValues cv = new ContentValues();
			cv.put("id_patient",selectedPatient.getUid());
			cv.put("date",alerte.getDate().getTime());
			cv.put("mesure",alerte.getMeasure());
			cv.put("niveau",alerte.getLevel());
			cv.put("message",alerte.getMessage());

			DataBaseConnector dbc = new DataBaseConnector(this);
			SQLiteDatabase db = dbc.openWrite();

			if(db!=null){
				res = db.insert(Constants.TABLE_ALERTE,null,cv);
				db.close();
			}
			dbc.close();
		}
		return res;
	}

	private long saveMesure(Measure mesure){
		ContentValues cv = new ContentValues();
		cv.put("id_patient",selectedPatient.getUid());
		cv.put("sensor",mesure.getSensor());
		cv.put("date",mesure.getDate().getTime());
		cv.put("valeur",mesure.getValue());
		cv.put("synchronized",0);

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openWrite();
		long res =-1;
		if(db!=null){
			res = db.insert(Constants.TABLE_MESURE,null,cv);
			db.close();
		}
		dbc.close();

		return res;
	}

	public void deleteMeasures(CompoundMeasure mesures) {
		//Deleting this measure from the context
		measuresListContext.remove(mesures);

		Iterator<Measure> it = mesures.iterator();
		while(it.hasNext()){
			Measure mesure = it.next();
			if(currentSession!=null){
				currentSession.removeMesure(mesure);
			}
			deleteMeasure(mesure);
		}
	}

	private void deleteMeasure(Measure mesure){
		int id = mesure.getMeasureId();

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openWrite();
		if(db!=null){
			Log.i(Constants.TAG, CLASSNAME+" Suppression de la mesure id "+id);

			db.execSQL("DELETE FROM "+Constants.TABLE_MESURE+" WHERE id=\""+id+"\"");


			if(mesure.getAlert()!=null){
				int alerte = mesure.getAlert().getAlerteId();
				db.execSQL("DELETE FROM "+Constants.TABLE_ALERTE+" WHERE id=\""+alerte+"\"");
			}
			db.close();
		}
		dbc.close();
	}

	public int getMesureComp(CompoundMeasure mesures){
		int comp=0;
		if(selectedPatient!=null){

			DataBaseConnector dbc = new DataBaseConnector(this);
			SQLiteDatabase db = dbc.openRead();

			if(db!=null){
				int sensormetric=mesures.get(0).getSensor();
				double sensorvalue=mesures.get(0).getValue();
				Date sensorDate=mesures.get(0).getDate();

				if(sensormetric==Measure.SENSOR_TENSION_SYS){
					if(mesures.size()>=3){
						sensormetric=mesures.get(2).getSensor();
						sensorvalue=mesures.get(2).getValue();
					}
				}

				//Compare the measure with the PREVIOUS measure from "mesures" here is the LAST !!!
				Cursor cursor = db.query(Constants.TABLE_MESURE, new String[] { "valeur" }, "id_patient=\""+selectedPatient.getUid()+"\" AND sensor=\""+sensormetric+"\" AND date <\""+sensorDate.getTime()+"\"", null, null, null, "date DESC","1");//limit 1 (pas d'offset) car date inf�rieure stricte !
				if (cursor != null){
					if(cursor.moveToFirst()){
						double valueOld=cursor.getDouble(0);
						//Log.i(Constants.TAG, CLASSNAME + "Valeur actuelle = " + sensorvalue+" old = "+valueOld);
						if(sensorvalue<valueOld){
							comp=-1;
						}else if(sensorvalue>valueOld){
							comp=1;
						}else{
							comp=0;
						}
					}
					cursor.close();
				}
				db.close();
			}
			dbc.close();
		}
		return comp;
	}

	private int getAllMeasuresNumber() {
		Log.i("TestMeasure",CLASSNAME+" Recherche du nombre total de mesures enregistrées en base");

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			Cursor cursor = db.query(Constants.TABLE_MESURE, new String[] {"id"}, null, null, null, null, null);
			int num = cursor.getCount();
			cursor.close();
			db.close();
			dbc.close();

			return num;
		}else{
			return -1;//error
		}
	}

	public ArrayList<ArrayList<Measure>> getMeasures(int period, int sensor) {
		long dateLastMeasure = 0;
		DataBaseConnector dbc = new DataBaseConnector(this);
		ArrayList<ArrayList<Measure>> measures = new ArrayList<ArrayList<Measure>>();
		Log.i(Constants.TAG, CLASSNAME + "getMeasuresss");
		if(selectedPatient!=null){
			Log.i(Constants.TAG,CLASSNAME+" Chargement mesures pour graphique sensor:"+sensor+"  period:"+period);

			SQLiteDatabase db = dbc.openRead();
			if(db!=null){

				if(sensor==Measure.SENSOR_TENSION){
					sensor=Measure.SENSOR_TENSION_SYS;
				}

				dateLastMeasure = 0;
				//r�cup�ration de la date de la dernière mesure
				Cursor cursorLast = db.query(Constants.TABLE_MESURE, new String[] {"date"}, "id_patient=\""+selectedPatient.getUid()+"\" AND sensor=\""+sensor+"\"", null, null, null, "date DESC", "1");
				if (cursorLast != null){
					if(cursorLast.moveToFirst()){
						dateLastMeasure=cursorLast.getLong(0);
					}
					cursorLast.close();
				}

				if(dateLastMeasure>0){
					ArrayList<Measure> list0 = new ArrayList<Measure>();
					long dateRange = dateLastMeasure - (((long)period) * 86400 * 1000);
					Cursor cursor = db.query(Constants.TABLE_MESURE, new String[] { "valeur", "date", "synchronized" }, "id_patient=\""+selectedPatient.getUid()+"\" AND sensor=\""+sensor+"\" AND date>=\""+dateRange+"\"", null, null, null, "date ASC");
					if (cursor != null){
						if(cursor.moveToFirst()){
							do{
								double value=cursor.getDouble(0);
								long date = cursor.getLong(1);
								boolean sync = cursor.getInt(2)==1;

								list0.add(new Measure(sensor,value,new Date(date),sync));

							} while(cursor.moveToNext());
							Log.i(Constants.TAG, CLASSNAME + CLASSNAME+" Chargement de "+list0.size()+" mesureeeeeee(s)");
							measures.add(list0);
						}
						cursor.close();
					}

					if(sensor==Measure.SENSOR_TENSION_SYS){
						sensor=Measure.SENSOR_TENSION_DIA;
						//cas particulier pour la tension : chargement aussi de pression diastolique
						ArrayList<Measure> list1 = new ArrayList<Measure>();


						Cursor cursor2 = db.query(Constants.TABLE_MESURE, new String[] { "valeur", "date", "synchronized" }, "id_patient=\""+selectedPatient.getUid()+"\" AND sensor=\""+sensor+"\" AND date>=\""+dateRange+"\"", null, null, null, "date ASC");
						if (cursor2 != null){
							if(cursor2.moveToFirst()){
								do{
									double value=cursor2.getDouble(0);
									long date = cursor2.getLong(1);
									boolean sync = cursor2.getInt(2)==1;

									list1.add(new Measure(sensor, value,new Date(date),sync));

								} while(cursor2.moveToNext());
								measures.add(list1);
							}
							cursor2.close();
						}
					}
				}
			}
			else
				Log.i(Constants.TAG, CLASSNAME + "dateLastMesure = " + dateLastMeasure);

			db.close();
			dbc.close();
		}

		return measures;
	}

	public ArrayList<CompoundMeasure> getLastMeasures() {
		ArrayList<CompoundMeasure> mesures = new ArrayList<CompoundMeasure>();
		if(selectedPatient!=null){
			//chargement de 4 fois dernieres CM
			mesures.add(getLastCompoundMeasure(selectedPatient.getUid(), Measure.SENSOR_OXY));
			mesures.add(getLastCompoundMeasure(selectedPatient.getUid(), Measure.SENSOR_POIDS));
			mesures.add(getLastCompoundMeasure(selectedPatient.getUid(), Measure.SENSOR_TENSION));
			mesures.add(getLastCompoundMeasure(selectedPatient.getUid(), Measure.SENSOR_CARDIO));
		}
		return mesures;
	}

	public CompoundMeasure searchMeasure(String uid, int sensor, Date date) {
		//search alert for these keys

		CompoundMeasure cm = new CompoundMeasure();
		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			if(sensor==Measure.SENSOR_TENSION){sensor=Measure.SENSOR_TENSION_SYS;}
			Cursor cursor = db.query(Constants.TABLE_MESURE, new String[] { "id", "valeur", "synchronized" }, "id_patient=\""+uid+"\" AND sensor=\""+sensor+"\" AND date=\""+date.getTime()+"\"", null, null, null, null);
			if (cursor != null){
				if(cursor.moveToFirst()){
					//Log.i(Constants.TAG, CLASSNAME+" Trouv� une mesure pour le type "+sensor+" valeur="+cursor.getDouble(1));
					Measure mesure = new Measure(sensor,date,uid,cursor.getDouble(1),cursor.getInt(2)==1);
					mesure.setMeasureId(cursor.getInt(0));
					mesure.setAlerte(getAlerte(uid, cursor.getInt(0)));
					cm.add(mesure);
				}
				cursor.close();
			}

			if(sensor==Measure.SENSOR_TENSION_SYS){
				sensor=Measure.SENSOR_TENSION_DIA;
				Cursor cursor2 = db.query(Constants.TABLE_MESURE, new String[] { "id", "valeur", "synchronized" }, "id_patient=\""+uid+"\" AND sensor=\""+sensor+"\" AND date=\""+date.getTime()+"\"", null, null, null, null);
				if (cursor2 != null){
					if(cursor2.moveToFirst()){
						Measure mesure = new Measure(sensor,date,uid,cursor2.getDouble(1),cursor2.getInt(2)==1);
						mesure.setMeasureId(cursor2.getInt(0));
						mesure.setAlerte(getAlerte(uid, cursor2.getInt(0)));
						cm.add(mesure);
					}
				}
				cursor2.close();
			}
			db.close();
		}
		dbc.close();

		return cm;
	}

	public int getLastMeasureType(){
		int sensor = 0;
		if(selectedPatient!=null){
			String uid=selectedPatient.getUid();
			DataBaseConnector dbc = new DataBaseConnector(this);
			SQLiteDatabase db = dbc.openRead();
			if(db!=null){
				Cursor cursor = db.query(Constants.TABLE_MESURE, new String[] { "sensor", }, "id_patient=\""+uid+"\"", null, null, null, "date DESC", "1");
				if (cursor != null){
					if(cursor.moveToFirst()){
						Log.i(Constants.TAG, CLASSNAME+" derniere mesure de type "+cursor.getInt(0));
						sensor=cursor.getInt(0);
					}
					cursor.close();
				}
				db.close();
			}
			dbc.close();
		}
		return sensor;
	}

	public CompoundMeasure getLastCompoundMeasure(String uid, int sensor) {
		//search alert for these keys
		CompoundMeasure cm = new CompoundMeasure();
		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			if(sensor==Measure.SENSOR_TENSION){sensor=Measure.SENSOR_TENSION_SYS;}
			Cursor cursor = db.query(Constants.TABLE_MESURE, new String[] { "id", "valeur", "date", "note", "noteDate" }, "id_patient=\""+uid+"\" AND sensor=\""+sensor+"\"", null, null, null, "date DESC", "1");
			if (cursor != null){
				if(cursor.moveToFirst()){
					Measure mesure = new Measure(sensor, new Date(cursor.getLong(2)),uid,cursor.getDouble(1),true);
					mesure.setMeasureId(cursor.getInt(0));
					mesure.setNote(cursor.getString(3));
					mesure.setNoteDate(cursor.getString(4));
					mesure.setAlerte(getAlerte(uid, cursor.getInt(0)));
					cm.add(mesure);
				}
				cursor.close();
			}

			if(sensor==Measure.SENSOR_TENSION_SYS){
				sensor=Measure.SENSOR_TENSION_DIA;
				Cursor cursor2 = db.query(Constants.TABLE_MESURE, new String[] { "id", "valeur", "date", "note", "noteDate" }, "id_patient=\""+uid+"\" AND sensor=\""+sensor+"\"", null, null, null, "date DESC", "1");
				if (cursor2 != null){
					if(cursor2.moveToFirst()){
						Measure mesure = new Measure(sensor, new Date(cursor2.getLong(2)),uid,cursor2.getDouble(1),true);
						mesure.setMeasureId(cursor2.getInt(0));
						mesure.setNote(cursor2.getString(3));
						mesure.setNoteDate(cursor2.getString(4));
						mesure.setAlerte(getAlerte(uid, cursor2.getInt(0)));
						cm.add(mesure);
					}
				}
				cursor2.close();
			}
			db.close();
		}
		dbc.close();
		return cm;
	}

	public Alert getAlerte(String patientId, int measureId){
		Alert alerte=null;

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){

			Cursor cursor = db.query(Constants.TABLE_ALERTE, new String[] { "date", "niveau", "message", "id"}, "id_patient=\""+patientId+"\" AND mesure=\""+measureId+"\"", null, null, null, null);
			if (cursor != null){
				if(cursor.moveToFirst()){
					Date date = new Date(cursor.getLong(0));
					alerte = new Alert(patientId, date,cursor.getInt(1),cursor.getString(2), cursor.getInt(3));
				}
			}
			cursor.close();
			db.close();
		}
		dbc.close();
		return alerte;
	}

	public Measure getMeasure(int measureId){
		Measure mesure=null;

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){

			Cursor cursor = db.query(Constants.TABLE_MESURE, new String[] { "sensor", "date", "valeur", "note", "noteDate", "id" }, "id=\""+measureId+"\"", null, null, null, null);
			if (cursor != null){
				if(cursor.moveToFirst()){
					Date date = new Date(cursor.getLong(2));
					mesure = new Measure(cursor.getInt(0),cursor.getDouble(2),date,false, cursor.getInt(5));
					mesure.setNote(cursor.getString(3));
					mesure.setNoteDate(cursor.getString(4));
				}
			}
			cursor.close();
			db.close();
		}
		dbc.close();
		return mesure;
	}

	public ArrayList<Alert> getAllAlertes(ArrayList<Integer> listofSensors){
		//TODO : get int array of measures types
		//and build mysql request from this array

		ArrayList<Alert> alertes = new ArrayList<Alert>();

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			//premiere requete on ne tient pas compte du filtre (moche mais jetable !)
			Cursor cursor = db.query(Constants.TABLE_ALERTE, new String[] { "date", "niveau", "message", "mesure", "id", "note", "noteDate" }, "", null, null, null, "date DESC");
			if (cursor != null){
				if(cursor.moveToFirst()){
					do{
						Date alerte_date = new Date(cursor.getLong(0));
						int alerte_niveau=cursor.getInt(1);
						String alerte_message=cursor.getString(2);
						int alerte_id=cursor.getInt(4);
						String alerte_note = cursor.getString(5);
						String alerte_noteDate = cursor.getString(6);
						Alert alerte = new Alert(alerte_date, alerte_niveau, alerte_message, alerte_id, alerte_note, alerte_noteDate);

						//R�cup�ration de la mesure li�e à l'alerte
						Measure measure = getMeasure(cursor.getInt(3));
						//FILTRAGE : On ne tient pas compte des alertes dont le type de mesure n'est pas dans le tableau donn� en parametre!
						//		  					if(listofSensors.contains(measure.getSensor())){
						//		  						alerte.setCompleteMeasure(measure);
						//		  						alertes.add(alerte);
						//		  					}//SINON : C'est que les filtres enlevent cette mesure !!
						alertes.add(alerte);
					} while(cursor.moveToNext());
					Log.i(Constants.TAG, CLASSNAME+" Chargement de "+alertes.size()+" alertes");
				}
				cursor.close();
			}

			db.close();
		}
		dbc.close();
		
		return alertes;
	}
	
	public ArrayList<Alert> getAlertes(ArrayList<Integer> listofSensors){
		//TODO : get int array of measures types
		//and build mysql request from this array

		ArrayList<Alert> alertes = new ArrayList<Alert>();
		if(selectedPatient!=null){
			DataBaseConnector dbc = new DataBaseConnector(this);
			SQLiteDatabase db = dbc.openRead();
			if(db!=null){
				//premiere requete on ne tient pas compte du filtre (moche mais jetable !)
				Cursor cursor = db.query(Constants.TABLE_ALERTE, new String[] { "date", "niveau", "message", "mesure", "id", "note", "noteDate" }, "id_patient=\""+selectedPatient.getUid()+"\"", null, null, null, "date DESC");
				if (cursor != null){
					if(cursor.moveToFirst()){
						do{
							Date alerte_date = new Date(cursor.getLong(0));
							int alerte_niveau=cursor.getInt(1);
							String alerte_message=cursor.getString(2);
							int alerte_id=cursor.getInt(4);
							String alerte_note = cursor.getString(5);
							String alerte_noteDate = cursor.getString(6);
							Alert alerte = new Alert(selectedPatient.getUid(),alerte_date, alerte_niveau, alerte_message, alerte_id, alerte_note, alerte_noteDate);

							//R�cup�ration de la mesure li�e à l'alerte
							Measure measure = getMeasure(cursor.getInt(3));
							//FILTRAGE : On ne tient pas compte des alertes dont le type de mesure n'est pas dans le tableau donn� en parametre!
							//		  					if(listofSensors.contains(measure.getSensor())){
							//		  						alerte.setCompleteMeasure(measure);
							//		  						alertes.add(alerte);
							//		  					}//SINON : C'est que les filtres enlevent cette mesure !!
							alertes.add(alerte);
						} while(cursor.moveToNext());
						Log.i(Constants.TAG, CLASSNAME+" Chargement de "+alertes.size()+" alertes");
					}
					cursor.close();
				}

				db.close();
			}
			dbc.close();
		}
		return alertes;
	}

	public ArrayList<Patient> getPatientsWithNotes(){
		
		ArrayList<Patient> patientsWithNotes = new ArrayList<Patient>();
		
		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			//premiere requete on ne tient pas compte du filtre (moche mais jetable !)
			Cursor cursor = db.query(Constants.TABLE_PATIENT, new String[] { "id", "idUser", "gender", "name", "surname", "symptome", "note", "noteDate" }, "noteDate<>''", null, null, null, "noteDate DESC");
			if (cursor != null){
				if(cursor.moveToFirst()){
					do{
						Patient p = new Patient(cursor.getInt(0),
								cursor.getString(1),
								cursor.getInt(2),
								cursor.getString(3),
								cursor.getString(4),
								cursor.getString(5),
								cursor.getString(6),
								cursor.getString(7));

						patientsWithNotes.add(p);
					} while(cursor.moveToNext());
					Log.i(Constants.TAG, CLASSNAME+" Chargement de "+patientsWithNotes.size()+" patients");
				}
				cursor.close();
			}

			db.close();
		}
		dbc.close();
		
		return patientsWithNotes;
		
	}
	
	public ArrayList<Alert> getAlertsWithNotes(){
			
		ArrayList<Alert> alertsWithNotes = new ArrayList<Alert>();
		
		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			//premiere requete on ne tient pas compte du filtre (moche mais jetable !)
			Cursor cursor = db.query(Constants.TABLE_ALERTE, new String[] { "date", "niveau", "message", "mesure", "id", "note", "noteDate" }, "noteDate<>''", null, null, null, "noteDate DESC");
			if (cursor != null){
				if(cursor.moveToFirst()){
					do{
						Date alerte_date = new Date(cursor.getLong(0));
						int alerte_niveau=cursor.getInt(1);
						String alerte_message=cursor.getString(2);
						int alerte_id=cursor.getInt(4);
						String alerte_note = cursor.getString(5);
						String alerte_noteDate = cursor.getString(6);
						Alert alerte = new Alert(alerte_date, alerte_niveau, alerte_message, alerte_id, alerte_note, alerte_noteDate);

						alertsWithNotes.add(alerte);
					} while(cursor.moveToNext());
					Log.i(Constants.TAG, CLASSNAME+" Chargement de "+alertsWithNotes.size()+" alertes");
				}
				cursor.close();
			}

			db.close();
		}
		dbc.close();
		
		return alertsWithNotes;
			
	}
	
	public ArrayList<Measure> getMeasuresWithNotes(){
		
		ArrayList<Measure> measuresWithNotes = new ArrayList<Measure>();
		
		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openRead();
		if(db!=null){
			//premiere requete on ne tient pas compte du filtre (moche mais jetable !)
			Cursor cursor = db.query(Constants.TABLE_MESURE, new String[] { "id","sensor","date","id_patient", "valeur", "synchronized", "note", "noteDate" }, "noteDate<>''", null, null, null, "noteDate DESC");
			if (cursor != null){
				if(cursor.moveToFirst()){
					do{
						String dateString = cursor.getString(2);
						boolean sync = false;
						sync = cursor.getInt(5) == 1;
						
						Measure m = new Measure(cursor.getInt(0),
								cursor.getInt(1),
								new Date(cursor.getLong(2)),
								cursor.getString(3), // patient id
								cursor.getDouble(4), // value
								sync, // sync
								cursor.getString(6), // note
								cursor.getString(7)); // noteDate

						measuresWithNotes.add(m);
					} while(cursor.moveToNext());
					Log.i(Constants.TAG, CLASSNAME+" Chargement de "+measuresWithNotes.size()+" measures");
				}
				cursor.close();
			}

			db.close();
		}
		dbc.close();
		
		return measuresWithNotes;
		
	}
	
	public ArrayList<NoteModel> getNoteList(){
		ArrayList<NoteModel> noteList = new ArrayList<NoteModel>();
		
		ArrayList<Patient> patientsWithNotes = getPatientsWithNotes();
		ArrayList<Alert> alertsWithNotes = getAlertsWithNotes();
		ArrayList<Measure> measuresWithNotes = getMeasuresWithNotes();
		
		for(Patient p : patientsWithNotes){
			noteList.add(new NoteModel(p.getNote(), p.getNoteDate(), p.getId(), Constants.PATIENT_TYPE));
		}
		
		for(Alert a : alertsWithNotes){
			noteList.add(new NoteModel(a.getNote(), a.getNoteDate(), a.getAlerteId(), Constants.ALERT_TYPE));
		}
		
		for(Measure m : measuresWithNotes){
			noteList.add(new NoteModel(m.getNote(), m.getNoteDate(), m.getMeasureId(), Constants.MEASURE_TYPE));
		}
		
		noteList = sortNotesByDate(noteList);
		
		return noteList;
	}
	
	private ArrayList<NoteModel> sortNotesByDate(ArrayList<NoteModel> noteList){
		Collections.sort(noteList, new NoteModelListComparator());
		return noteList;
	}
	
	public void sessionAction(){
		if(currentSession!=null){
			currentSession.action();
		}
		else
		{
			Log.i(Constants.TAG,CLASSNAME+" pas d'action");
		}
	}

	public Measure sessionAddMesure(Measure mesure){
		if(currentSession!=null){
			return currentSession.addMesure(mesure);
		}
		return null;
	}

	//Listener Session
	@Override
	public void disconnect() {
		Log.i(Constants.TAG, CLASSNAME+" Demande de déconnexion depuis la session");
		disconnectPatient();
		fireDisconnect();
	}

	public void launchFireNewMeasure(int sensor){
		CompoundMeasure mesures = new CompoundMeasure();
		switch(sensor){
		case Measure.SENSOR_OXY:
			double value2 = Math.round(85 + (new Random()).nextDouble() * 15);
			mesures.add(new Measure(Measure.SENSOR_OXY,value2,new Date(),false));
			double value21 = Math.round(50 + (new Random()).nextDouble() * 30);
			mesures.add(new Measure(Measure.SENSOR_CARDIO,value21,new Date(),false));
			break;
		case Measure.SENSOR_POIDS:
			double value3 = Math.round(50 + (new Random()).nextDouble() * 30);
			mesures.add(new Measure(Measure.SENSOR_POIDS,value3,new Date(),false));
			break;
		case Measure.SENSOR_CARDIO:
			double value7 = Math.round(50 + (new Random()).nextDouble() * 30);
			mesures.add(new Measure(Measure.SENSOR_CARDIO,value7,new Date(),false));
			break;
		case Measure.SENSOR_TENSION:
			double value4 = Math.round(90 + (new Random()).nextDouble() * 40);
			double value5 = Math.round(50 + (new Random()).nextDouble() * 40);
			double value6 = Math.round((value4 / 3) + ((2 * value5) / 3));
			mesures.add(new Measure(Measure.SENSOR_TENSION_SYS,value4,new Date(),false));
			mesures.add(new Measure(Measure.SENSOR_TENSION_DIA,value5,new Date(),false));
			break;

		default:
			mesures=null;

		}
		Log.i(Constants.TAG, CLASSNAME+" Génération mesure de TEST de type "+sensor);

		//serviceAntidoteClient.fireNewSensorMesure(mesures);
	}

	// vider la BDD
	public void clearDatabase() {
		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openWrite();
		if(db!=null){
			db.execSQL("DELETE FROM " + Constants.TABLE_PATIENT);
			db.execSQL("DELETE FROM " + Constants.TABLE_USER_CONSTANTS);
			db.execSQL("DELETE FROM " + Constants.TABLE_CONFIGURATION);
			db.execSQL("DELETE FROM " + Constants.TABLE_MESURE);
			db.execSQL("DELETE FROM " + Constants.TABLE_ALERTE);
			db.execSQL("DELETE FROM "+ Constants.TABLE_CAPTEUR);
			db.close();
		}
		dbc.close();
		//Toast.makeText(this, "Database truncated successfully", Toast.LENGTH_LONG).show();
	}

	//Methodes liees aux operations sur les notes
	// Suppression d'une note
	
	// Modification d'une note (update)

	// peut-�tre pourrait-on simplement utiliser les m�thodes savePatient, saveAlert et saveMeasure
	public int updateNoteFromPatient(Patient patient){
		int res = 0;
		///TODO ici
		Log.w(Constants.TAG," patient"+patient.getSurname());


		addresseServeur=getString(R.string.serverAdresse);
		new GetElements().execute(patient);



		ContentValues cv = new ContentValues();
		cv.put("note",patient.getNote());
		cv.put("noteDate",patient.getNoteDate());

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openWrite();
		if(db!=null){
			res = db.update(Constants.TABLE_PATIENT,cv,"id"+"="+patient.getId(),null);
			db.close();
		}
		dbc.close();

		return res;
	}

	public int updateNoteFromPatient(int patientId, NoteModel note){
		int res = 0;



		Log.w(Constants.TAG," from patient");

		ContentValues cv = new ContentValues();
		cv.put("note",note.getNote());
		cv.put("noteDate",note.getNoteDate());

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openWrite();
		if(db!=null){
			res = db.update(Constants.TABLE_PATIENT,cv,"id"+"="+patientId,null);
			db.close();
		}
		dbc.close();

		return res;
	}

	public int updateNoteFromMeasure(Measure measure){
		int res = 0;

		Log.w(Constants.TAG," from meqsure");
		ContentValues cv = new ContentValues();
		cv.put("note",measure.getNote());
		cv.put("noteDate",measure.getNoteDate());

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openWrite();
		if(db!=null){
			res = db.update(Constants.TABLE_MESURE,cv,"id"+"="+measure.getMeasureId(),null);
			db.close();
		}
		dbc.close();

		return res;
	}

	public int updateNoteFromMeasure(int measureId, NoteModel note){
		int res = 0;



		Log.w(Constants.TAG," from measureID");




		ContentValues cv = new ContentValues();
		cv.put("note",note.getNote());
		cv.put("noteDate",note.getNoteDate());

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openWrite();
		if(db!=null){
			res = db.update(Constants.TABLE_MESURE,cv,"id"+"="+measureId,null);
			db.close();
		}
		dbc.close();

		return res;
	}

	public int updateNoteFromAlert(Alert alert){
		int res = 0;

		Log.w(Constants.TAG," from alter");
		ContentValues cv = new ContentValues();
		cv.put("note",alert.getNote());
		cv.put("noteDate",alert.getNoteDate());

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openWrite();
		if(db!=null){
			res = db.update(Constants.TABLE_ALERTE,cv,"id"+"="+alert.getAlerteId(),null);
			db.close();
		}
		dbc.close();

		return res;
	}

	public int updateNoteFromAlert(int alertId, NoteModel note){
		int res = 0;

		Log.w(Constants.TAG," from alertID");
		ContentValues cv = new ContentValues();
		cv.put("note",note.getNote());
		cv.put("noteDate",note.getNoteDate());

		DataBaseConnector dbc = new DataBaseConnector(this);
		SQLiteDatabase db = dbc.openWrite();
		if(db!=null){
			res = db.update(Constants.TABLE_ALERTE,cv,"id"+"="+alertId,null);
			db.close();
		}
		dbc.close();

		return res;
	}


//	// D�marrer la synchronisation
//	public void synchronize() {
//		new Thread(){
//			@Override
//			public void run(){
//				Synchronizer synchronizer = Synchronizer.getInstance(ServiceEcare.this);
//				synchronizer.synchronize(false);
//			}
//		}.start();
//	}

	//Pour le debug !
//	public void resynchronize() {
//		DataBaseConnector dbc = new DataBaseConnector(this);
//		SQLiteDatabase db = dbc.openWrite();
//		if(db!=null){
//			db.execSQL("DELETE FROM " + Constants.TABLE_MESURE);
//			db.execSQL("DELETE FROM " + Constants.TABLE_ALERTE);
//			db.close();
//		}
//		dbc.close();
//
//		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//		Editor editor = preferences.edit();
//
//		editor.putLong("LastSynchronizedMeasureDate", 0);
//		editor.commit();
//
//		Log.i(Constants.TAG, CLASSNAME+" synchronize manually from menu");
//		synchronize();
//
//		Toast.makeText(this, "Synchronisation manuelle lancée", Toast.LENGTH_LONG).show();
//	}

	public void ApplicationStarts(){
		//This is launched by SplashScreen and reset some data
		serviceEcareUpdaterListeners = new ArrayList<ServiceEcareUpdatingListener>();
		sensorStateList = new HashMap<String,SensorState>();
		measuresList = new SparseArray<CompoundMeasure>();
		measuresListContext = new ArrayList<CompoundMeasure>();
	}

	@Override
	public void endOfUpdateMeasureAndAlertList(ArrayList<Measure> measureList, ArrayList<Alert> alertList) 
	{
		Intent intentWait = new Intent(getApplicationContext(), WaitActivity.class);
		intentWait.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		startActivity(intentWait);		
	}

	@Override
	public void endOfGetMeasureAndAlertListFromLocal(ArrayList<Measure> measureList, ArrayList<Alert> alertList) 
	{
		splashScreen.setConfigurationOk(true);
		Log.i(Constants.TAG, CLASSNAME+"Configuration OK ! ");
	}

	@Override
	public void errorNoMeasureAndAlertData() 
	{
		Log.i(Constants.TAG, CLASSNAME + " No measure and alert Data");
	}

	@Override
	public void runFireGetMeasureAndAlertListFromInternet() 
	{
//		patientsId.clear();
//		Log.i(Constants.TAG , CLASSNAME + " get measure and alert list from internet");
//		DataBaseConnector dbc = new DataBaseConnector(ServiceEcare.this);
//		SQLiteDatabase dbRead = dbc.openRead();
//		if(dbRead!=null){
//			Cursor cursor = dbRead.query(Constants.TABLE_PATIENT, new String[] {"idUser"},null, null, null, null, null);
//			if (cursor != null){
//				if(cursor.moveToFirst()){
//					do{
//						patientsId.add(cursor.getString(0));
//					} while(cursor.moveToNext());
//				}
//			}
//			cursor.close();
//		}
//		dbRead.close();
//		dbc.close();
////		try {
//			if(new EcareWriteMeasureAndAlertList().execute(this,null,null).get())
//			{
//				Log.i(Constants.TAG, CLASSNAME + "Liste measures and alerts success");
//				endOfGetMeasureAndAlertListFromLocal(measureList, alertList);
//			}
//			else
//			{
//				Intent intentNoServer = new Intent(getApplicationContext(), ServerExceptActivity.class);
//				intentNoServer.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//				startActivity(intentNoServer);
//				Log.i(Constants.TAG, CLASSNAME + "Write Liste of measures and alerts UNsuccess");
//			}
//		} catch (InterruptedException e) {
//			Log.i(Constants.TAG, CLASSNAME + "Socket Write NOT created : Execution Exception " + e.getMessage());
//		} catch (ExecutionException e) {
//			Log.i(Constants.TAG, CLASSNAME + "Socket Write NOT created : InterruptedException " + e.getMessage());
//		}	
	}

//	// Pour installer la MAJ de l'application
//	public void installUpdate()
//	{
//		File app = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ecare_android_v2.apk");
//
//		if (app.exists()){
//			final PackageManager pm = getPackageManager();
//			String apkName = "ecare_android_v2.apk";
//			String fullPath = Environment.getExternalStorageDirectory() + "/" + apkName;  
//			Log.e(Constants.TAG, CLASSNAME+"Full path : " + fullPath);
//			PackageInfo info = pm.getPackageArchiveInfo(fullPath, 0);
//			if(info.versionName.compareTo(versionName)>0)
//			{
//				// need to update the app
//				try {
//					Log.i(Constants.TAG, CLASSNAME+"need to update the app having the version name : " + getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName);
//				} catch (NameNotFoundException e) {
//					Log.e(Constants.TAG, CLASSNAME+ e.getMessage());
//				}
//				Intent intent = new Intent(Intent.ACTION_VIEW);
//				intent.setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory()+"/ecare_android_v2.apk")), "application/vnd.android.package-archive");
//				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
//				startActivity(intent);
//			}
//			else 
//			{
//				Toast.makeText(getApplicationContext(), "versionCode invalide", Toast.LENGTH_LONG).show();
//			}
//		}
//	}
//
//	// T�l�charger le fichier apk depuis le serveur
//	public void updateApp()
//	{
//		Log.i(Constants.TAG, CLASSNAME + "application update");
//		try {
//			if(new EcareWriteUpdateApp().execute(this,null,null).get())
//			{
//				Log.i(Constants.TAG, CLASSNAME + "update ok");
//				setNeedToUpdate(true);
//			}
//			else
//			{
//				Log.i(Constants.TAG, CLASSNAME + "Write update app UNsuccess");
//			}
//		} catch (InterruptedException e) {
//			Log.i(Constants.TAG, CLASSNAME + "Socket Write NOT created : Execution Exception " + e.getMessage());
//		} catch (ExecutionException e) {
//			Log.i(Constants.TAG, CLASSNAME + "Socket Write NOT created : InterruptedException " + e.getMessage());
//		}	
//	}
//
//	// voir s'il y a une mise a jour 
//	public void checkUpdate()
//	{
//		Log.i(Constants.TAG, CLASSNAME + " check application update");
//
//		try {
//			// demander au serveur s'il y a des mise a jour
//			if(new EcareWriteCheckUpdate().execute(this,null,null).get())
//			{
//				//après l'execution de chaque mise a jour, que doit-on faire apres
//				Log.i(Constants.TAG, CLASSNAME + "check update");
//				for(int i=0; i<updateType.size(); i++)
//				{
//					switch (updateType.get(i))
//					{
//					case "updatePatientList" : // après la mise a jour de la liste des patients, on vérifie si on fait la mise a jour des mesures
//						Log.i(Constants.TAG, CLASSNAME + "update patients list success");
//						if(patientsId.size()>0)
//						{
//							if(new EcareWriteMeasureAndAlertList().execute(this,null,null).get())
//							{
//								Log.i(Constants.TAG, CLASSNAME + "Liste measures and alerts success");
//							}
//							else
//							{
//								Log.i(Constants.TAG, CLASSNAME + "Write Liste of measures and alerts UNsuccess");
//							}
//						}
//						splashScreen.setUpdatePatient(true);
//						Intent intent = new Intent(getApplicationContext(), SplashScreen.class);
//						intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//						startActivity(intent);
//						break;
//					case "updateMeasuresAndAlerts" :
////						Log.i(Constants.TAG, CLASSNAME + " \nMAJ Measures and alerts dones");
////						splashScreen.setUpdatePatient(true);
////						Intent intentS = new Intent(getApplicationContext(), SplashScreen.class);
////						intentS.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
////						startActivity(intentS);
//						break;
//					case "updateApp" : 
//						if(newVersionName.compareTo(versionName)>0)
//						{
//							Log.i(Constants.TAG, CLASSNAME+"il faut telecharger la nouvelle version");
//							//if(configurationList.getCanUpdate()==1)
//								updateApp();
//							//else
//								//Log.i(Constants.TAG, CLASSNAME+" On peut pas mettre à jour l'application");
//						}
//						else
//							Log.i(Constants.TAG, CLASSNAME+"up-to-date");
//						break;
//					case "initApp" : 
//						Log.i(Constants.TAG, CLASSNAME + "\n Initialisation de l'application");
//						break;
//					case "noUpdates" :
//						Log.i(Constants.TAG, CLASSNAME + " \nPas de mise à jour !");
//						break;
//					default :
//						break;
//					}
//				}
//			}
//			else
//			{
//				Log.i(Constants.TAG, CLASSNAME + "Write check update app UNsuccess");
//			}
//		} catch (InterruptedException e) {
//			Log.i(Constants.TAG, CLASSNAME + "Socket Write NOT created : Execution Exception " + e.getMessage());
//		} catch (ExecutionException e) {
//			Log.i(Constants.TAG, CLASSNAME + "Socket Write NOT created : InterruptedException " + e.getMessage());
//		}	
//	}
	
	//GETTERS/SETTERS
	public void setSelectedPatient(Patient patient){
		currentSession=new Session(this);
		selectedPatient=patient;
	}

	public void setSelectedPatient(Patient patient, ArrayList<CompoundMeasure> mesures){
		setSelectedPatient(patient);

		//OnSelectedPatient On "MesureSetPatient View, Saving measure in the DataBase and (eventually the Alert generated previously)
		Iterator<CompoundMeasure> it = mesures.iterator();
		while(it.hasNext()){
			CompoundMeasure cm = it.next();
			saveMeasureAndSaveAlerte(cm);
		}
	}

	public Patient getSelectedPatient(){
		return selectedPatient;
	}


	public boolean getLaunchComplete(){
		return launchComplete;
	}

	public ArrayList<Patient> getPatientList(){
		Collections.sort(patientList, new PatientListComparator());
		return patientList;
	}

	public HashMap<String,SensorState> getSensorStateList(){
		return sensorStateList;
	}


	public ArrayList<CompoundMeasure> getMeasuresListContext(){
		return measuresListContext;
	}

	public void resetMeasuresListContext() {
		measuresListContext.clear();
	}
	
	
	public ArrayList<String> getUpdateType() 
	{
		return updateType;
	}

	public void setUpdateType(ArrayList<String> updateType) 
	{
		this.updateType = updateType;
	}

	public String getNewVersionName() 
	{
		return newVersionName;
	}

	public void setNewVersionName(String newVersionName) 
	{
		this.newVersionName = newVersionName;
	}


	public String getVersionName() 
	{
		return versionName;
	}

	public void setVersionName(String versionName) 
	{
		this.versionName = versionName;
	}

	public void setPatientList(ArrayList<Patient> patientList) 
	{
		this.patientList = patientList;
	}

	public String getMacAddress() 
	{
		return macAddress;
	}

	public void setMacAddress(String macAddress) 
	{
		this.macAddress = macAddress;
	}

	public static void setConfigurationList(ConfigurationList configurationList) 
	{
		ServiceEcare.configurationList = configurationList;
	}

	public static ConfigurationList getConfigurationList() 
	{
		return configurationList;
	}

	public boolean isNeedToUpdate() 
	{
		return needToUpdate;
	}

	public void setNeedToUpdate(Boolean needToUpdate) 
	{
		ServiceEcare.needToUpdate = needToUpdate;
	}

	public static boolean isSaveMeasure() 
	{
		return saveMeasure;
	}

	public static void setSaveMeasure(boolean saveMeasure) 
	{
		ServiceEcare.saveMeasure = saveMeasure;
	}

	public static String getHost() {
		return host;
	}

	public void setHost(String host) {
		ServiceEcare.host = host;
	}

	public static boolean isAntidote() {
		return antidote;
	}

	public static void setAntidote(boolean antidote) {
		ServiceEcare.antidote = antidote;
	}
	

	public ArrayList<String> getPatientsId() {
		return patientsId;
	}

	public void setPatientsId(ArrayList<String> patientsId) {
		this.patientsId = patientsId;
	}


	private class GetElements extends AsyncTask<Patient, Void, Void> {
		@Override
		protected Void doInBackground(Patient... params) {
			Patient patient = params[0];
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost("http://"+addresseServeur+":8000/update");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("nom",patient.getSurname()));
			nameValuePairs.add(new BasicNameValuePair("note", patient.getNote()));
			try {
				httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				HttpResponse response = httpclient.execute(httppost);
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
	}

}



