package com.semantic.ecare_android_v2.util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

import net.newel.android.Log;

import android.content.Context;
import android.content.res.AssetManager;

public class FunctionsPropertiesFile {
	private static String CLASSNAME="fr.semantic.ecare.android.util.FunctionsPropertiesFile";
    
    public static String getProperty(String fileName, String propertyKey, String defaultValue){
    	Properties properties = new Properties();
    	try{
    		properties.load(new FileInputStream(fileName));
    		return properties.getProperty(propertyKey);
    	}catch(IOException ioe){   		
    		Log.e(Constants.TAG, ioe);
    	}
    	
    	return defaultValue;
    }
    
    
    public static void copyFileFromAppToPath(Context context,String filePath, String filename){
		//This operation required to be ROOT
		//This Operation required to have the Constants.BINARIES_LOCATION (/system/bin) mounted as RW
		
		AssetManager assetManager = context.getAssets();
		
		//copy from assets to /sdcard
        InputStream in = null;
        OutputStream out = null;
        try {
          in = assetManager.open(filename);
          out = new FileOutputStream(filePath+filename);
          copyFile(in, out);
          in.close();
          in = null;
          out.flush();
          out.close();
          out = null;
        } catch(IOException e) {
            Log.e(Constants.TAG, CLASSNAME+" Failed to copy asset file: " + filename, e);
        }
	}
	
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
	
}
