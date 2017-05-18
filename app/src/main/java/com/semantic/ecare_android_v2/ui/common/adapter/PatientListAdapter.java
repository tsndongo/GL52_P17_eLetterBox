package com.semantic.ecare_android_v2.ui.common.adapter;

import java.util.ArrayList;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.object.Patient;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class PatientListAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private ArrayList<Patient> values = new ArrayList<Patient>();

	public ArrayList<Patient> getValues() {
		return values;
	}

	public void setValues(ArrayList<Patient> values) {
		this.values = values;
	}

	public PatientListAdapter(Context context, ArrayList<Patient> values) {
		mInflater = LayoutInflater.from(context);
		this.values=values;
	}
	
	@Override
	public int getCount() {
		return values.size();
	}

	@Override
	public Patient getItem(int position) {
		return values.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.listview_patient_item, null);
			holder = new ViewHolder();
			holder.tvPatientName = (TextView) convertView.findViewById(R.id.tvPatientName);
			holder.tvSubtitle = (TextView) convertView.findViewById(R.id.tvSubtitle);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		String patientName = genderIntToString(values.get(position).getGender())+" "+values.get(position).getSurname()+" "+values.get(position).getName().toUpperCase();
		String patientSymptome=values.get(position).getSymptome();
		holder.tvPatientName.setText(patientName);
		if(patientSymptome.equals("")){
			holder.tvSubtitle.setVisibility(View.INVISIBLE);
		}else{
			holder.tvSubtitle.setVisibility(View.VISIBLE);
			holder.tvSubtitle.setText(patientSymptome);
		}
		
		
	
		return convertView;

	}
	
	static class ViewHolder {
		TextView tvPatientName;
		TextView tvSubtitle;
	}
	
	private String genderIntToString(int gender){
		return gender==1 ? "M." : "Mme.";
	}

}
