package com.florian;

public class Client{
	private String password;
	private String name;




	@Override
	public String toString() {
		return "Client{" +
				"password='" + password + '\'' +
				", name='" + name + '\'' +
				'}';
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
