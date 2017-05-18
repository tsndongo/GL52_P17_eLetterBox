package com.semantic.ecare_android_v2.util;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.AlertLevelComparator;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.NoteModel;
import com.semantic.ecare_android_v2.ui.AlertActivity;
import com.semantic.ecare_android_v2.ui.NoteDialogActivity;
import com.semantic.ecare_android_v2.ui.common.activity.GenericActivity;

import net.newel.android.Log;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class FunctionsUIMeasure {
	private static String CLASSNAME="fr.semantic.ecare.android.util.FunctionsUIMeasure";
	
	
	public static int getCompIcon(int i){
    	switch(i){
	    	case -1:
	    		return R.drawable.icon_comp_down;
	    	case 1:
	    		return R.drawable.icon_comp_up;
	    	case 0:
	    	default:
	    		return R.drawable.icon_comp_null;
    	}
	}

	
	
	public static void displayMesure(ArrayList<CompoundMeasure> mesures, final GenericActivity act, ServiceEcare mBoundService, boolean patientNotConnected, boolean displayDate, int sensorIdFromGraphic){
		//ArrayList<Alerte> alertes = new ArrayList<Alerte>();
		
		
    	if(mesures!=null && mesures.size()>0){
    		Log.i(Constants.TAG, CLASSNAME+" Affichage de "+mesures.size()+" mesure(s)");
    		//There is at least 1 CompoundMeasure

    		//Affichage de la mesure du milieu
    		displayMeasureContent(mesures, act, mBoundService, patientNotConnected,0, displayDate, sensorIdFromGraphic);
    		
    		

    	}else{
    		Log.w(Constants.TAG, CLASSNAME+" Pas de mesure à afficher !!!");
    	}

	}
	
	
	public static void displayMeasureContent(final ArrayList<CompoundMeasure> mesures, final GenericActivity act, final ServiceEcare mBoundService, final boolean patientNotConnected, int mesureIndex, final boolean displayDate, int sensorIdFromGraphic){
		mBoundService.sessionAction();
		
		CompoundMeasure cm=mesures.get(mesureIndex);
		if(sensorIdFromGraphic!=0){
			Log.i(Constants.TAG, CLASSNAME+" Recherche pour revenir directement à la bonne mesure");
			//change TENSION (generic) to Tension systolic
			if(sensorIdFromGraphic==Measure.SENSOR_TENSION){sensorIdFromGraphic=Measure.SENSOR_TENSION_SYS;}
			//Graphic Activity send the current Sensor Id
			//searching in the list if there is a measure with this sensorId
			for(int i=0;i<mesures.size();i++){
				CompoundMeasure mesure = mesures.get(i);
				if(mesure.get(0).getSensor()==sensorIdFromGraphic){
					//Found a measure with this sensor Id in the measure list sent
					cm = mesure;
					mesureIndex=i;
					Log.i(Constants.TAG, CLASSNAME+" Une mesure trouvée correspondant au type "+sensorIdFromGraphic+" donc, affichage direct !");
					break;
				}
			}
		}
				
		
		
		//Enregistrement de la mesure affichée dans les données de la classe 
		act.setDisplaidMesure(cm);
		
		//On enleve tous les panneaux d'alertes
		hideOldAlert(act);
		
		
		ImageView iv_sensor = (ImageView) act.findViewById(R.id.iv_sensor);
    	ImageView iv_comp = (ImageView) act.findViewById(R.id.iv_comp);
    	TextView tv_mesure = (TextView) act.findViewById(R.id.tv_measure_value);
    	RelativeLayout bandeauWarning = (RelativeLayout) act.findViewById(R.id.lWarning);
    	Button noteButton = (Button) act.findViewById(R.id.measureNoteButton);
    	TextView textWarning = (TextView) act.findViewById(R.id.tvWarning);
    	TextView textDate = (TextView) act.findViewById(R.id.tvDate);

    	ImageView iv_left = (ImageView) act.findViewById(R.id.iv_left);
    	ImageView iv_right = (ImageView) act.findViewById(R.id.iv_right);
    	
    	final int measureId = cm.get(0).getMeasureId();
    	
        noteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Measure measure = act.getmBoundService().getMeasure(measureId);
				NoteModel model = new NoteModel(measure.getNote(), measure.getNoteDate(), measure.getMeasureId());
				
				Intent intent = new Intent(act, NoteDialogActivity.class);
				intent.putExtra(Constants.NOTEMODEL_KEY, model);
				act.startActivityForResult(intent, Constants.REQ_CODE_NOTEDIALOG_MEASURE);
			}
		});
    	
    	if(displayDate){
    		textDate.setVisibility(View.VISIBLE);
    		textDate.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(cm.get(0).getDate()));
    	}else{
    		textDate.setVisibility(View.GONE);
    	}
    	
    	
    	
    	
    	
    	if(cm.get(0).isSync()){
    		//disabling delete button because the measure is already sync with the server
    		//buttonDelete.setVisibility(View.GONE);
    		//TODO : disable delete in menu
    	}else{
    		//buttonDelete.setVisibility(View.VISIBLE);
    		//TODO : enable delete in menu
    	}
    	
    	
    	
    	final int mesureIndex2=mesureIndex;
    	
    	if(mesureIndex>0){
    		//there is a previous measure
    		iv_left.setVisibility(View.VISIBLE);
    		iv_left.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					displayMeasureContent(mesures,act, mBoundService,patientNotConnected,mesureIndex2-1,displayDate,0);
				}
			});
    	}else{
    		iv_left.setVisibility(View.INVISIBLE);
    		iv_left.setOnClickListener(null);
    	}
    	
    	if(mesures.size()>mesureIndex+1){
    		//there is a next measure !
    		iv_right.setVisibility(View.VISIBLE);
    		iv_right.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					displayMeasureContent(mesures,act, mBoundService,patientNotConnected,mesureIndex2+1,displayDate,0);
				}
			});
    	}else{
    		iv_right.setVisibility(View.INVISIBLE);
    		iv_right.setOnClickListener(null);
    	}
    	
    	
    	
    	
		//Set Icons
		if(patientNotConnected){
			iv_comp.setVisibility(View.GONE);
		}else{
			iv_comp.setImageResource(getCompIcon(mBoundService.getMesureComp(cm)));
		}
		
    	iv_sensor.setImageResource(Constants.SENSOR_ICON.get(cm.get(0).getSensor()));
    	
		
    	//Set the information of the mesure
    	//And creating alert list
    	//And display override panel if necessary
    	
    	String unit=act.getResources().getString(Constants.SENSOR_UNIT.get(cm.get(0).getSensor()));
    	ArrayList<Alert> alertes = new ArrayList<Alert>();
    	
    	switch(cm.get(0).getSensor()){
    	
			case Measure.SENSOR_TENSION_SYS:
				
				tv_mesure.setText((int) cm.get(0).getValue()+"/"+(int) cm.get(1).getValue()+" "+unit);

				if((cm.get(0)!=null) && (cm.get(0).getAlert()!=null)){alertes.add(cm.get(0).getAlert());}
				if((cm.get(1)!=null) && (cm.get(1).getAlert()!=null)){alertes.add(cm.get(1).getAlert());}
				if((cm.get(2)!=null) && (cm.get(2).getAlert()!=null)){alertes.add(cm.get(2).getAlert());}
				
		
				//Sur mesureSetPatientActivity, Override impossible car pas de patient sélectionné, normalement
				if(mBoundService.getSelectedPatient()!=null){
				    if(((cm.get(0)!=null) && cm.get(0).isOverride()) || ((cm.get(1)!=null) && cm.get(1).isOverride()) || ((cm.get(2)!=null) && cm.get(2).isOverride())) {
				        bandeauWarning.setVisibility(View.VISIBLE);
				        textWarning.setText(R.string.message_alert_newMeasure);
				    }
				}
				break;
			
    		case Measure.SENSOR_OXY:
    		case Measure.SENSOR_CARDIO:
    		case Measure.SENSOR_POIDS:
    			tv_mesure.setText(cm.get(0).getValue()+" "+unit);
				if(cm.get(0).getAlert()!=null){alertes.add(cm.get(0).getAlert());}
				
				//Sur mesureSetPatientActivity, Override impossible car pas de patient sélectionné, normalement
				if(mBoundService.getSelectedPatient()!=null){
				    if(cm.get(0).isOverride()) {
				        bandeauWarning.setVisibility(View.VISIBLE);
				        textWarning.setText(R.string.message_alert_newMeasure);
				    }
				}
    			//la mesure suivante commence à l'indice 1 (cardio ?)
    			break;
    	}
    	
    	if(alertes.size()>0){
			Collections.sort(alertes, new AlertLevelComparator());
			
			//Affichage de la première alerte de la liste
			displayAlert(alertes.get(0),act);
    	}
	}
	
	private static void hideOldAlert(Activity act){
		RelativeLayout bandeauAlert1 = (RelativeLayout) act.findViewById(R.id.lAlert1);
		RelativeLayout bandeauAlert2 = (RelativeLayout) act.findViewById(R.id.lAlert2);
		RelativeLayout bandeauAlert3 = (RelativeLayout) act.findViewById(R.id.lAlert3);
		RelativeLayout bandeauAlert4 = (RelativeLayout) act.findViewById(R.id.lAlert4);
		
		RelativeLayout rlAlert3Info = (RelativeLayout) act.findViewById(R.id.rlAlert3Info);
		RelativeLayout rlAlert4Info = (RelativeLayout) act.findViewById(R.id.rlAlert4Info);
		
		//hide old alert Panel
		bandeauAlert1.setVisibility(View.GONE);
		bandeauAlert2.setVisibility(View.GONE);
		bandeauAlert3.setVisibility(View.GONE);
		bandeauAlert4.setVisibility(View.GONE);

		if(rlAlert3Info!=null){//null sur measureSetPatient
			rlAlert3Info.setVisibility(View.GONE);
		}
		if(rlAlert4Info!=null){//null sur measureSetPatient
			rlAlert4Info.setVisibility(View.GONE);
		}
	}
	
	private static void displayAlert(Alert alerte, final GenericActivity act){
		RelativeLayout bandeauAlert1 = (RelativeLayout) act.findViewById(R.id.lAlert1);
		RelativeLayout bandeauAlert2 = (RelativeLayout) act.findViewById(R.id.lAlert2);
		RelativeLayout bandeauAlert3 = (RelativeLayout) act.findViewById(R.id.lAlert3);
		RelativeLayout bandeauAlert4 = (RelativeLayout) act.findViewById(R.id.lAlert4);
		
		TextView tvAlert1 = (TextView) act.findViewById(R.id.tvAlert1);
		TextView tvAlert2 = (TextView) act.findViewById(R.id.tvAlert2);
		TextView tvAlert3 = (TextView) act.findViewById(R.id.tvAlert3);
		TextView tvAlert4 = (TextView) act.findViewById(R.id.tvAlert4);
		
		RelativeLayout rlAlert3Info = (RelativeLayout) act.findViewById(R.id.rlAlert3Info);
		RelativeLayout rlAlert4Info = (RelativeLayout) act.findViewById(R.id.rlAlert4Info);
		
		Button noteButton1 = (Button) act.findViewById(R.id.HeaderAlert1NoteButton);
		Button noteButton2 = (Button) act.findViewById(R.id.HeaderAlert2NoteButton);
		Button noteButton3 = (Button) act.findViewById(R.id.HeaderAlert3NoteButton);
		Button noteButton4 = (Button) act.findViewById(R.id.HeaderAlert4NoteButton);
		
		hideOldAlert(act);
		
		
		final Intent intentAlert = new Intent(act,AlertActivity.class);
		final OnClickListener alertOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				act.startActivity(intentAlert);
				act.finish();
			}
		};
		
		final int alerteId = alerte.getAlerteId();

		final OnClickListener noteOnClickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				Alert alerte = act.findAlertByIdInAllAlerts(alerteId);
				if(alerte == null){
					return;
				}
				NoteModel model = new NoteModel(alerte.getNote(), alerte.getNoteDate(), alerteId);
				
				Intent intent = new Intent(act.getApplicationContext(), NoteDialogActivity.class);
				intent.putExtra(Constants.NOTEMODEL_KEY, model);
				act.startActivityForResult(intent, Constants.REQ_CODE_NOTEDIALOG_ALERT);
			}
		};
		
    	switch(alerte.getLevel()){
    		case Alert.NIVEAU_FAIBLE:
    			 bandeauAlert1.setVisibility(View.VISIBLE);
    			 tvAlert1.setText(alerte.getMessage());
    			 noteButton1.setOnClickListener(noteOnClickListener);
    			 bandeauAlert1.setOnClickListener(alertOnClickListener);
    			 break;
    		case Alert.NIVEAU_MODERE:
    			 bandeauAlert2.setVisibility(View.VISIBLE);
    			 tvAlert2.setText(alerte.getMessage());
    			 noteButton2.setOnClickListener(noteOnClickListener);
    			 bandeauAlert2.setOnClickListener(alertOnClickListener);
    			 break;
    		case Alert.NIVEAU_ELEVE:
    			 bandeauAlert3.setVisibility(View.VISIBLE);
    			 tvAlert3.setText(alerte.getMessage());
    			 bandeauAlert3.setOnClickListener(alertOnClickListener);
    			 noteButton3.setOnClickListener(noteOnClickListener);
    			 //Display also the more info panel
    			 if(rlAlert3Info!=null){//null on measureSetPatient
    				 rlAlert3Info.setVisibility(View.VISIBLE);
    				 //tvAlert3Info.setText(Html.fromHtml("<html>- "+alerte.getMessage()+"<br><center>Contactez votre médecin au plus vite !</center></html>"));
    			 }
    			 
    			 break;
    		case Alert.NIVEAU_CRITIQUE:
    			 bandeauAlert4.setVisibility(View.VISIBLE);
    			 tvAlert4.setText(alerte.getMessage());
    			 bandeauAlert4.setOnClickListener(alertOnClickListener);
    			 noteButton4.setOnClickListener(noteOnClickListener);
    			 //Display also the more info panel
    			 if(rlAlert4Info!=null){//null on measureSetPatient
    				 rlAlert4Info.setVisibility(View.VISIBLE);
    				 //tvAlert4Info.setText(Html.fromHtml("<html>- "+alerte.getMessage()+"<br><center>Contactez votre médecin au plus vite !</center></html>"));
    			 }
    			 break;
    	}
	    
	}
	
}
