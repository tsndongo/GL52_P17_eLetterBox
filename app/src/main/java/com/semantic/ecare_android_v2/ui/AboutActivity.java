package com.semantic.ecare_android_v2.ui;

import java.io.File;
import java.util.ArrayList;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceAntidoteClient;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.ui.common.activity.GenericDisconnectedActivity;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.FunctionsPropertiesFile;

import net.newel.android.Log;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;


public class AboutActivity extends GenericDisconnectedActivity {

	private String CLASSNAME=this.getClass().getName();

	

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(Constants.TAG, CLASSNAME+" Oncreate Class");
    }

	@Override
	protected void affichage_before_binding() {
        setContentView(R.layout.activity_about);
        
        TextView tv = (TextView) findViewById(R.id.tvVersion);
        try {
			String versionName = getPackageManager().getPackageInfo(getPackageName(), 0 ).versionName;
			int versionCode = getPackageManager().getPackageInfo(getPackageName(), 0 ).versionCode;
			String svnVersion = getResources().getString(R.string.svn_version);
			
	        tv.setText("version "+versionName);//+" (int: "+versionCode+" rev: "+svnVersion+")");
		} catch (NameNotFoundException e) {
			Log.e(Constants.TAG, e);
			e.printStackTrace();
		}
	}
    
    @Override
    protected void affichage(){
    	super.affichage();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_about, menu);
        
        // désactiver le menu Debug s'il est indiqué dans le ficher de propriétés
        //first : check if property file exists
      	File property_file = new File(Constants.PROPERTY_FILE_LOCATION,Constants.PROPERTY_FILE);
      	if(property_file.exists()){
      		String menu_enable=FunctionsPropertiesFile.getProperty(Constants.PROPERTY_FILE_LOCATION+Constants.PROPERTY_FILE, "menu_debug_enable", "false");
      		
      		if(menu_enable.compareTo("true")!=0)
            {
            	MenuItem m=menu.findItem(R.id.menu_debug);
            	m.setEnabled(false);
            }
      	}
        return true;
    }
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_close:
				if (ServiceEcare.getConfigurationList().getType()==2)
				{
					Log.i(Constants.TAG, CLASSNAME+" Retour sur le panneau Main patient");
					Intent i = new Intent(getApplicationContext(), WaitActivityPatient.class);
					startActivity(i);
					finish();
				}
				else
				{
					Log.i(Constants.TAG, CLASSNAME+" Retour sur le panneau Main");
					Intent i = new Intent(getApplicationContext(), MainActivity.class);
					startActivity(i);
					finish();
				}
				break;
				
			case R.id.menu_debug:
				Log.i(Constants.TAG, CLASSNAME+" accès sur le panneau de debug");
			    Intent intentDebug = new Intent(AboutActivity.this, DebugActivity.class);
			    startActivity(intentDebug);
			    finish();
				break;
		}	
		return true;
	}

	@Override
	protected void newMeasureReceived(ArrayList<CompoundMeasure> mesures){
		//new measure comes without selected patient
		Log.i(Constants.TAG, CLASSNAME+" Reception d'une mesure");
		Intent i = new Intent(getApplicationContext(), MeasureSetPatientActivity.class);
		i.putExtra("mesures", mesures);
		startActivity(i);
		this.finish();
	}
	
	@Override
	protected void onKeyBack() {
		if (ServiceEcare.getConfigurationList().getType()==2)
		{
			Log.i(Constants.TAG, CLASSNAME+" Retour sur le panneau Main patient");
			Intent i = new Intent(getApplicationContext(), WaitActivityPatient.class);
			startActivity(i);
			finish();
		}
		else
		{
			Log.i(Constants.TAG, CLASSNAME+" Retour sur le panneau Main");
			Intent i = new Intent(getApplicationContext(), MainActivity.class);
			startActivity(i);
			finish();
		}
	}
	
	@Override
	public void onResume(){
		Log.i(Constants.TAG,CLASSNAME+" OnResume");
		super.onResume();
		if(mBoundService!=null){
			mBoundService.addListener(this);
		}
	}
}