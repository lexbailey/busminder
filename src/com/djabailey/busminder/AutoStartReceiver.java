package com.djabailey.busminder;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
 
public class AutoStartReceiver extends BroadcastReceiver {
 
	StopData stops;
	
    @Override
    public void onReceive(Context context, Intent intent) {
    	stops = new StopData();
		stops.load(context.getSharedPreferences("busdata", Context.MODE_MULTI_PROCESS));
		stops.save(context.getSharedPreferences("busdata", Context.MODE_MULTI_PROCESS));
        Intent service = new Intent(context, BusDataPushService.class);
        context.startService(service);
    }
}
