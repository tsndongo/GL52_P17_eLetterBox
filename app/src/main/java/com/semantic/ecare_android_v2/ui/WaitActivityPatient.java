package com.semantic.ecare_android_v2.ui;


import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.achartengine.GraphicalView;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceAntidoteClient;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.ui.chart.SensorValuesChartWait;
import com.semantic.ecare_android_v2.ui.common.activity.GenericConnectedActivity;
import com.semantic.ecare_android_v2.ui.common.adapter.LastMeasuresListAdapter;
import com.semantic.ecare_android_v2.ui.common.adapter.SensorListAdapter;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.NetworkStatus;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import net.newel.android.Log;

public class WaitActivityPatient extends GenericConnectedActivity {

	private String CLASSNAME=this.getClass().getName();
	private Handler handler;
	private ListView LastMeasures;
	private Spinner spinnerSensor;
	private GraphicalView gview;
	private LinearLayout view;
	private int sensor;
	private Timer timerUpdateLastMeasures=null;
	private ArrayList<CompoundMeasure> mesures;
	private static boolean firstTime = true;
	private Activity self = this;
	
    public boolean isFirstTime() {
		return firstTime;
	}

	public void setFirstTime(boolean firstTime) {
		WaitActivityPatient.firstTime = firstTime;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(Constants.TAG, CLASSNAME+" Oncreate Class");

		handler = new Handler();
		LastMeasures = (ListView) findViewById(R.id.listViewLastMeasures);

		sensor=Measure.SENSOR_OXY;

		RelativeLayout lNotice = (RelativeLayout) findViewById(R.id.lNotice); //banner header
		TextView tvNotice = (TextView) findViewById(R.id.tvNotice); //error summary in the header banner

		tvNotice.setText(getResources().getString(R.string.text_header_wait));
		lNotice.setVisibility(View.VISIBLE);
	}
	
	@Override 
	public void onResume()
	{
		super.onResume();
		Bundle extras = getIntent().getExtras();
		if(extras !=null)
		{
			Log.i(Constants.TAG, CLASSNAME +"EXTRASSS");
			if(mesures!=null)
				mesures.clear();
			mesures = (ArrayList<CompoundMeasure>) extras.getSerializable("mesures");
			Intent i = 	new Intent(getApplicationContext(), MeasureActivity.class);
			i.putExtra("mesures", mesures);
			startActivity(i);
			finish();
		}
	}

	@Override
	protected void affichage_before_binding() {
		setContentView(R.layout.activity_wait);
	}

	@Override
	protected void affichage(){
		super.affichage();
		
		if(isFirstTime())
		{
			setFirstTime(false);
		}
		else
		{
			Log.i(Constants.TAG, CLASSNAME + "update patient list");
			if(mBoundService.isNeedToUpdate())
			{
		//		mBoundService.installUpdate();
				mBoundService.setNeedToUpdate(false);
				Log.i(Constants.TAG, CLASSNAME + " Need to install the app");
			}
			else
			{
				Log.i(Constants.TAG, CLASSNAME + " No need to install the app");
			//	mBoundService.checkUpdate();
			}
		}

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
					Log.i(Constants.TAG, CLASSNAME + e);
				}
			}//ELSE : on est en train de se déconnecter !
		}
	};

	private void displayGraph(int sensor){
		Log.i(Constants.TAG, CLASSNAME+" Raffraichissement du graphique");
		ArrayList<ArrayList<Measure>> mesures=updateMeasureList(SensorValuesChartWait.PERIOD_WAIT,sensor);
		if(mesures.size()>0){
			gview = new SensorValuesChartWait().execute(this, mesures, sensor);
			view.removeAllViews();
			view.addView(gview);
		}else{
			view.removeAllViews();
			TextView tv = new TextView(this);
			tv.setText(R.string.text_no_mesure);
			tv.setTextSize(20);

			view.setGravity(Gravity.CENTER);
			view.addView(tv);

			Log.i(Constants.TAG, CLASSNAME+" ATTENTION : pas de mesure à afficher !");
		}
	}

	private ArrayList<ArrayList<Measure>> updateMeasureList(int period, int sensor) {
		mBoundService.sessionAction();
		return mBoundService.getMeasures(period,sensor);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_wait_patient, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem itemTest = menu.findItem(R.id.mode_test);  
		if(ServiceEcare.isSaveMeasure())
			itemTest.setIcon(null);
		else
			itemTest.setIcon(R.drawable.check_mode_test);
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
			
		case R.id.menu_about:
		    Intent intentAbout = new Intent(this, AboutActivity.class);
		    startActivity(intentAbout);
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
		//disconnectPatient();
	}
}
