package com.example.notification_grabber;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.RemoteViews;

/*Nur für API 18 (Jellybean)*/
@TargetApi(18)
public class Mr_grabsJB extends NotificationListenerService {
	Notification_data noti;

	/*Holt aus der StatusBarNotification die Notification und extrahiert deren Infos mit Reflections(?). Diese Infos
	 werden an einen Intent gehangen und verschickt.
	 http://stackoverflow.com/questions/9292032/extract-notification-text-from-parcelable-contentview-or-contentintent
	 Hier ist der Link, wo ich den try-catch-Block her habe. Ist TomTasche's Antwort, die darunter liefert dazu noch eine
	 verständliche Erklärung.*/
	@Override
	public void onNotificationPosted(StatusBarNotification sbn) {
		Notification mNotification = sbn.getNotification();
		if (mNotification != null) {
			System.out.println("Notification posted");
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
					}
					noti = new Notification_data(text.get(16908310),text.get(16908358), null);
//					Intent intent = new Intent(
//							MainActivity.INTENT_ACTION_NOTIFICATION);
//					intent.putExtra("title", text.get(16908310));
//					intent.putExtra("text", text.get(16908358));
					// System.out.println("title is: " + text.get(16908310));
					// System.out.println("info is: " + text.get(16909082));
					// System.out.println("text is: " + text.get(16908358));
					System.out.println("Title in grabber API18: "
							+ text.get(16908310));
					System.out.println("Text in grabber API18: "
							+ text.get(16908358));
//					sendBroadcast(intent);
					MainActivity.receiveNoti(noti);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onNotificationRemoved(StatusBarNotification sbn) {
		// TODO Auto-generated method stub

	}

}
