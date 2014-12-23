package com.test;

public class SMSData { // SMS-Klasse

	private String number;
	private String body;
	
	public String getNumber(){
		return number;
	}
	
	public String getBody(){
		return body;
	}
	
	public void setBody(String body){
		this.body = body;
	}
	
	public void setNumber(String number){
		this.number = number;
	}
}
