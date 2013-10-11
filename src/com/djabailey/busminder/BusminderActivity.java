package com.djabailey.busminder;

import java.util.UUID;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

import com.djabailey.busminder.NetThread.gotDataCallback;

public class BusminderActivity extends Activity implements gotDataCallback{
	TextView tvText;
	EditText etRoute;
	EditText etStop1, etStop2;
	
	NetThread myNT = null;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        myNT = new NetThread();
        myNT.setGDCB(this);
        myNT.start();
        tvText = (TextView)findViewById(R.id.tvText);
        tvText.setText("Status...");
        
        etRoute = (EditText)findViewById(R.id.etRoute);
        etStop1 = (EditText)findViewById(R.id.etStop1);
        etStop1.addTextChangedListener(new TextWatcher() {
			
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				myNT.stop = etStop1.getText().toString();
			}
			
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
        etStop2 = (EditText)findViewById(R.id.etStop2);
    }

	public void gotData() {
		 // writing response to log
	    Log.d("Http Response:", myNT.response.toString());
	    Log.d("Http Response:", myNT.JSON);
	    runOnUiThread(new Runnable() {
			
			public void run() {
				//show data.
				tvText.setText(myNT.JSON);
			}
		});
	    boolean dataSent = false;
	    if (myNT.bussesAtStop != null){
	    	for (int i = 0; i<= myNT.bussesAtStop.length-1; i++){
	    		if (myNT.bussesAtStop[i] != null){
	    			if( myNT.bussesAtStop[i].route.contentEquals(etRoute.getText().toString())){
	    				updateWatch(myNT.bussesAtStop[i].route + " - " + myNT.bussesAtStop[i].time + "\n" + myNT.bussesAtStop[i].name);
	    				dataSent = true;
	    				break;
	    			}
	    		}
	    	}
	    }
	    if (!dataSent){
	    	updateWatch("No data found.");
	    }
	}
	
	// Push {range, hole, par} data to be displayed on Pebble's Golf app.
    // To simplify formatting, values are transmitted to Pebble as null-terminated strings.
    public void updateWatch(String textdata) {
        
        PebbleDictionary data = new PebbleDictionary();
        data.addString(0, textdata);
        
        // Once the dictionary has been populated, it is scheduled to be sent to the watch. The sender/recipient of
        // all PebbleKit messages is determined by the UUID. In this case, since we're sending the data to the golf app,
        // we specify the Golf UUID.
        PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString("0f08a738-2ee1-4506-a130-1122d0f632d5"), data);
    }
}