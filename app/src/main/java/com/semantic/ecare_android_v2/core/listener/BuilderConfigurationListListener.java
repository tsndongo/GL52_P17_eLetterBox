package com.semantic.ecare_android_v2.core.listener;

import com.semantic.ecare_android_v2.object.ConfigurationList;



public interface BuilderConfigurationListListener {
	
	void endOfUpdateConfigurationList(ConfigurationList list); //End of updating configuration list (with Internet)
	void endOfGetConfigurationListFromLocal(ConfigurationList list); //End of building configuration List from local DB (without Internet)
	void errorNoConfigurationData(); //No Internet connection, and No data in the local DB
	void runFireGetConfigListFromInternet(); // get configuration list from from the internet
	void runErrorNoInternet(); // Pas de connection internet
	
}
