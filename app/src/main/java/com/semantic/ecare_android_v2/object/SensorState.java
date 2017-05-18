package com.semantic.ecare_android_v2.object;

public class SensorState {
	public static final int STATE_CONNECTED=1;
	public static final int STATE_ASSOCIATED=2;
	public static final int STATE_MEASUREMENT=3;
	public static final int STATE_DISASSOCIATED=4;
	public static final int STATE_DISCONNECTED=5;
	public static final int STATE_ERROR = 6;
	
	
	private int state=0;
	private String macAddr="";
	
	
	public SensorState(String macAddr, int state){
		this.macAddr=macAddr;
		this.state=state;
	}
	
	public int getState(){
		return state;
	}
	public String getMacAddr(){
		return macAddr;
	}
}
