package com.djabailey.busminder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

public class BusminderActivity extends Activity{
	
	ListView elvStops;
	ListAdapter elaStops;
	
	StopData stops;
	
	private static final String WATCHAPP_FILENAME = "BusFace.pbw";
	private static final int ID_ACTIVITY_UPDATEWATCHAPP = 2;
	
	private void sendAppToWatch() {
        try {
                InputStream input = getAssets().open(WATCHAPP_FILENAME);
                File file = new File(Environment.getExternalStorageDirectory(), WATCHAPP_FILENAME);
                file.setReadable(true, false);
                OutputStream output = new FileOutputStream(file);
                try {
                        byte[] buffer = new byte[1024];
                        int read;
                        while ((read = input.read(buffer)) != -1)
                                output.write(buffer, 0, read);
                        output.flush();
                } finally {
                        output.close();
                }
                input.close();
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "application/octet-stream");
                startActivityForResult(intent, ID_ACTIVITY_UPDATEWATCHAPP);
        } catch (Exception e) {
                Toast.makeText(this, "Couldn't load pebble app.", Toast.LENGTH_LONG).show();
        }
	}
	
	public class myListAdapter implements ListAdapter{
		
		@Override
		public boolean isEmpty() {
			return (stops.busStopIDs.size() <=0);
		}
		
		@Override
		public boolean hasStableIds() {
			return false;
		}

		@Override
		public int getCount() {
			if ((stops != null) && (stops.busStopIDs != null)){
				return stops.busStopIDs.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int arg0) {
			// NAH
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			// NAH
			return 0;
		}

		@Override
		public int getItemViewType(int arg0) {
			return 1;
		}

		@Override
		public View getView(final int position, View arg1, ViewGroup arg2) {
			LinearLayout llRow = new LinearLayout(getApplicationContext());
			
			//get screen size
			DisplayMetrics dm = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(dm);
	        int screenWidth = dm.widthPixels;
	        
			llRow.setMinimumWidth(screenWidth);
			llRow.setMinimumHeight(50);
			
			CheckBox cbEnable = new CheckBox(getApplicationContext());
			cbEnable.setFocusable(false);
			cbEnable.setChecked(stops.busStopEnabled.get(position).booleanValue());
			cbEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					stops.busStopEnabled.set(position, Boolean.valueOf(isChecked));
					PushServiceUtils.RestartService(stops, BusminderActivity.this);
				}
			});
			llRow.addView(cbEnable);
			
			TextView tvListItemText = new TextView(getApplicationContext());
			tvListItemText.setTextSize(25);
			tvListItemText.setText(stops.busStopNames.get(position));
			tvListItemText.setTextColor(Color.BLACK);
			llRow.addView(tvListItemText);
			llRow.setTag(Integer.valueOf(position));
			return llRow;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}
		
		DataSetObserver myDso;
		
		@Override
		public void registerDataSetObserver(DataSetObserver dso) {
			myDso = dso;
		}

		@Override
		public void unregisterDataSetObserver(DataSetObserver dso) {
			myDso = null;
		}
		
		public void notifyChange(){
			if (myDso!=null){
				myDso.onChanged();
			}
		}

		@Override
		public boolean areAllItemsEnabled() {
			return true;
		}

		@Override
		public boolean isEnabled(int arg0) {
			return true;
		}
	}
	
	
	
	OnItemClickListener myItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Intent intent = new Intent(BusminderActivity.this, StopManagerActivity.class);
			intent.putExtra("ID", position);
			startActivity(intent);	
		}
	};
	
	private void loadStops(){
		stops = new StopData();
		stops.load(getSharedPreferences("busdata", MODE_MULTI_PROCESS));
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        elvStops = (ListView)findViewById(R.id.elvStops);
        elaStops = new myListAdapter();
        loadStops();
        elvStops.setAdapter(elaStops);
        elvStops.setOnItemClickListener(myItemClickListener);
        
        if (stops.firstRun){
        	AlertDialog.Builder alert = new AlertDialog.Builder(this);

    		alert.setTitle("Install Watch App");
    		alert.setMessage("Would you like to install the Pebble watch app for busminder?");

    		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
    			public void onClick(DialogInterface dialog, int whichButton) {
    				sendAppToWatch();
    			}
    		});
    		
    		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
    			  public void onClick(DialogInterface dialog, int whichButton) {
    			    // Canceled.
    			  }
    		});
    		
    		alert.show();
        }
        
        PushServiceUtils.RestartService(stops, BusminderActivity.this);
    }

    @Override
    protected void onStop(){
       super.onStop();
       PushServiceUtils.saveStops(stops, BusminderActivity.this);
    }
    
    @Override
    protected void onResume(){
    	super.onResume();
    	loadStops();
    	((myListAdapter) elaStops).notifyChange();
    	Log.i("App life", "onResume"); 
    }
    
    @Override
    protected void onStart(){
    	super.onStart();
    	loadStops();
    	((myListAdapter) elaStops).notifyChange();
    	Log.i("App life", "onStart"); 
    }

    /*
	private void saveStops() {
		stops.save(getSharedPreferences("busdata", MODE_MULTI_PROCESS));
	}
    
	private void RestartService() {
		saveStops();
		Intent serviceStart = new Intent(getApplicationContext(), BusDataPushService.class);
        stopService(new Intent(getApplicationContext(),BusDataPushService.class));
        serviceStart = new Intent(getApplicationContext(), BusDataPushService.class);
        startService(serviceStart);
	}
	*/
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
		if (requestCode == ID_ACTIVITY_UPDATEWATCHAPP){
			Toast.makeText(this, "Pebble app updated.", Toast.LENGTH_LONG).show();
		}
		else{
		if (resultCode == Activity.RESULT_OK){
		Log.i("Location selected", "Lat: " + data.getDoubleExtra("Lat", 0));
		Log.i("Location selected", "Lng: " + data.getDoubleExtra("Lng", 0));

		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Add a stop");
		alert.setMessage("Enter new stop details...");

		// Set an EditText view to get user input
		final LinearLayout items = new LinearLayout(getApplicationContext());
		items.setOrientation(LinearLayout.VERTICAL);
			final LinearLayout rowName = new LinearLayout(getApplicationContext());
			rowName.setOrientation(LinearLayout.HORIZONTAL);
				final TextView rowNameLabel = new TextView(getApplicationContext());
				rowNameLabel.setText("Name: ");
				rowNameLabel.setWidth(120);
				rowNameLabel.setTextColor(Color.BLACK);
				rowName.addView(rowNameLabel);
				
				final EditText rowNameEdit = new EditText(getApplicationContext());
				rowNameEdit.setWidth(500);
				rowNameEdit.setTextColor(Color.BLACK);
				rowName.addView(rowNameEdit);
				
			final LinearLayout rowID = new LinearLayout(getApplicationContext());
			rowID.setOrientation(LinearLayout.HORIZONTAL);
				final TextView rowIDLabel = new TextView(getApplicationContext());
				rowIDLabel.setText("Number: ");
				rowIDLabel.setWidth(120);
				rowIDLabel.setTextColor(Color.BLACK);
				rowID.addView(rowIDLabel);
				
				final EditText rowIDEdit = new EditText(getApplicationContext());
				rowIDEdit.setWidth(500);
				rowIDEdit.setTextColor(Color.BLACK);
				rowID.addView(rowIDEdit);
			
		items.addView(rowName);
		items.addView(rowID);
		
		alert.setView(items);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
			if (stops.busStopIDs == null){stops.busStopIDs = new ArrayList<String>(1);}
			if (stops.busStopNames == null){stops.busStopNames = new ArrayList<String>(1);}
			if (stops.busStopLocations == null){stops.busStopLocations = new ArrayList<LatLng>(1);}
			if (stops.routeFilters == null){stops.routeFilters = new ArrayList<ArrayList<String> >(1);}
			if (stops.busStopEnabled == null){stops.busStopEnabled = new ArrayList<Boolean>(1);}
			stops.busStopNames.add(rowNameEdit.getText().toString());
			stops.busStopIDs.add(rowIDEdit.getText().toString());
			stops.routeFilters.add(new ArrayList<String>());
			stops.busStopLocations.add(new LatLng(data.getDoubleExtra("Lat", 0), data.getDoubleExtra("Lng", 0)));
			stops.busStopEnabled.add(Boolean.valueOf(true));
			PushServiceUtils.RestartService(stops, BusminderActivity.this);
		  }
		});
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			  public void onClick(DialogInterface dialog, int whichButton) {
			    // Canceled.
			  }
			});
		
		alert.show();
		elvStops.invalidate();
		elvStops.invalidateViews();
		elvStops.requestLayout();
		
		
		}
		}
	}
	
	public void addNewStop(){
		
		Intent intent = new Intent(this, MapActivity.class);
		startActivityForResult(intent, 0);
		
	}

	
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);
	    if (stops.serviceEnabled){
        	menu.findItem(R.id.action_start).setTitle(R.string.action_start_stop);
		}
		else{
			menu.findItem(R.id.action_start).setTitle(R.string.action_start_start);
		}
	    return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	        case R.id.action_add:
	        	addNewStop();
	            return true;
	        case R.id.action_start:
	        	stops.serviceEnabled = !stops.serviceEnabled;
	        	PushServiceUtils.RestartService(stops, BusminderActivity.this);
				if (stops.serviceEnabled){
					item.setTitle("Stop Service");
				}
				else{
					item.setTitle("Start Service");
				}
	            return true;
	        case R.id.action_help:
	            //openHelp();
	            return true;
	        case R.id.action_upload:    
	            sendAppToWatch();
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
}