package com.djabailey.busminder;

import java.util.UUID;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

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
		    		if ((nearestStop>=0) && (stops.busStopIDs.size()>nearestStop)){
		    			setStopNumber(stops.busStopIDs.get(nearestStop));
		    		}
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
	
	NotificationManager mNotificationManager;
	int mId;
	@Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        
        stops = new StopData();
		stops.load(getSharedPreferences("busdata", MODE_MULTI_PROCESS));
        if (!stops.serviceEnabled){ //if the service is not wanted, GO AWAY!
        	myNT.kill();
        	this.stopSelf();
        }
		
		int num = stops.busStopLocations.size();
		busStopLocations = new Location[num];
		for (int i = 0; i<= num-1; i++){
			busStopLocations[i] = new Location("busstop");
			busStopLocations[i].setLatitude(stops.busStopLocations.get(i).latitude);
			busStopLocations[i].setLongitude(stops.busStopLocations.get(i).longitude);
		}
		
		Bitmap icon = BitmapFactory.decodeResource(getApplicationContext().getResources(),
                R.drawable.ic_launcher);
		
		NotificationCompat.Builder mBuilder =
		        new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_launcher)
				.setLargeIcon(icon)
		        .setContentTitle("Busminder service is running")
		        .setContentText("Tap to configure")
		        .setOngoing(true);
		
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, BusminderActivity.class);

		// The stack builder object will contain an artificial back stack for the
		// started Activity.
		// This ensures that navigating backward from the Activity leads out of
		// your application to the Home screen.
		TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
		// Adds the back stack for the Intent (but not the Intent itself)
		stackBuilder.addParentStack(BusminderActivity.class);
		// Adds the Intent that starts the Activity to the top of the stack
		stackBuilder.addNextIntent(resultIntent);
		PendingIntent resultPendingIntent =
		        stackBuilder.getPendingIntent(
		            0,
		            PendingIntent.FLAG_UPDATE_CURRENT
		        );
		mBuilder.setContentIntent(resultPendingIntent);
		mNotificationManager =
		    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// mId allows you to update the notification later on.
		mId = 1;
		mNotificationManager.notify(mId, mBuilder.build());
		Log.i("Notif", "ication");
		
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
    	myNT.kill();
    	mNotificationManager.cancel(mId);
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
	    	
	    	for (int i = 0; i<= myNT.bussesAtStop.length-1; i++){
	    	
	    		if (myNT.bussesAtStop[i] != null){
	    		
	    			if (((stops.routeFilters != null)&&(stops.routeFilters.size() >= nearestStop+1))
	    			&&(stops.routeFilters.get(nearestStop).size()>=1)){
	    			
	    				for (int j = 0; j<=stops.routeFilters.get(nearestStop).size()-1; j++){
	    				
	    					if( myNT.bussesAtStop[i].route.contentEquals(stops.routeFilters.get(nearestStop).get(j))){
	    					
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
				updateWatch("No busses match the filters.");
				dataSent = true;
			}
	    }
	    
	    if (myNT.error != null){
	    	if (!dataSent){
		    	if (myNT.error.equals("no_busses_within_hour")){
		    		updateWatch("No busses departing from here within the next hour.");
			    	dataSent = true;	
				}
		    }
		    
		    if (!dataSent){
		    	if (myNT.error.equals("scrape_error")){
		    		updateWatch("Bus stop not found.");
			    	dataSent = true;	
				}
		    }
		    if (!dataSent){
		    	if (myNT.error.equals("empty_get")){
		    		updateWatch("Data server or request error.");
			    	dataSent = true;	
				}
		    }
		    if (!dataSent){
		    	if (myNT.error.equals("no_stop_specified")){
		    		updateWatch("Stop ID missing");
			    	dataSent = true;	
				}
		    }
	    }
	    if (!dataSent){
	    		updateWatch("Updating, please wait...");
		    	dataSent = true;
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
