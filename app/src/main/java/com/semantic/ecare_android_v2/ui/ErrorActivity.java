package com.semantic.ecare_android_v2.ui;

import java.util.ArrayList;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceAntidoteClient;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.ui.common.activity.GenericDisconnectedActivity;
import com.semantic.ecare_android_v2.util.Constants;

import net.newel.android.Log;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ErrorActivity extends GenericDisconnectedActivity {
	private String errorTitle="Erreur";
	private String errorDetail="Detail de l'erreur";
	private String errorSummary="Une erreur est survenue !";
	
	protected String CLASSNAME=this.getClass().getName();
	
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.i(Constants.TAG, CLASSNAME+" Oncreate Class");
        
        //To avoid to give an Activity as Bundle of the intent, When the user come here, the previous activity must be not closed !

        Bundle extras = getIntent().getExtras();
	    if(extras !=null){
	    	errorTitle = extras.getString("errorTitle");
	    	errorDetail = extras.getString("errorDetail");
	    	errorSummary = extras.getString("errorSummary");
	    }

	    

		    
    }
    
    
	@Override
	protected void affichage_before_binding() {
		 setContentView(R.layout.activity_error);
	}
	
	
	@Override
    protected void affichage(){
    	super.affichage();
    	
	    if(errorTitle!=null && errorDetail!=null && errorSummary!=null){
	    	//fill in the error message in the fields of this activity
		    RelativeLayout lError = (RelativeLayout) findViewById(R.id.lError); //banner header
		    TextView tvError = (TextView) findViewById(R.id.tvError); //error summary in the header banner
		    TextView tvErrorTitle = (TextView) findViewById(R.id.tvErrorTitle);
		    TextView tvErrorDetail = (TextView) findViewById(R.id.tvErrorDetail);
		    
		    
		    tvError.setText(errorSummary);
		    tvErrorTitle.setText(errorTitle);
		    tvErrorDetail.setText(errorDetail);
		    lError.setVisibility(View.VISIBLE);
		    
		    
		    
		    ImageView ivBack = (ImageView) findViewById(R.id.ivBack);
		    ivBack.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					//closing this activity = only finish this, and return on the previous which must always be opened
					finish();
				}
			});
		    
	    }
	    
	}
    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_error, menu);
        return true;
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_back:
				finish();
				break;

		}	
		return true;
	}



	@Override
	protected void newMeasureReceived(ArrayList<CompoundMeasure> mesures){
		//nothing ?
	}
	


	@Override
	protected void onKeyBack() {
		finish();
	}

}
