package com.example.message_streamer;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
//import android.app.Notification;
//import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class Notification_worker_preJB extends AccessibilityService {

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		if (event.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			if (event.getPackageName().equals("com.android.mms")
					|| event.getPackageName().equals("com.android.phone")) {
				/*Notification notification = (Notification) event.getParcelableData();
				
				String nachricht;
				Log.d("tos", "toString");
				nachricht = notification.tickerText.toString();
				Log.d("text", nachricht);
				Intent intent = new Intent(MainActivity.INTENT_ACTION_NOTIFICATION);
				intent.putExtra("msg", nachricht);
				sendBroadcast(intent);
*/

final String packagename = String.valueOf(event.getPackageName());  
				final String text = String.valueOf(event.getText());
				/*
				String bla = String.valueOf(event.getParcelableData());
				String tt = notification.tickerText.toString();
				Log.d("tt", tt);
				Log.d("bla", bla);
				*/
				Log.d("String P", packagename);
				Log.d("tickertext", text);
				MainActivity.send(text);
				/*
				 * // Unwichtige, aber interessante Details: Log.d("INC",
				 * "Incoming notification!"); Log.d("PACK", "App: " +
				 * event.getPackageName()); Log.d("TEXT", "Ticker: " +
				 * notification.tickerText); Log.d("INTENT", "Text: " +
				 * notification.contentIntent); Log.d("TIME", "Uhr: " +
				 * event.getEventTime()); Log.d("TYP", "Typ: " +
				 * event.getEventType()); Log.d("ITXT", "Text: " +
				 * event.getText());
				 */

				// List<CharSequence> notificationList = event.getText();
				// System.out.println(notificationList); // M�glichkeiten �ber
				// M�glichkeiten ...

				//System.out.println(notification); // So sieht die normale, nicht
													// geparste Version aus.

				// Parser:
				// http://stackoverflow.com/questions/9292032/extract-notification-text-from-parcelable-contentview-or-contentintent
				// unterster Beitrag

				/*RemoteViews views = notification.contentView;
				Class secretClass = views.getClass();

				try {
					Map<Integer, String> text = new HashMap<Integer, String>();

					Field outerField = secretClass.getDeclaredField("mActions");
					outerField.setAccessible(true);
					ArrayList<Object> actions = (ArrayList<Object>) outerField
							.get(views);

					for (Object action : actions) {
						Field innerFields[] = action.getClass()
								.getDeclaredFields();
						Field innerFieldsSuper[] = action.getClass()
								.getSuperclass().getDeclaredFields();

						Object value = null;
						Integer type = null;
						Integer viewId = null;
						for (Field field : innerFields) {
							field.setAccessible(true);
							if (field.getName().equals("value")) {
								value = field.get(action);
							} else if (field.getName().equals("type")) {
								type = field.getInt(action);
							}
						}
						for (Field field : innerFieldsSuper) {
							field.setAccessible(true);
							if (field.getName().equals("viewId")) {
								viewId = field.getInt(action);
							}
						}

						if (value != null && type != null && viewId != null
								&& (type == 9 || type == 10)) {
							text.put(viewId, value.toString());
						}
					}

					System.out.println("title is: " + text.get(16908310));
					System.out.println("info is: " + text.get(16909082));
					System.out.println("text is: " + text.get(16908358));
					Intent intent = new Intent(
							MainActivity.INTENT_ACTION_NOTIFICATION);
					intent.putExtra("title", text.get(16908310));
					intent.putExtra("text", text.get(16908358));
					sendBroadcast(intent);
*/
				}

				//catch (Exception e) {
				//	e.printStackTrace();
				//}

				// Das oben zeigt an, was ich bisher auslesen kann
				// Die Daten sind aber noch verschl�sselt in den Paketen =>
				// Pakete
				// m�ssen geparst werden

				/*
				 * In Arbeit: Suche nach dem passenden Parser f�r SMS und
				 * weitere... Intent result = new Intent("com.test",
				 * Uri.parse("content://result_uri")); Uri uri =
				 * Uri.parse("content://com.test"); Cursor c =
				 * getContentResolver().query(uri, null, null, null, null);
				 * 
				 * SMSData sms = new SMSData();
				 * sms.setBody(c.getString(c.getColumnIndexOrThrow
				 * ("body")).toString());
				 * sms.setNumber(c.getString(c.getColumnIndexOrThrow
				 * ("adress")).toString()); c.close();
				 * 
				 * Log.d("SMS", "Text: " + sms.getBody() + sms.getNumber());
				 */

			}
		
	}

	@Override
	protected void onServiceConnected() {
		Log.d("MS NW","Service connected");
		AccessibilityServiceInfo info = new AccessibilityServiceInfo();
		// look for notification events
		info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
		info.notificationTimeout = 100;
		info.feedbackType = AccessibilityEvent.TYPES_ALL_MASK;
		setServiceInfo(info);
	}

	@Override
	public void onInterrupt(){
		Log.d("MS NW","onInterrupt");
		Toast.makeText(getApplicationContext(), "onInterr", Toast.LENGTH_SHORT)
				.show();
	}

}
