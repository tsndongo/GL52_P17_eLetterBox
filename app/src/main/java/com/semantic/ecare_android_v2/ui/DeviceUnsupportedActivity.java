package com.semantic.ecare_android_v2.ui;



import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.R.id;
import com.semantic.ecare_android_v2.R.layout;
import com.semantic.ecare_android_v2.R.menu;
import com.semantic.ecare_android_v2.core.ServiceEcare;

import com.semantic.ecare_android_v2.util.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class DeviceUnsupportedActivity extends Activity {
	private ServiceEcare serviceEcare;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.device_unsuportted);
				
	}
	
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		getMenuInflater().inflate(R.menu.rafraichir_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()){
		case R.id.menu_rafraichir:
			Intent ig = new Intent(getApplicationContext(), SplashScreen.class);
			startActivity(ig);
			finish();
			
		}
		return super.onOptionsItemSelected(item);
	}
	
	
}
