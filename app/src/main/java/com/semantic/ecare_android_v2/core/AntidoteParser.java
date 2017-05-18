package com.semantic.ecare_android_v2.core;


import java.util.Date;

import net.newel.android.Log;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.Functions;



public class AntidoteParser {
	private static String CLASSNAME=AntidoteParser.class.getName();

	//TODO
	//FIXME : TRES IMPORTANT : faire un premier filtrage des mesures : 0 < oxy <120 ; poids >0 ...
	
	
	public static CompoundMeasure parseWeight(Document d){
		CompoundMeasure cm = new CompoundMeasure();
		
		//global
		Element entries_element = (Element) d.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(1);
		
		//get value
		String nodeValue = entries_element.getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(2).getChildNodes().item(0).getNodeValue();
		if(!nodeValue.equals("NaN")){
			double value = Double.parseDouble(nodeValue);
			value = Functions.round(value, 1);
			if(value>20 && value<200){
				//get Date (global)
				//FIXME : Disabled while sensors are not up to date
				//Element entries_element_date = (Element) entries_element.getChildNodes().item(1).getChildNodes().item(0).getChildNodes().item(1);
				//Date date = getDateFromElementDate(entries_element_date);
				Date date = new Date();
				cm.add(new Measure(Measure.SENSOR_POIDS,value,date,false));
			}else{
				Log.e(Constants.TAG, CLASSNAME+" ATTENTION : mesure POIDS en dehors de la plage normale : entre 0 et 200 kg");
			}
		}
		return cm;
	}	
	
	public static CompoundMeasure parseOxymeter(Document d) {
		Element datalist_element = (Element) (d.getChildNodes()).item(0);
		
		CompoundMeasure cm = new CompoundMeasure();
		
		for(int i=0;i<2;i++){
			int sensor_type = Integer.parseInt(datalist_element.getChildNodes().item(i).getChildNodes().item(1).getChildNodes().item(1).getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue());
			//fix the sensor id for heart beat integrated in dioxygen saturation
			if(sensor_type==Measure.SENSOR_CARDIO_OXY){sensor_type=Measure.SENSOR_CARDIO;}
			
			Log.i(Constants.TAG, CLASSNAME+" sensor type : " + sensor_type);
			//global
			Element entries_element = (Element) datalist_element.getChildNodes().item(i).getChildNodes().item(1).getChildNodes().item(1);
			
			
			
			//get value
			String nodeValue = entries_element.getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(2).getChildNodes().item(0).getNodeValue();
			if(!nodeValue.equals("NaN")){
				double value = Double.parseDouble(nodeValue);
				value = Functions.round(value, 1);
				if((sensor_type==Measure.SENSOR_CARDIO && (value>=20 && value<=250))
					|| (sensor_type==Measure.SENSOR_OXY && (value>=70 && value<=100))){
					//get Date (global)
					//FIXME : Disabled while sensors are not up to date
					//Element entries_element_date = (Element) entries_element.getChildNodes().item(1).getChildNodes().item(0).getChildNodes().item(1);			
					//Date date = getDateFromElementDate(entries_element_date);
					Date date = new Date();
					cm.add(new Measure(sensor_type,value,date,false));
				}else{
					if(sensor_type==Measure.SENSOR_CARDIO){
						Log.e(Constants.TAG, CLASSNAME+" ATTENTION : mesure CARDIO (oxy) en dehors de la plage normale :  entre 20 et 250 bpm");
					}
					if(sensor_type==Measure.SENSOR_OXY){
						Log.e(Constants.TAG, CLASSNAME+" ATTENTION : mesure OXY en dehors de la plage normale : entre 70 et 100 %SPO2");
					}
				}
			}
		}	
			
			
		
		return cm;
	}
	
	

	
	public static CompoundMeasure parseCardio(Document d) {
		CompoundMeasure cm = new CompoundMeasure();
		
		//global
		Element entries_element = (Element) d.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(1);
		
		
		
		//get value
		String nodeValue = entries_element.getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(2).getChildNodes().item(0).getNodeValue();
		if(!nodeValue.equals("NaN")){
			double value = Double.parseDouble(nodeValue);
			value = Functions.round(value, 1);
			if(value>40 && value<200){
				//get Date (global)
				//FIXME : Disabled while sensors are not up to date
				//Element entries_element_date = (Element) entries_element.getChildNodes().item(1).getChildNodes().item(0).getChildNodes().item(1);
				//Date date = getDateFromElementDate(entries_element_date);
				Date date = new Date();
				cm.add(new Measure(Measure.SENSOR_CARDIO,value,date,false));
			}else{
				Log.e(Constants.TAG, CLASSNAME+" ATTENTION : mesure CARDIO(tension) en dehors de la plage normale : entre 40 et 200 bpm");
			}
		}
		return cm;
	}

	
	
	
	public static CompoundMeasure parseTension(Document d) {
		CompoundMeasure cm = new CompoundMeasure();
		
		//global
		Element entries_element = (Element) d.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(1);

		Element entries_values_element = (Element) entries_element.getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(1);
		
		
		//get value
		int id1 = Integer.parseInt(entries_values_element.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue());
		int id2 = Integer.parseInt(entries_values_element.getChildNodes().item(1).getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue());
		int id3 = Integer.parseInt(entries_values_element.getChildNodes().item(2).getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue());
		
		String nodeValue1=entries_values_element.getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(2).getChildNodes().item(0).getNodeValue();
		String nodeValue2=entries_values_element.getChildNodes().item(1).getChildNodes().item(1).getChildNodes().item(2).getChildNodes().item(0).getNodeValue();
		String nodeValue3=entries_values_element.getChildNodes().item(2).getChildNodes().item(1).getChildNodes().item(2).getChildNodes().item(0).getNodeValue();
		
		//get Date (global)
		//FIXME : Disabled while sensors are not up to date
		//Element entries_element_date = (Element) entries_element.getChildNodes().item(1).getChildNodes().item(0).getChildNodes().item(1);
		//Date date = getDateFromElementDate(entries_element_date);
		Date date = new Date();
		
		if(!nodeValue1.equals("NaN")){
			double value1 = Double.parseDouble(nodeValue1);
			if(value1>20 && value1<280){
				value1 = Functions.round(value1, 1);
				cm.add(new Measure(id1,value1,date,false));
			}else{
				Log.e(Constants.TAG, CLASSNAME+" ATTENTION : mesure TENSION_SYS en dehors de la plage normale : entre 20 et 280 mmHg");
			}
		}
		if(!nodeValue2.equals("NaN")){
			double value2 = Double.parseDouble(nodeValue2);
			if(value2>20 && value2<280){
				value2 = Functions.round(value2, 1);
				cm.add(new Measure(id2,value2,date,false));
			}else{
				Log.e(Constants.TAG, CLASSNAME+" ATTENTION : mesure TENSION_DIA en dehors de la plage normale : entre 20 et 280 mmHg");
			}
		}
		if(!nodeValue3.equals("NaN")){
			double value3 = Double.parseDouble(nodeValue3);
			if(value3>20 && value3<280){
				value3 = Functions.round(value3, 1);
				cm.add(new Measure(id3,value3,date,false));
			}else{
				Log.e(Constants.TAG, CLASSNAME+" ATTENTION : mesure TENSION_MOY en dehors de la plage normale : entre 20 et 280 mmHg");
			}
		}
		
		
		
		return cm;
	}
}
