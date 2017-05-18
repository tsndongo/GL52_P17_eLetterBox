package com.semantic.ecare_android_v2.ui.common.activity;

import android.os.Bundle;

public abstract class GenericDisconnectedActivity extends GenericActivity {
	
	protected String CLASSNAME=this.getClass().getName();
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    protected abstract void affichage_before_binding();

	protected void affichage(){//this method is called ONLY after service binding !
		super.affichage();
    	//Generic displaying
			
    }
	
	@Override
	public void disconnect() {
		//anything (not connected!)
	}
	
	//Menus gérés dans les classes filles
}
