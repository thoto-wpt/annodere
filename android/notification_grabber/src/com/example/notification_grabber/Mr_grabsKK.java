package com.example.notification_grabber;

//import java.io.ByteArrayOutputStream;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
//import android.graphics.Bitmap;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

/*Nur für API 19 (Kitkat)*/
@TargetApi(19)
public class Mr_grabsKK extends NotificationListenerService {
	Notification_data noti;

	/*Extrahiert aus der StatusBarNotification die Notification. In deren Subklasse Bundle liegen alle Informationen, 
	 die man mit den entsprechenden Keys "EXTRA_TITLE", "EXTRA_TEXT", usw. holen kann. Diese werden dann an einen
	 Intent gehangen und verschickt.*/
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		Notification mNotification = sbn.getNotification();
		if (mNotification != null) {
			System.out.println("Notification posted");
			Bundle extras = mNotification.extras;
			String notificationTitle = extras
					.getString(Notification.EXTRA_TITLE);
			// Bitmap notificationIcon = ((Bitmap) extras
			// .getParcelable(Notification.EXTRA_SMALL_ICON));
			String notificationText = extras.getString(Notification.EXTRA_TEXT);
			noti = new Notification_data(notificationTitle,notificationText, null);
//			Intent intent = new Intent(MainActivity.INTENT_ACTION_NOTIFICATION);
//			intent.putExtra("title", notificationTitle);
//			intent.putExtra("text", notificationText);
//			System.out.println("Title in grabber API>18: " + notificationTitle);
//			System.out.println("Text in grabber API>18: " + notificationText);
			// intent.putExtra("icon", bitmap_to_bytearray(notificationIcon));
//			sendBroadcast(intent);
			MainActivity.receiveNoti(noti);
		}
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification arg0) {
		// TODO Auto-generated method stub

	}

	// private byte[] bitmap_to_bytearray(Bitmap bmp) {
	// ByteArrayOutputStream stream = new ByteArrayOutputStream();
	// bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
	// byte[] byteArray = stream.toByteArray();
	// return byteArray;
	// }
}
