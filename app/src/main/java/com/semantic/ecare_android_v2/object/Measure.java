package com.semantic.ecare_android_v2.object;


import java.io.Serializable;
import java.util.Date;

public class Measure implements Serializable{
	private static final long serialVersionUID = -6592291536895156592L;
	
	
	public static final int SENSOR_TENSION=18948;
	public static final int SENSOR_TENSION_SYS=18949;
	public static final int SENSOR_TENSION_DIA=18950;
	public static final int SENSOR_POIDS=57664;
	public static final int SENSOR_OXY=19384;
	public static final int SENSOR_CARDIO=18474;
	public static final int SENSOR_CARDIO_OXY=18458;//Cardio via Oxy(replace this value by TYPE_CARDIO)
	public static final int SENSOR_TEMPERATURE=19320;
	
	private int sensor;
	private Date date;
	private double value;
	private Alert alert;
	private boolean override=false;
	private int measureId;
	private String patientId; //used to build list for synchronized
	//private String deviceId=""; //used to build list for synchronized
	private boolean sync=false;
	private String note="";
	private String noteDate="";
	
	
	public Measure(int measureId, int sensor, Date date, String patient_id, double value, boolean sync, String note, String noteDate) {
		this.measureId=measureId;
		this.sensor=sensor;
		this.value=value;
		this.date=date;
		this.patientId=patient_id;
		this.sync=sync;
		this.note=note;
		this.noteDate=noteDate;
	}
	
	public Measure(int sensor, double value, Date date, boolean sync) {
		this.sensor=sensor;
		this.value=value;
		this.date=date;
		this.sync=sync;
	}
	
	public Measure(int sensor, double value, Date date, boolean sync, int id) {
		this.sensor=sensor;
		this.value=value;
		this.date=date;
		this.sync=sync;
		this.measureId=id;
	}
	
	public Measure(int sensor, double value, Date date, boolean sync, boolean override) {
		this(sensor, value, date,sync);
		this.override=override;
	}
	
	public Measure(int sensor, Date date, String patient_id, double value, boolean sync) {
		this.sensor=sensor;
		this.value=value;
		this.date=date;
		this.patientId=patient_id;
		this.sync=sync;
	}
	
	
	public void updateNote(NoteModel note){
		this.note = note.getNote();
		this.noteDate = note.getNoteDate();
	}

	public void setAlerte(Alert alert){
		this.alert=alert;
	}
	public Alert getAlert(){
		return alert;
	}
	
	public void setOverride(boolean override){
		this.override=override;
	}
	public boolean isOverride(){
		return override;
	}
	public int getSensor(){
		return sensor;
	}
	public Date getDate(){
		return date;
	}
	public double getValue(){
		return value;
	}
	public void setMeasureId(int mesureId) {
		//This field allows the application to know the measure ID in the local database to delete easily
		this.measureId=mesureId;
	}
	
	public int getMeasureId(){
		return measureId;
	}
	
	public String getPatientId(){
		return patientId;
	}
	
	public String getNote(){
		return note;
	}
	
	public String getNoteDate(){
		return noteDate;
	}
	
	public void setNote(String note){
		this.note=note;
	}
	
	public void setNoteDate(String noteDate){
		this.noteDate=noteDate;
	}
	
	public boolean isSync(){
		return sync;
	}
	public void setPatientId(String uid) {
		patientId=uid;
	}
}
