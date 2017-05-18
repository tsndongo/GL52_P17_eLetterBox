package com.semantic.ecare_android_v2.util;


import net.newel.android.Log;
import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabaseLockedException;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.Date;
/*!
Classe responsable de la premiére connection
et de l'initialisation des différentes table.
 */
public class DataBaseConnector {

	private String req;
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private Context context;
	private static DataBaseConnector instance;
	private String CLASSNAME = this.getClass().getName();
	
	public static synchronized DataBaseConnector getInstance(Context context)
	{
		if(instance==null)
			instance = new DataBaseConnector(context);
		
		return instance;
	}
	
	
	public DataBaseConnector(Context context){
		this.context=context;
	}
	
	

	public SQLiteDatabase openRead() throws SQLException{
		//FIXME !!!!!! : Try catch android.database.sqlite.SQLiteDatabaseLockedException: database is locked
		//Si erreur ici : soit tenter de recommencer, soit relancer l'application ?
		try{
			mDbHelper = new DatabaseHelper(context);		
			mDb = mDbHelper.getReadableDatabase();		
			return mDb;
		}catch(SQLiteDatabaseLockedException e){
			Log.e(Constants.TAG, CLASSNAME + " erreurrr");
			return null;
		}
	}
	
	
	public SQLiteDatabase openWrite() throws SQLException{
		//TODO : Try catch android.database.sqlite.SQLiteDatabaseLockedException: database is locked
		//Si erreur ici : soit tenter de recommencer, soit relancer l'application ?
		try{
			mDbHelper = new DatabaseHelper(context);
			mDb = mDbHelper.getWritableDatabase();
			return mDb;
		}catch(SQLiteDatabaseLockedException e){
			return null;
		}
	}	 
	
	public void close()
	{
		mDbHelper.close();
	}
	
	
	
	
	
	private class DatabaseHelper extends SQLiteOpenHelper {
		 
		DatabaseHelper(Context context) {
			//Open or creating database id the base doesn't exist
			super(context, Constants.DATABASE_NAME, null, Constants.DATABASE_VERSION);
		}
		 
		@Override
		public void onCreate(SQLiteDatabase db) {
			//method called when the database is open for the first time
			// Appel le script de creation de la table patient
			db.execSQL(Constants.CREATE_TABLE_PATIENT);
            //Creation de la table constnte @see dans la classe contante_table_user (string
			db.execSQL(Constants.CREATE_TABLE_USER_CONSTANTS);
			db.execSQL(Constants.CREATE_TABLE_CONFIGURATION);
			db.execSQL(Constants.CREATE_TABLE_MESURE);
			db.execSQL(Constants.CREATE_TABLE_ALERTE);
			db.execSQL(Constants.CREATE_TABLE_CAPTEUR);
		    

			try{
				String request="INSERT OR REPLACE INTO " + Constants.TABLE_CONFIGURATION + " VALUES(1 , " + Constants.PORT_WRITE + "," + Constants.PORT_HL7 + ", 1, 2, 1, \"\",\"\",\"\",0, \"\",0)";
				db.execSQL(request);
			}catch(Exception e){
				Log.e(Constants.TAG,CLASSNAME + " Exception à l'insertion de la configuration " );
				Log.e(Constants.TAG, e);
			}
			//données de test
			//patient
			try{
				String request="INSERT OR REPLACE INTO " + Constants.TABLE_PATIENT + " VALUES(1 ,'patient1', 1,'patient1', 'patient1', '', 'Note pour patient 1', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_PATIENT + " VALUES(2 ,'patient2', 1,'patient2', 'patient2', '', 'Note pour patient 2', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_PATIENT + " VALUES(3 ,'patient3', 1,'patient3', 'patient3', '', 'Note pour patient 3', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
			}catch(Exception e){
				Log.e(Constants.TAG,CLASSNAME + " Exception à l'insertion des patients " );
				Log.e(Constants.TAG, e);
			}
			
			try{
				String request="INSERT OR REPLACE INTO " + Constants.TABLE_CAPTEUR + " VALUES(1 ,18948, 'tensiometre', 'xxxxxxxxx')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_CAPTEUR + " VALUES(2 ,18949, 'systolique', 'xxxxxxxxx')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_CAPTEUR + " VALUES(3 ,18950, 'diastolique', 'xxxxxxxxx')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_CAPTEUR + " VALUES(4 ,19384, 'oxymetre', 'xxxxxxxxx')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_CAPTEUR + " VALUES(5 ,57664, 'pese-personne', 'xxxxxxxxx')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_CAPTEUR + " VALUES(6 ,18474, 'fréquence cardiaque', 'xxxxxxxxx')";
				db.execSQL(request);
				
			}catch(Exception e){
				Log.e(Constants.TAG,CLASSNAME + " Exception à l'insertion des capteurs" );
				Log.e(Constants.TAG, e);
			}
			
		
			// mesures
			//patient 1
			try{
				String request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(1 ,'patient1', 18949, 1427123334000, 120, 1, 'Note pour mesure patient 1', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(2 ,'patient1', 18949, 1427223334000, 110, 1, 'Note pour mesure patient 1', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(3 ,'patient1', 18949, 1427323334000, 130, 1, 'Note pour mesure patient 1', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(4 ,'patient1', 18950, 1427123334000, 80, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(5 ,'patient1', 18950, 1427223334000, 70, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(6 ,'patient1', 18950, 1427323334000, 90, 1, '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(7 ,'patient1', 19384, 1427123334000, 95, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(8 ,'patient1', 19384, 1427223334000, 92, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(9 ,'patient1', 19384, 1427323334000, 96, 1, '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(10 ,'patient1', 18474, 1427123334000, 75, 1,'', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(11 ,'patient1', 18474, 1427223334000, 72, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(12 ,'patient1', 18474, 1427323334000, 76, 1, '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(13 ,'patient1', 57664, 1427123334000, 75, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(14 ,'patient1', 57664, 1427223334000, 74, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(15 ,'patient1', 57664, 1427323334000, 75, 1, '', '')";
				db.execSQL(request);
				
				
				
				
				//patient 2
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(16 ,'patient2', 18949, 1427123334000, 120, 1, 'Note pour mesure patient 2', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(17 ,'patient2', 18949, 1427223334000, 110, 1, 'Note pour mesure patient 2', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(18 ,'patient2', 18949, 1427323334000, 130, 1, 'Note pour mesure patient 2', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(19 ,'patient2', 18950, 1427123334000, 80, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(20 ,'patient2', 18950, 1427223334000, 70, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(21 ,'patient2', 18950, 1427323334000, 90, 1, '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(22 ,'patient2', 19384, 1427123334000, 95, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(23 ,'patient2', 19384, 1427223334000, 92, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(24 ,'patient2', 19384, 1427323334000, 96, 1, '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(25 ,'patient2', 18474, 1427123334000, 75, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(26 ,'patient2', 18474, 1427223334000, 72, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(27 ,'patient2', 18474, 1427323334000, 76, 1, '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(28 ,'patient2', 57664, 1427123334000, 75, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(29 ,'patient2', 57664, 1427223334000, 74, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(30 ,'patient2', 57664, 1427323334000, 75, 1, '', '')";
				db.execSQL(request);
				
				
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(31 ,'patient3', 18949, 1427123334000, 120, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(32 ,'patient3', 18949, 1427223334000, 110, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(33 ,'patient3', 18949, 1427323334000, 130, 1, '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(34 ,'patient3', 18950, 1427123334000, 80, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(35 ,'patient3', 18950, 1427223334000, 70, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(36 ,'patient3', 18950, 1427323334000, 90, 1, '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(37 ,'patient3', 19384, 1427123334000, 95, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(38 ,'patient3', 19384, 1427223334000, 92, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(39 ,'patient3', 19384, 1427323334000, 96, 1, '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(40 ,'patient3', 18474, 1427123334000, 75, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(41 ,'patient3', 18474, 1427223334000, 72, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(42 ,'patient3', 18474, 1427323334000, 76, 1, '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(43 ,'patient3', 57664, 1427123334000, 75, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(44 ,'patient3', 57664, 1427223334000, 74, 1, '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_MESURE + " VALUES(45 ,'patient3', 57664, 1427323334000, 75, 1, '', '')";
				db.execSQL(request);
				
			}catch(Exception e){
				Log.e(Constants.TAG,CLASSNAME + " Exception à l'insertion des mesures" );
				Log.e(Constants.TAG, e);
			}
			
			
			// mesures
			//patient 1
			try{
				String request="INSERT OR REPLACE INTO " + Constants.TABLE_ALERTE + " VALUES(1 ,'patient1', 1427123334000, 1, 1, 'alerte 1', 'Note pour alerte 1 patient 1', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_ALERTE + " VALUES(2 ,'patient1', 1427123334000, 5, 2, 'alerte 2', '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_ALERTE + " VALUES(3 ,'patient1', 1427123334000, 9, 3, 'alerte 3', '', '')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_ALERTE + " VALUES(4 ,'patient2', 1427123334000, 16, 1, 'alerte 1', '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_ALERTE + " VALUES(5 ,'patient2', 1427123334000, 20, 2, 'alerte 2', '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_ALERTE + " VALUES(6 ,'patient2', 1427123334000, 24, 3, 'alerte 3', 'Note pour alerte 3 patient 2', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				
				request="INSERT OR REPLACE INTO " + Constants.TABLE_ALERTE + " VALUES(7 ,'patient3', 1427123334000, 31, 1, 'alerte 1', '', '')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_ALERTE + " VALUES(8 ,'patient3', 1427123334000, 35, 2, 'alerte 2', 'Note pour alerte 2 patient 3', '2015-05-07 15:40:18.000')";
				db.execSQL(request);
				request="INSERT OR REPLACE INTO " + Constants.TABLE_ALERTE + " VALUES(9 ,'patient3', 1427123334000, 39, 3, 'alerte 3', '', '')";
				db.execSQL(request);
				
			}catch(Exception e){
				Log.e(Constants.TAG,CLASSNAME + " Exception à l'insertion des mesures" );
				Log.e(Constants.TAG, e);
			}
		}
		 
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			//Before recreate new tables, deleting existent
			db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_PATIENT);
			db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_USER_CONSTANTS);
			db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_CONFIGURATION);
			db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_MESURE);
			db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_ALERTE);
			db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_CAPTEUR);
			
			//now creating tables
			onCreate(db);
		}
	}	
}
