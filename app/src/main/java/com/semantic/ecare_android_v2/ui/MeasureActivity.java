package com.semantic.ecare_android_v2.ui;


import java.util.ArrayList;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceAntidoteClient;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.NoteModel;
import com.semantic.ecare_android_v2.ui.common.activity.GenericConnectedActivity;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.FunctionsUIMeasure;

import net.newel.android.Log;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MeasureActivity extends GenericConnectedActivity{
	
	private final int DIALOG_DELETE_MEASURE=1;

	private String CLASSNAME=this.getClass().getName();
	private AlertDialog dialog=null;
	private ArrayList<CompoundMeasure> mesures;
	private boolean displayDate;
	private boolean returnToGraphView=false;
	private int sensorIdFromGraphic=0;

    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i(Constants.TAG, CLASSNAME+" Oncreate Class");
        
        Bundle extras = getIntent().getExtras();
	    if(extras !=null){
	    	mesures = (ArrayList<CompoundMeasure>) extras.getSerializable("mesures");
	    	displayDate=extras.getBoolean("GraphMesure");
	    	returnToGraphView=extras.getBoolean("returnToGraphView");
	    	sensorIdFromGraphic=extras.getInt("sensor");
	    }
    }
    
	@Override
	protected void affichage_before_binding() {
		setContentView(R.layout.activity_mesure);
	}
    
	@Override
	protected void affichage(){//this method is called ONLY after service binding !
		super.affichage();
	    if(mesures==null){
	    	//OR : load CM from service Ecare
	    	mesures=mBoundService.getMeasuresListContext();
	    	//get the sensorNumber from graphic activity and display the sensorid measure immediately
	    	displayDate=true;
	    	//TODO : afficher les alertes générées pour ces mesures (il faut les charger déja dans le modèle)
	    }
	    
	    if(mesures.size()==0){
	    	Log.w(Constants.TAG, CLASSNAME+" Affichage du panneau de mesure SANS mesure à afficher !");
			Intent i = new Intent(getApplicationContext(), WaitActivity.class);
			startActivity(i);
    		finish();
	    }
		FunctionsUIMeasure.displayMesure(mesures, this,mBoundService, false, displayDate, sensorIdFromGraphic);
     }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_mesure, menu);
        return true;
    }
    
	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_close:
			Intent iw = null;
			if(returnToGraphView){//Signifie qu'on est venu depuis l'affichage des graphiques, on y retourne
				iw = new Intent(getApplicationContext(), ChartActivity.class);
				int sensor =  displaidMesure.get(0).getSensor();
				if(sensor==Measure.SENSOR_TENSION_SYS){sensor=Measure.SENSOR_TENSION;}
				iw.putExtra("sensor", sensor);
			}else{//Sinon, direction Wait
				if(ServiceEcare.getConfigurationList().getType()==1)
					iw = new Intent(getApplicationContext(), WaitActivity.class);
				else if(ServiceEcare.getConfigurationList().getType()==2)
					iw = new Intent(getApplicationContext(), WaitActivityPatient.class);
			}
			startActivity(iw);
			finish();
			break;
			
		case R.id.menu_delete:
			showDialog(DIALOG_DELETE_MEASURE);
			break;
			
		case R.id.menu_deco:
			disconnectPatient();
			break;

		case R.id.menu_graph:
			Intent ig = new Intent(getApplicationContext(), ChartActivity.class);
			int sensor =  displaidMesure.get(0).getSensor();
			if(sensor==Measure.SENSOR_TENSION_SYS){sensor=Measure.SENSOR_TENSION;}
			ig.putExtra("sensor", sensor);
			startActivity(ig);
			finish();
			break;
			
		case R.id.menu_alert:
			Intent ia = new Intent(getApplicationContext(), AlertActivity.class);
			startActivity(ia);
			finish();
			break;
			
		case R.id.menu_test_sensor_tension:	
			mBoundService.launchFireNewMeasure(Measure.SENSOR_TENSION);
			mBoundService.launchFireNewMeasure(Measure.SENSOR_CARDIO);
			break;	
			
		case R.id.menu_test_sensor_oxy:	
			mBoundService.launchFireNewMeasure(Measure.SENSOR_OXY);
			break;	
			
		case R.id.menu_test_sensor_balance:	
			mBoundService.launchFireNewMeasure(Measure.SENSOR_POIDS);
			break;

		}
		return true;
	}
	
	@Override
	protected Dialog onCreateDialog(final int id) {
		switch(id){
			case DIALOG_DELETE_MEASURE:
				//Before deleting this measure check if not already synchronized
				if(!displaidMesure.get(0).isSync()){
					
					
				    Builder builder = new AlertDialog.Builder(this);
				    builder.setTitle("Suppression de Mesure");
				    builder.setMessage("Voulez-vous supprimer cette mesure ?");
				    builder.setCancelable(true);
				    builder.setPositiveButton("Oui", new DialogInterface.OnClickListener(){
	
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mBoundService.sessionAction();
							mBoundService.deleteMeasures(displaidMesure);
							
							if(displaidMesure.size()>0){
								//si autre mesure dans l'arrayList des mesures du contexte, passer automatiquement sur celle là !!!
								//(displayd_mesure is switched to the previous value)
								mesures.remove(displaidMesure);
								if(mesures.size()>0){
									//left measure(s) to display
									FunctionsUIMeasure.displayMeasureContent(mesures,MeasureActivity.this, mBoundService,false,0,displayDate,0);
								}else{
									//no measure to display : return to waitActivity
									Intent iw = null;
									if(ServiceEcare.getConfigurationList().getType()==1)
										iw = new Intent(getApplicationContext(), WaitActivity.class);
									else if(ServiceEcare.getConfigurationList().getType()==2)
										iw = new Intent(getApplicationContext(), WaitActivityPatient.class);
									startActivity(iw);
									finish();
								}
							}else{
								Intent iw;
								if(returnToGraphView){//Signifie qu'on est venu depuis l'affichage des graphiques, on y retourne
									iw = new Intent(getApplicationContext(), ChartActivity.class);
									int sensor =  displaidMesure.get(0).getSensor();
									if(sensor==Measure.SENSOR_TENSION_SYS){sensor=Measure.SENSOR_TENSION;}
									iw.putExtra("sensor", sensor);
								}else{//go to WaitActivity
									iw = new Intent(getApplicationContext(), WaitActivity.class);
								}
								startActivity(iw);
								finish();
							}
							
						}
				    	  
				    });
				    builder.setNegativeButton("Non", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
							mBoundService.sessionAction();}
				    });
				    dialog = builder.create();
				    dialog.show();
				}else{
					Builder builder = new AlertDialog.Builder(this);
				    builder.setTitle("Suppression impossible");
				    builder.setMessage("Cette mesure a déjà été synchronisée avec le serveur et ne peut plus être supprimée");
				    builder.setCancelable(true);
				    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener(){
						@Override
						public void onClick(DialogInterface dialog, int which) {
						}  
				    });
				    dialog = builder.create();
				    dialog.show();
				}
				break;
				
		}
		
		return null;

	}
	
	
	@Override
	protected void newMeasureReceived(ArrayList<CompoundMeasure> mesures){
		if(dialog!=null && dialog.isShowing()){
			Log.i(Constants.TAG, CLASSNAME+" closing delete measure Dialog");
			dialog.cancel();
		}

		//ATTENTION : pour fonctionner, finish doit être DEVANT le intent.		
		Intent i = new Intent(getApplicationContext(), ChartActivity.class);
		i.removeExtra("mesures");
		i.putExtra("mesures", mesures);
		startActivity(i);
		finish();
	}
	

	@Override
	protected void onKeyBack() {
		Intent iw = null;
		if(returnToGraphView){//Signifie qu'on est venu depuis l'affichage des graphiques, on y retourne
			iw = new Intent(getApplicationContext(), ChartActivity.class);
			int sensor =  displaidMesure.get(0).getSensor();
			if(sensor==Measure.SENSOR_TENSION_SYS){sensor=Measure.SENSOR_TENSION;}
			iw.putExtra("sensor", sensor);
		}else{//Sinon, direction Wait
			if(ServiceEcare.getConfigurationList().getType()==1)
				iw = new Intent(getApplicationContext(), WaitActivity.class);
			else if(ServiceEcare.getConfigurationList().getType()==2)
				iw = new Intent(getApplicationContext(), WaitActivityPatient.class);
		}
		startActivity(iw);
		finish();
	}
	

}
