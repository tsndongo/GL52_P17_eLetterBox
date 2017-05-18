package com.semantic.ecare_android_v2.ui;

import java.util.ArrayList;
import java.util.Iterator;

import net.newel.android.Log;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.ConfigurationList;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.NoteModel;
import com.semantic.ecare_android_v2.object.Patient;
import com.semantic.ecare_android_v2.ui.common.activity.GenericActivity;
import com.semantic.ecare_android_v2.ui.common.activity.GenericConnectedActivity;
import com.semantic.ecare_android_v2.ui.common.activity.GenericDisconnectedActivity;
import com.semantic.ecare_android_v2.ui.common.adapter.PatientListAdapter;
import com.semantic.ecare_android_v2.util.Constants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class AllNotesActivity extends GenericDisconnectedActivity {

	private ArrayList<NoteModel> noteList;
	private TableLayout noteTableLayout;
	private Context context = this;
	private Handler handler;
	private ArrayList<TableRow> rowList;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.noteList = new ArrayList<NoteModel>();
		this.handler = new Handler();
		this.rowList = new ArrayList<TableRow>();
	}


	@Override
	protected void affichage_before_binding() {
		setContentView(R.layout.activity_all_notes);
	}
	
	
	@Override
	protected void affichage(){
    	super.affichage();
    	Log.i(Constants.TAG, CLASSNAME +  "affichage appellee");
		
		if(mBoundService.isNeedToUpdate())
		{
			mBoundService.setNeedToUpdate(false);
			Log.i(Constants.TAG, CLASSNAME + "Need to install the app");
		}
		else
		{
			Log.i(Constants.TAG, CLASSNAME + "No need to install the app");
		}

    	if(!mBoundService.getLaunchComplete()){
			Intent i = new Intent(getApplicationContext(), SplashScreen.class);
			startActivity(i);
			finish();
    	}
    	
    	new Thread(){
    		@Override
    		public void run(){
    			createNoteTableLayout();
    		}
    	}.start();
    }
	
	private void createNoteTableLayout(){
		
		noteList = mBoundService.getNoteList();
		rowList = new ArrayList<TableRow>();
		this.noteTableLayout = (TableLayout)findViewById(R.id.tableNotes);
		int tagInt = 0;
		for(NoteModel n : noteList){
			TableRow row = addUINote(new NoteModel(n.getNote(), n.getNoteDate(), n.getTargetId(), n.getTargetTypeId()));
			row.setTag(tagInt++);
			rowList.add(row);
		}
		
		handler.post(runUpdateTableLayout);
	}
	
	private Runnable runUpdateTableLayout = new Runnable(){
		@Override
		public void run(){	
			// clear the noteList
			noteTableLayout.removeAllViews();
			
			// rebuild it
        	Iterator<TableRow> it = rowList.iterator();
        	while(it.hasNext()){
        		TableRow row = it.next();
        		noteTableLayout.addView(row);
        	}
		}
	};
	
	private TableRow addUINote(NoteModel note){
		TableRow inflateRow = (TableRow)View.inflate(context, R.layout.note_row_inflate, null);
		
		
		TextView tvNote = (TextView) inflateRow.findViewById(R.id.tvNoteMessage);
		TextView tvNoteDate = (TextView) inflateRow.findViewById(R.id.tvNoteDate);
		TextView tvNoteTargetType = (TextView) inflateRow.findViewById(R.id.tvNoteTargetType);
		Button viewNoteButton = (Button) inflateRow.findViewById(R.id.viewNoteButton);
		Button deleteNoteButton = (Button) inflateRow.findViewById(R.id.deleteNoteButton);
		
		
		switch(note.getTargetTypeId()){
		case Constants.ALERT_TYPE:
			tvNoteTargetType.setText("Alerte");
			break;
		case Constants.PATIENT_TYPE:
			tvNoteTargetType.setText("Patient");
			break;
		case Constants.MEASURE_TYPE:
			tvNoteTargetType.setText("Mesure");
			break;
		}
		
		tvNote.setText(note.getNote());
		tvNoteDate.setText(note.getNoteDate());
		
		// todo : bind the buttons
		
		final int targetId = note.getTargetId();
		final int targetTypeId = note.getTargetTypeId();
		
		deleteNoteButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				AlertDialog validateDialog = initiateValidateDialog(targetId, targetTypeId);
				validateDialog.show();
			}
		});
		
		viewNoteButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				
				if(targetTypeId == Constants.ALERT_TYPE){
					Alert alerte = findAlertByIdInAllAlerts(targetId);
					NoteModel model = new NoteModel(alerte.getNote(), alerte.getNoteDate(), targetId, targetTypeId);
					
					Intent intent = new Intent(context, NoteDialogActivity.class);
					intent.putExtra(Constants.NOTEMODEL_KEY, model);
					startActivityForResult(intent, Constants.REQ_CODE_NOTEDIALOG_ALERT);
				}
				
				else if (targetTypeId == Constants.PATIENT_TYPE){
					Patient patient = findPatientById(targetId);
					NoteModel model = new NoteModel(patient.getNote(), patient.getNoteDate(), targetId, targetTypeId);
					
					Intent intent = new Intent(context, NoteDialogActivity.class);
					intent.putExtra(Constants.NOTEMODEL_KEY, model);
					startActivityForResult(intent, Constants.REQ_CODE_NOTEDIALOG_PATIENT);
				}
				
				else if(targetTypeId == Constants.MEASURE_TYPE){
					Measure measure = mBoundService.getMeasure(targetId);
					NoteModel model = new NoteModel(measure.getNote(), measure.getNoteDate(), targetId, targetTypeId);
					
					Intent intent = new Intent(context, NoteDialogActivity.class);
					intent.putExtra(Constants.NOTEMODEL_KEY, model);
					startActivityForResult(intent, Constants.REQ_CODE_NOTEDIALOG_MEASURE);
				}
			}
		});
		
		return inflateRow;
	}

	private AlertDialog initiateValidateDialog(final int targetId, final int targetTypeId){
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		builder.setTitle("Etes vous s�r de vouloir supprimer cette note ?");

		builder.setPositiveButton("Oui", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	        	   switch(targetTypeId){
					case Constants.ALERT_TYPE:
						mBoundService.updateNoteFromAlert(targetId, new NoteModel("" ,""));
						break;
					case Constants.PATIENT_TYPE:
						mBoundService.updateNoteFromPatient(targetId, new NoteModel("" ,""));
						break;
					case Constants.MEASURE_TYPE:
						mBoundService.updateNoteFromMeasure(targetId, new NoteModel("" ,""));
						break;
					}
	        	   dialog.dismiss();
	        	   
	        	   // reload activity to refresh the list
	        	   finish();
	        	   startActivity(getIntent());
	           }
	       });
		
		builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
	           public void onClick(DialogInterface dialog, int id) {
	               dialog.cancel();
	           }
	    });
		
		return builder.create();
	}
	
	@Override
	protected void newMeasureReceived(ArrayList<CompoundMeasure> mesures) {
		//new measure comes without selected patient
		Log.i(Constants.TAG, CLASSNAME+" Reception d'une mesure");
		Intent i = null;
		if(ServiceEcare.isSaveMeasure())
		{
			i = new Intent(getApplicationContext(), MeasureSetPatientActivity.class);
			i.putExtra("mesures", mesures);
			startActivity(i);
			this.finish();
		}
		else
		{
			Log.i(Constants.TAG, CLASSNAME +  "ok");
			i = new Intent(getApplicationContext(), MeasureActivity.class);
			i.putExtra("mesures", mesures);
			startActivity(i);
			this.finish();
		}
		
	}

	@Override
	protected void onKeyBack() {
		Intent iw = null;
		iw = new Intent(getApplicationContext(), MainActivity.class);
		startActivity(iw);
		finish();
	}
	
	@Override
	public void onResume(){
		Log.i(Constants.TAG,CLASSNAME+" OnResume");		
		super.onResume();
		if(mBoundService!=null)
		{
	        Log.i(Constants.TAG, CLASSNAME +  "bound");
			mBoundService.addListener(this);
		}
	}


	
}
