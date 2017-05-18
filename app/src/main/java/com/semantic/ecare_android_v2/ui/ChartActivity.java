package com.semantic.ecare_android_v2.ui;

import java.util.ArrayList;

import org.achartengine.GraphicalView;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceAntidoteClient;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.ui.chart.SensorValuesChart;
import com.semantic.ecare_android_v2.ui.chart.SensorValuesChartGraph;
import com.semantic.ecare_android_v2.ui.common.activity.GenericConnectedActivity;
import com.semantic.ecare_android_v2.ui.common.adapter.GraphListAdapter;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.DataBaseConnector;
import com.semantic.ecare_android_v2.util.MyApplication;

import net.newel.android.Log;
import android.os.Bundle;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

public class ChartActivity extends GenericConnectedActivity{

	//display all alerts
	private String CLASSNAME=this.getClass().getName();


	private ServiceEcare serviceEcare = new ServiceEcare();
	private Spinner spinnerSensor, spinnerPeriod;
	private int period;
	private ArrayList<ArrayList<Measure>> mesures;
	private int sensor;
	private GraphicalView gview;
	private LinearLayout view;
	private ArrayList<CompoundMeasure> mesure;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Log.i(Constants.TAG, CLASSNAME+" Oncreate Class");

		spinnerSensor = (Spinner) findViewById(R.id.spinnerSensor);
		spinnerPeriod = (Spinner) findViewById(R.id.spinnerPeriod);
		view = (LinearLayout) findViewById(R.id.viewGraph);


		Bundle extras = getIntent().getExtras();
		if(extras !=null){
			sensor = extras.getInt("sensor");

			//Selection du sensor fait à l'initialisation du spinner en fonction de la valeur se "sensor"
		}

		if(sensor==0){sensor=Measure.SENSOR_OXY;}
		period=SensorValuesChart.PERIOD_WEEK;



	}
	
	@Override
	public void onResume()
	{
		super.onResume();
		Bundle extras = getIntent().getExtras();
		if(extras !=null && extras.getSerializable("mesures") != null){
			if(mesures!=null)
				mesures.clear();
			mesure = (ArrayList<CompoundMeasure>) extras.getSerializable("mesures");
			Intent i = 	new Intent(getApplicationContext(), MeasureActivity.class);
			i.putExtra("mesures", mesures);
			startActivity(i);
			finish();
		}
	}

	private void displayGraph(){
		Log.i(Constants.TAG, CLASSNAME+" Raffraichissement de la vue");
		if(mesures.size()>0){
			gview = new SensorValuesChartGraph().execute(this,mBoundService, mesures, period, sensor);
			view.removeAllViews();

			view.addView(gview);
		}else{
			view.removeAllViews();
			TextView tv = new TextView(this);
			tv.setText(R.string.text_no_mesure);
			tv.setTextColor(getResources().getColor(R.color.ecare_gray));
			tv.setTextSize(30);

			view.setGravity(Gravity.CENTER);
			view.addView(tv);

			Log.i(Constants.TAG, CLASSNAME+" ATTENTION : pas de mesure à afficher !");
		}
	}

	@Override
	protected void affichage_before_binding() {
		setContentView(R.layout.activity_chart);
	}

	@Override
	protected void affichage(){//this method is called ONLY after service binding !
		super.affichage();



		String[] listeStringsPeriode = new String[3];
		listeStringsPeriode[0]="Semaine";
		listeStringsPeriode[1]="Mois";
		listeStringsPeriode[2]="Année"; 
		//arrayAdapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,listeStringsPeriode);
		//spinnerPeriod.setAdapter(arrayAdapter1);
		spinnerPeriod.setAdapter(new GraphListAdapter(getApplicationContext(), listeStringsPeriode));
		spinnerPeriod.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				Log.i(Constants.TAG, CLASSNAME+" Changement p�riode pour "+period);

				switch((int)arg3) {
				case 0 :
					period=SensorValuesChart.PERIOD_WEEK;
					break;
				case 1 :
					period=SensorValuesChart.PERIOD_MONTH;
					break;
				case 2 :
					period=SensorValuesChart.PERIOD_YEAR;
					break;
				}

				mesures=updateMeasureList(period,sensor);
				displayGraph();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}

		});



		//initialisation du spinner selon l'intent passé en param !
		int selectedId = 0;
		switch(sensor) {
		case Measure.SENSOR_OXY :
			//je met à jour la liste en fonction du sensor
			selectedId=0;
			break;
		case Measure.SENSOR_POIDS :
			selectedId=1;
			break;
		case Measure.SENSOR_TENSION :
			selectedId=2;
			break;
		case Measure.SENSOR_CARDIO :
			selectedId=3;
			break;
		}


		//initialisation du spinner du sensor
		String[] listeStringsType = new String[4];
		listeStringsType[0]="Saturation O2";
		listeStringsType[1]="Poids";
		listeStringsType[2]="Pression artérielle"; 
		listeStringsType[3]="Fréquence cardiaque"; 
		//arrayAdapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,listeStringsType);
		//spinnerSensor.setAdapter(arrayAdapter2);
		spinnerSensor.setAdapter(new GraphListAdapter(getApplicationContext(), listeStringsType));
		spinnerSensor.setSelection(selectedId);
		spinnerSensor.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				switch((int)arg3) {
				case 0 :
					//je met à jour la liste en fonction du sensor
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

				mesures=updateMeasureList(period,sensor);
				displayGraph();
				Log.i(Constants.TAG, CLASSNAME+" Changement Sensor pour "+sensor);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {}

		});

		//Don't update here (at loading spinner auto selection will load saturation graph


		//TODO : surveiller que sur un onResume n rééxécute l'appel au graphique (ds le cas ou on supprime une mesure, etre sur de raffraichir le graph)
	}



	private ArrayList<ArrayList<Measure>> updateMeasureList(int period, int sensor) {
		mBoundService.sessionAction();
		return mBoundService.getMeasures(period,sensor);
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_graphique, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem itemDeco = menu.findItem(R.id.menu_deco);  
		if(ServiceEcare.getConfigurationList().getType()==2)
		{
			itemDeco.setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_deco:
			disconnectPatient();
			break;


		case R.id.menu_graph:
			Intent ig = new Intent(getApplicationContext(), ChartActivity.class);
			startActivity(ig);
			finish();
			break;

//		case R.id.menu_alert:
//			Intent ia = new Intent(getApplicationContext(), AlertActivity.class);
//			startActivity(ia);
//			finish();
//			break;

		case R.id.menu_close:
			Intent iw = null;
			//start measureActivity or WaitActivity depending on the context
			if(mBoundService.getMeasuresListContext().size()>0){
				iw = new Intent(getApplicationContext(), MeasureActivity.class);
				//send the sensor Number To display directly this measure
				iw.putExtra("sensor", sensor);
			}else{
				if(ServiceEcare.getConfigurationList().getType()==1)
					iw = new Intent(getApplicationContext(), WaitActivity.class);
				else if(ServiceEcare.getConfigurationList().getType()==2)
					iw = new Intent(getApplicationContext(), WaitActivityPatient.class);
			}
			startActivity(iw);
			finish();
			break;
		}
		return true;
	}



	@Override
	protected void newMeasureReceived(ArrayList<CompoundMeasure> mesures){
		//new measure comes : display new panel and close this one !
		Intent i = new Intent(getApplicationContext(), MeasureActivity.class);
		i.putExtra("mesures", mesures);
		startActivity(i);

		this.finish();
	}




	@Override
	protected void onKeyBack() {
		//start meausreActivity of Wait activity depending on the context
		Intent iw = null;
		if(mBoundService.getMeasuresListContext().size()>0){
			iw = new Intent(getApplicationContext(), MeasureActivity.class);
			//send the sensor Number To display directly this measure
			iw.putExtra("sensor", sensor);
		}else{
			if(ServiceEcare.getConfigurationList().getType()==1)
				iw = new Intent(getApplicationContext(), WaitActivity.class);
			else if(ServiceEcare.getConfigurationList().getType()==2)
				iw = new Intent(getApplicationContext(), WaitActivityPatient.class);
		}
		startActivity(iw);
		finish();
	}

	// true : service , false : domicile
	public boolean getMode()
	{
		int res = 0;
		DataBaseConnector dbc = new DataBaseConnector(MyApplication.getContext());

		if(dbc == null)
			Log.i(Constants.TAG, CLASSNAME + " dbc null");
		SQLiteDatabase dbRead = dbc.openRead();

		if(dbRead!=null){
			Cursor cursor = dbRead.query(Constants.TABLE_CONFIGURATION, new String[] {"type"},null, null, null, null, null);
			if (cursor != null){
				cursor.moveToFirst();
				res = cursor.getInt(0);
			}
			cursor.close();
		}
		dbRead.close();
		dbc.close();
		return (res==1)?true:false;
	}
}
