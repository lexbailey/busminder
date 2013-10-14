package com.djabailey.busminder;

import java.util.UUID;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import com.djabailey.busminder.NetThread;
import com.djabailey.busminder.NetThread.gotDataCallback;
import com.getpebble.android.kit.PebbleKit;
import com.getpebble.android.kit.util.PebbleDictionary;

public class BusDataPushService extends Service implements gotDataCallback{

	NetThread myNT = null;
	
	String routeFilter = "";
	
	StopData stops;
	
	int nearestStop = -1;
	
	
	Location busStopLocations[];
	float busStopDistances[];
	
	float shortestDistance = -1;
	
	boolean isFirstRun = true;
	
	@Override
	public void onCreate(){
		Log.i("BusDataPushService", "creating push service.");
		
		// Acquire a reference to the system Location Manager
		LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		LocationListener locationListener = new LocationListener() {
		    public void onLocationChanged(Location location) {
		      // Called when a new location is found by the network location provider.
		    	if (busStopLocations != null){
		    		busStopDistances = new float[busStopLocations.length];
		    		for (int i = 0; i<= busStopLocations.length-1; i++){
		    			if (busStopLocations[i] != null){
		    				busStopDistances[i] = location.distanceTo(busStopLocations[i]);
		    				if ((((shortestDistance <= -1) || (shortestDistance > busStopDistances[i])))
		    						&&(stops.busStopEnabled.get(i).booleanValue())){
		    					shortestDistance = busStopDistances[i];
		    					nearestStop = i;
		    				}
		    			}
		    		}
		    		Log.i("busstop", "Nearest stop is " + shortestDistance + "m away");
		    		setStopNumber(stops.busStopIDs.get(nearestStop));
		    		if (isFirstRun){
		    			myNT.interrupt();
		    			isFirstRun = false;
		    		}
		    	}
		    }

		    public void onStatusChanged(String provider, int status, Bundle extras) {}

		    public void onProviderEnabled(String provider) {}

		    public void onProviderDisabled(String provider) {}
		  };

		// Register the listener with the Location Manager to receive location updates
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
		
		myNT = new NetThread();
	    myNT.setGDCB(this);
	    myNT.start();
	    
	    super.onCreate();
	}
	
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("BusDataPushService", "starting push service.");
        
        stops = new StopData();
		stops.load(getSharedPreferences("busdata", MODE_MULTI_PROCESS));
        
		int num = stops.busStopLocations.size();
		busStopLocations = new Location[num];
		for (int i = 0; i<= num-1; i++){
			busStopLocations[i] = new Location("busstop");
			busStopLocations[i].setLatitude(stops.busStopLocations.get(i).latitude);
			busStopLocations[i].setLongitude(stops.busStopLocations.get(i).longitude);
		}
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	myNT.kill();
    	super.onDestroy();
    	//blah
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void gotData() {
		boolean dataSent = false;
	    if (myNT.bussesAtStop != null){
	    	String tag = "gotData function.";
	    	Log.i(tag, "Bus stop has busses!");
	    	for (int i = 0; i<= myNT.bussesAtStop.length-1; i++){
	    		Log.i(tag, "Investigating bus "+i);
	    		if (myNT.bussesAtStop[i] != null){
	    			Log.i(tag, "Bus at " + i + "is valid.");
	    			if (((stops.routeFilters != null)&&(stops.routeFilters.size() >= nearestStop+1))
	    			&&(stops.routeFilters.get(nearestStop).size()>=1)){
	    				Log.i(tag, "Bus stop has filters!");
	    				for (int j = 0; j<=stops.routeFilters.get(nearestStop).size()-1; j++){
	    					Log.i(tag, "Checking filter " + j);
	    					if( myNT.bussesAtStop[i].route.contentEquals(stops.routeFilters.get(nearestStop).get(j))){
	    						Log.i(tag, "This bus matches the filter!");
	    						routeFilter = stops.routeFilters.get(nearestStop).get(j);
	    						updateWatch(
	    							stops.busStopNames.get(nearestStop) + "\n"
	    								+myNT.bussesAtStop[i].name + " " + myNT.bussesAtStop[i].route + "\n"
	    								+ myNT.bussesAtStop[i].time);
	    						dataSent = true;
	    						break;
	    					}
	    				}
	    				if (dataSent){break;}
	    				
	    			}
	    			else{
	    				Log.i(tag, "No filters, showing the first bus if available");
	    				if (myNT.bussesAtStop.length >= 1){
	    					dataSent = true;
	    				updateWatch(
    							stops.busStopNames.get(nearestStop) + "\n"
    								+myNT.bussesAtStop[0].name + " " + myNT.bussesAtStop[0].route + "\n"
    								+ myNT.bussesAtStop[0].time);
	    				}
	    			}
	    		}
	    	}
	    	if (!dataSent){
				Log.i(tag, "No busses matching filters");
				updateWatch("No busses match the filters.");
			}
	    }
	    if (!dataSent){
	    	updateWatch("No data found.");
	    }
	}
	
	public void setStopNumber(String number){
		myNT.stop = number;
	}
	
	public void updateWatch(String textdata) {
        PebbleDictionary data = new PebbleDictionary();
        data.addString(1234, textdata);
        PebbleKit.sendDataToPebble(getApplicationContext(), UUID.fromString("0f08a738-2ee1-4506-a130-1122d0f632d5"), data);
    }	

}
