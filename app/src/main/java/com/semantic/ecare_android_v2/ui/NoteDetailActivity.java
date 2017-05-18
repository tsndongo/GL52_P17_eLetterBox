package com.semantic.ecare_android_v2.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.NoteModel;
import com.semantic.ecare_android_v2.object.Patient;
import com.semantic.ecare_android_v2.ui.common.activity.GenericDisconnectedActivity;
import com.semantic.ecare_android_v2.util.Constants;

import java.util.ArrayList;

/**
 * Created by Syntiche on 5/18/2017.
 */
public class NoteDetailActivity extends Activity {

    private static final int VOICE_RECOGNITION_REQUEST_CODE_RECORD = 1234;
    private static final int VOICE_RECOGNITION_REQUEST_CODE_COMPLETE = 4321;
    private NoteModel note;
    private TextView title;
    private TextView date;
    private TextView type;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_detail);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            this.note = (NoteModel) extras.get(Constants.NOTEMODEL_KEY);
        }


        this.title = (TextView) findViewById(R.id.note_title_label);
        this.date = (TextView) findViewById(R.id.note_title_label);
        this.type = (TextView) findViewById(R.id.note_title_label);

        switch (note.getTargetTypeId()) {
            case Constants.ALERT_TYPE:
                this.type.setText("Alerte");
                break;
            case Constants.PATIENT_TYPE:
                this.type.setText("Patient");
                break;
            case Constants.MEASURE_TYPE:
                this.type.setText("Mesure");
                break;
        }
    }

}
