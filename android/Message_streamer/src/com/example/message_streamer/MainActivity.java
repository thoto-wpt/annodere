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
import android.widget.TextView;

public class MainActivity extends Activity {
	protected Noti_receiver nReceiver=null;
	public static final String INTENT_ACTION_NOTIFY =
			"com.example.Message_streamer.notify";

	protected Connect_receiver cReceiver=null;
	public static final String INTENT_ACTION_CONNECT=
			"com.example.Message_streamer.connect";

	protected State_receiver sReceiver=null;
	public static final String INTENT_ACTION_STATE=
			"com.example.Message_streamer.state";

	private connection_worker cw=null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		cw=new connection_worker(this);
		setContentView(R.layout.activity_main);

		register_receivers();

		if (android.os.Build.VERSION.SDK_INT
				<= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			TextView act_button=(TextView) findViewById(R.id.de_activate);
			act_button.setText(getString(R.string.activate)+"\n"
					+getString(Notification_worker_preJBROB.active?
							R.string.state_active:R.string.state_inactive));
		}
	}

	protected void onDestroy(){
		super.onDestroy();

		unregister_receivers();
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

		if(nReceiver!=null) Log.e("MS MA","Receiver is not null!");
		nReceiver = new Noti_receiver();
		IntentFilter infilter=new IntentFilter(INTENT_ACTION_NOTIFY);
		registerReceiver(nReceiver, infilter);
		if(cReceiver==null){
			cReceiver = new Connect_receiver();
			IntentFilter icfilter=new IntentFilter(INTENT_ACTION_CONNECT);
			registerReceiver(cReceiver, icfilter);
		}else Log.d("MS MA","Receiver is not null!");
		if(sReceiver==null){
			sReceiver = new State_receiver();
			IntentFilter isfilter=new IntentFilter(INTENT_ACTION_STATE);
			registerReceiver(sReceiver, isfilter);
		}else Log.d("MS MA","Receiver is not null!");
	}

	private void unregister_receivers(){
		Log.d("MS MA","UNregister Receivers");

		if(nReceiver!=null){
			unregisterReceiver(nReceiver);
			nReceiver=null;
		}else Log.e("MS MA","Notify-Receiver is already null!");
		if(cReceiver!=null){
			unregisterReceiver(cReceiver);
			cReceiver=null;
		}else Log.d("MS MA","Receiver is already null!");
		if(sReceiver!=null){
			unregisterReceiver(sReceiver);
			sReceiver=null;
		}else Log.d("MS MA","sReceiver is already null!");
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	public void de_activate(View view) {
		if (android.os.Build.VERSION.SDK_INT 
				> android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
			Intent intent = new Intent(
					"android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
			startActivity(intent);
		}
		else{
			Intent intent = new Intent(
					android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
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

	public class Noti_receiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("MS MA", "Notification receiver called.");
			if (intent != null) {
				Bundle extras = intent.getExtras();
				String msg=extras.getString("msg");
				if(msg==null) return;
				/*JSONObject json = new JSONObject();
				try {
					json.put("title", notificationTitle);
					json.put("text", notificationText);
				}catch (JSONException e) {
					e.printStackTrace();
				}json.toString();*/
				if(cw!=null)
					cw.send_notification(msg);
				else Log.d("Message_streamer",
						"NULL connection_worker. Will ignore notification.");
			}

		}
	}

	public class State_receiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Log.d("MS MA", "State receiver called.");
			if (intent != null) {
				Bundle extras = intent.getExtras();
				String var=extras.getString("var");
				String val=extras.getString("val");
				if(var==null||val==null) return;
				if(var.equals("aeenabled")){
					// Accessibility Event enabled or disabled
					TextView act_button=(TextView) 
							findViewById(R.id.de_activate);

					if(val.equals("true"))
						act_button.setText(getString(R.string.activate)+"\n"
								+getString(R.string.state_active));
					else if(val.equals("false"))
						act_button.setText(getString(R.string.activate)+"\n"
								+getString(R.string.state_inactive));
					else Log.e("MS MA","Invalid state of accessibility event!");
				}else if(var.equals("connected")){
					TextView con_view=(TextView)
							findViewById(R.id.textViewConnected);

					if(val.equals("true")) con_view.setText(R.string.connected);
					else if(val.equals("false"))
						con_view.setText(R.string.not_connected);
					else Log.e("MS MA","Invalid state of connection recvd!");
				}
			}
		}
	}
}
