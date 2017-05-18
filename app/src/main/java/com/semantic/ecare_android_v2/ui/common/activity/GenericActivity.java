package com.semantic.ecare_android_v2.ui.common.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.core.listener.ServiceEcareListener;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.NoteModel;
import com.semantic.ecare_android_v2.object.Patient;
import com.semantic.ecare_android_v2.object.SensorState;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.Functions;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import net.newel.android.Log;

public abstract class GenericActivity extends Activity implements ServiceEcareListener {
	protected boolean mIsBound;
	protected ServiceEcare mBoundService;
	private String CLASSNAME=this.getClass().getName();
	
	private HashMap<String,LinearLayout> listOfSensorStateLL;
	protected CompoundMeasure displaidMesure=null;
	protected LinearLayout llSensorState;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        affichage_before_binding();
        
        //volume button controls media volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC); 

        ActionBar bar = getActionBar();
        bar.setCustomView(R.layout.actionbar_view);
        bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        
        llSensorState = (LinearLayout) bar.getCustomView().findViewById(R.id.layout_sensor_state);
        
        listOfSensorStateLL = new HashMap<String,LinearLayout>();        
        //OnBlindService in onStart method to execute while returning in this view
        
    }
    
    
    protected abstract void affichage_before_binding();
    
    
    protected void affichage(){//this method is called ONLY after service binding !
    	//Generic display for View
	    
    	View rootElement = findViewById(R.id.rootElement);
    	if(rootElement!=null){
    		//There is an element for this name (some activity could not have this element ?)
	    	rootElement.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					mBoundService.sessionAction();
					return false;
				}
			});
    	}

    	//Indicate to Service that there is action in UI
    	mBoundService.sessionAction();

    	
    	//At loading : display current sensorState saved in ServiceEcare
		if(mBoundService.getSensorStateList().size()>0){
			Log.i(Constants.TAG, CLASSNAME+" Ajout de "+mBoundService.getSensorStateList().size()+" Icone d'état de capteurs");
			for(int i=0;i<mBoundService.getSensorStateList().size();i++){
				Iterator<SensorState> it = mBoundService.getSensorStateList().values().iterator();
				while(it.hasNext()){
					SensorState state = it.next();
					
					//before adding this state, check if it didn't be added by the listener
					LinearLayout ll = getLayoutSensorState(state);
					if(ll!=null){
						Log.i(Constants.TAG, CLASSNAME+" Modification d'un état qui a déja été ajouté par le listener !");
						ImageView iv = (ImageView) ll.getChildAt(0);
						iv.setImageResource(getStateIconFromSensorState(state));
					}else{
						Log.i(Constants.TAG, CLASSNAME+" Ajout d'un état");
						llSensorState.addView(addSensorStateUI(state));
					}
				}
			}
		}
    }
    

	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
	    if(requestCode == Constants.REQ_CODE_NOTEDIALOG_SELECTED_PATIENT && data != null) {
	        NoteModel note = (NoteModel) data.getExtras().getSerializable(Constants.NOTEMODEL_KEY);
	        if(resultCode == Constants.SAVE_NOTE && note.hasBeenEdited()){
	        	Patient p = mBoundService.getSelectedPatient();
	        	p.updateNote(note);
	        	mBoundService.updateNoteFromPatient(p);
	        }
	    }
	    
	    else if(requestCode == Constants.REQ_CODE_NOTEDIALOG_MEASURE && data != null) {
	        NoteModel note = (NoteModel) data.getExtras().getSerializable(Constants.NOTEMODEL_KEY);
	        if(resultCode == Constants.SAVE_NOTE && note.hasBeenEdited()){
	        	Measure m = mBoundService.getMeasure(note.getTargetId());
	        	m.updateNote(note);
	        	mBoundService.updateNoteFromMeasure(m);
	        }
	    }
	    
	    else if(requestCode == Constants.REQ_CODE_NOTEDIALOG_ALERT && data != null) {
	        NoteModel note = (NoteModel) data.getExtras().getSerializable(Constants.NOTEMODEL_KEY);
	        if(resultCode == Constants.SAVE_NOTE && note.hasBeenEdited()){
	        	Alert a = findAlertByIdInAllAlerts(note.getTargetId());
	        	a.updateNote(note);
	        	mBoundService.updateNoteFromAlert(a);
	        }
	    }
	    
	    else if(requestCode == Constants.REQ_CODE_NOTEDIALOG_PATIENT && data != null) {
	        NoteModel note = (NoteModel) data.getExtras().getSerializable(Constants.NOTEMODEL_KEY);
	        if(resultCode == Constants.SAVE_NOTE && note.hasBeenEdited()){
	        	Patient p = findPatientById(note.getTargetId());
	        	p.updateNote(note);
	        	mBoundService.updateNoteFromPatient(p);
	        }
	    }
	}
	
	protected Patient findPatientById(int id){
		ArrayList<Patient> patientList = mBoundService.getPatientList();
		
		for(Patient p : patientList){
			if(p.getId() == id){
				return p;
			}
		}
		
		return null;
	}
	
	protected ArrayList<Alert> getAllAlertes(){
    	mBoundService.sessionAction();
    	ArrayList<Integer> listOfSensors = new ArrayList<Integer>();
		listOfSensors.add(Measure.SENSOR_OXY);
		listOfSensors.add(Measure.SENSOR_POIDS);
		listOfSensors.add(Measure.SENSOR_TENSION_SYS);
		listOfSensors.add(Measure.SENSOR_TENSION_DIA);
		listOfSensors.add(Measure.SENSOR_CARDIO);
    	
		return mBoundService.getAllAlertes(listOfSensors);
    }
    
    public Alert findAlertByIdInAllAlerts(int alerteId){
    	ArrayList<Alert> alertes = getAllAlertes();
    	
    	for(Alert a : alertes){
    		if(a.getAlerteId() == alerteId)
    			return a;
    	}
    	
    	return null;
    }
    
	public ServiceEcare getmBoundService(){
		return this.mBoundService;
	}
	
    //To be sure to "exit", the ondestroy is to far, onPause is immediately while displaying another view
	@Override
    public void onPause(){//when another view comes in front
		Log.i(Constants.TAG, CLASSNAME+" OnPause !!");
    	super.onPause();
    	try{
    	if (mIsBound) {
    		if(mBoundService!=null){
	    		//mBoundService.removeListener(this);
				doUnbindService();
    		}
		}
    	}catch(Exception e){
    		Log.e(Constants.TAG, e);
    	}
    	
    }
    
	@Override
    public void onStart(){//when returning in this view
    	super.onStart();   	
        if(!mIsBound){
        	doBindService();
        }
    }
	
	@Override
    public void onResume(){//when returning in this view
    	super.onResume();  
    	Log.e("TestDB", "on resume generic activity called");
        if(!mIsBound){
        	doBindService();
        }
    }
	
 
	//Contextual menu managed in extended classes
    

    
    //Service Binding
    
  	private ServiceConnection mConnection = new ServiceConnection() {
  	    public void onServiceConnected(ComponentName className, IBinder service) {
  	        mBoundService = ((ServiceEcare.LocalBinder)service).getService();
  	        mBoundService.addListener(GenericActivity.this);
  	        
  	        Log.i(Constants.TAG,CLASSNAME+" Connected to service");
  	        if(mBoundService==null){
  	        	Log.e(Constants.TAG,CLASSNAME+" mBoundService==null");
  	        	finish();
  	        	
  	        }else{
  	      	Log.e(Constants.TAG,CLASSNAME+" affichage()");
  	  	        affichage();
  	        }
  	    }

  	    public void onServiceDisconnected(ComponentName className) {
  	        mBoundService = null;
  	        
  	        Log.i(Constants.TAG,CLASSNAME+" Disconnected from service");
  	    }
  	};
  	
  	protected void doBindService() {
  		try{
  			bindService(new Intent(getApplicationContext(), ServiceEcare.class), mConnection, Context.BIND_AUTO_CREATE);
  			mIsBound = true;
  		}catch(Exception e){
  			Log.e(Constants.TAG, e);
  			finish();
  		}
  	}

  	protected void doUnbindService() {
  	    if (mIsBound) {
  	        unbindService(mConnection);
  	        mIsBound = false;
  	    }
  	}
  	
	@Override
	public void newSensorState(SensorState state){
		Log.i(Constants.TAG, CLASSNAME+" Changement d'état : " + state.getMacAddr()+" - "+state.getState());
		
		switch(state.getState()){
			case SensorState.STATE_CONNECTED:
				//adding this element
				llSensorState.addView(addSensorStateUI(state));
				break;
				
			case SensorState.STATE_DISCONNECTED:
				LinearLayout ll_con = getLayoutSensorState(state);
				llSensorState.removeView(ll_con);
				listOfSensorStateLL.remove(state.getMacAddr());
				break;
				
			case SensorState.STATE_ASSOCIATED:
			case SensorState.STATE_DISASSOCIATED:
			case SensorState.STATE_MEASUREMENT:
			case SensorState.STATE_ERROR:
				LinearLayout ll = getLayoutSensorState(state);
				if(ll!=null){
					ImageView iv = (ImageView) ll.getChildAt(0);
					iv.setImageResource(getStateIconFromSensorState(state));
				}
				break;
		}

	}
	
	@Override
	public void newSensorMeasure(ArrayList<CompoundMeasure> mesures) {
		//Receiving new Measure
		//Test of CM is NULL (it could be null, if saving in database failed !
		if(mesures!=null){
			newMeasureReceived(mesures);
		}else{
			newMeasureSavingError();
		}
	}
	
	protected abstract void newMeasureReceived(ArrayList<CompoundMeasure> mesures);
	
	private void newMeasureSavingError(){
		//An error occurred while saving this measure
		//unable to continue to other panel
		//> display the ErrorHeader
		RelativeLayout lError = (RelativeLayout) findViewById(R.id.lError); //banner header
	    TextView tvError = (TextView) findViewById(R.id.tvError); //error summary in the header banner
	    ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
	    
	    if(tvError!=null){
		    tvError.setText(getResources().getString(R.string.error_measure_saving));
		    ivBack.setVisibility(View.GONE);
		    lError.setVisibility(View.VISIBLE);
	    }
	}

	
	private LinearLayout addSensorStateUI(SensorState state){
		Log.i(Constants.TAG, CLASSNAME+" Ajout d'un sensorState graphique");
		LinearLayout ll = new LinearLayout(this);
		listOfSensorStateLL.put(state.getMacAddr(),ll);
		
		ll.setGravity(Gravity.CENTER_VERTICAL);
		ll.setOrientation(LinearLayout.HORIZONTAL);
		ImageView iv = new ImageView(this);
		iv.setImageResource(getStateIconFromSensorState(state));
		ll.addView(iv);
		TextView tv2 = new TextView(this);
		LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		llp.setMargins(5, 0, 10, 0); // llp.setMargins(left, top, right, bottom);
		tv2.setLayoutParams(llp);
		tv2.setTextSize(20);
		tv2.setText(getResources().getString(Functions.getDeviceName(state)));
		ll.addView(tv2);
		
		Log.i(Constants.TAG, CLASSNAME+" Présence de "+listOfSensorStateLL.size()+" elements dans la liste listOfSensorStateLL");
		return ll;
	}
	
	
	private LinearLayout getLayoutSensorState(SensorState state){
		return listOfSensorStateLL.get(state.getMacAddr());
	}
	

	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode){
			case KeyEvent.KEYCODE_BACK:
				onKeyBack();
				break;

		}
	    return false;
	}
	
	protected abstract void onKeyBack();
	
	
	
	private int getStateIconFromSensorState(SensorState state){
		switch(state.getState()){
			case SensorState.STATE_CONNECTED:
			case SensorState.STATE_DISCONNECTED:
				return R.drawable.icon_red_new;
				
			case SensorState.STATE_ASSOCIATED:
			case SensorState.STATE_DISASSOCIATED:
				return R.drawable.icon_orange_new;
				
			case SensorState.STATE_MEASUREMENT:
				return R.drawable.icon_green_new;
				
			case SensorState.STATE_ERROR:
				return R.drawable.icon_red_new;
	
			default:
				return R.drawable.icon_red_new;
		}
	}
	
	
	
	public CompoundMeasure getDisplaidMesure(){
		return displaidMesure;
	}
	public void setDisplaidMesure(CompoundMeasure cm){
		this.displaidMesure=cm;
	}

	
}
