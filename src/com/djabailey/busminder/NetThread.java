package com.djabailey.busminder;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class NetThread extends Thread{
	
	public boolean gotdata = false;
	
	interface gotDataCallback{
		void gotData();
	}
	
	gotDataCallback gdcb = null;
	
	public HttpResponse response;
	public String JSON;
	public JSONObject data;
	
	public class busData{
		public String route;
		public String name;
		public String time;
	}
	
	public busData bussesAtStop[];

	static String convertStreamToString(java.io.InputStream is) {
	    java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
	    return s.hasNext() ? s.next() : "";
	}
	
	public String stop = "";
	
	private void getLatestData(){
		// Creating HTTP client
		HttpClient httpClient = new DefaultHttpClient();
		 
		// Creating HTTP Post
		HttpGet httpGet = new HttpGet("http://thewayitusedtobe.co.uk/getbusdata.php?stop="+stop);
		
		// Making HTTP Request
		try {
		    response = httpClient.execute(httpGet);
		    
		    JSON = convertStreamToString(response.getEntity().getContent());
		    
		    
		    try {
				data = new JSONObject(JSON);
				JSONArray jaBus = data.getJSONArray("data");
				bussesAtStop = new busData[jaBus.length()];
				for (int i = 0; i<= jaBus.length()-1; i++){
					JSONObject thisBus = jaBus.getJSONObject(i);
					bussesAtStop[i] = new busData();
					bussesAtStop[i].name = thisBus.getString("dest");
					bussesAtStop[i].route = thisBus.getString("route");
					bussesAtStop[i].time = thisBus.getString("est");
				}
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		 
		} catch (ClientProtocolException e) {
		    // writing exception to log
		    e.printStackTrace();
		         
		} catch (IOException e) {
		    // writing exception to log
		    e.printStackTrace();
		}
	}
	
	public void setGDCB(gotDataCallback gdcb){
		this.gdcb = gdcb;
	}
	
	private boolean running = true;
	
	public void kill(){
		running = false;
	}
	
	@Override
	public void run(){
		super.run();
		while(running){
			getLatestData();
			gotdata = true;
			if (gdcb != null){
				gdcb.gotData();
			}
			try {
				Thread.sleep(15000);//15 seconds
			} catch (InterruptedException e) {
			}
		}
	}

}
