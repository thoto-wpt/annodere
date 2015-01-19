package com.example.message_streamer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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

public class connection_worker {
	private String session_token;
	private ConnectivityManager conn_man;
	private String url;
	/**
	 * Creates connection_worker and connects to server
	 * @param ip IP address to connect to
	 * @param token Authentication token to provide
	 */
	public connection_worker(Context c,String ip, String token){
		url="http://"+ip+":10080/annodere";
		conn_man=(ConnectivityManager)
				c.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo net_info=conn_man.getActiveNetworkInfo();
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
	public void send_notification(String not_str){
		NetworkInfo net_info=conn_man.getActiveNetworkInfo();
		if (net_info!=null && net_info.isConnected()){
			JSONArray params=new JSONArray();
			params.put(session_token);params.put(not_str);
			json_request request=new json_request(url,"notify",params);
			System.out.println("sending ... ");
			new http_task().execute(request);
			System.out.println("done");
		} else {
			System.out.println("Connection broke.");
		}
	}
	protected void set_session_token(String tok){
		session_token=tok;
	}
	protected void callback(json_result res){
		// TODO -> should be removed and arbitrary method included in request
		if(res.rq.method.equals("register")) set_session_token(res.str_val);
	}
	
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
	private class json_result{
		public int val_type=0;
		public boolean err=true;
		public String str_val;
		public Integer int_val;
		public boolean bool_val;
		public boolean err_critical;
		public json_request rq;
		
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
		public json_result(boolean b,JsonReader jr,json_request rq) 
				throws IOException{//error case
			this.rq=rq;
			jr.skipValue();
			
			err=true;err_critical=b;
			str_val="some error TODO"; // TODO
		}

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
	private class http_task extends AsyncTask<json_request,Integer,json_result>{
		@Override
		protected json_result doInBackground(json_request... req) {
			HttpClient client;
			HttpPost post;
			HttpResponse response;
			InputStream inputStream = null;
			
			json_result res = null;
			JsonReader jr;
			String jn;
			
			try {
				// send HTTP request
				client=new DefaultHttpClient();
				post=new HttpPost(req[0].get_url());
				post.setEntity(new StringEntity(req[0].get_request()));
				response=client.execute(post);
				
				// parse response
				inputStream = response.getEntity().getContent();
				if(inputStream != null){ // parse JSON part
					jr=new JsonReader(new BufferedReader(
							new InputStreamReader(inputStream)));
					
					jr.beginObject();
					while(jr.hasNext()){
						jn=jr.nextName();
						if(jn.equals("result")){ // return result
							res=new json_result(jr,req[0]);
						}else if(jn.equals("jsonrpc")){
							String version=jr.nextString();
							if(!version.equals("2.0")){
								jr.close();
								return null;
							}
						}else if(jn.equals("error")){
							res=new json_result(false,jr,req[0]);
						}else if(jn.equals("id")){
							jr.skipValue();
						}else{
							Log.d("JSON","skip.");
							jr.skipValue();
						}
					}
					jr.close();
					inputStream.close();
					if(res!=null) System.out.println("Result: "+res.toString());
					Thread.sleep(6000);
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
