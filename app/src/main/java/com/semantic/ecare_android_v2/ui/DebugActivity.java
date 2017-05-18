package com.semantic.ecare_android_v2.ui;



import java.io.IOException;
import java.util.ArrayList;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceAntidoteClient;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.ui.common.activity.GenericDisconnectedActivity;
import com.semantic.ecare_android_v2.util.Constants;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import net.newel.android.Log;

public class DebugActivity extends GenericDisconnectedActivity {
	
	private String CLASSNAME=this.getClass().getName();
	private SharedPreferences preferences;
	private Editor editor;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		editor = preferences.edit();

        
        Log.i(Constants.TAG, CLASSNAME+" Oncreate Class");
        
    	final int[] listeRaw = new int[3];
    	listeRaw[0]=R.raw.bip;
    	listeRaw[1]=R.raw.bip_custom;
    	listeRaw[2]=R.raw.bip_bip;
        
        
        Button buttonClearDatabase = (Button) findViewById(R.id.buttonClearDatabase);
        buttonClearDatabase.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBoundService.clearDatabase();
			}
		});
        
        Button buttonResynchronize = (Button) findViewById(R.id.buttonReSynchronize);
        buttonResynchronize.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			//	mBoundService.resynchronize();
			}
		});
        
        final Spinner spinnerBipSound = (Spinner) findViewById(R.id.spinnerBipSound);
    	String[] listeStrings = new String[3];
        listeStrings[0]="Bip Simple";
        listeStrings[1]="Bip Worms";
        listeStrings[2]="Bip Bip";
        spinnerBipSound.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item,listeStrings));
        
        
        final int soundRaw=preferences.getInt("soundRaw", R.raw.bip);
        int position = find(listeRaw,soundRaw);
        System.out.println("position trouvée : "+position);
        if(position>-1){
        	spinnerBipSound.setSelection(position);
        }


        spinnerBipSound.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				int soundRaw = listeRaw[spinnerBipSound.getSelectedItemPosition()];
				editor.putInt("soundRaw", soundRaw);
				editor.commit();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				
			}
        	
        });
        
        Button buttonPlaySound = (Button) findViewById(R.id.buttonPlaySound);
        buttonPlaySound.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mBoundService.playBip();
			}
		});
        
        Button buttonAskSu = (Button) findViewById(R.id.buttonAskSu);
        buttonAskSu.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//Test de demande de droits root
				try {
					Runtime.getRuntime().exec("su");
				} catch (IOException e) {
					Log.e(Constants.TAG, e);
				}
			}
		});    
        
    }
    

	@Override
	protected void affichage_before_binding() {
        setContentView(R.layout.activity_debug);
	}
    
    
    @Override
    protected void affichage(){
    	super.affichage();
    	
    	
    }
    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_debug, menu);
        return true;
    }
    
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_close:
				Intent i = new Intent(getApplicationContext(), AboutActivity.class);
				startActivity(i);
				finish();
				break;

		}	
		return true;
	}
    


	@Override
	protected void newMeasureReceived(ArrayList<CompoundMeasure> mesures){
		//new measure comes without selected patient
		Intent i = new Intent(getApplicationContext(), MeasureSetPatientActivity.class);
		i.putExtra("mesures", mesures);
		startActivity(i);
		
		this.finish();
	}   


	@Override
	protected void onKeyBack() {
		Log.w(Constants.TAG, CLASSNAME+" Retour sur le panneau About");
		Intent i = new Intent(getApplicationContext(), AboutActivity.class);
		startActivity(i);
		finish();
	}
	
	
	private int find(int[] array, int value) {
	    for(int i=0; i<array.length; i++){
	         if(array[i] == value){
	             return i;
	         }
	    }
	    return -1;
	}


}
