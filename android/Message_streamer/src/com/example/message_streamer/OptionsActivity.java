package com.example.message_streamer;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.nsd.NsdManager.DiscoveryListener;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class OptionsActivity extends Activity {

	Spinner zc_sp;
	ArrayAdapter<zc_spinner_item> zc_adapter;

	private class zc_spinner_item{
		String host;
		String ip;
		public zc_spinner_item(String name, String ip){
			this.ip=ip;
			this.host=name+" "+ip;
		}

		public String getValue(){
			return ip;
		}

		public String toString(){
			return "Name: "+host;
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void init_disco(){
		NsdManager nsd_man=
				(NsdManager) this.getSystemService(Context.NSD_SERVICE);

		DiscoveryListener dnssd_listener = 
				new NsdManager.DiscoveryListener(){

			@Override
			public void onDiscoveryStarted(String serviceType) {
				// TODO Auto-generated method stub
				//zc_adapter.add(new zc_spinner_item("DISCO START",1));
				Log.d("NSD","disco start");
			}

			@Override
			public void onDiscoveryStopped(String serviceType) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onServiceFound(NsdServiceInfo serviceInfo) {
				Log.d("NSD","Service disco successful.");
				if(serviceInfo.getServiceType().equals("_annodere._tcp")){
					serviceInfo.getHost().getHostAddress();
					zc_adapter.add(new zc_spinner_item(
							serviceInfo.getHost().getHostName(),
							serviceInfo.getHost().getHostAddress()));
				}
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onServiceLost(NsdServiceInfo serviceInfo) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStartDiscoveryFailed(String serviceType, 
					int errorCode) {
				// TODO Auto-generated method stub
				Log.d("NSD","disco start failed");
				
			}

			@Override
			public void onStopDiscoveryFailed(String serviceType, 
					int errorCode) {
				// TODO Auto-generated method stub
				
			}
			
		};

		nsd_man.discoverServices("_annodere._tcp",
				NsdManager.PROTOCOL_DNS_SD,dnssd_listener);
		
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
		EditText edip=(EditText) findViewById(R.id.editTextIP);
		edip.setImeActionLabel(getString(R.string.connect),
				KeyEvent.KEYCODE_ENTER);
		edip.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE
						|| actionId==KeyEvent.KEYCODE_ENTER
						|| ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) &&
							(event.getAction() == KeyEvent.ACTION_DOWN ))){
					Log.d("MS-OA","FOO!");
					Intent i = new Intent(MainActivity.INTENT_ACTION_CONNECT);
					i.putExtra("ip", v.getText().toString());
					i.putExtra("token", "1234");
					sendBroadcast(i);
					return true;
				}
				return false;
			}
		});

		zc_sp=(Spinner) findViewById(R.id.spinner1);

		zc_adapter=new ArrayAdapter<zc_spinner_item>(
				this, android.R.layout.simple_spinner_item);
		
		zc_sp.setAdapter(zc_adapter);
		//zc_sp.setOnItemSelectedListener(new zc_select_listener());
		if(android.os.Build.VERSION.SDK_INT >= 
				android.os.Build.VERSION_CODES.JELLY_BEAN){
			init_disco();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}
}
