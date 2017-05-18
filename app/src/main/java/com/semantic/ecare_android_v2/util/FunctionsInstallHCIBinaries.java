package com.semantic.ecare_android_v2.util;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import com.semantic.ecare_android_v2.R;
import com.semantic.ecare_android_v2.object.Mount;

import net.newel.android.Log;
import android.content.Context;
import android.content.res.AssetManager;
import android.widget.Toast;


public class FunctionsInstallHCIBinaries {
	private static String CLASSNAME="fr.semantic.ecare.android.util.FunctionsInstallBinaries";	
	
	private static void execCommandWithoutResult(String[] command){
		try{
			Process process = Runtime.getRuntime().exec(command);
		    process.waitFor();
			
		} catch (IOException e) {
		    Log.e(Constants.TAG, e);
		} catch (InterruptedException e) {
			Log.e(Constants.TAG, e);
		}
	}
	
	
	public static void launchHCISUCommand(Context context){

		checkBinariesAndProceed(context);
		
		
		String[] command = new String[3];
		command[0] = "su";
		command[1] = "-c";
        command[2] = Constants.BINARY_HCICONFIG+" hci0 sspmode 0";
        
        execCommandWithoutResult(command);
		


	    Toast.makeText(context, context.getResources().getString(R.string.toast_disable_ssp), Toast.LENGTH_LONG).show();
	}
	
	private static void changeMountMode(Mount partition, String newMode){
		if(!partition.getMode().equals(newMode)){
		
			String[] command = new String[3];
			command[0] = "su";
			command[1] = "-c";
            command[2] = "mount -o remount,"+newMode+" -t "+partition.getType()+" "+partition.getDev()+" "+partition.getPath();
            
            execCommandWithoutResult(command);
            
            Log.i(Constants.TAG, CLASSNAME+" montage de "+partition.getPath()+" en "+newMode);
            partition.setMode(newMode);
		
		}else{
			Log.i(Constants.TAG, CLASSNAME+" "+partition.getPath()+" est déja en "+newMode);
		}
	}
	
	
	private static Mount getPartitionInfo(String partition){
		///dev/block/platform/omap/omap_hsmmc.1/by-name/FACTORYFS /system ext4 rw,relatime,barrier=1,data=ordered 0 0
		Mount mount=null;
		try {
			FileInputStream fstream = new FileInputStream("/proc/mounts");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				String[] array = strLine.split(" ");
				if(array[1].equals(partition)){
					String dev=array[0];
					String type=array[2];
					String[] array2 = array[3].split(",");
					String mode=array2[0];
					
					mount=new Mount(partition, mode, type, dev);
					break;
				}
			}
			in.close();
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	
		
		return mount;
	}
	
	private static void checkBinariesAndProceed(Context context) {
		File binary_hciconfig = new File(Constants.BINARIES_LOCATION,Constants.BINARY_HCICONFIG);
		if(!binary_hciconfig.exists()){
			Log.i(Constants.TAG, CLASSNAME+" HCIConfig non détecté !");
			
			Mount partition = getPartitionInfo(Constants.BINARIES_PARTITION);
			if(partition!=null){
				boolean systemPartitionMounted = !partition.getMode().equals("rw");
				
				if(systemPartitionMounted){
					changeMountMode(partition, "rw");
				}

				
				copyFileFromAppToSystem(context, Constants.BINARIES_LOCATION, Constants.TMP_LOCATION, Constants.BINARY_HCICONFIG);
				
				
				if(systemPartitionMounted){
					changeMountMode(partition, "ro");
				}
				
				Toast.makeText(context, context.getResources().getString(R.string.toast_binaries_copied), Toast.LENGTH_LONG).show();   
				Log.i(Constants.TAG, CLASSNAME+" Copie des fichiers binaires terminé");

				
			}else{
				Log.e(Constants.TAG, CLASSNAME+" ATTENTION : Impossible de récupérer la partition "+Constants.BINARIES_PARTITION+" !");
			}

			
		}/*else{
			System.out.println("HCIConfig détecté. On ne fait rien");
		}*/

	}
	
	private static void copyFileFromAppToSystem(Context context,String filePath, String tmpPath, String filename){
		//This operation required to be ROOT
		//This Operation required to have the Constants.BINARIES_LOCATION (/system/bin) mounted as RW
		
		AssetManager assetManager = context.getAssets();
		
		//copy from assets to /sdcard
        InputStream in = null;
        OutputStream out = null;
        try {
          in = assetManager.open(filename);
          out = new FileOutputStream(tmpPath+filename);
          copyFile(in, out);
          in.close();
          in = null;
          out.flush();
          out.close();
          out = null;
        } catch(IOException e) {
            Log.e(Constants.TAG, CLASSNAME+" Failed to copy asset file: " + filename, e);
        }
        
        //move from /sdcard to /system/bin
		String[] command1 = new String[3];
		command1[0] = "su";
		command1[1] = "-c";
        command1[2] = "cp "+tmpPath+filename+" "+filePath+filename;
        execCommandWithoutResult(command1);
        
        //chmod executable
		String[] command2 = new String[3];
		command2[0] = "su";
		command2[1] = "-c";
        command2[2] = "chmod 0550 "+filePath+filename;
        execCommandWithoutResult(command2);
        
        //Delete sdcard file
		String[] command3 = new String[3];
		command3[0] = "su";
		command3[1] = "-c";
        command3[2] = "rm "+tmpPath+filename;
        execCommandWithoutResult(command3);
	}
	
	private static void copyFile(InputStream in, OutputStream out) throws IOException {
	    byte[] buffer = new byte[1024];
	    int read;
	    while((read = in.read(buffer)) != -1){
	      out.write(buffer, 0, read);
	    }
	}
	
}
