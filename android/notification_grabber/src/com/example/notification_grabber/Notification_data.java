package com.example.notification_grabber;

public class Notification_data {
	String title;
	String text;
	String bigText;

	public Notification_data(String title, String text, String big) {
		this.title = title;
		this.text = text;
		this.bigText = big;
	}

	public String getTitle() {
		return title;
	}

	public String getText() {
		return text;
	}

	public String getBigText() {
		return bigText;
	}
	
	
}
