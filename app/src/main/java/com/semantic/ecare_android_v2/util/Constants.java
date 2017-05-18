package com.semantic.ecare_android_v2.util;

import java.util.HashMap;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.object.Measure;

import android.os.Environment;
import android.util.SparseIntArray;

public class Constants {


	public static final int PORT_WRITE=18444;
	public static final int PORT_HL7=18445;
	
	public static final String TAG="Ecare";

	/* Constants concerning the notes */
	public static final int CANCEL_NOTE = 0;
	public static final int SAVE_NOTE = 1;
	public static final String NOTEABLE_TYPE_KEY = "type";
	public static final String NOTEABLE_ID_KEY = "id";
	public static final String NOTEMODEL_KEY = "noteModel";
	public static final int PATIENT_TYPE = 1;
	public static final int ALERT_TYPE = 2;
	public static final int MEASURE_TYPE = 3;
	
	
	public final static int REQ_CODE_NOTEDIALOG_SELECTED_PATIENT = 4567;
	public final static int REQ_CODE_NOTEDIALOG_MEASURE = 4568;
	public final static int REQ_CODE_NOTEDIALOG_ALERT = 4569;
	public final static int REQ_CODE_NOTEDIALOG_PATIENT = 4570;

	
	public static final String ABONNEMENT_DOMICILE="domicile";
	public static final String ABONNEMENT_HOPITAL="service";
	
	
	public static final String PROPERTY_FILE_LOCATION=Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
	public static final String PROPERTY_FILE="ecare_properties.txt";
	
	
	public final static String BINARIES_PARTITION="/system";
	public final static String BINARIES_LOCATION=BINARIES_PARTITION+"/bin/";
	public final static String BINARY_HCICONFIG="hciconfig";
	public final static String TMP_LOCATION=Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
	
	
	
	//public static final String PROPERTIES_FILE=Environment.getExternalStorageDirectory().getAbsolutePath()+"/";
	
	//public static Typeface TF_GOTHIC = Typeface.create("GOTHIC", Typeface.NORMAL);//null;//Typeface.createFromFile(Environment.getExternalStorageDirectory()+"/GOTHIC.ttf");
	
	
	public static final String[] DAY_ARRAY=new String[]{"Dimanche","Lundi","Mardi","Mercredi","Jeudi","Vendredi","Samedi"};
	
	
	
	
	
	
	
	public static final SparseIntArray SENSOR_UNIT = new SparseIntArray(8);
	static{
		SENSOR_UNIT.append(Measure.SENSOR_CARDIO, R.string.sensor_unit_cardio);
		SENSOR_UNIT.append(Measure.SENSOR_TENSION, R.string.sensor_unit_tension);
		SENSOR_UNIT.append(Measure.SENSOR_TENSION_SYS, R.string.sensor_unit_tension);
		SENSOR_UNIT.append(Measure.SENSOR_TENSION_DIA, R.string.sensor_unit_tension);
		SENSOR_UNIT.append(Measure.SENSOR_OXY, R.string.sensor_unit_oxy);
		SENSOR_UNIT.append(Measure.SENSOR_POIDS, R.string.sensor_unit_poids);
		SENSOR_UNIT.append(Measure.SENSOR_TEMPERATURE, R.string.sensor_unit_temperature);
		
	}
	
	public static final SparseIntArray SENSOR_TITLE = new SparseIntArray(7);
	static{
		SENSOR_TITLE.append(Measure.SENSOR_CARDIO, R.string.sensor_title_cardio);
		SENSOR_TITLE.append(Measure.SENSOR_TENSION, R.string.sensor_title_tension);
		SENSOR_TITLE.append(Measure.SENSOR_TENSION_SYS, R.string.sensor_title_tension_sys);
		SENSOR_TITLE.append(Measure.SENSOR_TENSION_DIA, R.string.sensor_title_tension_dia);
		SENSOR_TITLE.append(Measure.SENSOR_OXY, R.string.sensor_title_oxy);
		SENSOR_TITLE.append(Measure.SENSOR_POIDS, R.string.sensor_title_poids);
		
	}
	public static final SparseIntArray SENSOR_LEGEND = new SparseIntArray(6);
	static{
		SENSOR_LEGEND.append(Measure.SENSOR_CARDIO, R.string.sensor_legend_cardio);
		SENSOR_LEGEND.append(Measure.SENSOR_TENSION, R.string.sensor_legend_tension);
		SENSOR_LEGEND.append(Measure.SENSOR_TENSION_SYS, R.string.sensor_legend_tension_sys);
		SENSOR_LEGEND.append(Measure.SENSOR_TENSION_DIA, R.string.sensor_legend_tension_dia);
		SENSOR_LEGEND.append(Measure.SENSOR_OXY, R.string.sensor_legend_oxy);
		SENSOR_LEGEND.append(Measure.SENSOR_POIDS, R.string.sensor_legend_poids);
		
	}
	

	public static final SparseIntArray SENSOR_NAME = new SparseIntArray(4);
	static{
		SENSOR_NAME.append(Measure.SENSOR_CARDIO, R.string.sensor_name_cardio);
		SENSOR_NAME.append(Measure.SENSOR_TENSION, R.string.sensor_name_tension);
		SENSOR_NAME.append(Measure.SENSOR_OXY, R.string.sensor_name_oxy);
		SENSOR_NAME.append(Measure.SENSOR_POIDS, R.string.sensor_name_poids);
		
	}
	
	public static final SparseIntArray SENSOR_ICON = new SparseIntArray(4);
	static{
		SENSOR_ICON.append(Measure.SENSOR_TENSION_SYS, R.drawable.icon_sensor_tension);
		SENSOR_ICON.append(Measure.SENSOR_OXY, R.drawable.icon_sensor_oxy);
		SENSOR_ICON.append(Measure.SENSOR_CARDIO, R.drawable.icon_sensor_cardio);
		SENSOR_ICON.append(Measure.SENSOR_POIDS, R.drawable.icon_sensor_balance);
		
	}
	
	public static final int DATABASE_VERSION = 15; //TODO : changer cette version pour faire un "OnUpdate" de la base et ainsi supprimer l'ancienne structure de la BDD de l'application
	public static final String DATABASE_NAME = "ecaropital";
	public static final String TABLE_PATIENT = "patient";
	public static final String TABLE_USER_CONSTANTS = "user_constant";
	public static final String TABLE_CONFIGURATION = "configuration";
	public static final String TABLE_MESURE = "mesure";
	public static final String TABLE_ALERTE = "alerte";
	public static final String TABLE_CAPTEUR = "capteur";
	
	
	public static final String CREATE_TABLE_PATIENT ="CREATE TABLE " + TABLE_PATIENT + " ("
	+"id BIGINT PRIMARY KEY ,"
	+"idUser TEXT NOT NULL,"
	+"gender INTEGER(1) NOT NULL,"
	+"name TEXT NOT NULL," // nom
	+"surname TEXT NOT NULL," // prenom
	+"symptome TEXT NOT NULL,"
	+"note TEXT NOT NULL,"
	+"noteDate TEXT NOT NULL" // format must be "YYYY-MM-DD HH:MM:SS.SSS" (ISO8601)
	+")";
	
	public static final String CREATE_TABLE_USER_CONSTANTS ="CREATE TABLE " + TABLE_USER_CONSTANTS + " ("
	+"id_patient BIGINT NOT NULL,"
	+"sensor INTEGER NOT NULL,"
	+"valeur REAL NOT NULL,"
	+"PRIMARY KEY (id_patient, sensor)"
	+")";
	
	public static final String CREATE_TABLE_CONFIGURATION ="CREATE TABLE " + TABLE_CONFIGURATION + " ("
	+"id INTEGER PRIMARY KEY NOT NULL,"
	+"portWrite INTEGER NOT NULL,"
	+"portHL7 INTEGER NOT NULL,"
	+"init INTEGER NOT NULL,"
	+"active INTEGER NOT NULL,"
	+"type INTEGER NOT NULL," // 0: pas definie, 1 : service, 2 : domicile
	+"idUserService TEXT NOT NULL,"
	+"nameOrEstablishment TEXT NOT NULL,"
	+"surnameOrService TEXT NOT NULL,"
	+"gender INTEGER NOT NULL," // 0: pas d�finie, 1: homme, ou 2: femme
	+"canUpdate INTEGER(1) NOT NULL," // 0: pas de MAJ de l'app, 1: MAJ lorsqu'il y en a une nouvelle version
	+"blocInfo TEXT NOT NULL"
	+")";
	
	public static final String CREATE_TABLE_MESURE ="CREATE TABLE " + TABLE_MESURE + " ("
	+"id INTEGER PRIMARY KEY AUTOINCREMENT,"
	+"id_patient TEXT NOT NULL,"
	+"sensor INTEGER NOT NULL," // l'identifiant du type de mesure
	//+"macAddress TEXT NOT NULL,"
	+"date BIGINT NOT NULL,"
	+"valeur REAL NOT NULL,"
	+"synchronized INTEGER(1) DEFAULT '0',"
	+"note TEXT NOT NULL,"
	+"noteDate TEXT NOT NULL" // format must be "YYYY-MM-DD HH:MM:SS.SSS" (ISO8601)
	+")";
	
	public static final String CREATE_TABLE_ALERTE ="CREATE TABLE " + TABLE_ALERTE + " ("
	+"id INTEGER PRIMARY KEY AUTOINCREMENT,"
	+"id_patient TEXT NOT NULL,"
	+"date BIGINT NOT NULL,"
	+"mesure INTEGER(1) NOT NULL,"
	+"niveau INTEGER(1) NOT NULL,"
	+"message TEXT NOT NULL,"
	+"note TEXT NOT NULL,"
	+"noteDate TEXT NOT NULL" // format must be "YYYY-MM-DD HH:MM:SS.SSS" (ISO8601)
	+")";
	
	public static final String CREATE_TABLE_CAPTEUR ="CREATE TABLE " + TABLE_CAPTEUR + " ("
	+"id INTEGER PRIMARY KEY AUTOINCREMENT,"
	+"id_type INT NOT NULL," // le numero par exemple tensiometre 18948
	+"type TEXT NOT NULL," // le type du capteur : tensiom�tre, p�se-personne, oxym�tre
	+"adresse_mac TEXT NOT NULL"
	+")";
}
