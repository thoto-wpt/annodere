package com.test;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Parcelable;


public class NotificationService extends AccessibilityService
{

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) // Wenn eine NOTIFICATION aufgefunden wurde ...
	{
		System.out.println("onAccessibilityEvent");		
		
		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) 
	      {
			Notification notification = (Notification) event.getParcelableData();

			  Log.d("INC", "Incoming notification!");
			  Log.d("PACK", "App: " + event.getPackageName());
			  Log.d("TEXT", "Ticker: " + notification.tickerText);
			  Log.d("INTENT", "Text: " + notification.contentIntent);
			  Log.d("TIME", "Uhr: " + event.getEventTime());
			  Log.d("TYP", "Typ: " + event.getEventType());
			  Log.d("ITXT", "Text: " + event.getText());
			  
			  // Das oben zeigt an, was ich bisher auslesen kann
			  // Die Daten sind aber noch verschlüsselt in den Paketen => Pakete müssen geparst werden
			  
			  /* In Arbeit: Suche nach dem passenden Parser für SMS und weitere...
			  Intent result = new Intent("com.test", Uri.parse("content://result_uri"));
			  Uri uri = Uri.parse("content://com.test");
			  Cursor c = getContentResolver().query(uri, null, null, null, null);
			 
			  SMSData sms = new SMSData();
			  sms.setBody(c.getString(c.getColumnIndexOrThrow("body")).toString());
			  sms.setNumber(c.getString(c.getColumnIndexOrThrow("adress")).toString());
			  c.close();
			  
			  Log.d("SMS", "Text: " + sms.getBody() + sms.getNumber());*/
	      }
	}

	@Override
	protected void onServiceConnected() // Wenn der Service aktiv ist...
	{
		System.out.println("onServiceConnected"); // ... soll er sich melden, ...
		
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		//info.feedbackType = AccessibilityServiceInfo.FEEDBACK_SPOKEN; // Art des Feedbacks: z.B. hier: Das Smartphone liest vor
		
		info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED; // ... nach NOTIFICATIONS Ausschau halten, ...
		
		info.notificationTimeout = 100;	// ... und zwar jede 100 ms ...
		info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK; // und als Feedback alle Typen ermöglichen.
		setServiceInfo(info);
	}

	@Override
	public void onInterrupt() // Dürfte eigentlich nie angezeigt werden, ist nur pro forma
	{
		System.out.println("onInterrupt");
		Toast.makeText(getApplicationContext(), "onInterr", Toast.LENGTH_SHORT).show();
	}

	
	
	
	
}
