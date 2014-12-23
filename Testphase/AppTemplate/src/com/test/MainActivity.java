package com.test;

import android.app.*;
import android.os.*;
import android.view.*;
import android.content.Intent;

public class MainActivity extends Activity
{
	
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
	
	public void message(View view){
		
		Notify("blabla","mrx");
		System.out.println("Message");
		
	}
	
	@SuppressWarnings("deprecation")
	private void Notify(String notificationTitle, String notificationMessage){
		
		System.out.println("inNotify");
		
		NotificationManager noti = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		@SuppressWarnings("deprecation")
		Notification notif = new Notification(R.drawable.ic_launcher,"New Nachricht", System.currentTimeMillis());
		
		Intent ni = new Intent(this, MainActivity.class);
		PendingIntent pi = PendingIntent.getActivity(this, 0, ni, 0);
		
		notif.setLatestEventInfo(MainActivity.this, notificationTitle, notificationMessage, pi);
		noti.notify(9999, notif);
		
		
	}
	
	private final Runnable runnable = new Runnable() {
	    public void run()   {	    	
	    		handler.postDelayed(runnable,500);
	    }
	};
	
	
}
