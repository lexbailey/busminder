package com.djabailey.busminder;

import java.util.ArrayList;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.TextView;

public class StopManagerActivity extends Activity implements OnMenuItemClickListener{

	ListView elvRoutes;
	ListAdapter elaRoutes;
	
	String stopID, stopName;
	boolean stopEnabled;
	
	StopData stops;
	
	int stopListID;
	
	TextView tvLblItems;
	TextView tvStopName;
    TextView tvStopNumber;
	
    
    
	public class myFilterListAdapter implements ListAdapter{
		
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
			if (stops.routeFilters != null){
				if (stops.routeFilters.get(stopListID)!=null){
					return stops.routeFilters.get(stopListID).size();
				}
			}
			return 1;
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public int getItemViewType(int arg0) {
			return 1;
		}

		
		
		@Override
		public View getView(final int position, View arg1, ViewGroup arg2) {
			
				final int id = position;
						
				TextView tvRoute = new TextView(getApplicationContext());
				tvRoute.setText("   Route: " + stops.routeFilters.get(stopListID).get(id));
				tvRoute.setTextColor(Color.BLACK);
				tvRoute.setTextSize(18);
				tvRoute.setMinimumHeight(80);
				tvRoute.setGravity(Gravity.CENTER_VERTICAL);
				
				LinearLayout llRow = new LinearLayout(getApplicationContext());
				llRow.addView(tvRoute);
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
	
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.manage_stop);
	    Intent intent = getIntent();
	    stopListID = intent.getIntExtra("ID", -1);
	    if (stopListID < 0){
	    	finish();
	    }
		
	    stops = new StopData();
		stops.load(getSharedPreferences("busdata", MODE_MULTI_PROCESS));
	    
		elvRoutes = (ListView)findViewById(R.id.lvRoutes);
        elaRoutes = new myFilterListAdapter();
		
        elvRoutes.setAdapter(elaRoutes);
        elvRoutes.setOnItemClickListener(myItemClickListener);
        
        stopID = stops.busStopIDs.get(stopListID);
        stopName = stops.busStopNames.get(stopListID);
        stopEnabled = stops.busStopEnabled.get(stopListID);
        
        tvStopName = (TextView)findViewById(R.id.tvStopName);
        tvStopNumber = (TextView)findViewById(R.id.tvStopNumber);
        
        tvStopName.setText(stopName);
        tvStopNumber.setText("Number: " + stopID);
        
        tvLblItems = (TextView)findViewById(R.id.tvLblItems);
        
        if (elaRoutes.getCount() <= 0){
			//no items
			tvLblItems.setText("No route filters for this stop.");
		}
		else{
			//some items
			tvLblItems.setText("Active route filters:");
		}
		
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    // Inflate the menu items for use in the action bar
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.stop_manage_activity_actions, menu);
	    return super.onCreateOptionsMenu(menu);
	}
	
	public void deleteRoute(int id)
	{
		stops.routeFilters.get(stopListID).remove(id);
		elvRoutes.invalidate();
		elvRoutes.invalidateViews();
		elvRoutes.requestLayout();
		((myFilterListAdapter) elaRoutes).notifyChange();
		PushServiceUtils.RestartService(stops, this);
	}
	
	public void deleteStop(){
		stops.busStopIDs.remove(stopID);
		stops.busStopNames.remove(stopID);
		stops.busStopLocations.remove(stopID);
		stops.busStopEnabled.remove(stopID);
		stops.routeFilters.remove(stopID);
		PushServiceUtils.RestartService(stops, this);
		finish();
	}
	
	public void editStop(){
		AlertDialog.Builder alert = new AlertDialog.Builder(StopManagerActivity.this);

		alert.setTitle("Edit stop");
		alert.setMessage("Edit the details below...");

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
		                rowNameEdit.setText(stops.busStopNames.get(stopListID));
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
		                rowIDEdit.setText(stops.busStopIDs.get(stopListID));
		                rowIDEdit.setTextColor(Color.BLACK);
		                rowID.addView(rowIDEdit);
		        
		items.addView(rowName);
		items.addView(rowID);

		alert.setView(items);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		        if (stops.busStopIDs == null){stops.busStopIDs = new ArrayList<String>(1);}//shouldn't happen here
		        if (stops.busStopNames == null){stops.busStopNames = new ArrayList<String>(1);}
		        stops.busStopNames.set(stopListID ,rowNameEdit.getText().toString());
		        stops.busStopIDs.set(stopListID ,rowIDEdit.getText().toString());
		        tvStopName.setText(stops.busStopNames.get(stopListID));
		        tvStopNumber.setText(stops.busStopIDs.get(stopListID));
		        ((myFilterListAdapter) elaRoutes).notifyChange();
				PushServiceUtils.RestartService(stops, StopManagerActivity.this);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		          public void onClick(DialogInterface dialog, int whichButton) {
		            // Canceled.
		          }
		        });

		alert.show();

	}
	
	public void editFilter(final int filter){
		AlertDialog.Builder alert = new AlertDialog.Builder(StopManagerActivity.this);

		alert.setTitle("Edit route filter");
		alert.setMessage("Type a new route filter...");
		
        final LinearLayout rowName = new LinearLayout(getApplicationContext());
        rowName.setOrientation(LinearLayout.HORIZONTAL);
        	final TextView rowNameLabel = new TextView(getApplicationContext());
		    	rowNameLabel.setText("Route: ");
		        rowNameLabel.setWidth(120);
		        rowNameLabel.setTextColor(Color.BLACK);
		        rowName.addView(rowNameLabel);
		                
		    final EditText rowNameEdit = new EditText(getApplicationContext());
		        rowNameEdit.setWidth(500);
		        rowNameEdit.setText(stops.routeFilters.get(stopListID).get(filter));
		        rowNameEdit.setTextColor(Color.BLACK);
		        rowName.addView(rowNameEdit);
		        
		alert.setView(rowName);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
				stops.routeFilters.get(stopListID).set(filter, rowNameEdit.getText().toString());
				((myFilterListAdapter) elaRoutes).notifyChange();
				PushServiceUtils.RestartService(stops, StopManagerActivity.this);
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		          public void onClick(DialogInterface dialog, int whichButton) {
		            // Canceled.
		          }
		        });

		alert.show();

	}
	
	public void addNewFilter(){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Add a route filter");
		alert.setMessage("Enter the bus route number you want for this stop.");
			
		final EditText rowLocationEdit = new EditText(getApplicationContext());
		rowLocationEdit.setMinimumWidth(600);
		rowLocationEdit.setTextColor(Color.BLACK);
			
		alert.setView(rowLocationEdit);
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (stops.routeFilters==null){
					stops.routeFilters = new ArrayList<ArrayList<String> >(1);
				}
				if (stops.routeFilters.get(stopListID)==null){
					stops.routeFilters.add(stopListID, new ArrayList<String>()); 
				}
				stops.routeFilters.get(stopListID).add(rowLocationEdit.getText().toString());
				((myFilterListAdapter) elaRoutes).notifyChange();
				PushServiceUtils.RestartService(stops, StopManagerActivity.this);	
			}
		});
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		
		alert.show();
	}
	
	int contextPosition;
	
	OnItemClickListener myItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			//deleteRoute(position);
			PopupMenu popup = new PopupMenu(StopManagerActivity.this, view);
		    popup.inflate(R.menu.route_filter_context);
		    contextPosition = position;
		    popup.setOnMenuItemClickListener(StopManagerActivity.this);
		    popup.show();
		}
	};
	
	@Override
	public boolean onMenuItemClick(MenuItem item) {
	    switch (item.getItemId()) {
	       case R.id.route_delete:
	        	deleteRoute(contextPosition);
	        	return true;
	        case R.id.route_edit:
	        	editFilter(contextPosition);
	        	return true;
	        default:
	            return false;
	    }
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle presses on the action bar items
	    switch (item.getItemId()) {
	    	case R.id.action_edit_stop:
	    		editStop();
	    		return true;
	        case R.id.action_add_filter:
	        	addNewFilter();
	            return true;
	        case R.id.action_delete_stop:
	        	deleteStop();
	            return true;
	        case R.id.action_help:
	            //openHelp(); TODO
	            return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
}
