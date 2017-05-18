package com.semantic.ecare_android_v2.ui;

import java.util.ArrayList;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceAntidoteClient;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.core.listener.ServiceEcareUpdatingListener;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.util.Constants;
import com.semantic.ecare_android_v2.util.Functions;

import net.newel.android.Log;
import android.os.Bundle;
import android.os.IBinder;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SplashScreen extends Activity {

	private boolean mIsBound;
	private ServiceEcare mBoundService;
	private Intent serviceIntent;
	private ProgressBar progressBar;
	private TextView textLoading;
	private String CLASSNAME=this.getClass().getName();
	private static boolean configurationOk = false;
	private static boolean updatePatient = false;
	private static boolean firstTime = true;
	private AlertDialog dialog=null;
	private final int DIALOG_EXIT=1;
	private MainActivity mainActivity = new MainActivity();
	private WaitActivityPatient waitActivityPatient = new WaitActivityPatient();
	private ArrayList<CompoundMeasure> mesures;

	//Service Binding

	private ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName className, IBinder service) {
			mBoundService = ((ServiceEcare.LocalBinder)service).getService();
			mBoundService.ApplicationStarts();

			Log.i(Constants.TAG,CLASSNAME+" Connected to service");
		}

		public void onServiceDisconnected(ComponentName className) {
			mBoundService = null;

			Log.i(Constants.TAG,CLASSNAME+" Disconnected from service");
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.w(Constants.TAG, CLASSNAME+" START Application");

		setContentView(R.layout.activity_splash_screen);

		ActionBar bar = getActionBar();
		bar.setCustomView(R.layout.actionbar_view);
		bar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

		progressBar = (ProgressBar) findViewById(R.id.progressBar1);
		textLoading = (TextView) findViewById(R.id.tvLoading); // app is loading

		//launching ServiceEcare
		//Here, the service starts (it doesn't start with the phone
		//Then, the service stay alive even if activities bind/un-bind to it
        //Launch the acticvity Service eCare
		serviceIntent = new Intent(getApplicationContext(), ServiceEcare.class);

		if(mIsBound){
			doUnbindService();
		}
		doBindService();
	}

	@Override
	public void onResume()
	{
		super.onResume();
		mainActivity.setFirstTime(true);
		waitActivityPatient.setFirstTime(true);
		Bundle extras = getIntent().getExtras();
		if(extras !=null){
			if(mesures!=null)
				mesures.clear();
			mesures = (ArrayList<CompoundMeasure>) extras.getSerializable("mesures");
			Intent i = 	new Intent(getApplicationContext(), MeasureActivity.class);
			i.putExtra("mesures", mesures);
			startActivity(i);
			finish();
		}
		else
		{
			if(!isUpdatePatient())
			{
				if(isConfigurationOk())
				{
					Log.e(Constants.TAG, CLASSNAME + " Configuration ok : " + isConfigurationOk());
					mBoundService.buildConfigurationList();
				}
				else
				{
					Log.e(Constants.TAG, CLASSNAME + " Configuration not ok : " + isConfigurationOk());
					if(firstTime == true)
					{
						firstTime = false;
						Log.e(Constants.TAG, CLASSNAME + " first time, configuration not ok : " + isConfigurationOk());
					}
					else
					{
						mBoundService.buildConfigurationList();
						Log.e(Constants.TAG, CLASSNAME + " configuration after building everything : " + isConfigurationOk());
					}
				}
			}
			else
			{
				setUpdatePatient(false);
				Log.e(Constants.TAG, CLASSNAME + " set update Patient true");
				mBoundService.buildConfigurationList();
			}

		}
		Log.e(Constants.TAG, CLASSNAME + " Finish on resume");
	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_splash_screen, menu);
		return true;
	}

	@Override
	public void onDestroy(){
		super.onDestroy();
		if (mIsBound) {
			doUnbindService();
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_update:
			mBoundService.buildConfigurationList();
			break;

		case R.id.menu_reboot:
			showDialog(DIALOG_EXIT);
			break;

		}
		return true;
	}



	private void doBindService() {
		bindService(new Intent(getApplicationContext(), ServiceEcare.class), mConnection, Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	private void doUnbindService() {
		if (mIsBound) {
			unbindService(mConnection);
			mIsBound = false;
		}
	}

	@Override
	protected Dialog onCreateDialog(final int id) {
		switch(id){
		case DIALOG_EXIT:
			Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(getResources().getString(R.string.dialog_reboot_title));
			builder.setMessage(getResources().getString(R.string.dialog_reboot_content));
			builder.setCancelable(true);
			builder.setPositiveButton(getResources().getString(R.string.dialog_reboot_yes), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					reboot();
				}

			});
			builder.setNegativeButton(getResources().getString(R.string.dialog_reboot_no), new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {}
			});
			dialog = builder.create();
			dialog.show();
			break;
		}
		return null;
	}

	private void reboot(){
		Log.w(Constants.TAG, CLASSNAME+" Stop app and reboot");
		if (mIsBound) {
			doUnbindService();
		}

		Intent serviceIntent = new Intent(getApplicationContext(), ServiceEcare.class);
		stopService(serviceIntent);

		this.finish();

		Functions.execCommand("reboot");

	}

	private void callServiceBuildConfigurationList(){
		mBoundService.buildConfigurationList();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode){
		case KeyEvent.KEYCODE_BACK:
			break;

		case KeyEvent.KEYCODE_HOME:
			break;
		}
		return false;
	}

	public boolean isConfigurationOk() {
		return configurationOk;
	}

	public void setConfigurationOk(boolean configurationOk) {
		SplashScreen.configurationOk = configurationOk;
	}

	public boolean isUpdatePatient() {
		return updatePatient;
	}

	public void setUpdatePatient(boolean updatePatient) {
		SplashScreen.updatePatient = updatePatient;
	}


}
