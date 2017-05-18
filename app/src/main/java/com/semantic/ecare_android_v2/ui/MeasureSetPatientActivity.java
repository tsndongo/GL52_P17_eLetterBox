package com.semantic.ecare_android_v2.ui;


import java.util.ArrayList;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceAntidoteClient;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.Patient;
import com.semantic.ecare_android_v2.ui.common.activity.GenericDisconnectedActivity;
import com.semantic.ecare_android_v2.ui.common.adapter.MesureSetPatientListAdapter;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.FunctionsUIMeasure;

import net.newel.android.Log;
import android.os.Bundle;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class MeasureSetPatientActivity extends GenericDisconnectedActivity{

	private String CLASSNAME=this.getClass().getName();
	private final int DIALOG_DELETE_MEASURE=1;
	
	private AlertDialog dialog=null;
	private ArrayList<CompoundMeasure> mesures;
	private boolean eraseMeasure; //used if a lot of measues comes without patient connected !
	
	
    @SuppressWarnings("unchecked")
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Bundle extras = getIntent().getExtras();
	    if(extras !=null){
	    	mesures = (ArrayList<CompoundMeasure>) extras.getSerializable("mesures");
	    	eraseMeasure = extras.getBoolean("eraseMeasure"); //true if a new measure comes without selected patient
	    }
	    
        //Button button=(Button) findViewById(R.id.button1);
               
        //deplier bandeau warning et message : renseigner patient
        RelativeLayout bandeauWarning = (RelativeLayout) findViewById(R.id.lWarning);
        TextView textWarning = (TextView) findViewById(R.id.tvWarning);
        bandeauWarning.setVisibility(View.VISIBLE);
        textWarning.setText(R.string.message_alert_set_patient);
        

   
        if(eraseMeasure){
        	//TOUJOURS PAS de patient choisi ! Affichage d'une erreur ce coup ci !
	        RelativeLayout bandeauError = (RelativeLayout) findViewById(R.id.lError);
	        TextView textError = (TextView) findViewById(R.id.tvError);
	        ImageView ivError = (ImageView) findViewById(R.id.ivBack);
	        bandeauError.setVisibility(View.VISIBLE);
	        bandeauWarning.setVisibility(View.GONE);
	        ivError.setVisibility(View.GONE);
	        textError.setText(R.string.message_alert_set_patient_error);
        }
        
    }
    
	@Override
	protected void affichage_before_binding() {
		setContentView(R.layout.activity_mesure_set_patient);
	}
	
	@Override
    protected void affichage(){
    	super.affichage();
    	
    	if(mBoundService==null || mBoundService.getPatientList()==null || mesures.size()==0){
    		Log.w(Constants.TAG, CLASSNAME+" LANCEMENT DE MESURE SET PATIENT avec un mBoundService NULL ou une liste de mesure vide !");
			Intent i = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(i);
    		finish();
    	}else{
	    	Log.i(Constants.TAG,CLASSNAME+" display "+mBoundService.getPatientList().size()+" patient(s)");
	
	    	
	    	ListView listview = (ListView) findViewById(R.id.listViewPatients);
	    	listview.setAdapter(new MesureSetPatientListAdapter(getApplicationContext(), mBoundService.getPatientList()));
	    	
	        listview.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
					//selecting Patient
					int selectedIndex = position;
					Patient selectedPatient = mBoundService.getPatientList().get(selectedIndex);
					mBoundService.setSelectedPatient(selectedPatient, mesures);
					
					Intent i = new Intent(getApplicationContext(), MeasureActivity.class);
					i.putExtra("mesures", mesures);
					startActivity(i);
					finish();
					
				}
			});

	        FunctionsUIMeasure.displayMesure(mesures, this,mBoundService, true, false,0);
	        
    	}
    }
    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_mesure_set_patient, menu);
        return true;
    }
    

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch(id){
			case DIALOG_DELETE_MEASURE:
				//Create out AlterDialog
			    Builder builder = new AlertDialog.Builder(this);
			    builder.setTitle("Suppression de mesure NON enregistrée");
			    builder.setMessage("Voulez-vous supprimer cette mesure non enregistrée ?");
			    builder.setCancelable(true);
			    builder.setPositiveButton("Oui", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
			
						if(displaidMesure.size()>0){
							//si autre mesure dans l'arrayList des mesures du contexte, passer automatiquement sur celle là !!!
							//(displayd_mesure is switched to the previous value)
							mesures.remove(displaidMesure);
							if(mesures.size()>0){
								//left measure(s) to display
								FunctionsUIMeasure.displayMeasureContent(mesures,MeasureSetPatientActivity.this, mBoundService,false,0,false,0);
							}else{
								//no measure to display : return to waitActivity
								Intent iw = new Intent(getApplicationContext(), MainActivity.class);
								startActivity(iw);
								finish();
							}
						}else{
							Intent iw = new Intent(getApplicationContext(), MainActivity.class);
							startActivity(iw);
							finish();
						}
						
					}
			    	  
			    });
			    builder.setNegativeButton("Non", new DialogInterface.OnClickListener(){

					@Override
					public void onClick(DialogInterface dialog, int which) {
					}
			    	  
			    });
			    dialog = builder.create();
			    dialog.show();
				break;
		}
		
		return null;

	}
	
	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
	    
			case R.id.menu_delete:
				showDialog(DIALOG_DELETE_MEASURE);
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
	protected void newMeasureReceived(ArrayList<CompoundMeasure> mesures){
		if(dialog!=null && dialog.isShowing()){
			Log.i(Constants.TAG,CLASSNAME+" closing delete measure Dialog");
			dialog.cancel();
		}
		this.finish();
		
		////ATTENTION : to be operational, THIS class : MesureSetPatientActivity Must NOT be single_task or Single_Top !!!
		Intent i = new Intent(getApplicationContext(), MeasureSetPatientActivity.class);
		i.putExtra("mesures", mesures);
		i.putExtra("eraseMeasure", true);
		startActivity(i);
	}
	

	@SuppressWarnings("deprecation")
	@Override
	protected void onKeyBack() {
		showDialog(DIALOG_DELETE_MEASURE);
	}
}
