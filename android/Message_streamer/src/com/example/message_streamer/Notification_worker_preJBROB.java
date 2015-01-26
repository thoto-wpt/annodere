package com.example.message_streamer;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Intent;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

public class Notification_worker_preJBROB extends AccessibilityService {

	@Override
	public void onAccessibilityEvent(AccessibilityEvent event) {
		String tickertext;
		Log.d("MS NW","pre JBROB: got AE");

		if (event.getEventType() ==
				AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
			if (event.getPackageName().equals("com.android.mms")
					|| event.getPackageName().equals("com.android.phone")) {
				Notification notification =
						(Notification) event.getParcelableData();

				tickertext = notification.tickerText.toString();

				Intent intent = new Intent(
						MainActivity.INTENT_ACTION_NOTIFY);
				intent.putExtra("msg", tickertext);
				sendBroadcast(intent);

				Log.d("MS NW","pre JBROB: Intent sent. Message: "+tickertext);
			}
		}
	}

	@Override
	protected void onServiceConnected() {
		Log.d("MS NW","pre JBROB: Service connected");
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
