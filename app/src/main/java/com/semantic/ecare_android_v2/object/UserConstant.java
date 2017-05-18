package com.semantic.ecare_android_v2.object;

import java.io.Serializable;

public class UserConstant{
	private double value=50;
	private int sensor=0;
	private String patientId;
	
	
	public UserConstant(String patientId, double value, int sensor){
		this.patientId=patientId;
		this.value=value;
		this.sensor=sensor;
	}


	public double getValue() {
		return value;
	}


	public void setValue(double value) {
		this.value = value;
	}


	public int getSensor() {
		return sensor;
	}


	public void setSensor(int sensor) {
		this.sensor = sensor;
	}
	
	public String getPatientId(){
		return patientId;
	}
	
}
