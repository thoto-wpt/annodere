package com.example.notification_grabber;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
//import android.graphics.Bitmap;
//import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
//import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity extends Activity {

//	protected MyReceiver mReceiver = new MyReceiver();
	public static final String INTENT_ACTION_NOTIFICATION = "com.example.notification_grabber";
	private static int noti_counter = 0;

	protected static TextView title1;
	protected static TextView title2;
	protected static TextView title3;
	protected static TextView text1;
	protected static TextView text2;
	protected static TextView text3;

	// protected ImageView icon1, icon2, icon3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		/*Checkt die Version und benutzt dann die dementsprechende Klasse zum Extrahieren der Notifications*/
		// API 19 and above
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
			Mr_grabsKK grabber = new Mr_grabsKK();
			System.out.println("____________________Mr_grabsKK initialised");
		}
		// API 18
		else if (android.os.Build.VERSION.SDK_INT == android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			Mr_grabsJB grabber = new Mr_grabsJB();
			System.out.println("____________________Mr_grabsJB initialised");
		}
		// API 17 and below
		else {
			Mr_grabs grabber = new Mr_grabs();
			System.out.println("____________________Mr_grabs initialised");
		}

		title1 = (TextView) findViewById(R.id.noti_title1);
		title2 = (TextView) findViewById(R.id.noti_title2);
		title3 = (TextView) findViewById(R.id.noti_title3);
		text1 = (TextView) findViewById(R.id.noti_text1);
		text2 = (TextView) findViewById(R.id.noti_text2);
		text3 = (TextView) findViewById(R.id.noti_text3);
		// icon1 = (ImageView) findViewById(R.id.noti_icon1);
		// icon2 = (ImageView) findViewById(R.id.noti_icon2);
		// icon3 = (ImageView) findViewById(R.id.noti_icon3);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/*Methode um schnell zu den Optionen zu kommen und den NotificationListener zu erlauben.*/
	public void unlock(View view) {
		Intent intent = new Intent(
				"android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
//		if (mReceiver == null)
//			mReceiver = new MyReceiver();
//		registerReceiver(mReceiver,
//				new IntentFilter(INTENT_ACTION_NOTIFICATION));
	}

	@Override
	protected void onPause() {
		super.onPause();
//		unregisterReceiver(mReceiver);
	}
	
	public static void receiveNoti(Notification_data noti) {
		switch (noti_counter % 3) {
		case 0:
			title1.setText(noti.getTitle());
			text1.setText(noti.getText());
			// if(notificationIcon!=null) {
			// icon1.setImageBitmap(notificationIcon);
			// }
			break;
		case 1:
			title2.setText(noti.getTitle());
			text2.setText(noti.getText());
			// if(notificationIcon!=null) {
			// icon2.setImageBitmap(notificationIcon);
			// }
			break;
		case 2:
			title3.setText(noti.getTitle());
			text3.setText(noti.getText());
			// if(notificationIcon!=null) {
			// icon3.setImageBitmap(notificationIcon);
			// }
			break;

		default:
			break;
		}
		noti_counter++;
	}
	
	

	/*Receiver zum Erhalten von Broadcasts, die die jeweilige Mr_grabs Klasse schickt. In dem Intent des Broadcasts
	 sind die Strings mit den relevanten Informationen enthalten und mit den Keys "title" und "text" abrufbar.*/
//	public class MyReceiver extends BroadcastReceiver {
//
//		@Override
//		public void onReceive(Context context, Intent intent) {
//			System.out.println("Intent received");
//			String notificationTitle = null;
//			String notificationText = null;
//			// Bitmap notificationIcon = null;
//			if (intent != null) {
//				notificationTitle = intent.getStringExtra("title");
//				notificationText = intent.getStringExtra("text");
//				System.out.println("Title in Main: " + notificationTitle);
//				System.out.println("Text in Main: " + notificationText);
//				// notificationIcon =
//				// bytearray_to_bitmap(intent.getByteArrayExtra("icon"));
//				switch (noti_counter % 3) {
//				case 0:
//					title1.setText(notificationTitle);
//					text1.setText(notificationText);
//					// if(notificationIcon!=null) {
//					// icon1.setImageBitmap(notificationIcon);
//					// }
//					break;
//				case 1:
//					title2.setText(notificationTitle);
//					text2.setText(notificationText);
//					// if(notificationIcon!=null) {
//					// icon2.setImageBitmap(notificationIcon);
//					// }
//					break;
//				case 2:
//					title3.setText(notificationTitle);
//					text3.setText(notificationText);
//					// if(notificationIcon!=null) {
//					// icon3.setImageBitmap(notificationIcon);
//					// }
//					break;
//
//				default:
//					break;
//				}
//				noti_counter++;
//			}
//
//		}

		// private Bitmap bytearray_to_bitmap(byte[] byteArray) {
		// // byte[] byteArray = getIntent().getByteArrayExtra("image");
		// Bitmap bmp = BitmapFactory.decodeByteArray(byteArray, 0,
		// byteArray.length);
		// return bmp;
		// }
//	}
}
