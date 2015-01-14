package com.example.message_streamer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.app.Notification;
import android.content.Intent;
import android.os.Bundle;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;

public class Notification_worker extends NotificationListenerService {

	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			if (sbn.getPackageName().equals("com.android.mms")
					|| sbn.getPackageName().equals("com.android.phone")) {
				Notification mNotification = sbn.getNotification();
				if (mNotification != null) {
					Bundle extras = mNotification.extras;
					String notificationTitle = sbn.getPackageName() + ": "
							+ extras.getString(Notification.EXTRA_TITLE);
					String notificationText = extras
							.getString(Notification.EXTRA_TEXT);
					Intent intent = new Intent(
							MainActivity.INTENT_ACTION_NOTIFICATION);
					intent.putExtra("title", notificationTitle);
					intent.putExtra("text", notificationText);
					sendBroadcast(intent);
				}
			}
		} else if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			if (sbn.getPackageName().equals("com.android.mms")
					|| sbn.getPackageName().equals("com.android.phone")) {
				Notification mNotification = sbn.getNotification();
				if (mNotification != null) {
					RemoteViews views = mNotification.contentView;
					Class secretClass = views.getClass();
					try {
						Map<Integer, String> text = new HashMap<Integer, String>();
						Field outerFields[] = secretClass.getDeclaredFields();
						for (int i = 0; i < outerFields.length; i++) {
							if (!outerFields[i].getName().equals("mActions"))
								continue;
							outerFields[i].setAccessible(true);
							@SuppressWarnings("unchecked")
							ArrayList<Object> actions = (ArrayList<Object>) outerFields[i]
									.get(views);
							for (Object action : actions) {
								Field innerFields[] = action.getClass()
										.getDeclaredFields();
								Object value = null;
								Integer type = null;
								Integer viewId = null;
								for (Field field : innerFields) {
									field.setAccessible(true);
									if (field.getName().equals("value")) {
										value = field.get(action);
									} else if (field.getName().equals("type")) {
										type = field.getInt(action);
									} else if (field.getName().equals("viewId")) {
										viewId = field.getInt(action);
									}
								}
								if (type == 9 || type == 10) {
									text.put(viewId, value.toString());
								}
								Intent intent = new Intent(
										MainActivity.INTENT_ACTION_NOTIFICATION);
								intent.putExtra("title", text.get(16908310));
								intent.putExtra("text", text.get(16908358));
								sendBroadcast(intent);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}

	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		// TODO Auto-generated method stub

	}

}
