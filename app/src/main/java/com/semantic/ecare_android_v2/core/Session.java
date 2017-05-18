package com.semantic.ecare_android_v2.core;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.semantic.ecare_android_v2.core.listener.SessionListener;
import com.semantic.ecare_android_v2.object.Measure;
import com.semantic.ecare_android_v2.util.Constants;

import net.newel.android.Log;
import android.util.SparseArray;

public class Session {
	//Cette classe représente une session de mesure
	//Créé à la connexion d'un patient
	//Supprimée à la déconnexion d'un patient
	
	private String CLASSNAME=this.getClass().getName();
	
	private SparseArray<Measure> mesures;
	private Date creationDate;
	private Date lastActionDate;
	private Timer timerDisconnect;
	private ServiceEcare serviceEcare;
	
	private final Collection<SessionListener> sessionListeners = new ArrayList<SessionListener>();
	
	
	public Session(ServiceEcare serviceEcare){
		creationDate=new Date();
		mesures = new SparseArray<Measure>();
		this.serviceEcare=serviceEcare;
		addListener(serviceEcare);
		
		Log.i(Constants.TAG,CLASSNAME+" Creation nouvelle Session  " + DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(creationDate));

		restartDisconnectTimer();		
	}
	
	//Si timer arrive à la fin lancement de l'évenement fireDisconnect();
	
	
	//retour : null si la mesure n'existe pas encore ; 
	//retour : Mesure m si elle existe : objectif : la supprimer de la base de données
	public Measure addMesure(Measure mesure){
		Measure m = mesures.get(mesure.getSensor());
		mesures.put(mesure.getSensor(), mesure);
		return m;
	}
	
	
	public void removeMesure(Measure mesure){
		mesures.remove(mesure.getSensor());
	}
	
	
	public Date getCreationDate(){
		return creationDate;
	}

	public Date getLastActionDate(){
		return lastActionDate;
	}
	public void action(){
		lastActionDate = new Date();
		Log.i(Constants.TAG,CLASSNAME+" Nouvelle action dans la session à "+DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM).format(lastActionDate));
		restartDisconnectTimer();
	}
	
	
	public void addListener(SessionListener listener){
		sessionListeners.add(listener);
	}
	
	private void fireDisconnect() {
		for (SessionListener listener : sessionListeners) {
			listener.disconnect();
		}
	}
	
	
	private void restartDisconnectTimer(){
		if(timerDisconnect!=null){
			//Log.i(Constants.TAG, CLASSNAME+" Arrêt forcé du timer !");
			timerDisconnect.cancel();
			timerDisconnect.purge();
		}
		timerDisconnect = new Timer();
		
		TimerTask tt = new TimerTask(){
			@Override
			public void run() {
				Log.i(Constants.TAG, CLASSNAME+" Fin de la session par fin du timer !");
				timerDisconnect.cancel();
				timerDisconnect.purge();
				fireDisconnect();
			}
			
		};
		
		/*try {
			timerDisconnect.schedule(tt, serviceEcare.getConfigurationList().getLongConfiguration("disconnectAfter"));
			//Log.i(Constants.TAG, CLASSNAME+" relancement du timer de déconnexion automatique. Expiration dans "+serviceEcare.getConfigurationList().getLongConfiguration("disconnectAfter")+"ms");
		} catch (Exception e) {
			Log.e(Constants.TAG, e);
		}*/
	}

	public void stopAll() {
		//Called to stop the timer !
		timerDisconnect.cancel();
		timerDisconnect.purge();
	}
}
