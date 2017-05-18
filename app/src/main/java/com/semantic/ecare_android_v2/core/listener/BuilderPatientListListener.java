package com.semantic.ecare_android_v2.core.listener;

import java.util.ArrayList;

import com.semantic.ecare_android_v2.object.Patient;


/**!
 * Function use fir listner to know when the BuilderPatientList have finish
 *
 */
public interface BuilderPatientListListener {
	
	void endOfUpdatePatientList(ArrayList<Patient> list); //End of updating patient list (with Internet)
	void endOfGetPatientListFromLocal(ArrayList<Patient> list); //End of building patient List from local DB (without Internet)
	void errorNoPatientData(); //No Internet connection, and No data in the local DB
	void runFireGetPatientListFromInternet(); // get patient list from the internet
	void endOfUpdatePatientListFromInternet(ArrayList<Patient> list);
}
