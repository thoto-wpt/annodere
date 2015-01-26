package com.example.message_streamer;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class Notification_worker extends NotificationListenerService {

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			if (sbn.getPackageName().equals("com.android.mms")
					|| sbn.getPackageName().equals("com.android.phone")) {
				Notification mNotification = sbn.getNotification();
				if (mNotification != null && mNotification.tickerText!=null) {
					String tickertext = mNotification.tickerText.toString();
					Intent intent = new Intent(
							MainActivity.INTENT_ACTION_NOTIFY);
					intent.putExtra("msg", tickertext);
					sendBroadcast(intent);
					Log.d("MS NW","Intent sent. Message: "+tickertext);
				}
			}
		}
	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		// TODO Auto-generated method stub

	}

}
