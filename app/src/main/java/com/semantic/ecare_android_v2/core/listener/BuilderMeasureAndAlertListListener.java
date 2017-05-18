package com.semantic.ecare_android_v2.core.listener;

import java.util.ArrayList;

import com.semantic.ecare_android_v2.object.Alert;
import com.semantic.ecare_android_v2.object.Measure;

public interface BuilderMeasureAndAlertListListener {	
	void endOfUpdateMeasureAndAlertList(ArrayList<Measure> measureList, ArrayList<Alert> alertList); //End of updating measure and alert list (with Internet)
	void endOfGetMeasureAndAlertListFromLocal(ArrayList<Measure> measureList, ArrayList<Alert> alertList); //End of building measure and alert List from local DB (without Internet)
	void errorNoMeasureAndAlertData(); //No Internet connection, and No data in the local DB
	void runFireGetMeasureAndAlertListFromInternet(); // get patient list from the internet
	}
