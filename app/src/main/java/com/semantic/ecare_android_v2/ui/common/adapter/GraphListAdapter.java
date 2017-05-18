package com.semantic.ecare_android_v2.ui.common.adapter;



import com.semantic.ecare_android_v2.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class GraphListAdapter extends ArrayAdapter<String> {

	private LayoutInflater mInflater;
	private String[] values;
	//private int selectedIdx=0;
	
	//private Typeface tf;

	public GraphListAdapter(Context context, String[] values) {
		super(context, R.layout.spinner_select_graph2);//list
		mInflater = LayoutInflater.from(context);
		this.values=values;
		//tf= Typeface.createFromAsset(context.getAssets(),"fonts/GOTHIC.ttf");
	}
	
	@Override
	public int getCount() {
		return values.length;
	}

	@Override
	public String getItem(int position) {
		return values[position];
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.spinner_select_graph, null);//spinner once selected
			holder = new ViewHolder();
			

			holder.tvSensor = (TextView) convertView.findViewById(R.id.textSensor);
			//holder.tvSensor.setTypeface(tf);
			holder.tvSensor.setText(getItem(position));

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		
		
	
		return convertView;

	}

	
	private static class ViewHolder {
		TextView tvSensor;
	}
	
	
}
