package com.semantic.ecare_android_v2.object;

import java.io.Serializable;
import java.util.Date;

public class Alert  implements Serializable{
	private static final long serialVersionUID = -3306242857172906354L;
	
	public static final int NIVEAU_FAIBLE=1;
	public static final int NIVEAU_MODERE=2;
	public static final int NIVEAU_ELEVE=3;
	public static final int NIVEAU_CRITIQUE=4;
	
	
	private String patientId;
	private Date date;
	private int measureId;
	private int alertId;
	private int level;
	private String message;
	private Measure completeMeasure;
	private String note="";
	private String noteDate="";
	
	
	public Alert(String id_patient, Date date, int measure, int level, String message, String note, String noteDate) {
		this.patientId=id_patient;
		this.date=date;
		this.measureId=measure;
		this.level=level;
		this.message=message;
		this.note=note;
		this.noteDate=noteDate;
	}
	
	public Alert(int id, String id_patient, Date date, int measure, int level, String message, String note, String noteDate) {
		this.alertId = id;
		this.patientId=id_patient;
		this.date=date;
		this.measureId=measure;
		this.level=level;
		this.message=message;
		this.note=note;
		this.noteDate=noteDate;
	}
	
	public Alert(String id_patient, int measure, Date date, int level, String message, String note, String noteDate) {
		this(id_patient,date,measure,level,message, note, noteDate);
	}
	
	public Alert(String id_patient, Date date, int level, String message) {
		this.patientId=id_patient;
		this.date=date;
		this.level=level;
		this.message=message;
	}
	
	public Alert(String id_patient, Date date, int level, String message, int alertId) {
		this.patientId=id_patient;
		this.date=date;
		this.level=level;
		this.message=message;
		this.alertId=alertId;
	}
	
	public Alert(String id_patient, Date date, int level, String message, int alertId, String note, String noteDate) {
		this.patientId=id_patient;
		this.date=date;
		this.level=level;
		this.message=message;
		this.alertId=alertId;
		this.note = note;
		this.noteDate = noteDate;
	}
	
	public Alert(Date date, int level, String message, int alertId, String note, String noteDate) {
		this.date=date;
		this.level=level;
		this.message=message;
		this.alertId=alertId;
		this.note = note;
		this.noteDate = noteDate;
	}
	
	public void setMeasureId(int id){
		this.measureId=id;
	}
	
	public void setAlertId(int alertId) {
		//ce champ permet de connaitre l'ID de la mesure dans la base de données. pour une suppression plus facile !
		this.alertId=alertId;
	}
	
	public int getAlerteId(){
		return alertId;
	}
	
	public String getPatientId() {
		return patientId;
	}
	public Date getDate() {
		return date;
	}
	public int getMeasure() {
		return measureId;
	}
	public int getLevel() {
		return level;
	}
	public String getMessage() {
		return message;
	}
	
	public void setNote(String note){
		this.note=note;
	}
	
	public void setNoteDate(String noteDate){
		this.noteDate=noteDate;
	}
	
	public String getNote(){
		return note;
	}
	
	public String getNoteDate(){
		return noteDate;
	}
	
	public void setCompleteMeasure(Measure measure) {
		this.completeMeasure=measure;
	}
	public Measure getCompleteMeasure(){
		return completeMeasure;
	}
	
	public void updateNote(NoteModel note){
		this.note = note.getNote();
		this.noteDate = note.getNoteDate();
	}
	
}
