package com.semantic.ecare_android_v2.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.NoteModel;
import com.semantic.ecare_android_v2.object.Patient;
import com.semantic.ecare_android_v2.ui.common.activity.GenericDisconnectedActivity;
import com.semantic.ecare_android_v2.util.Constants;

import net.newel.android.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Syntiche on 5/18/2017.
 */
public class NoteListActivity extends GenericDisconnectedActivity {

    private ArrayList<NoteModel> noteDataList;
    private ListView noteListView;
    private Context context = this;
    NoteListAdapter adapter;
    private Handler handler;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        noteDataList = new ArrayList<NoteModel>();
        handler = new Handler();
        noteListView = (ListView) findViewById(R.id.note_list_view);
    }

    @Override
    protected void affichage_before_binding() {
        setContentView(R.layout.activity_note_list);
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
                createNoteListLayout();
            }
        }.start();
    }

    private void createNoteListLayout(){
        noteDataList = mBoundService.getNoteList();
        adapter = new NoteListAdapter(this,
                R.layout.listview_note_item, noteDataList);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                noteListView.setAdapter(adapter);
            }
        });


        noteListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final int targetId = noteDataList.get(position).getTargetId();
                final int targetTypeId = noteDataList.get(position).getTargetTypeId();

                if(targetTypeId == Constants.ALERT_TYPE){
                    Alert alerte = findAlertByIdInAllAlerts(targetTypeId);
                    NoteModel model = new NoteModel(alerte.getNote(), alerte.getNoteDate(), targetId, targetTypeId);

                    Intent intent = new Intent(context, NoteDetailActivity.class);
                    intent.putExtra(Constants.NOTEMODEL_KEY, model);
                    startActivityForResult(intent, Constants.REQ_CODE_NOTEDIALOG_ALERT);
                } else if (targetTypeId == Constants.PATIENT_TYPE){
                    Patient patient = findPatientById(targetTypeId);
                    NoteModel model = new NoteModel(patient.getNote(), patient.getNoteDate(), targetId, targetTypeId);

                    Intent intent = new Intent(context, NoteDetailActivity.class);
                    intent.putExtra(Constants.NOTEMODEL_KEY, model);
                    startActivityForResult(intent, Constants.REQ_CODE_NOTEDIALOG_PATIENT);
                } else if(targetTypeId == Constants.MEASURE_TYPE){
                    Measure measure = mBoundService.getMeasure(targetTypeId);
                    NoteModel model = new NoteModel(measure.getNote(), measure.getNoteDate(), targetId, targetTypeId);

                    Intent intent = new Intent(context, NoteDetailActivity.class);
                    intent.putExtra(Constants.NOTEMODEL_KEY, model);
                    startActivityForResult(intent, Constants.REQ_CODE_NOTEDIALOG_MEASURE);
                }
            }
        });
    }

    private class NoteListAdapter extends ArrayAdapter{

        private Context context;
        private ArrayList<NoteModel> noteList;
        //private ListView
       // private  LayoutInflater inflater = null;

        public NoteListAdapter(Context context, int resource, ArrayList<NoteModel> notes) {
            super(context, resource, notes);
            this.context = context;
            this.noteList = new ArrayList<NoteModel>(notes);

        }

        public int getCount() {
            return noteList.size();
        }

        public NoteModel getItem(int position) {
            return noteList.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            //final ViewHolder holder;
            try {
                if (row == null) {
                    LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                    row = inflater.inflate(R.layout.listview_note_item, null);
//                    holder = new ViewHolder();
//
//                    holder.display_name = (TextView) vi.findViewById(R.id.display_name);
//                    holder.display_number = (TextView) vi.findViewById(R.id.display_number);


                    //vi.setTag(holder);
                } else {
                    //holder = (ViewHolder) vi.getTag();
                }

                NoteModel note = noteList.get(position);
                TextView noteType = (TextView)findViewById(R.id.note_type_label);
                TextView noteTitle = (TextView)findViewById(R.id.note_title_label);
                TextView noteDate = (TextView)findViewById(R.id.note_date_label);

                switch(note.getTargetTypeId()){
                    case Constants.ALERT_TYPE:
                        noteType.setText("Alerte");
                        break;
                    case Constants.PATIENT_TYPE:
                        noteType.setText("Patient");
                        break;
                    case Constants.MEASURE_TYPE:
                        noteType.setText("Mesure");
                        break;
                }
                noteTitle.setText(note.getNote());
                noteDate.setText(note.getNoteDate());



//                holder.display_name.setText(lProducts.get(position).name);
//                holder.display_number.setText(lProducts.get(position).number);


            } catch (Exception e) {


            }
            return row;
        }


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
