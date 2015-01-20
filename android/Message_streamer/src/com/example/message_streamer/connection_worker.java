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
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.JsonToken;
import android.util.Log;

/**
 * Worker to handle every kind of network RPC stuff
 * @author thoto
 *
 */
public class connection_worker {
	private String session_token;
	private ConnectivityManager conn_man;
	private String url;
	Queue<String> not_queue;
	Timer retry_timer;
	String token;

	/**
	 * Creates connection_worker and connects to server
	 * @param ip IP address to connect to
	 * @param token Authentication token to provide
	 */
	public connection_worker(Context c,String ip, String token){
		not_queue=new LinkedList<String>();
		conn_man=(ConnectivityManager)
				c.getSystemService(Context.CONNECTIVITY_SERVICE);

		url="http://"+ip+":10080/annodere";
		this.token=token;

		register();

		retry_timer=new Timer();
		retry_timer.scheduleAtFixedRate(new retry_timer_task(),500,2000);
	}

	/**
	 * enqueue notification to be sent over network
	 * @param not_str notification text to send
	 */
	public void send_notification(String not_str){
		if(not_queue.isEmpty()){
			not_queue.add(not_str);
			process_notification_queue();
		}else not_queue.add(not_str);
	}

	/**
	 * Do RPC-call to register at Desktop application
	 */
	private void register(){
		NetworkInfo net_info=conn_man.getActiveNetworkInfo(); // FIXME REMOVE
		if (net_info!=null && net_info.isConnected()){
			JSONArray params=new JSONArray();
			params.put(token);
			json_request request=new json_request(url,"register",params);
			System.out.println("start ... ");
			new http_task().execute(request);
			System.out.println("done");
		} else {
			System.out.println("No connection available.");
		}
	}

	private boolean notification_queue_mutex_up=false;
	private synchronized boolean notification_queue_mutex(boolean p){
		if(p){ // set mutex up
			if(!notification_queue_mutex_up){ // mutex is available
				Log.d("CW Mtx","lock");
				notification_queue_mutex_up=true;
				return true;
			}else // mutex is unavailable
				return false;
		}else{ // set mutex down
			Log.d("CW Mtx","unlock");
			notification_queue_mutex_up=false;
			return true;
		}
	}

	/**
	 * Do RPC-Call to send oldest message to desktop
	 */
	private void process_notification_queue(){
		if(!notification_queue_mutex(true)) return; // processing in progress
		muffi:{
			String not_str=not_queue.peek();
			if(not_str==null) break muffi; // no more to send
	
			NetworkInfo net_info=conn_man.getActiveNetworkInfo(); // FIXME Remove
			if (net_info!=null && net_info.isConnected()){
				// compose request
				JSONArray params=new JSONArray();
				if(session_token==null){
					Log.d("CW","session token null");
					register();
					break muffi;
				}
				params.put(session_token);params.put(not_str);
				Log.d("CW","Connection Token:"+session_token);
				json_request request=new json_request(url,"notify",params);
				// send
				Log.d("CW","sending ... ");
				new http_task().execute(request);
				return; // everything went right
			} else {
				Log.e("CW","Connection broke.");
			}
		}
		notification_queue_mutex(false); // unlock muex in error case
	}

	/**
	 * Callback method to call after call returned and reception is complete
	 * @param res response
	 */
	protected void callback(json_result res){
		// process result of register call: Set session token
		if(res.rq.method.equals("register")) session_token=res.str_val;
		else if(res.rq.method.equals("notify")){
			notification_queue_mutex(false); // make method available again
			Log.d("CW","Notification Result: ");
			if(res.val_type==3 && res.bool_val==true){
				Log.d("CW","OK!");
				not_queue.poll(); // successfully sent: remove top element
				if(!not_queue.isEmpty()) process_notification_queue();
			}else{
				try { //wait some time to prevent flooding
					Thread.sleep(500);
				} catch (InterruptedException e) {} // don't care
				if(res.val_type==4){
					Log.d("CW","Unregistered");
					// unregistered. Try to re-register
					register();
				}else{
					Log.d("CW","err "+res.val_type);
					// could not send: retry!
					process_notification_queue(); // retry
				}
			}
		}
	}

	/**
	 * Timer to periodically retry sending notifications
	 * @author thoto
	 *
	 */
	private class retry_timer_task extends TimerTask{
		@Override
		public void run() {
			if(!not_queue.isEmpty()) process_notification_queue();
		}
	}

	/**
	 * JSON RPC call request structure
	 * @author thoto
	 *
	 */
	private class json_request{
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
	 * result structure emitted by JSON RPC calls
	 * @author thoto
	 *
	 */
	private class json_result{
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
	 * Task to execute HTTP request
	 * @author thoto
	 *
	 */
	private class http_task extends AsyncTask<json_request,Integer,json_result>{
		@Override
		protected json_result doInBackground(json_request... req) {
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
								return null;
							}
						}else if(jn.equals("error")){ // return error TODO
							res=new json_result(false,jr,req[0]);
						}else if(jn.equals("id")){ // don't handle id TODO
							jr.skipValue();
						}else{ // unknown thingy
							Log.d("JSON","skip.");
							jr.skipValue();
						}
					}
					jr.close();
					inputStream.close();
					if(res!=null) System.out.println("Result: "+res.toString());
					callback(res);
					return res;
				}
			} catch (Exception e) {
				Log.e("CW Async", "Exception");
				String msg=e.getMessage();
				if(msg!=null) Log.e("CW Async",e.getMessage());
				else Log.e("CW Async","NULL exception");
				Log.e("CW Async",e.toString());
			}
			return null;
		}
	}

}
