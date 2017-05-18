package com.semantic.ecare_android_v2.ui;


import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceAntidoteClient;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.NoteModel;
import com.semantic.ecare_android_v2.object.Patient;
import com.semantic.ecare_android_v2.ui.common.activity.GenericConnectedActivity;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.FunctionsUI;

import net.newel.android.Log;
import android.os.Bundle;
import android.os.Handler;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.PopupMenu;  

public class AlertActivity extends GenericConnectedActivity{
	
	//display all alerts
	private String CLASSNAME=this.getClass().getName();
	private TableLayout tableLayout=null;
	private Date now=null;
	private CheckBox cbFilter_oxy, cbFilter_weight, cbFilter_pressure, cbFilter_cardio;
	
	private HashMap<Long,TextView> listOfHeadersTitle;
	private HashMap<Long,ArrayList<TableRow>> listOfAlerts;
	private TextView lastTvDate=null;
	private Date lastAlertDate=null;
	private ProgressDialog progressDialog=null;
	private Handler handler=null;
	private ArrayList<TableRow> listOfTableRow=null;
	private Context context = this;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(Constants.TAG, CLASSNAME+" Oncreate Class");
        handler = new Handler();
        now=new Date();
        
    	cbFilter_oxy=(CheckBox) findViewById(R.id.cbFilterOxy);
    	cbFilter_weight=(CheckBox) findViewById(R.id.cbFilterWeight);
    	cbFilter_pressure=(CheckBox) findViewById(R.id.cbFilterPressure);
    	cbFilter_cardio=(CheckBox) findViewById(R.id.cbFilterCardio);
        
    	cbFilter_oxy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateAlertes();
			}
		});
    	cbFilter_weight.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateAlertes();
			}
		});
    	cbFilter_pressure.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateAlertes();
			}
		});
    	cbFilter_cardio.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				updateAlertes();
			}
		});
    }

	@Override
	protected void affichage_before_binding() {
		setContentView(R.layout.activity_alert);
	}
    
    @Override
    protected void affichage(){//this method is called ONLY after service binding !
    	super.affichage();

    	updateAlertes();
    }
    
    public void noteButtonClickHandler(View v){

    	 AlertDialog.Builder dialog = new AlertDialog.Builder( AlertActivity.this );
         dialog.setView( getLayoutInflater().inflate( R.layout.popup_note_dialog, null ) );
         dialog.show();
         
    }
    
    private TableRow addUiAlerte(Alert alerte){
    	int alertBackground = R.color.header_alert1;
    	int alertImage = R.drawable.icon_warning_white;
    	switch(alerte.getLevel()){
    		case 1:
    			alertBackground = R.color.header_alert1;
    			alertImage = R.drawable.icon_warning_white;
    			break;
    		case 2:
    			alertBackground = R.color.header_alert2;
    			alertImage = R.drawable.icon_warning_white;
    			break;
    		case 3:
    			alertBackground = R.color.header_alert3;
    			alertImage = R.drawable.icon_warning_white;
    			break;
    		case 4:
    			alertBackground = R.color.header_alert4;
    			alertImage = R.drawable.icon_error_white;
    			break;
    	}
    	
    	LayoutInflater inflater = getLayoutInflater();
    	TableRow row = (TableRow)inflater.inflate(R.layout.alert_row_inflate, tableLayout, false);

    	LinearLayout bgAlert = (LinearLayout) row.findViewById(R.id.bgAlert);
    	bgAlert.setBackgroundResource(alertBackground);
    	LinearLayout bgAlert2 = (LinearLayout) row.findViewById(R.id.bgAlert2);
    	bgAlert2.setBackgroundResource(alertBackground);
    	
    	ImageView ivAlert = (ImageView) row.findViewById(R.id.ivAlert);
    	ivAlert.setImageResource(alertImage);
    	
    	TextView tvSensor = (TextView) row.findViewById(R.id.tvSensor);
    	if(alerte.getCompleteMeasure()!=null){
    		String html="<html><b>"+
    				getResources().getString(Constants.SENSOR_TITLE.get(alerte.getCompleteMeasure().getSensor()))+
    				" :</b> "+
    				alerte.getCompleteMeasure().getValue()+
    				" "+
    				getResources().getString(Constants.SENSOR_UNIT.get(alerte.getCompleteMeasure().getSensor()))+
    				"</html>";
    		tvSensor.setText(Html.fromHtml(html));	
    	}else{
    		tvSensor.setText("Mesure inconnue");
    	}
    	
    	TextView tvMessage = (TextView) row.findViewById(R.id.tvMessage);
    	tvMessage.setText(alerte.getMessage());	
    	
    	TextView tvDate = (TextView) row.findViewById(R.id.tvDate);
    	tvDate.setText(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(alerte.getDate()));
    	
    	Button noteButton = (Button) row.findViewById(R.id.alertNoteButton);
    	
    	final int alerteId = alerte.getAlerteId();
    	
    	noteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Alert alerte = findAlertByIdInAllAlerts(alerteId);
				NoteModel model = new NoteModel(alerte.getNote(), alerte.getNoteDate(), alerteId);
				
				Intent intent = new Intent(context, NoteDialogActivity.class);
				intent.putExtra(Constants.NOTEMODEL_KEY, model);
				startActivityForResult(intent, Constants.REQ_CODE_NOTEDIALOG_ALERT);
			}
		});
    	
    	listOfTableRow.add(row);
    	
    	return row;
    }
    
    private TextView addTimeSpanHeader(long nb_jour, final long key, boolean firstElement){
    	//key is the timestamp of the first day of the period
    	LayoutInflater inflater = getLayoutInflater();
    	TableRow row = (TableRow)inflater.inflate(R.layout.alert_row_time_span, tableLayout, false);
    	
    	RelativeLayout headerTimeSpan = (RelativeLayout) row.findViewById(R.id.headerTimeSpan);
    	TextView tvTimeSpan = (TextView) row.findViewById(R.id.tvTimeSpan);
    	TextView tvDate = (TextView) row.findViewById(R.id.tvDate);
    	lastTvDate=tvDate;
    	
    	final ImageView iv = (ImageView) row.findViewById(R.id.ivArrow);
    	
    	//Build timeSpan label from date
    	Date date = new Date(key);
    	Calendar cal = Calendar.getInstance();
    	int now_day_num = cal.get(Calendar.DAY_OF_WEEK);//de 1 � 7 (dimanche � samedi)
    	
    	
    	String timeSpanStr="";
    	boolean reduceAtLoading=false;
    	boolean displayDate=true;
		if(nb_jour==0){//Aujourd'hui
			timeSpanStr="Aujourd'hui";
		}else if(nb_jour==1){//Hier
			timeSpanStr="Hier";
		}else if(nb_jour<=6){//De 2 � 6 jours en arriere : affichage du nom du jour
			int numDay = FunctionsUI.getDayModulo((int) (now_day_num - nb_jour));
			timeSpanStr=Constants.DAY_ARRAY[numDay-1];
		}else if(nb_jour==7){//1 semaine
			timeSpanStr="Il y a une semaine";
		}else if(nb_jour<=14){//2 semaines
			timeSpanStr="Il y a 2 semaines";
			reduceAtLoading=true;
		}else if(nb_jour<=21){//3 semaines
			timeSpanStr="Il y a 3 semaines";
			reduceAtLoading=true;
		}else if(nb_jour<=28){//il y a 1 mois (=4 semaines)
			timeSpanStr="Il y a 1 mois";
			reduceAtLoading=true;
		}else{//encore plus anciens !
			timeSpanStr="Anciennes alertes";
			reduceAtLoading=true;
			displayDate=false;
		}
		
		tvTimeSpan.setText(timeSpanStr);
		
		
    	if(displayDate){
	    	tvDate.setText(getDateFormat(date));
    	}
    	
    	
    	headerTimeSpan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//using the contentDescription
				//true = enlarged !
				if(iv.getContentDescription().equals("true")){
					iv.setImageResource(android.R.drawable.arrow_down_float);
					iv.setContentDescription("false");
				}else{
					iv.setImageResource(android.R.drawable.arrow_up_float);
					iv.setContentDescription("true");
				}
				reduceEnlargeAlertPeriod(key, iv.getContentDescription().equals("true"));
			}
		});
    	
    	if(reduceAtLoading && !firstElement){
			iv.setImageResource(android.R.drawable.arrow_down_float);
			iv.setContentDescription("false");
    	}
    	
    	
    	listOfTableRow.add(row);
    	
    	return tvTimeSpan;
    }
    
    
    

    private void reduceEnlargeAlertPeriod(long key, boolean enlarge){
    	//enlarge false => reduire
    	
    	//get elements concerned by the reducing/enlarging
    	Iterator<TableRow> it = listOfAlerts.get(key).iterator();
    	while(it.hasNext()){
    		TableRow tr = it.next();
    		if(enlarge){
    			tr.setVisibility(View.VISIBLE);
    		}else{
    			tr.setVisibility(View.GONE);
    		}
    	}
    }
    
    private void updateAlertes(){
		handler.post(runDisplayLoadingDialog);
		
    	listOfHeadersTitle=new HashMap<Long,TextView>();//List of "timeSpan" in the header (to edit when we have the exact count of element)
    	listOfAlerts=new HashMap<Long,ArrayList<TableRow>>(); //List of Alert for this specific date
    	
		tableLayout = (TableLayout) findViewById(R.id.tableAlertes);
    	tableLayout.removeAllViews();
    	
    	listOfTableRow=new ArrayList<TableRow>();

    	new Thread(){
    		@Override
    		public void run(){

		    	//Premiere chose : On cree le tableLayout Principal
    			//Comme ca on peut y ajouter les elements de fa�on synchrone au thread, et � la toute fin on le donne � la vue

		    	//masquer le dialog de chargement
		    	mBoundService.sessionAction();
		   
		    	ArrayList<Integer> listOfSensors = new ArrayList<Integer>();
		    	if(cbFilter_oxy.isChecked()){
		    		listOfSensors.add(Measure.SENSOR_OXY);
		    	}
		    	if(cbFilter_weight.isChecked()){
		    		listOfSensors.add(Measure.SENSOR_POIDS);
		    	}
		    	if(cbFilter_pressure.isChecked()){
		    		listOfSensors.add(Measure.SENSOR_TENSION_SYS);
		    		listOfSensors.add(Measure.SENSOR_TENSION_DIA);
		    	}
		    	if(cbFilter_cardio.isChecked()){
		    		listOfSensors.add(Measure.SENSOR_CARDIO);
		    	}
		    	
		    	ArrayList<Alert> alertes = mBoundService.getAlertes(listOfSensors);
		    	Log.e(Constants.TAG, CLASSNAME + " recuperation de la liste des alerts ");
		    	if(alertes.size()>0){
		    		Calendar cal = Calendar.getInstance();
					cal.set(Calendar.HOUR_OF_DAY, 12);
					cal.set(Calendar.MINUTE, 0);
					cal.set(Calendar.SECOND, 0);
					cal.set(Calendar.MILLISECOND,0);
					now=cal.getTime();
		
			    	Iterator<Alert> it = alertes.iterator();
			    	while(it.hasNext()){
			    		Alert alerte = it.next();
			    		
		    			//build Date for this alert
		    			Calendar calAlert = Calendar.getInstance();
		    			calAlert.setTime(alerte.getDate());
		    			calAlert.set(Calendar.HOUR_OF_DAY, 12);
		    			calAlert.set(Calendar.MINUTE, 0);
		    			calAlert.set(Calendar.SECOND, 0);
		    			calAlert.set(Calendar.MILLISECOND,0);
		    			Date alert_date=calAlert.getTime();
		    			lastAlertDate=alert_date;
		
		    			//d�termination du nombre de jour entre l'alerte et aujourd'hui
		    			long nb_jour = (now.getTime() - alert_date.getTime()) /86400000;
		    			
		    			
		    			//cr�ation de la cl� pour la hashmap
		    			//La cl� correspond au jour de DEBUT de p�riode
		    			
		    			long key; //Key for the HashMap representing the period of this alert
		
		    			if(nb_jour<=7){//Chaque jour de cette semaine + le meme jour de la semaine derniere
		    				key = (now.getTime()-(nb_jour*86400l*1000l));
		    			}else if(nb_jour<=14){//semaine d'avant
		    				key = (now.getTime()-(14l*86400l*1000l));
		    			}else if(nb_jour<=21){//semaine encore avant
		    				key = (now.getTime()-(21l*86400l*1000l));
		    			}else if(nb_jour<=28){//mois dernier
		    				key = (now.getTime()-(28l*86400l*1000l));
		    			}else{//encore plus anciens !
		    				key = 0;
		    			}
		    			
		    			
		    			
		    			if(!listOfHeadersTitle.containsKey(key)){
		    				//This key does not exists in the maps
		    				//creating this key in the both
		    				TextView tvTimeSpan = addTimeSpanHeader(nb_jour, key, (listOfHeadersTitle.size()==0));
		    				
		    				listOfHeadersTitle.put(key, tvTimeSpan);
		    				listOfAlerts.put(key,new ArrayList<TableRow>());
		    			}
		    			
		
		    			TableRow trAlert = addUiAlerte(alerte);
		
		    			if((nb_jour>7) && (listOfHeadersTitle.size()>1)){
		    				trAlert.setVisibility(View.GONE);
		    			}
		    			
		    			//recherche si cette cl� existe d�ja
		    			//Si la cl� n'existe pas, on la cr�� + on ajoute le bandeau de pr�sentation
		    			if(listOfHeadersTitle.containsKey(key)){
		    				//Adding this alert to the arraylist for this key
		    				listOfAlerts.get(key).add(trAlert);
		    			}
			    	}
			    	
			    	
			    	//iterate on the header banners to add the number of element of each period
			    	Set<Long> keys = listOfAlerts.keySet();
			    	Iterator<Long> itKeys = keys.iterator();
			    	while(itKeys.hasNext()){
			    		long key = itKeys.next();
			    		int number = listOfAlerts.get(key).size();
			    		
			    		TextView tvTimeSpan = listOfHeadersTitle.get(key);
			    		tvTimeSpan.setText(tvTimeSpan.getText()+" ("+number+")");
			    		
			    		if(key==0){
			    			//Alertes Anciennes, On r��crit la date pour afficher la date de l'alerte la plus ancienne
			    	    	lastTvDate.setText(getDateFormat(lastAlertDate));
			    		}
			    	}
			    	
		    	}else{
		        	LayoutInflater inflater = getLayoutInflater();
		        	TableRow row = (TableRow)inflater.inflate(R.layout.alert_row_no_alert, tableLayout, false);
		        	listOfTableRow.add(row);
		    	}

		    	handler.post(runUpdateTableLayout);
		    	
		    	//ICI : Toute fin
		    	
		    	handler.post(runDismissLoadingDialog);
    		}
    	}.start();
    }
    

	
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_alert, menu);
        return true;
    }
    
	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		MenuItem itemDeco = menu.findItem(R.id.menu_deco);  
//		MenuItem itemClose = menu.findItem(R.id.menu_close);
		if(ServiceEcare.getConfigurationList().getType()==2)
		{
			itemDeco.setVisible(false);
//			itemClose.setVisible(false);
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
			
		case R.id.menu_close:
			//start meausreActivity of Wait activity depending on the context
			Intent iw = null;
			if(mBoundService.getMeasuresListContext().size()>0){
				iw = new Intent(getApplicationContext(), MeasureActivity.class);
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
		}else{
			if(ServiceEcare.getConfigurationList().getType()==1)
				iw = new Intent(getApplicationContext(), WaitActivity.class);
			else if(ServiceEcare.getConfigurationList().getType()==2)
				iw = new Intent(getApplicationContext(), WaitActivityPatient.class);
		}
		startActivity(iw);
		finish();
	}
	
	
	private String getDateFormat(Date date){
    	Calendar calAlert = Calendar.getInstance();
    	calAlert.setTime(date);
    	int day = calAlert.get(Calendar.DAY_OF_MONTH);
    	int month = calAlert.get(Calendar.MONTH)+1;
    	int year = calAlert.get(Calendar.YEAR);
    	
    	String day_str=String.valueOf(day);
    	if(day<10){
    		day_str="0"+day_str;
    	}
    	String month_str=String.valueOf(month);
    	if(month<10){
    		month_str="0"+month_str;
    	}
    	return day_str+"/"+month_str+"/"+year;
	}

	
	
	
	
	
	
	//RUNNABLES CALLED
	
	private Runnable runUpdateTableLayout = new Runnable(){
		@Override
		public void run(){
        	Iterator<TableRow> it = listOfTableRow.iterator();
        	while(it.hasNext()){
        		TableRow row = it.next();
        		
        		tableLayout.addView(row);
        	}
		}
	};
	
	private Runnable runDisplayLoadingDialog = new Runnable(){
		@Override
		public void run(){
			progressDialog = ProgressDialog.show(AlertActivity.this, "", "Chargement des alertes ...",true,false);
		}
	};
	
	private Runnable runDismissLoadingDialog = new Runnable(){
		@Override
		public void run(){
			progressDialog.dismiss();
		}
	};

}
