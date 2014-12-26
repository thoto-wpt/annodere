package com.test;

import java.util.Calendar;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.*;
import android.os.*;
import android.view.*;
import android.widget.TextView;
import android.content.Intent;

public class MainActivity extends Activity
{
	protected static TextView title;
	protected static TextView texti;
	protected static TextView time;
	protected static TextView info;
	
	protected Handler handler;	
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        System.out.println("onCreate");
        
        this.handler = new Handler();
    	this.handler.postDelayed(runnable,500);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		System.out.println("onCreateOptionsMenu");
		
		title = (TextView) findViewById(R.id.title);
		texti = (TextView) findViewById(R.id.texti);
		time = (TextView) findViewById(R.id.time);
		info = (TextView) findViewById(R.id.info);
		
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public MenuInflater getMenuInflater()
	{
		System.out.println("getMenuInflater");
		
		return super.getMenuInflater();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		System.out.println("onOptionsItemSelected");
		switch(item.getItemId()){
			case R.id.exit:
				System.out.println("Exit");
				return true;
			case R.id.einstellungen:
				System.out.println("Einstellungen");
				Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
				startActivityForResult(intent, 0);
			default:
				System.out.println("nix");
		return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo)
	{
		super.onCreateContextMenu(menu, v, menuInfo);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		System.out.println("onCreateContextMenu");
	}
	
    ////////////////////message////////////////////////////
	public void message(View view){
		
		Notify("blabla","mrx");
		System.out.println("Message");
		
	}
	
    ////////////////////message=>Notify////////////////////////////
	@SuppressWarnings("deprecation") // ab API 6
	private void Notify(String notificationTitle, String notificationMessage){
		
		System.out.println("inNotify");
		
		NotificationManager noti = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		//@SuppressWarnings("deprecation") // Sollten Probleme auftreten bitte auskommentieren
		Notification notif = new Notification(R.drawable.ic_launcher,"New Nachricht", System.currentTimeMillis());
		
		Intent ni = new Intent(this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, ni, 0);
		
		notif.setLatestEventInfo(MainActivity.this, notificationTitle, notificationMessage, pi);
		noti.notify(9999, notif);
		
		
	}
	
    ////////////////////message2////////////////////////////
	@SuppressLint("NewApi") // erst ab API 16
	public void message2(View view){
		Notification.Builder notific = new Notification.Builder(this);
		notific.setSmallIcon(R.drawable.ic_launcher);
		notific.setContentTitle("Titel");
		notific.setContentText("Hallo, wie gehts Dir?");
		
		NotificationManager nm = (NotificationManager) 
				  getSystemService(NOTIFICATION_SERVICE); 
		
		nm.notify(100,notific.build());
	}
	
	
	////////////////////receiveNoti////////////////////////////
	public static void receiveNoti(Map<Integer, String> noti) {
		title.setText(noti.get(16908310));
		texti.setText(noti.get(16908358));
		info.setText(noti.get(16909082));
		String mydate = java.text.DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime());
		time.setText(mydate);
	}
	
	
////////////////////Runnable////////////////////////////
	private final Runnable runnable = new Runnable() {
	    public void run()   {	    	
	    		handler.postDelayed(runnable,500);
	    }
	};
	
	
}
