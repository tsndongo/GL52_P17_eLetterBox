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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.newel.android.Log;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import com.semantic.ecare_android_v2.core.ServiceEcare;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.ui.MeasureActivity;
import com.semantic.ecare_android_v2.util.Constants;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint.Align;
import android.view.View;
import android.widget.Toast;


public class SensorValuesChart extends AbstractChart {
	

	private double min, max;
	private GraphicalView view;
	private String CLASSNAME=this.getClass().getName();


	public static final int PERIOD_WEEK=7;
	public static final int PERIOD_MONTH=31;
	public static final int PERIOD_YEAR=365;
	
	
	

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

  /**
   * Executes the chart demo.
   * 
   * @param context the context
   * @return the built intent
   */

  	public GraphicalView execute(final Activity act, final ServiceEcare service, ArrayList<ArrayList <Measure>> l, int period, final int sensor) {
	
		String dateFormat="EEEE d/MM";
		switch(period){
			case PERIOD_WEEK:
				dateFormat="EEEE d/MM";
				break;
			case PERIOD_MONTH:
				 dateFormat="d MMMM";
				break;
			case PERIOD_YEAR:
				 dateFormat="MMMM";
				break;
		}
			
		//Log.i(Constants.TAG, CLASSNAME+" Format de la période : "+dateFormat);
		
		view = ChartFactory.getTimeChartView(service, 
				getDataset(service,l, period, sensor), 
				getRenderer(service, l, period, sensor), 
				dateFormat);
		
		view.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				SeriesSelection seriesSelection = view.getCurrentSeriesAndPoint();
				// double[] xy = view.toRealPoint(0);
				service.sessionAction();
				
				if (seriesSelection == null) {
					//Toast.makeText(context, "Sélectionnez une mesure",Toast.LENGTH_SHORT).show();
				} else {
					//affiche le panneau de mesure avec quelques nouvelles informations et date et etc etc
					double date_double = seriesSelection.getXValue();
					Long date_long = (long) date_double;
					Date date = new Date(date_long);

					Log.i(Constants.TAG, CLASSNAME+" Recherche de la CM du point en question : "+service.getSelectedPatient().getUid()+" "+sensor+" "+DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(date));
					//Search in database the measure for this sensor at this date for this patient
					CompoundMeasure cm = service.searchMeasure(service.getSelectedPatient().getUid(),sensor,date);
					
					if(cm.size()>0){
						ArrayList<CompoundMeasure> mesures = new ArrayList<CompoundMeasure>();
						mesures.add(cm);
						
						
						Intent iw = new Intent(service, MeasureActivity.class);
						iw.putExtra("mesures", mesures);
						iw.putExtra("GraphMesure", true);
						iw.putExtra("returnToGraphView", true);
						act.startActivity(iw);
						act.finish();
					}else{
						Toast.makeText(service," La mesure du " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(date)+ "\n vaut " + seriesSelection.getValue()+ " " + service.getResources().getString(Constants.SENSOR_UNIT.get(sensor)),Toast.LENGTH_LONG).show();
					}
				}
			}
		});

		      
		return view;
  }
		  
  private XYMultipleSeriesRenderer getRenderer(Context context, ArrayList<ArrayList <Measure>> l, int period, int sensor) {
		XYMultipleSeriesRenderer renderer = new XYMultipleSeriesRenderer();    
		renderer.setYAxisMax(max+1);
		renderer.setYAxisMin(min-1);
		renderer.setYLabelsAlign(Align.RIGHT);
		renderer.setXTitle("");
		//long now = new Date().getTime();
		////last measure = max X
		long lastmeasure = l.get(0).get(l.get(0).size()-1).getDate().getTime();
		renderer.setXAxisMax(lastmeasure);
		renderer.setXAxisMin(lastmeasure-(86400000*((double)period)));
		renderer.setXLabelsAlign(Align.CENTER); 
		XYSeriesRenderer r = new XYSeriesRenderer();
		r.setColor(Color.BLUE);
		r.setPointStyle(PointStyle.X);
		//r.setFillBelowLine(true);
		r.setFillBelowLineColor(Color.WHITE);
		r.setLineWidth(2);
		r.setFillPoints(true);	    
		renderer.addSeriesRenderer(r);
		
		if(l.size()>=2) {
			r = new XYSeriesRenderer();
			r.setPointStyle(PointStyle.X);
			r.setColor(Color.RED);
			r.setLineWidth(2);
			r.setFillPoints(true);
			renderer.addSeriesRenderer(r);
			
		}
		
		renderer.setYLabels(10);
		renderer.setXLabels(10);
			    

		renderer.setAxisTitleTextSize(30);
		renderer.setLabelsTextSize(20);
		renderer.setLegendTextSize(30);
		renderer.setPointSize(5f);

		renderer.setMargins(new int[] {70, 70, 30, 70}); //(dessus,gauche,bas,droite)
		renderer.setAxesColor(Color.DKGRAY);  //DKGRAY);
		renderer.setGridColor(Color.DKGRAY);
		renderer.setLabelsColor(Color.WHITE);  //LTGRAY);
		
		renderer.setShowGrid(true);
		

		renderer.setChartTitle(context.getResources().getString(Constants.SENSOR_TITLE.get(sensor)));
		renderer.setChartTitleTextSize(50);
		renderer.setYTitle(context.getResources().getString(Constants.SENSOR_UNIT.get(sensor)));

		
		
		renderer.setClickEnabled(true);
		renderer.setSelectableBuffer(50);
		//Log.i(Constants.TAG, CLASSNAME+" Renderer graphique pret");
		return renderer;
		}
			  
		private XYMultipleSeriesDataset getDataset(Context context, ArrayList<ArrayList<Measure>> l, int period, int sensor) {
			//int k=0;
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
			
			//Log.i(Constants.TAG, CLASSNAME+" Datasets prets");
			return buildDateDataset(titles, x, values);
		}
		
		private void searchMinAndMax(ArrayList<ArrayList<Measure>> l) {
			//Log.i(Constants.TAG, CLASSNAME+" Recherche Min/Max");
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
			Log.i(Constants.TAG, CLASSNAME+" Min = "+min+" ; Max = "+max);
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
			//Log.i(Constants.TAG, CLASSNAME+" Build DetaDataSet OK");
			return dataset;
		}

}
