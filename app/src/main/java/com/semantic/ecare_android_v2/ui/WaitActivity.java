package com.semantic.ecare_android_v2.ui;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.GraphicalView;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceAntidoteClient;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.NoteModel;
import com.semantic.ecare_android_v2.object.Patient;
import com.semantic.ecare_android_v2.ui.chart.SensorValuesChartWait;
import com.semantic.ecare_android_v2.ui.common.activity.GenericConnectedActivity;
import com.semantic.ecare_android_v2.ui.common.adapter.LastMeasuresListAdapter;
import com.semantic.ecare_android_v2.ui.common.adapter.PatientListAdapter;
import com.semantic.ecare_android_v2.ui.common.adapter.SensorListAdapter;
import com.semantic.ecare_android_v2.util.Constants;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import net.newel.android.Log;

public class WaitActivity extends GenericConnectedActivity {

	private String CLASSNAME=this.getClass().getName();
    private Handler handler;
	private ListView LastMeasures;
	private Spinner spinnerSensor;
    private GraphicalView gview;
    private LinearLayout view;
    private int sensor;
    private Timer timerUpdateLastMeasures=null;
    private Activity self = this;
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(Constants.TAG, CLASSNAME+" Oncreate Class");
        
        handler = new Handler();
        LastMeasures = (ListView) findViewById(R.id.listViewLastMeasures);
        
        sensor=Measure.SENSOR_OXY;
        
        // binding the note buttons
        
	    RelativeLayout lNotice = (RelativeLayout) findViewById(R.id.lNotice); //banner header
	    TextView tvNotice = (TextView) findViewById(R.id.tvNotice); //error summary in the header banner
	    
	    tvNotice.setText(getResources().getString(R.string.text_header_wait));
	    lNotice.setVisibility(View.VISIBLE);
	}
    
	@Override
	protected void affichage_before_binding() {
		 setContentView(R.layout.activity_wait);
	}
	
    
	
    @Override
    protected void affichage(){
    	super.affichage();
    	
    	//Here : display the UI elements (service binded here)
    	//Reset measures Context !
    	mBoundService.resetMeasuresListContext();
    	
		//Affichage du graphique
		spinnerSensor = (Spinner) findViewById(R.id.spinnerSensor);
		view = (LinearLayout) findViewById(R.id.viewGraph);
		
	       //initialisation du spinner du sensor
        String[] listeTypes = new String[4];
        listeTypes[0]=getResources().getString(Constants.SENSOR_LEGEND.get(Measure.SENSOR_OXY));
        listeTypes[1]=getResources().getString(Constants.SENSOR_LEGEND.get(Measure.SENSOR_POIDS));
        listeTypes[2]=getResources().getString(Constants.SENSOR_LEGEND.get(Measure.SENSOR_TENSION));
        listeTypes[3]=getResources().getString(Constants.SENSOR_LEGEND.get(Measure.SENSOR_CARDIO));
        spinnerSensor.setAdapter(new SensorListAdapter(getApplicationContext(), listeTypes));
        
        int lastMeasureSensor=mBoundService.getLastMeasureType();
        switch(lastMeasureSensor) {
			case Measure.SENSOR_OXY :
				spinnerSensor.setSelection(0);
				break;
			case Measure.SENSOR_POIDS :
				spinnerSensor.setSelection(1);
				break;
			case Measure.SENSOR_TENSION :
				spinnerSensor.setSelection(2);
				break;
			case Measure.SENSOR_CARDIO :
				spinnerSensor.setSelection(3);
				break;
		}
        //set the sensor visible
        sensor=lastMeasureSensor;
        
        spinnerSensor.setOnItemSelectedListener(new OnItemSelectedListener(){
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				//int sensor=Measure.SENSOR_OXY;

				switch((int)arg3) {
					case 0 :
						sensor=Measure.SENSOR_OXY;
						break;
					case 1 :
						sensor=Measure.SENSOR_POIDS;
						break;
					case 2 :
						sensor=Measure.SENSOR_TENSION;
						break;
					case 3 :
						sensor=Measure.SENSOR_CARDIO;
						break;
				}
				
				displayGraph(sensor);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}
        	
        });
        
        timerUpdateLastMeasures = new Timer();
        TimerTask tt = new TimerTask(){
			@Override
			public void run() {
				handler.post(runDisplayLastMeasures);
			}
        };
       
        timerUpdateLastMeasures.schedule(tt, 10, 60000);
		menusDeroulant();

        
    }

	private  void menusDeroulant(){
		//Gestion de l'affichage du menus deroulant
		ListView listview = (ListView) findViewById(R.id.listViewPatients);
		final DrawerLayout mDrawerLayout ;
		String mActivityTitle = getTitle().toString();


		mActivityTitle = getTitle().toString();

		listview.setAdapter(new PatientListAdapter(getApplicationContext(), mBoundService.getPatientList()));

		mDrawerLayout = (DrawerLayout)findViewById(R.id.drawer_layout);

		ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,mDrawerLayout,R.drawable.ic_drawer,R.string.open,R.string.close) {

			/**
			 * Called when a drawer has settled in a completely open state.
			 */
			public void onDrawerOpened(View drawerView) {
				super.onDrawerOpened(drawerView);

				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			/**
			 * Called when a drawer has settled in a completely closed state.
			 */
			public void onDrawerClosed(View view) {
				super.onDrawerClosed(view);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerToggle.setDrawerIndicatorEnabled(true);
		mDrawerLayout.setDrawerListener(mDrawerToggle);
		this.getActionBar().setDisplayHomeAsUpEnabled(true);
		this.getActionBar().setHomeButtonEnabled(true);

		listview.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				//selecting Patient
				int selectedIndex = position;
				Patient selectedPatient = mBoundService.getPatientList().get(selectedIndex);
				mBoundService.setSelectedPatient(selectedPatient);
				Log.i(Constants.TAG, CLASSNAME + "selectedPatient : " +selectedPatient.getUid());
				mDrawerLayout.closeDrawers();
				Intent intent = new Intent(getApplicationContext(), WaitActivity.class);
				startActivity(intent);
				//	finish();
			}
		});
		listview.setAdapter(new PatientListAdapter(getApplicationContext(), mBoundService.getPatientList()));
		// FIN DE LA GESTION DU MENUS DEROULANT
	}
    
	private Runnable runDisplayLastMeasures = new Runnable(){
		@Override
		public void run(){
			Log.i(Constants.TAG, CLASSNAME+" Raffraichissement de la vue WaitActivity");
			if(mBoundService.getSelectedPatient()!=null){
			    //TODO : Attention affichage de certaines mesures fait une scrollBar
				//TODO : Attention sur certaines mesures pas de "il y a.."
				//Build Last Measure List + Alert From BDD
				try{
					ArrayList<CompoundMeasure> mesures = mBoundService.getLastMeasures();
					
					// binding the note buttons
					
					Button noteButton = (Button) findViewById(R.id.lastMeasureNoteButton);
					
				    LastMeasures.setAdapter(new LastMeasuresListAdapter(getApplicationContext(), mesures, self));
				
				    LastMeasures.setOnItemClickListener(new OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
							int sensor=Measure.SENSOR_OXY;
							switch((int)arg3) {
							case 0 :
								sensor=Measure.SENSOR_OXY;
								spinnerSensor.setSelection(0);
								break;
							case 1 :
								sensor=Measure.SENSOR_POIDS;
								spinnerSensor.setSelection(1);
								break;
							case 2 :
								sensor=Measure.SENSOR_TENSION;
								spinnerSensor.setSelection(2);
								break;
							case 3 :
								sensor=Measure.SENSOR_CARDIO;
								spinnerSensor.setSelection(3);
								break;
							}
						
							displayGraph(sensor);
						}
					});
				}catch(Exception e){
					Log.e(Constants.TAG, e);
				}
			}//ELSE : on est en train de se déconnecter !
		}
	
	};
	

	private void displayGraph(int sensor){
		Log.i(Constants.TAG, CLASSNAME+" Raffraichissement du graphique");
		ArrayList<ArrayList<Measure>> mesures=updateMeasureList(SensorValuesChartWait.PERIOD_WAIT,sensor);
		if(mesures.size()>0){
			Log.i(Constants.TAG, CLASSNAME + " mesures.size()>0");
			gview = new SensorValuesChartWait().execute(this, mesures, sensor);
			view.removeAllViews();
    		view.addView(gview);
		}else{
			Log.i(Constants.TAG, CLASSNAME + " mesures.size()=0");
    		view.removeAllViews();
    		TextView tv = new TextView(this);
    		tv.setText(R.string.text_no_mesure);
    		tv.setTextSize(20);

    		view.setGravity(Gravity.CENTER);
    		view.addView(tv);
    		
    		Log.i(Constants.TAG, CLASSNAME+" ATTENTION : pas de mesure � afficher !");
		}
    }
	
//	@Override
//	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
//	    if(requestCode == Constants.REQ_CODE_NOTEDIALOG && data != null) {
//	        NoteModel note = (NoteModel) data.getExtras().getSerializable(Constants.NOTEMODEL_KEY);
//	        if(resultCode == Constants.SAVE_NOTE && note.hasBeenEdited()){
//	        	Measure m = findMeasureById(note.getTargetId());
//	        	m.updateNote(note);
//	        	mBoundService.updateNoteFromMeasure(m);
//	        }
//	    }
//	}
	
	private Measure findMeasureById(int id){
		for(CompoundMeasure cm : mBoundService.getLastMeasures()){
			Measure m = cm.findMeasureById(id);
			if(m != null){
				return m;
			}
		}
		return null;
	}
    
	private ArrayList<ArrayList<Measure>> updateMeasureList(int period, int sensor) {
		mBoundService.sessionAction();
		return mBoundService.getMeasures(period,sensor);
	}
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_wait, menu);
        return true;
    }
    
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem itemTest = menu.findItem(R.id.mode_test);  
		if(ServiceEcare.isSaveMeasure())
		{
			itemTest.setIcon(null);
		}
		else
		{
			itemTest.setIcon(R.drawable.check_mode_test);
		}
		return true;
	}
    
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.mode_test:
			if(ServiceEcare.isSaveMeasure())
			{
				ServiceEcare.setSaveMeasure(false);
				item.setIcon(R.drawable.check_mode_test);
			}
			else
			{
				ServiceEcare.setSaveMeasure(true);
				item.setIcon(null);
			}
			break;
			
		case R.id.menu_deco:
			disconnectPatient();
			break;

			
		case R.id.menu_graph:
			Intent ig = new Intent(getApplicationContext(), ChartActivity.class);
			ig.putExtra("sensor", sensor);
			startActivity(ig);
			finish();
			break;
			
		case R.id.menu_alert:
			Intent ia = new Intent(getApplicationContext(), AlertActivity.class);
			startActivity(ia);
			finish();
			break;

		}
		return true;
	}
    
	@Override
	protected void newMeasureReceived(ArrayList<CompoundMeasure> mesures){
		//New measure comes : display MesureActivity
		Intent i = new Intent(getApplicationContext(), MeasureActivity.class);
		i.putExtra("mesures", mesures);
		startActivity(i);
		Log.i(Constants.TAG, CLASSNAME+" Envoi de "+mesures.size()+" CM mesures");
		this.finish();
	}
	
	@Override
	public void disconnect() {
		super.disconnect();
	}
	
	@Override
	protected void onKeyBack(){
		disconnectPatient();
	}
}
