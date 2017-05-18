package com.semantic.ecare_android_v2.object;

public class Configuration<T> {
	private String key="";
	private T value;
	private String type;
	
	public Configuration(String key, T value, String type){
		this.key=key;
		this.value=value;
		this.type=type;
	}
	
	
	public void setKey(String key){
		this.key=key;
	}
	public String getKey(){
		return key;
	}
	
	public void setValue(T value){
		this.value=value;
	}
	public T getValue(){
		return value;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
}
