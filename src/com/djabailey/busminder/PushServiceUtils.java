package com.djabailey.busminder;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public class PushServiceUtils {
	
	public static void saveStops(StopData stops, Activity a) {
		stops.save(a.getSharedPreferences("busdata", android.content.Context.MODE_MULTI_PROCESS));
	}
    
	public static void RestartService(StopData stops, Activity a) {
		saveStops(stops, a);
		Context c = a.getApplicationContext();
		Intent serviceStart = new Intent(c, BusDataPushService.class);
        a.stopService(new Intent(c,BusDataPushService.class));
        serviceStart = new Intent(c, BusDataPushService.class);
        a.startService(serviceStart);
	}
}
