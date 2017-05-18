/**
 * Copyright (C) 2009, 2010 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.semantic.ecare_android_v2.ui.chart;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;




import net.newel.android.Log;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;




import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.util.Constants;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint.Align;



public class SensorValuesChartWait extends AbstractChart {
	

	private double min, max;
	private GraphicalView view;


	public static final int PERIOD_WAIT=3; // 3 Jours
	private String CLASSNAME=this.getClass().getName();
	

  /**
   * Returns the chart name.
   * 
   * @return the chart name
   */
  public String getName() {
    return "Sensor data";
  }

  /**
   * Returns the chart description.
   * 
   * @return the chart description
   */
  public String getDesc() {
    return "short description";
  }

  
  	public GraphicalView execute(final Activity act, ArrayList<ArrayList <Measure>> l, final int sensor) {
	
		String dateFormat="d/MM";
			
		Log.i(Constants.TAG, CLASSNAME+" Format de la période : "+dateFormat);
		
		view = ChartFactory.getTimeChartView(act,
				getDataset(act, l, sensor), 
				getRenderer(act, l, sensor), 
				dateFormat);
		
		      
		return view;
  	}
		  
  	private XYMultipleSeriesRenderer getRenderer(Context context, ArrayList<ArrayList <Measure>> l, int sensor) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();    
		renderer.setYAxisMax(max+1);
		renderer.setYAxisMin(min-1);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setXTitle("");
		

		
		//long now = new Date().getTime();
		////last measure = max X
		long lastmeasure = l.get(0).get(l.get(0).size()-1).getDate().getTime();
		renderer.setXAxisMax(lastmeasure);
		renderer.setXAxisMin(lastmeasure-(86400000*((double)PERIOD_WAIT)));

		renderer.setXLabelsAlign(Align.CENTER); 
		renderer.setXLabelsColor(Color.DKGRAY);
		
		renderer.setYLabelsColor(0, Color.DKGRAY);
		
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.rgb(116, 116, 116));
		r.setPointStyle(PointStyle.X);
		
		//r.setFillBelowLine(true);
		//r.setFillBelowLineColor(Color.rgb(215, 215, 215));
		r.setLineWidth(2);
		r.setFillPoints(true);
		renderer.addSeriesRenderer(r);
		
		renderer.setMarginsColor(Color.rgb(222, 222, 222));
		
		if(l.size()>=2) {
			r = new XYSeriesRenderer();
			r.setPointStyle(PointStyle.X);
			r.setColor(Color.BLACK);
			//r.setFillBelowLine(true);
			//r.setFillBelowLineColor(Color.rgb(180, 180, 180));
			r.setLineWidth(2);
			r.setFillPoints(true);
			renderer.addSeriesRenderer(r);
			
		}
		
		renderer.setYLabels(4);
		renderer.setXLabels(4);
			    

		renderer.setAxisTitleTextSize(20);
		renderer.setLabelsTextSize(15);
		renderer.setLegendTextSize(20);
		renderer.setPointSize(5f);

		
		
		
		renderer.setMargins(new int[] {0, 50, 0, 0}); //(dessus,gauche,bas,droite)
		renderer.setAxesColor(Color.DKGRAY);  //DKGRAY);
		renderer.setGridColor(Color.rgb(200, 200, 200));
		renderer.setLabelsColor(Color.DKGRAY);  //LTGRAY);

		renderer.setBackgroundColor(Color.WHITE);
		renderer.setApplyBackgroundColor(true);
		
		renderer.setShowGrid(true);
		renderer.setLegendHeight(50);
		
		renderer.setYTitle(context.getResources().getString(Constants.SENSOR_UNIT.get(sensor)));
		
		renderer.setSelectableBuffer(50);

		return renderer;
	}
			  
	private XYMultipleSeriesDataset getDataset(Context context, ArrayList<ArrayList<Measure>> l, int sensor) {
		String[] titles = new String[l.size()];
		
		List<Date[]> x = new ArrayList<Date[]>();
		List<double[]> values = new ArrayList<double[]>();
		for(int i=0;i<l.size();i++) {
			Date[] date_tab = new Date[l.get(i).size()];
			double[] value_tab = new double[l.get(i).size()];
			for(int j=0; j<l.get(i).size();j++) {
				date_tab[j]=l.get(i).get(j).getDate();
				value_tab[j]=l.get(i).get(j).getValue();
			}	
			x.add(date_tab);
			values.add(value_tab);
		}
		
		switch(sensor){
		case Measure.SENSOR_TENSION:
			titles[0] = context.getResources().getString(Constants.SENSOR_LEGEND.get(Measure.SENSOR_TENSION_SYS));
			titles[1] = context.getResources().getString(Constants.SENSOR_LEGEND.get(Measure.SENSOR_TENSION_DIA));
			break;
		default:
			titles[0]=context.getResources().getString(Constants.SENSOR_LEGEND.get(sensor));
		}

		
		
		searchMinAndMax(l);
		
		Log.i(Constants.TAG, CLASSNAME+" Datasets prets");
		return buildDateDataset(titles, x, values);
	}
		
	private void searchMinAndMax(ArrayList<ArrayList<Measure>> l) {
		Log.i(Constants.TAG, CLASSNAME+" Recherche Min/Max");
		min=l.get(0).get(0).getValue(); //par défaut je met la première valeur comme min
		max=l.get(0).get(0).getValue(); //par défaut je met la première valeur comme max
		for(int i=0;i<l.size();i++){ //je parcours chaque liste 
			for(int j=0;j<l.get(i).size();j++) { // je parcours la liste à la recherche du min et du max
				if(l.get(i).get(j).getValue()<min)
					min=l.get(i).get(j).getValue();
				else if(l.get(i).get(j).getValue()>max)
					max=l.get(i).get(j).getValue();	
			}
		}
	}

	protected XYMultipleSeriesDataset buildDateDataset(String[] titles, List<Date[]> xValues, List<double[]> yValues) {
		XYMultipleSeriesDataset dataset = new XYMultipleSeriesDataset();
		int length = titles.length;
		for (int i = 0; i < length; i++) {
			TimeSeries series = new TimeSeries(titles[i]);
			Date[] xV = xValues.get(i);
			double[] yV = yValues.get(i);
			int seriesLength = xV.length;
			for (int k = 0; k < seriesLength; k++) {
				series.add(xV[k], yV[k]);
			}
			dataset.addSeries(series);
		}
		Log.i(Constants.TAG, CLASSNAME+" Build DetaDataSet OK");
		return dataset;
	}

}
