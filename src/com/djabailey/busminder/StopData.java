package com.djabailey.busminder;

import java.util.ArrayList;

import android.content.SharedPreferences;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class StopData {
	ArrayList<ArrayList<String> > routeFilters;
	ArrayList<String> busStopIDs;
	ArrayList<String> busStopNames;
	ArrayList<Boolean> busStopEnabled;
	ArrayList<LatLng> busStopLocations;
	
	public void save(SharedPreferences prefs){
		Log.i("Load", "About to save " + busStopIDs.size()+ " stops");
		if (busStopIDs.size()>=1){
			
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("Numstops", busStopIDs.size());
			for (int i = 0; i<= busStopIDs.size()-1; i++){
				editor.putString("Stop" + i + "-ID", busStopIDs.get(i));
				editor.putString("Stop" + i + "-Name", busStopNames.get(i));
				editor.putLong("Stop" + i + "-Lat", Double.doubleToRawLongBits(busStopLocations.get(i).latitude));
				editor.putLong("Stop" + i + "-Lng", Double.doubleToRawLongBits(busStopLocations.get(i).longitude));
				editor.putBoolean("Stop" + i + "-Enabled", busStopEnabled.get(i).booleanValue());
				editor.putInt("Stop"+ i+ "-Numroutes", routeFilters.get(i).size());
				if(routeFilters.get(i).size()>=1){
					for (int j = 0; j<= routeFilters.get(i).size()-1; j++){
						editor.putString("Stop" + i + "-Filter" + j, routeFilters.get(i).get(j));
					}
				}
			}
	    	editor.commit();
		}
	}
	public void load(SharedPreferences prefs){
		int numItems = prefs.getInt("Numstops", 0);
		Log.i("Load", "About to load " + numItems + " stops");
		routeFilters = new ArrayList<ArrayList<String> >(numItems);
		routeFilters = new ArrayList<ArrayList<String> >(numItems);
		busStopIDs = new ArrayList<String>(numItems);
		busStopNames = new ArrayList<String>(numItems);
		busStopLocations = new ArrayList<LatLng>(numItems);
		busStopEnabled = new ArrayList<Boolean>(numItems);
		
		for (int i = 0; i<= numItems-1; i++){
			Log.i("Load", "Loading stop " + i);
			busStopIDs.add(i, prefs.getString("Stop" + i + "-ID", ""));
			busStopNames.add(i, prefs.getString("Stop" + i + "-Name", ""));
			double lat = Double.longBitsToDouble(prefs.getLong("Stop" + i + "-Lat", 0));
			double lng = Double.longBitsToDouble(prefs.getLong("Stop" + i + "-Lng", 0));
			busStopLocations.add(i, new LatLng(lat, lng));
			busStopEnabled.add(i, Boolean.valueOf(prefs.getBoolean("Stop" + i + "-Enabled", true)));
			
			int numRoutes =	prefs.getInt("Stop"+ i+ "-Numroutes", 0);
			routeFilters.add(i, new ArrayList<String>(numRoutes));
			for (int j = 0; j<= numRoutes-1; j++){
				routeFilters.get(i).add(j, prefs.getString("Stop" + i + "-Filter" + j, ""));
			}
		}
	}
}