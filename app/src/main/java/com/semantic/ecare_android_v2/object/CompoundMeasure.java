package com.semantic.ecare_android_v2.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import com.semantic.ecare_android_v2.util.Constants;

import net.newel.android.Log;


public class CompoundMeasure implements Serializable {
	private static final long serialVersionUID = -3894670361645793615L;
	
	private ArrayList<Measure> measures;
	
	public CompoundMeasure(){
		measures=new ArrayList<Measure>();
	}
	
	
	
	public Measure get(int idx){
		Measure measure=null;
		try{
			measure = measures.get(idx);
		}catch (IndexOutOfBoundsException e){
			Log.w(Constants.TAG,"IndexOutOfBoundsException ; Certainement que nous utilisons une mesure de tension générée par le reset (qui ne génère pas le PAM !)");
		}
		return measure;
	}
	
	public void add(Measure measure){
		measures.add(measure);
	}
	
	public int size(){
		return measures.size();
	}
	
	public Measure findMeasureById(int id){
		for(Measure m : measures){
			if(m.getMeasureId() == id){
				return m;
			}
		}
		return null;
	}
	
	public Iterator<Measure> iterator(){
		return measures.iterator();
	}
	
}
