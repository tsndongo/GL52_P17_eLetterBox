package com.semantic.ecare_android_v2.object;

public class InferenceParametre {
	private double value;
	private long date;
	
	
	public InferenceParametre(double value, long date){
		this.value=value;
		this.date=date;
	}
	
	public double getValue(){
		return value;
	}
	public long getDate(){
		return date;
	}
}
