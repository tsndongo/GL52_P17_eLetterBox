package com.semantic.ecare_android_v2.object;

public class Mount {
	//Construct
	
	//for THIS partition
	
	private String path="";
	private String mode="";
	private String type="";
	private String dev="";
	
	
	public Mount(String path, String mode, String type, String dev){
		this.path=path;
		this.mode=mode;
		this.type=type;
		this.dev=dev;
	}
	
	public String getPath(){
		return path;
	}
	
	public String getMode(){
		return mode;
	}
	
	public String getType(){
		return type;
	}
	
	public String getDev(){
		return dev;
	}

	public void setMode(String mode) {
		this.mode=mode;
	}
}
