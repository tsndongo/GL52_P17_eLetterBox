package com.semantic.ecare_android_v2.core;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.semantic.ecare_android_v2.core.listener.ServiceAntidoteClientListener;
import com.semantic.ecare_android_v2.object.CompoundMeasure;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.object.SensorState;
import com.semantic.ecare_android_v2.util.Constants;
import com.signove.health.service.HealthAgentAPI;
import com.signove.health.service.HealthServiceAPI;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import net.newel.android.Log;

public class ServiceAntidoteClient extends Service {

	private final Collection<ServiceAntidoteClientListener> serviceAntidoteClientListeners = new ArrayList<ServiceAntidoteClientListener>();
	private String CLASSNAME=this.getClass().getName();

	private HealthServiceAPI api;
	private Handler tm;
	private int [] specs = {0x1004, 0x100, 0x1029, 0x100f};
	private Map <String, String> map;

	private final IBinder mBinder = new LocalBinder();
	public class LocalBinder extends Binder {
		public ServiceAntidoteClient getService() {
			return ServiceAntidoteClient.this;
		}
	}

	@Override
	public IBinder onBind(Intent arg0) {
		Log.i(Constants.TAG,CLASSNAME+" OnBind ServiceAntidoteClient");
		return mBinder;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(Constants.TAG,CLASSNAME+" On create ServiceAntidoteClient");

		map = new HashMap<String, String>();
		tm = new Handler();

		//start and bind Service : HealthService
		Intent intent = new Intent("com.signove.health.service.HealthService");
		startService(intent);
		bindService(intent, serviceConnection, 0);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Intent intent = new Intent("com.signove.health.service.HealthService");
		stopService(intent);
		Log.i(Constants.TAG,CLASSNAME+" ServiceAntidoteClient et Health Service destroyed");
		try {
			Log.i(Constants.TAG, CLASSNAME + " Unconfiguring...");
			api.Unconfigure(agent);
		} catch (Throwable t) {
			Log.i(Constants.TAG, CLASSNAME + " Erro tentando desconectar");
		}
		unbindService(serviceConnection);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// We don't want this service to continue running if it is explicitly
		// stopped, so return not sticky.
		return START_NOT_STICKY;
	}

	public Document parse_xml(String xml){
		Document d = null;

		try {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			d = db.parse(new ByteArrayInputStream(xml.getBytes("UTF-8")));
		} catch (ParserConfigurationException e) {
			Log.i(Constants.TAG, CLASSNAME + " XML parser error");
			Log.i(Constants.TAG, e);
		} catch (SAXException e) {
			Log.i(Constants.TAG, CLASSNAME + " SAX exception");
			Log.i(Constants.TAG, e);
		} catch (IOException e) {
			Log.i(Constants.TAG, CLASSNAME + " IO exception in xml parsing");
			Log.i(Constants.TAG, e);
		}

		return d;
	}


	public void handle_packet_connected(String path, String dev){
		map.put(path, dev);
		Log.i(Constants.TAG, CLASSNAME + "dev : " + dev);
		if(dev!=null){
			SensorState state = new SensorState(dev, SensorState.STATE_CONNECTED);
			fireNewSensorState(state);
			Log.i(Constants.TAG, CLASSNAME + " mac address : " + dev);
		}
	}

	public void handle_packet_disconnected(String path){
		String dev = map.get(path);
		if(dev!=null){
			SensorState state = new SensorState(dev, SensorState.STATE_DISCONNECTED);
			fireNewSensorState(state);
		}
		map.remove(path);
	}

	public void handle_packet_associated(String path, String xml){		
		String dev = map.get(path);
		if(dev!=null){
			SensorState state = new SensorState(dev, SensorState.STATE_ASSOCIATED);
			fireNewSensorState(state);
		}
	}

	public void handle_packet_disassociated(String path){
		String dev = map.get(path);
		if(dev!=null){
			SensorState state = new SensorState(dev, SensorState.STATE_DISASSOCIATED);
			fireNewSensorState(state);
		}
	}

	public void handle_packet_description(String path, String xml){
		//nothing
	}

	public String get_xml_text(Node n) {
		String s = null;
		NodeList text = n.getChildNodes();

		for (int l = 0; l < text.getLength(); ++l) {
			Node txt = text.item(l);
			if (txt.getNodeType() == Node.TEXT_NODE) {
				if (s == null) {
					s = "";
				}
				s += txt.getNodeValue();							
			}
		}
		return s;
	}

	public void handle_packet_measurement(String path, String xml)
	{
		String dev = map.get(path);
		if(ServiceEcare.SENSOR_TYPE.get(dev) != null)
		{
			SensorState state = new SensorState(dev, SensorState.STATE_MEASUREMENT);
			fireNewSensorState(state);

			Document d = parse_xml(xml);

			if (d == null) {
				return;
			}

			int sensor_type = Integer.parseInt(d.getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(1).getChildNodes().item(0).getChildNodes().item(0).getChildNodes().item(1).getChildNodes().item(0).getNodeValue().replace(" ", "").replace("\n", ""));

			switch(sensor_type){
			case Measure.SENSOR_TENSION:
				CompoundMeasure mesuresTension = AntidoteParser.parseTension(d);
				if(mesuresTension.size()>0){
					fireNewSensorMesure(mesuresTension);
					Log.i(Constants.TAG,CLASSNAME + " Mesure Tension : " + mesuresTension.get(0).getSensor() + " : SYS : " + mesuresTension.get(0).getValue()+"/ DIA : "+mesuresTension.get(1).getValue()+"/ PAM : "+mesuresTension.get(2).getValue()+ " ; Date : " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(mesuresTension.get(0).getDate()));
				}else{
					fireNewSensorState(new SensorState(dev, SensorState.STATE_ERROR));
					Log.i(Constants.TAG, CLASSNAME+" Erreur de données, pas de mesure contenue dans cette CM !");
				}
				break;

			case Measure.SENSOR_POIDS:
				CompoundMeasure mesuresPoids = AntidoteParser.parseWeight(d);
				if(mesuresPoids.size()>0){
					fireNewSensorMesure(mesuresPoids);
					Log.i(Constants.TAG,CLASSNAME + " Mesure Balance : " + mesuresPoids.get(0).getSensor() + " : " + mesuresPoids.get(0).getValue() +  " ; Date : " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(mesuresPoids.get(0).getDate()));
				}else{
					fireNewSensorState(new SensorState(dev, SensorState.STATE_ERROR));
					Log.i(Constants.TAG, CLASSNAME+" Erreur de données, pas de mesure contenue dans cette CM !");
				}
				break;

			case Measure.SENSOR_OXY:
				CompoundMeasure mesuresOxy = AntidoteParser.parseOxymeter(d);
				if(mesuresOxy.size()>0){
					fireNewSensorMesure(mesuresOxy);
					Log.i(Constants.TAG,CLASSNAME + "Mesure Oxy + cardio : " + mesuresOxy.get(0).getSensor() + " : " + mesuresOxy.get(0).getValue() +  " ; Date : " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(mesuresOxy.get(0).getDate()));
					Log.i(Constants.TAG,CLASSNAME + "Mesure Oxy + cardio : " + mesuresOxy.get(1).getSensor() + " : " + mesuresOxy.get(1).getValue() +  " ; Date : " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(mesuresOxy.get(1).getDate()));
				}else{
					fireNewSensorState(new SensorState(dev, SensorState.STATE_ERROR));
					Log.i(Constants.TAG, CLASSNAME+" Erreur de données, pas de mesure contenue dans cette CM !");
				}
				break;

			case Measure.SENSOR_CARDIO:
				if(!(ServiceEcare.SENSOR_TYPE.get(dev) == 18948))
				{
					CompoundMeasure mesuresCardio = AntidoteParser.parseCardio(d);
					if(mesuresCardio.size()>0){
						fireNewSensorMesure(mesuresCardio);
						Log.i(Constants.TAG,"Mesure Cardio : " + mesuresCardio.get(0).getSensor() + " : " + mesuresCardio.get(0).getValue() +  " ; Date : " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(mesuresCardio.get(0).getDate()));

					}else{
						fireNewSensorState(new SensorState(dev, SensorState.STATE_ERROR));
						Log.i(Constants.TAG, CLASSNAME+" Erreur de données, pas de mesure contenue dans cette CM !");
					}
				}
				break;
			}
		}

		else	
			fireNewSensorState(new SensorState(dev, SensorState.STATE_ERROR));
	}

	private void RequestConfig(String dev)
	{	
		try {
			Log.i(Constants.TAG, CLASSNAME + " Getting configuration ");
			String xmldata = api.GetConfiguration(dev);
			Log.i(Constants.TAG, CLASSNAME + " Received configuration");
			Log.i(Constants.TAG, CLASSNAME + " .." + xmldata);
		} catch (RemoteException e) {
			Log.i(Constants.TAG, CLASSNAME + " Exception (RequestConfig)");
			Log.i(Constants.TAG, e);
		}
	}

	private void RequestDeviceAttributes(String dev)
	{	
		try {
			Log.i(Constants.TAG, CLASSNAME + " Requested device attributes");
			api.RequestDeviceAttributes(dev);
		} catch (RemoteException e) {
			Log.i(Constants.TAG, CLASSNAME + " Exception (RequestDeviceAttributes)");
			Log.i(Constants.TAG, e);
		}
	}




	private HealthAgentAPI.Stub agent = new HealthAgentAPI.Stub() {
		@Override
		public void Connected(String dev, String addr) {
			Log.i(Constants.TAG, CLASSNAME + " Connected " + dev);
			Log.i(Constants.TAG, CLASSNAME + " ..." + addr);
			handle_packet_connected(dev, addr);
		}

		@Override
		public void Associated(String dev, String xmldata) {
			final String idev = dev;
			Log.i(Constants.TAG, CLASSNAME + " Associated " + dev);			
			Log.i(Constants.TAG, CLASSNAME + " ...." + xmldata);			
			handle_packet_associated(dev, xmldata);

			Runnable req1 = new Runnable() {
				public void run() {
					RequestConfig(idev);
				}
			};
			Runnable req2 = new Runnable() {
				public void run() {
					RequestDeviceAttributes(idev);
				}
			};
			tm.postDelayed(req1, 1); 
			tm.postDelayed(req2, 500); 


		}
		@Override
		public void MeasurementData(String dev, String xmldata) {
			Log.i(Constants.TAG, CLASSNAME + " MeasurementData " + dev);
			Log.i(Constants.TAG, CLASSNAME + " ....." + xmldata);
			handle_packet_measurement(dev, xmldata);
		}
		@Override
		public void DeviceAttributes(String dev, String xmldata) {
			Log.i(Constants.TAG, CLASSNAME + " DeviceAttributes " + dev);			
			Log.i(Constants.TAG, CLASSNAME + " .." + xmldata);
			handle_packet_description(dev, xmldata);
		}

		@Override
		public void Disassociated(String dev) {
			Log.i(Constants.TAG, CLASSNAME + " Disassociated " + dev);						
			handle_packet_disassociated(dev);
		}

		@Override
		public void Disconnected(String dev) {
			Log.i(Constants.TAG, CLASSNAME + " Disconnected " + dev);
			handle_packet_disconnected(dev);
		}
	};

	//Binding service AntidoteServer
	private ServiceConnection serviceConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			Log.i(Constants.TAG, CLASSNAME + " Service connection established");

			// that's how we get the client side of the IPC connection
			api = HealthServiceAPI.Stub.asInterface(service);
			try {
				Log.i(Constants.TAG, CLASSNAME + " Configuring...");
				api.ConfigurePassive(agent, specs);
			} catch (RemoteException e) {
				Log.i(Constants.TAG, CLASSNAME + " Failed to add listener", e);
				Log.i(Constants.TAG, CLASSNAME + e);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			Log.i(Constants.TAG, CLASSNAME + " Service connection closed");
		}
	};


	//Listeners to send data to service ecare
	public void addListener(ServiceAntidoteClientListener listener){		
		serviceAntidoteClientListeners.add(listener);
	}

	//this method will normally fix the problem of multiple "ServiceEcare" Listeners
	public void removeListener(ServiceAntidoteClientListener listener){
		serviceAntidoteClientListeners.remove(listener);
	}

	//public pour lancer des tests en debug
	public void fireNewSensorMesure(CompoundMeasure mesure) {
		if(mesure!=null){
			for (ServiceAntidoteClientListener listener : serviceAntidoteClientListeners) {
				listener.newSensorMeasure(mesure);
			}
		}
	}

	private void fireNewSensorState(SensorState state) {
		for (ServiceAntidoteClientListener listener : serviceAntidoteClientListeners) {
			listener.newSensorState(state);
		}
	}

}
