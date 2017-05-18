package com.semantic.ecare_android_v2.core.listener;

public interface ServiceEcareUpdatingListener {
	void endOfPatientListBuild();//End Of building patient List (from Internet or from local DB)
	void endOfPatientListNoData(); //No data (from Internet or from local DB)
	void endOfConfigurationListBuild();//End Of building configuration List (from Internet or from local DB)
	void endOfConfigurationListNoData(); //No data (from Internet or from local DB)
	
}
