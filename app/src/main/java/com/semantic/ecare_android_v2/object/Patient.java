package com.semantic.ecare_android_v2.object;

import java.io.Serializable;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

public class Patient{
	private int id;
	private String uid;
	private int gender=1;
	private String name="";
	private String surname="";
	private SparseArray<ArrayList<UserConstant>> userConstants;
	private String symptome="";
	private String note="";
	private String noteDate="";
	
	//TODO : Avoir aussi al liste des constantes médicales
	
	
	
	public Patient(int id,String uid, int gender, String name, String surname, String symptome, String note, String noteDate){
		this.id=id;
		this.uid=uid;
		this.gender=gender;
		this.name=name;
		this.surname=surname;
		this.symptome=symptome;
		this.note=note;
		this.noteDate=noteDate;
		
		this.userConstants = new SparseArray<ArrayList<UserConstant>>();
	}	
	
	public int getGender() {
		return gender;
	}
	public void setGender(int gender) {
		this.gender = gender;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getSurname() {
		return surname;
	}
	public void setSurname(String surname) {
		this.surname = surname;
	}
	public SparseArray<ArrayList<UserConstant>> getUserConstants() {
		return userConstants;
	}
	public void setRanges(SparseArray<ArrayList<UserConstant>> userConstants) {
		this.userConstants = userConstants;
	}
	public String getSymptome(){
		return symptome;
	}
	public void setSymptome(String symptome){
		this.symptome=symptome;
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

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
	
	public void updateNote(NoteModel note){
		this.note = note.getNote();
		this.noteDate = note.getNoteDate();
	}

	@Override
	public String toString() {
		return "Patient{" +
				"id=" + id +
				", uid='" + uid + '\'' +
				", gender=" + gender +
				", name='" + name + '\'' +
				", surname='" + surname + '\'' +
				", userConstants=" + userConstants +
				", symptome='" + symptome + '\'' +
				", note='" + note + '\'' +
				", noteDate='" + noteDate + '\'' +
				'}';
	}
}
