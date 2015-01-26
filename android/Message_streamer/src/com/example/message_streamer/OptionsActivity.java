package com.example.message_streamer;

import android.annotation.SuppressLint;
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
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class OptionsActivity extends Activity {

	private Spinner zc_sp;
	private ArrayAdapter<zc_spinner_item> zc_adapter;
	private NsdManager nsd_man;
	private EditText edip;
	DiscoveryListener dnssd_listener;
	boolean zc_running=false;

	/**
	 * Zeroconf host selection item
	 * @author thoto
	 *
	 */
	private class zc_spinner_item{
		String host;
		String ip;
		int port;
		public zc_spinner_item(String name, String ip, int port){
			this.ip=ip;
			this.host=name+"("+ip+")";
			this.port=port;
		}

		public String getUrl(){
			return ip+":"+port;
		}

		public String toString(){
			return "Name: "+host;
		}
	}

	/**
	 * Zerconf discovery listener for this service
	 * @author thoto
	 *
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private class disco_listener implements NsdManager.DiscoveryListener{
		@Override
		public void onDiscoveryStarted(String serviceType) {
			Log.i("NSD","disco start");
		}

		@Override
		public void onDiscoveryStopped(String serviceType) {
			Log.i("NSD","disco stopped");
		}

		@Override
		public void onServiceFound(NsdServiceInfo serviceInfo) {
			Log.i("NSD","Service disco successful.");
			resolve_annodere_service(serviceInfo);
		}

		@Override
		public void onServiceLost(NsdServiceInfo serviceInfo) {
			//FIXME
		}

		@Override
		public void onStartDiscoveryFailed(String serviceType, int err) {
			Log.e("NSD","disco start failed: "+err);
		}

		@Override
		public void onStopDiscoveryFailed(String serviceType, int err) {
			Log.e("NSD","disco stop failed: "+err);
		}
	}

	/**
	 * Zerconf discovery resolver for discovered service
	 * @author thoto
	 *
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private class disco_res_listener implements NsdManager.ResolveListener {
		@Override
		public void onResolveFailed(NsdServiceInfo si, int err) {
			Log.e("NSR", "Resolve of "+si+"failed with err: "+err);
			switch (err) {
			case NsdManager.FAILURE_ALREADY_ACTIVE:
				Log.e("NSR", "ALREADY_ACTIVE ... retry"); //try again
				resolve_annodere_service(si); break;
			case NsdManager.FAILURE_INTERNAL_ERROR: //try again
				Log.e("NSR", "INTERNAL_ERROR EEH");
				resolve_annodere_service(si); break;
			case NsdManager.FAILURE_MAX_LIMIT:
				Log.e("NSR", "MAX_LIMIT EEH"); break;
			}
		}

		@Override
		public void onServiceResolved(NsdServiceInfo si) {
			Log.i("NSR", "Service Resolved: "+si);
			// validate and add to list
			if(si.getHost()!=null && si.getHost().getHostName()!=null
					&& si.getHost().getHostAddress()!=null
					&& si.getPort()!=0);
				push_zc_host(new zc_spinner_item(si.getHost().getHostName(),
						si.getHost().getHostAddress(),si.getPort()));
		}
	}

	/**
	 * Zeroconf discovery setup including handler
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void init_disco(){
		nsd_man=(NsdManager) this.getSystemService(Context.NSD_SERVICE);
		dnssd_listener = new disco_listener();
		start_disco();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void start_disco(){
		if(zc_running) return;
		nsd_man.discoverServices("_annodere._tcp", NsdManager.PROTOCOL_DNS_SD,
				dnssd_listener);
		zc_running=true;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void stop_disco(){
		if(!zc_running) return;
		nsd_man.stopServiceDiscovery(dnssd_listener);
		zc_running=false;
	}

	/**
	 * resolves service in network and puts it into spinner
	 * @param si service to look for
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void resolve_annodere_service(NsdServiceInfo si){
		NsdManager.ResolveListener rl = new disco_res_listener();
		nsd_man.resolveService(si,rl);
	}

	/**
	 * Push registration to main form.
	 * @param v
	 */
	protected void send_intent_ip(){
		String text=edip.getText().toString();

		//find appendix of entered url and check if it's a port FIXME
		int deli=text.lastIndexOf(':');
		try{
			if(deli<0 || Integer.parseInt(text.substring(deli+1))<=0)
				text=text+":10080"; // there is no port: append it!
		}catch(Exception e){
			text=text+":10080"; // int parsing failed ... append!
		}

		Intent i = new Intent(MainActivity.INTENT_ACTION_CONNECT);
		i.putExtra("ip", text);
		i.putExtra("token", "1234");
		sendBroadcast(i);
	}

	/**
	 * add new item to zeroconf spinner
	 * @param i
	 */
	protected void push_zc_host(final zc_spinner_item i){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				zc_adapter.add(i);
			}
		});
	}

	/**
	 * sets edit text where to enter ip and connects to it TODO
	 * @param url
	 */
	protected void set_target(final String url){
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				edip.setText(url);
			}
		});
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);

		// enable entry of IP address
		edip=(EditText) findViewById(R.id.editTextIP);
		edip.setImeActionLabel(getString(R.string.connect),
				KeyEvent.KEYCODE_ENTER);

		// connect on enter
		edip.setOnEditorActionListener(new OnEditorActionListener(){
			@Override
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if(actionId == EditorInfo.IME_ACTION_DONE
						|| actionId==KeyEvent.KEYCODE_ENTER
						|| ((event.getKeyCode() == KeyEvent.KEYCODE_ENTER) &&
							(event.getAction() == KeyEvent.ACTION_DOWN ))){
					Log.d("MS-OA","FOO!");
					send_intent_ip();
					return true;
				}
				return false;
			}
		});

		// set button to invoke connect call
		findViewById(R.id.buttonConnect).setOnClickListener(
			new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					send_intent_ip();
				}
			});

		// zerconf
		zc_sp=(Spinner) findViewById(R.id.spinnerZC);
		if(android.os.Build.VERSION.SDK_INT >= 
				android.os.Build.VERSION_CODES.JELLY_BEAN){
			// setup zeroconf UI stuff
			zc_adapter=new ArrayAdapter<zc_spinner_item>(
					this, android.R.layout.simple_spinner_item);

			zc_sp.setAdapter(zc_adapter);
			zc_sp.setOnItemSelectedListener(new OnItemSelectedListener(){
				@Override
				public void onItemSelected(AdapterView<?> parent, View v,
						int p, long id) {
					set_target(((zc_spinner_item)parent.getItemAtPosition(p))
							.getUrl());
				}

				@Override
				public void onNothingSelected(AdapterView<?> arg0){}
			});
			init_disco();
		}else zc_sp.setVisibility(View.INVISIBLE); // not supported OS version
	}

	@Override
	protected void onResume() {
		super.onResume();
		if(android.os.Build.VERSION.SDK_INT >=
				android.os.Build.VERSION_CODES.JELLY_BEAN){
			start_disco();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if(android.os.Build.VERSION.SDK_INT >=
				android.os.Build.VERSION_CODES.JELLY_BEAN){
			stop_disco();
		}
	}

	public void onBackPressed(){
		send_intent_ip();
		super.onBackPressed();
	}
}
