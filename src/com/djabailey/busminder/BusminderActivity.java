package com.djabailey.busminder;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class BusminderActivity extends Activity{
	
	ExpandableListView elvStops;
	ExpandableListAdapter elaStops;
	
	
	StopData stops;
	
	public class myExpandableListAdapter extends BaseExpandableListAdapter{
		
		@Override
		public boolean isEmpty() {
			return (stops.busStopIDs.size() <=0);
		}
		
		@Override
		public boolean isChildSelectable(int groupPosition, int childPosition) {
			return false;
		}
		
		@Override
		public boolean hasStableIds() {
			return false;
		}
		
		@Override
		public View getGroupView(final int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			LinearLayout llRow = new LinearLayout(getApplicationContext());
			
			//get screen size
			DisplayMetrics dm = new DisplayMetrics();
	        getWindowManager().getDefaultDisplay().getMetrics(dm);
	        int screenWidth = dm.widthPixels;
	        
			llRow.setMinimumWidth(screenWidth);
			llRow.setMinimumHeight(50);
			TextView spacer = new TextView(getApplicationContext());
			spacer.setWidth(70);
			llRow.addView(spacer);
			
			CheckBox cbEnable = new CheckBox(getApplicationContext());
			cbEnable.setFocusable(false);
			cbEnable.setChecked(stops.busStopEnabled.get(groupPosition).booleanValue());
			cbEnable.setOnCheckedChangeListener(new OnCheckedChangeListener() {
				
				@Override
				public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
					stops.busStopEnabled.set(groupPosition, Boolean.valueOf(isChecked));
					RestartService();
				}
			});
			llRow.addView(cbEnable);
			
			TextView tvListItemText = new TextView(getApplicationContext());
			tvListItemText.setTextSize(25);
			tvListItemText.setText(stops.busStopNames.get(groupPosition) + " (" + stops.busStopIDs.get(groupPosition) + ")");
			tvListItemText.setWidth(300);
			llRow.addView(tvListItemText);
			
			ImageButton btnMenu = new ImageButton(getApplicationContext());
			btnMenu.setImageDrawable((getResources().getDrawable(android.R.drawable.ic_menu_more)));
			btnMenu.setFocusable(false);
			btnMenu.setMinimumHeight(96);
			
			llRow.addView(btnMenu);
			
			return llRow;
		}
		
		@Override
		public long getGroupId(int groupPosition) {
			return groupPosition;
		}
		
		@Override
		public int getGroupCount() {
			if ((stops != null) && (stops.busStopIDs != null)){
				return stops.busStopIDs.size();
			}
			return 0;
		}
		
		
		@Override
		public Object getGroup(int groupPosition) {
			return null;
		}
	
		@Override
		public int getChildrenCount(int groupPosition) {
			if (stops.routeFilters != null){
				if (stops.routeFilters.get(groupPosition)!=null){
					return stops.routeFilters.get(groupPosition).size()+1;
				}
			}
			return 1;
		}
		
		@Override
		public View getChildView(final int groupPosition, int childPosition,
				boolean isLastChild, View convertView, ViewGroup parent) {
			if (childPosition == 0){	
				LinearLayout llRow1 = new LinearLayout(getApplicationContext());
				LinearLayout llRow2 = new LinearLayout(getApplicationContext());
				LinearLayout llCol = new LinearLayout(getApplicationContext());
				
				TextView tvStopsLabel = new TextView(getApplicationContext());
				tvStopsLabel.setText("This Stop: ");
				//tvStopsLabel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
				tvStopsLabel.setHeight(96);
				tvStopsLabel.setGravity(Gravity.CENTER);
				
				TextView tvNoRouteLabel = new TextView(getApplicationContext());
				tvNoRouteLabel.setText("Filters: ");
				//tvNoRouteLabel.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));
				tvNoRouteLabel.setHeight(96);
				tvNoRouteLabel.setGravity(Gravity.CENTER);
					
				ImageButton btnEdit = new ImageButton(getApplicationContext());
				btnEdit.setImageDrawable((getResources().getDrawable(android.R.drawable.ic_menu_edit)));
				btnEdit.setFocusable(false);
				btnEdit.setMinimumHeight(96);
				btnEdit.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						// TODO Auto-generated method stub
						AlertDialog.Builder alert = new AlertDialog.Builder(BusminderActivity.this);

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
								rowName.addView(rowNameLabel);
								
								final EditText rowNameEdit = new EditText(getApplicationContext());
								rowNameEdit.setWidth(500);
								rowNameEdit.setText(stops.busStopNames.get(groupPosition));
								rowName.addView(rowNameEdit);
								
							final LinearLayout rowID = new LinearLayout(getApplicationContext());
							rowID.setOrientation(LinearLayout.HORIZONTAL);
								final TextView rowIDLabel = new TextView(getApplicationContext());
								rowIDLabel.setText("Number: ");
								rowIDLabel.setWidth(120);
								rowID.addView(rowIDLabel);
								
								final EditText rowIDEdit = new EditText(getApplicationContext());
								rowIDEdit.setWidth(500);
								rowIDEdit.setText(stops.busStopIDs.get(groupPosition));
								rowID.addView(rowIDEdit);
							
						items.addView(rowName);
						items.addView(rowID);
						
						alert.setView(items);

						alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							if (stops.busStopIDs == null){stops.busStopIDs = new ArrayList<String>(1);}//shouldn't happen here
							if (stops.busStopNames == null){stops.busStopNames = new ArrayList<String>(1);}
							stops.busStopNames.set(groupPosition ,rowNameEdit.getText().toString());
							stops.busStopIDs.set(groupPosition ,rowIDEdit.getText().toString());
						  }
						});
						
						alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
							  public void onClick(DialogInterface dialog, int whichButton) {
							    // Canceled.
							  }
							});
						
						alert.show();
					}
				});
				
				ImageButton btnDelete = new ImageButton(getApplicationContext());
				btnDelete.setImageDrawable((getResources().getDrawable(android.R.drawable.ic_menu_delete)));
				btnDelete.setFocusable(false);
				btnDelete.setMinimumHeight(96);
				btnDelete.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								stops.busStopIDs.remove(groupPosition);
								stops.busStopNames.remove(groupPosition);
								stops.busStopLocations.remove(groupPosition);
								stops.busStopEnabled.remove(groupPosition);
								stops.routeFilters.remove(groupPosition);
								elvStops.invalidate();
								elvStops.invalidateViews();
								elvStops.requestLayout();
								((BaseExpandableListAdapter) elaStops).notifyDataSetChanged();
								RestartService();
							}
						});
						
					}
				});
					
					
				ImageButton btnAdd = new ImageButton(getApplicationContext());
				btnAdd.setImageDrawable((getResources().getDrawable(android.R.drawable.ic_menu_add)));
				btnAdd.setFocusable(false);
				btnAdd.setMinimumHeight(40);
				btnAdd.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						addNewFilter(groupPosition); 
					}
				});
				llCol.setOrientation(LinearLayout.VERTICAL);
					
				llRow1.addView(tvStopsLabel);
				llRow1.addView(btnEdit);
				llRow1.addView(btnDelete);
				llRow1.addView(tvNoRouteLabel);
				llRow1.addView(btnAdd);
				
				llCol.addView(llRow1);
				llCol.addView(llRow2);
				
				TextView tvLblItems = new TextView(getApplicationContext());
				if (getChildrenCount(groupPosition) == 1){
					//no items
					tvLblItems.setText("No route filters for this stop.");
				}
				else{
					//some items
					tvLblItems.setText("Active route filters:");
				}
				llCol.addView(tvLblItems);
				return llCol;
			}
			else{
				final int id = childPosition - 1;
				
				
				ImageButton btnDelete = new ImageButton(getApplicationContext());
				btnDelete.setImageDrawable((getResources().getDrawable(android.R.drawable.ic_menu_delete)));
				btnDelete.setFocusable(false);
				btnDelete.setMinimumHeight(96);
				btnDelete.setOnClickListener(new OnClickListener() {
					
					@Override
					public void onClick(View v) {
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								stops.routeFilters.get(groupPosition).remove(id);
								elvStops.invalidate();
								elvStops.invalidateViews();
								elvStops.requestLayout();
								((BaseExpandableListAdapter) elaStops).notifyDataSetChanged();
								RestartService();
							}
						});
						
					}
				});
				
				TextView tvRoute = new TextView(getApplicationContext());
				tvRoute.setText("Route: " + stops.routeFilters.get(groupPosition).get(id));
				
				LinearLayout llRow = new LinearLayout(getApplicationContext());
				llRow.addView(tvRoute);
				llRow.addView(btnDelete);
				return llRow;
			}
			//return null;
		}
		
		@Override
		public long getChildId(int groupPosition, int childPosition) {
			return childPosition;
		}
		
		@Override
		public Object getChild(int groupPosition, int childPosition) {
			return null;
		}
	}
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button btnAddStop = (Button)findViewById(R.id.btnAdd);
        btnAddStop.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addNewStop();
			}
		});
        
        elvStops = (ExpandableListView)findViewById(R.id.elvStops);
        elaStops = new myExpandableListAdapter();
		stops = new StopData();
		stops.load(getSharedPreferences("busdata", MODE_MULTI_PROCESS));
        elvStops.setAdapter(elaStops);
        RestartService();
        
        final Button start = (Button)findViewById(R.id.btnStart);
        start.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stops.serviceEnabled = !stops.serviceEnabled;
				RestartService();
				if (stops.serviceEnabled){
					start.setText("Stop Service");
				}
				else{
					start.setText("Start Service");
				}
			}
		});
        
        if (stops.serviceEnabled){
			start.setText("Stop Service");
		}
		else{
			start.setText("Start Service");
		}
    }

    @Override
    protected void onStop(){
       super.onStop();
       saveStops();
    }

	private void saveStops() {
		stops.save(getSharedPreferences("busdata", MODE_MULTI_PROCESS));
	}
    
	private void RestartService() {
		Log.i("service", "restarting service");
		saveStops();
		Intent serviceStart = new Intent(getApplicationContext(), BusDataPushService.class);
        stopService(new Intent(getApplicationContext(),BusDataPushService.class));
        serviceStart = new Intent(getApplicationContext(), BusDataPushService.class);
        startService(serviceStart);
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
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
				rowName.addView(rowNameLabel);
				
				final EditText rowNameEdit = new EditText(getApplicationContext());
				rowNameEdit.setWidth(500);
				rowName.addView(rowNameEdit);
				
			final LinearLayout rowID = new LinearLayout(getApplicationContext());
			rowID.setOrientation(LinearLayout.HORIZONTAL);
				final TextView rowIDLabel = new TextView(getApplicationContext());
				rowIDLabel.setText("Number: ");
				rowIDLabel.setWidth(120);
				rowID.addView(rowIDLabel);
				
				final EditText rowIDEdit = new EditText(getApplicationContext());
				rowIDEdit.setWidth(500);
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
		((BaseExpandableListAdapter) elaStops).notifyDataSetChanged();
		RestartService();
		}
	}
	
	public void addNewStop(){
		
		Intent intent = new Intent(this, MapActivity.class);
		startActivityForResult(intent, 0);
		
	}

		public void addNewFilter(final int stop){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);

			alert.setTitle("Add a route filter");
			alert.setMessage("Enter the bus route number you want for this stop.");
			
			final EditText rowLocationEdit = new EditText(getApplicationContext());
			rowLocationEdit.setMinimumWidth(600);
			
			alert.setView(rowLocationEdit);

			alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				if (stops.routeFilters==null){
					stops.routeFilters = new ArrayList<ArrayList<String> >(1);
				}
				if (stops.routeFilters.get(stop)==null){
					stops.routeFilters.add(stop, new ArrayList<String>()); 
				}
				stops.routeFilters.get(stop).add(rowLocationEdit.getText().toString());
				
			  }
			});
		
		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    // Canceled.
		  }
		});
		
		alert.show();
		((BaseExpandableListAdapter) elaStops).notifyDataSetInvalidated();
		((BaseExpandableListAdapter) elaStops).notifyDataSetChanged();
		elvStops.invalidate();
		elvStops.invalidateViews();
		elvStops.requestLayout();
		RestartService();
	}
}