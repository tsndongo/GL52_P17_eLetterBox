package com.semantic.ecare_android_v2.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.NoteModel;
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
        NoteListAdapter adapter = new NoteListAdapter(this,
                R.layout.listview_note_item, noteDataList);
    }

    private class NoteListAdapter extends ArrayAdapter{

        private Context context;
        private ArrayList<NoteModel> lPerson;
       // private  LayoutInflater inflater = null;

        public NoteListAdapter(Context context, int itemView, ArrayList<NoteModel> notes) {
            super(context, itemView, notes);
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
