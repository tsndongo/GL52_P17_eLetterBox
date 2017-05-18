package com.semantic.ecare_android_v2.object;

import java.io.Serializable;

public class NoteModel implements Serializable{

	/* TODO : also add the type of the target of the note, and its ID */
	private static final long serialVersionUID = -8581660173932579767L;
	private boolean hasBeenEdited;
	private int targetId;
	private int targetTypeId; // patient, measure or alert
	private String note;
	private String noteDate;
	
	public NoteModel(){
		
	}
	
	public NoteModel(String note,String noteDate, int targetId){
		this.note = note;
		this.noteDate = noteDate;
		this.hasBeenEdited = false;
		this.targetId = targetId;
	}
	
	public NoteModel(String note,String noteDate, int targetId, int targetTypeId){
		this.note = note;
		this.noteDate = noteDate;
		this.hasBeenEdited = false;
		this.targetId = targetId;
		this.targetTypeId = targetTypeId;
	}
	
	public NoteModel(String note,String noteDate){
		this.note = note;
		this.noteDate = noteDate;
		this.hasBeenEdited = false;
	}
	
	public NoteModel(boolean hasBeenEdited, String note, String noteDate){
		this.hasBeenEdited = hasBeenEdited;
		this.note = note;
		this.noteDate = noteDate;
	}

	public boolean hasBeenEdited() {
		return hasBeenEdited;
	}

	public void setHasBeenEdited(boolean hasBeenEdited) {
		this.hasBeenEdited = hasBeenEdited;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getNoteDate() {
		return noteDate;
	}

	public void setNoteDate(String noteDate) {
		this.noteDate = noteDate;
	}

	public int getTargetId() {
		return targetId;
	}

	public void setTargetId(int targetId) {
		this.targetId = targetId;
	}

	public int getTargetTypeId() {
		return targetTypeId;
	}

	public void setTargetTypeId(int targetTypeId) {
		this.targetTypeId = targetTypeId;
	}
	
	
}
