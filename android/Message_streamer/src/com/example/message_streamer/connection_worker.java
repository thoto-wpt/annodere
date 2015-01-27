package com.example.message_streamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

/**
 * result structure emitted by JSON RPC calls
 * @author thoto
 *
 */
class json_result{
	public int val_type=0; //! 0: Err 1: Str, 2: Num, 3: Bool, 4: Null
	public boolean err=true;
	public String str_val;
	public Integer int_val;
	public boolean bool_val;
	public boolean err_critical;
	public json_request rq;

	/**
	 * return result
	 * @param reader read value from this reader
	 * @param rq request to include reference to
	 */
	public json_result(JsonReader reader,json_request rq){
		JsonToken json_type;

		this.rq=rq;
		try {
			json_type = reader.peek();
			err=false;
			switch(json_type){
			case STRING:
				val_type=1; str_val=reader.nextString(); break;
			case NUMBER:
				val_type=2; int_val=reader.nextInt(); break;
			case BOOLEAN:
				val_type=3; bool_val=reader.nextBoolean(); break;
			case NULL:
				val_type=4; reader.skipValue(); break;
			default:
				err=true; err_critical=false; str_val="no valid return";
				val_type=0; reader.skipValue(); break;
			}
		} catch (IOException e) {
			err=true;
			err_critical=true;
			str_val="IO Exception while reading";
			e.printStackTrace();
		}
	}

	/**
	 * Constructor for error result
	 * @param b is error critical?
	 * @param jr reader to skip element
	 * @param rq request to include reference to
	 * @throws IOException
	 */
	public json_result(boolean b,JsonReader jr,json_request rq)
			throws IOException{//error case
		this.rq=rq;
		jr.skipValue();

		err=true;err_critical=b;
		str_val="some error TODO"; // TODO
	}

	/**
	 * make printable string for debugging purposes
	 */
	@Override
	public String toString(){
		if(err) return "EE"+(err_critical?"C":"E")+": "+str_val;

		switch(val_type){
		case 1:  return str_val;
		case 2:  return int_val.toString();
		case 3:  return Boolean.valueOf(bool_val).toString();
		case 4:  return "NULL";
		default: return "Err";
		}
	}
}


/**
 * JSON RPC call request structure
 * @author thoto
 *
 */
class json_request{
	public String method;
	public JSONArray params;
	private String url;
	public json_request(String url, String method, JSONArray params){
		this.url=url;
		this.method=method;
		this.params=params;
	}
	public String get_url(){
		return url;
	}
	public String get_request(){
		JSONObject jsonrq=new JSONObject();
		try {
			jsonrq.put("jsonrpc", "2.0");
			jsonrq.put("id", "null");
			jsonrq.put("method", method);
			jsonrq.put("params", params);
			return "request="+jsonrq.toString();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return "request=error";
		}
	}
}




/**
 * Worker to handle every kind of network RPC stuff
 * @author thoto
 *
 */
public class connection_worker {
	private Queue<String> not_queue;
	private Timer retry_timer;
	private String token=null;
	private String session_token=null;
	private String url=null;
	private Context c;

	public enum cw_state_t{CW_INIT,CW_UNREGISTERED,CW_REGISTERED};
	/**
	 * State machine for connection_worker
	 * @author thoto
	 *
	 */
	public class cw_state_machine{
		private cw_state_t cw_state;
		private String cw_err=null;

		cw_state_machine(){
			cw_state=cw_state_t.CW_INIT;
		}

		public void set(cw_state_t state,String err){
			cw_err=err;
			cw_state=state;
			if(state==cw_state_t.CW_REGISTERED){
				Intent intent = new Intent(
						MainActivity.INTENT_ACTION_STATE);
				intent.putExtra("var", "connected");
				intent.putExtra("val", "true");
				c.sendBroadcast(intent);
			}else{
				Intent intent = new Intent(
						MainActivity.INTENT_ACTION_STATE);
				intent.putExtra("var", "connected");
				intent.putExtra("val", "false");
				c.sendBroadcast(intent);
			}
		}

		public void set(cw_state_t state){
			set(state,null);
		}

		public cw_state_t get(){
			return cw_state;
		}

		public String get_err(){
			return cw_err;
		}
	}

	private cw_state_machine cw_state;

	/**
	 * Creates connection_worker and connects to server
	 * @param c Context to build ConnectivityManager
	 */
	public connection_worker(Context pc){
		c=pc;
		not_queue=new LinkedList<String>();
		cw_state=new cw_state_machine();

		retry_timer=new Timer();
		retry_timer.scheduleAtFixedRate(new retry_timer_task(),500,2000);
	}

	/**
	 * sets session token if state is unregistered
	 * @param tok new session token
	 */
	private synchronized void set_session_token(String tok){
		if(cw_state.get()==cw_state_t.CW_UNREGISTERED){
			this.session_token=tok;
		}
	}

	/**
	 * Connects to server (desktop application)
	 * @param ip IP address to connect to
	 * @param token Authentication token to provide
	 */
	public void connect(String ip, String newtoken){
		String newurl="http://"+ip+"/annodere";
		token=newtoken;

		if(url==null||!newurl.equals(url)){
			//new connetion
			url=newurl;
			session_token=null;
			register();
			cw_state.set(cw_state_t.CW_UNREGISTERED);
		}
	}

	/**
	 * external call to connect with current properties
	 */
	public void connect(){
		if(connect_props_set()) register();
	}

	/**
	 * check if registration is enabled
	 * @return true if connect() has been called
	 */
	public boolean connect_props_set(){
		return url!=null&&token!=null;
	}

	/**
	 * enqueue notification to be sent over network
	 * @param not_str notification text to send
	 */
	public void send_notification(String not_str){
		assert(not_str!=null);
		boolean queue_state=not_queue.isEmpty();
		not_queue.add(not_str);
		if(queue_state&&cw_state.get()==cw_state_t.CW_REGISTERED){
			Log.d("CW","Got notification. Start processing immediately.");
			process_notification_queue();
		}else Log.d("CW","Got notification. Enqueued.");
	}

	/**
	 * Do RPC-call to register at Desktop application
	 */
	private void register(){
		assert(url!=null);assert(token!=null);
		// check if data are valid
		if(cw_state.get()==cw_state_t.CW_REGISTERED) return;
		// compose request
		JSONArray params=new JSONArray();
		params.put(token);
		json_request request=new json_request(url,"register",params);
		// send request
		Log.d("CW","sending registration request ... ");
		new http_task().execute(request);
	}

	private boolean notification_queue_mutex_up=false;
	/**
	 * mutex to lock synchronized notification sending
	 * @param p whether to set lock or unlock mutex
	 * @return
	 */
	private synchronized boolean notification_queue_mutex(boolean p){
		if(p){ // set mutex up
			if(!notification_queue_mutex_up){ // mutex is available
				Log.d("CW","Mutex lock");
				notification_queue_mutex_up=true;
				return true;
			}else // mutex is unavailable
				return false;
		}else{ // set mutex down
			Log.d("CW","Mutex unlock");
			notification_queue_mutex_up=false;
			return true;
		}
	}

	/**
	 * Do RPC-Call to send oldest message to desktop
	 */
	private void process_notification_queue(){
		assert(url!=null);assert(token!=null);
		if(cw_state.get()!=cw_state_t.CW_REGISTERED){
			register();
			return;
		}

		if(!notification_queue_mutex(true)) return; // processing in progress
		muffi:{
			String not_str=not_queue.peek();
			if(not_str==null) break muffi; // no more to send
			//TODO: check if local network access available!
			// compose request
			JSONArray params=new JSONArray();
			if(session_token==null||session_token.isEmpty()){
				Log.e("CW","session token null");
				register();
				break muffi;
			}
			params.put(session_token);params.put(not_str);
			json_request request=new json_request(url,"notify",params);
			// send
			Log.d("CW","sending notification with token "+session_token);
			new http_task().execute(request);
			// everything went right
		}
		notification_queue_mutex(false); // unlock mutex in error case
	}

	/**
	 * Callback method to call after call returned and reception is complete
	 * @param res response
	 */
	protected void callback(json_result res){
		assert(res!=null);
		// process result of register call: Set session token
		if(res.rq.method.equals("register")) {
			if(res.val_type==1){
				set_session_token(res.str_val);
				cw_state.set(cw_state_t.CW_REGISTERED);
			}else Log.e("CW","Registration failed: "+res.toString());
		}
		else if(res.rq.method.equals("notify")){
			notification_queue_mutex(false); // make method available again
			if(res.val_type==3 && res.bool_val==true){
				Log.d("CW","OK!"); // successfully sent: remove top element
				// and process queue if not empty yet
				if(not_queue.poll()==null) Log.i("CW","Sent message twice.");
				if(!not_queue.isEmpty()) process_notification_queue();
			}else{
				try { //wait some time to prevent flooding
					Thread.sleep(500);
				} catch (InterruptedException e) {} // don't care
				if(res.val_type==4){
					cw_state.set(cw_state_t.CW_UNREGISTERED);
					Log.d("CW","Unregistered");
					// unregistered. Try to re-register
					register();
				}else{
					Log.d("CW","err "+res.val_type);
					// could not send: retry!
					process_notification_queue(); // retry
				}
			}
		}else Log.e("CW","callback received result for unknown method!");
	}

	/**
	 * Timer to periodically retry sending notifications
	 * @author thoto
	 *
	 */
	private class retry_timer_task extends TimerTask{
		@Override
		public void run() {
			if(cw_state.get()==cw_state_t.CW_UNREGISTERED) register();
			else if(cw_state.get()==cw_state_t.CW_REGISTERED
					&& !not_queue.isEmpty()) process_notification_queue();
		}
	}

	/**
	 * Task to execute HTTP request
	 * @author thoto
	 *
	 */
	private class http_task extends AsyncTask<json_request,Integer,json_result>{
		@Override
		protected json_result doInBackground(json_request... req) {
			assert(req[0].get_url()!=null);

			// initialization
			HttpClient client;
			HttpPost post;
			HttpResponse response;
			InputStream inputStream = null;

			json_result res = null;
			JsonReader jr;
			String jn;

			try {
				// Thread.sleep(6000); TODO TEST POINT!
				// send HTTP request
				client=new DefaultHttpClient();
				post=new HttpPost(req[0].get_url());
				post.setEntity(new StringEntity(req[0].get_request()));
				response=client.execute(post);

				if(response.getEntity()==null){
					Log.e("CW HT","Entity is null");
					return null;
				}
				// parse response
				inputStream = response.getEntity().getContent();
				if(inputStream != null){
					jr=new JsonReader(new BufferedReader(
							new InputStreamReader(inputStream)));

					// parse JSON part
					jr.beginObject();
					while(jr.hasNext()){
						jn=jr.nextName();
						if(jn.equals("result")){ // return result
							res=new json_result(jr,req[0]);
						}else if(jn.equals("jsonrpc")){ // check version
							String version=jr.nextString();
							if(!version.equals("2.0")){
								jr.close();
								Log.e("CW HT","Invalid RPC version");
								return null;
							}
						}else if(jn.equals("error")){ // return error TODO
							res=new json_result(false,jr,req[0]);
						}else if(jn.equals("id")){ // don't handle id TODO
							jr.skipValue();
						}else{ // unknown thingy
							Log.d("CW HT","skip.");
							jr.skipValue();
						}
					}
					jr.close();
					if(res!=null) Log.d("CW HT","Result: "+res.toString());
					else Log.d("CW HT","Result: NULL");
					callback(res);
					return res;
				}
			} catch (Exception e) {
				Log.e("CW HT", "Exception");
				String msg=e.getMessage();
				if(msg!=null) Log.e("CW HT",e.getMessage());
				else Log.e("CW HT","NULL exception");
				Log.e("CW HT",e.toString());
			}
			return null;
		}
	}

}