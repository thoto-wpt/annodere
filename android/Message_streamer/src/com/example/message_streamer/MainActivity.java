package com.example.message_streamer;

//import org.json.JSONException;
//import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity {
	public static final String INTENT_ACTION_NOTIFICATION = 
			"com.example.Message_streamer.notify";

	protected Connect_receiver cReceiver;
	public static final String INTENT_ACTION_CONNECT=
			"com.example.Message_streamer.connect";

	private static connection_worker cw=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cw=new connection_worker(this);
		setContentView(R.layout.activity_main);
		register_receivers();
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

	private void register_receivers(){
		Log.d("MS MA","Register recvs");
		cReceiver = new Connect_receiver();
		IntentFilter icfilter=new IntentFilter(INTENT_ACTION_CONNECT);
		registerReceiver(cReceiver, icfilter);
	}

	@Override
	protected void onResume() {
		super.onResume();
		register_receivers();
	}

	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(cReceiver);
		cReceiver=null;
	}

	public void de_activate(View view) {
		if (android.os.Build.VERSION.SDK_INT 
				> android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			Intent intent = new Intent(
					"android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
			startActivity(intent);
		}
		else{
			Intent intent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
			startActivity(intent);
		}
	}

	public void options(View view) {
		Intent intent = new Intent(this, OptionsActivity.class);
		startActivity(intent);
	}

	public void connect(View view){
		if(cw.connect_props_set()){
			cw.connect();
		}
		else{
			Intent intent = new Intent(this, OptionsActivity.class);
			startActivity(intent);
		}
	}

	public class Connect_receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("MS MA", "Connect receiver called.");
			if(intent.hasExtra("ip")&&intent.hasExtra("token")){
				Log.d("MS-MA","Connect to "+intent.getExtras().getString("ip")+
						" with token "+intent.getExtras().getString("token"));
				cw.connect(intent.getExtras().getString("ip"),
						intent.getExtras().getString("token"));
			}
		}
	}

	
	public static void send(String bla){
		Log.d("Main", bla);
		cw.send_notification("NOTIFY: "+ bla);
	}
}
