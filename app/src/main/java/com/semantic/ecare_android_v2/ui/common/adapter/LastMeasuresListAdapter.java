package com.semantic.ecare_android_v2.ui.common.adapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.AlertLevelComparator;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.NoteModel;
import com.semantic.ecare_android_v2.object.Patient;
import com.semantic.ecare_android_v2.ui.NoteDialogActivity;
import com.semantic.ecare_android_v2.util.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class LastMeasuresListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<CompoundMeasure> values;
	private Context context;
	private Activity parentActivity;
	
	

	public LastMeasuresListAdapter(Context context, ArrayList<CompoundMeasure> values,Activity parentActivity) {
		mInflater = LayoutInflater.from(context);
		this.context=context;
		this.values=values;
		this.parentActivity = parentActivity;
	}
	
	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public CompoundMeasure getItem(int position) {
		return values.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_last_measure_item, null);
			holder = new ViewHolder();

			holder.bgMeasureAlert = (LinearLayout) convertView.findViewById(R.id.bgMeasureAlert);
			holder.tvMeasureValue = (TextView) convertView.findViewById(R.id.tvMeasureValue);
			holder.tvMeasureDate = (TextView) convertView.findViewById(R.id.tvMeasureDate);
			holder.noteButton = (Button) convertView.findViewById(R.id.lastMeasureNoteButton);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		
		CompoundMeasure mesures = values.get(position);
		if(mesures.size()>0){
			
			ArrayList<Alert> alertes = new ArrayList<Alert>();
			/*Iterator<Mesure> it= mesures.iterator();
			while(it.hasNext()){
				Mesure mesure = it.next();
			}*/
		//System.out.println("nb de mesure "+mesures.size());
			for(int i=0;i<+mesures.size();i++){
				//System.out.println("itération "+i+" ; alerte = "+mesures.get(i).getAlerte());
				if(mesures.get(i).getAlert()!=null){
					alertes.add(mesures.get(i).getAlert());
				}
			}
			/*if(mesures.size()==1){
				if(mesures.get(0).getAlerte()!=null){
					alertes.add(mesures.get(0).getAlerte());
				}
			}else if(mesures.size()==3){//tension
				if(mesures.get(0).getAlerte()!=null){
					alertes.add(mesures.get(0).getAlerte());
				}
				if(mesures.get(1).getAlerte()!=null){
					alertes.add(mesures.get(1).getAlerte());
				}
				if(mesures.get(2).getAlerte()!=null){
					alertes.add(mesures.get(2).getAlerte());
				}
			}*/
			
	    	if(alertes.size()>0){
				Collections.sort(alertes, new AlertLevelComparator());
				
				Alert alerte = alertes.get(0);
				//System.out.println("alerte="+alerte.getNiveau());
				int background;
				switch(alerte.getLevel()){
					case 1:
						background=R.color.header_alert1;
						break;
					case 2:
						background=R.color.header_alert2;
						break;
					case 3:
						background=R.color.header_alert3;
						break;
					case 4:
						background=R.color.header_alert4;
						break;
					default:
						background=R.color.ecare_gray;
				}
				holder.bgMeasureAlert.setBackgroundColor(parent.getResources().getColor(background));
				holder.bgMeasureAlert.setVisibility(View.VISIBLE);
	    	}else{
	    		holder.bgMeasureAlert.setVisibility(View.INVISIBLE);
	    	}
			
	    	String measureValue="<html><b>"+getSensorType(mesures.get(0).getSensor())+" :</b> "+mesures.get(0).getValue();
	    	
			if(mesures.size()>=2){//tension avec ou sans PAM
				measureValue+="/"+mesures.get(1).getValue();
			}
			measureValue+=" "+getSensorUnite(mesures.get(0).getSensor())+"</html>";
			
			
			
			holder.tvMeasureValue.setText(Html.fromHtml(measureValue));
			holder.tvMeasureDate.setText("Il y a "+getDt(mesures.get(0).getDate()));
			
			final int measureId = mesures.get(0).getMeasureId();
	    	
			holder.noteButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
		
					Measure measure = findMeasureById(measureId);
					NoteModel model = new NoteModel(measure.getNote(), measure.getNoteDate(), measureId);
					Log.i(Constants.TAG, "Clicking on the measure " + measureId);
					Intent intent = new Intent(context, NoteDialogActivity.class);
					intent.putExtra(Constants.NOTEMODEL_KEY, model);
					parentActivity.startActivityForResult(intent, Constants.REQ_CODE_NOTEDIALOG_MEASURE);
					
				}
			});
			
		}else{//mesure nulle
			String mesureType="";
			switch(position){
				case 0:
					mesureType="saturation en dioxygène";
					break;
				case 1:
					mesureType="poids";
					break;
				case 2:
					mesureType="tension";
					break;
				case 3:
					mesureType="rythme cardiaque";
					break;
			}
			holder.tvMeasureValue.setText("Pas de mesure de "+mesureType);
			holder.tvMeasureDate.setVisibility(View.GONE);
			holder.bgMeasureAlert.setVisibility(View.INVISIBLE);
		}
	
		return convertView;

	}
	
	private Measure findMeasureById(int id){
		for(CompoundMeasure cm : values){
			Measure m = cm.findMeasureById(id);
			if(m != null){
				return m;
			}
		}
		return null;
	}
	
	
	private static class ViewHolder {
		LinearLayout bgMeasureAlert;
		TextView tvMeasureValue;
		TextView tvMeasureDate;
		Button noteButton;
	}
	
	
	private String getSensorType(int sensor){
		String type="Mesure";
		
		switch (sensor){
			case Measure.SENSOR_CARDIO:
				type="Rythme Cardiaque";
				break;

			case Measure.SENSOR_TENSION:
			case Measure.SENSOR_TENSION_DIA:
			case Measure.SENSOR_TENSION_SYS:
				type="Tension";
				break;

			case Measure.SENSOR_OXY:
				type="Saturation en O₂";
				break;

			case Measure.SENSOR_POIDS:
				type="Poids";
				break;
		
		
		}
		return type;
	}
	
	
	private String getSensorUnite(int sensor){
		String unite="Mesure";
		
		switch (sensor){
			case Measure.SENSOR_CARDIO:
				unite="bpm";
				break;

			case Measure.SENSOR_TENSION:
			case Measure.SENSOR_TENSION_DIA:
			case Measure.SENSOR_TENSION_SYS:
				unite="mmHg";
				break;

			case Measure.SENSOR_OXY:
				unite="%SpO₂";
				break;

			case Measure.SENSOR_POIDS:
				unite="kg";
				break;
		
		
		}
		return unite;
	}

	private String getDt(Date date){
		Date now = new Date();
		long now_long=now.getTime();
		long date_long=date.getTime();
		
		long dt = (now_long - date_long)/1000;
		
		String ret;
		if(dt<60){
			ret="moins d'une minute";
		}else if(dt<3600){
			int dtt=(int)dt/60;
			ret=dtt+" minute";
			if(dtt>1){
				ret+="s";
			}
		}else if(dt<86400){
			int dtt=(int)dt/3600;
			ret=dtt+" heure";
			if(dtt>1){
				ret+="s";
			}
		}else if(dt<(7*86400)){
			int dtt=(int)dt/86400;
			ret=dtt+" jour";
			if(dtt>1){
				ret+="s";
			}
		}else{
			int dtt=(int)dt/(7*86400);
			ret=dtt+" semaine";
			if(dtt>1){
				ret+="s";
			}
		}
		
		
		
		return ret;
	}
}
