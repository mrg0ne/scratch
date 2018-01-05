package com.scratch.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.R;
import com.scratch.data.storage.IDataStorage;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;
import com.scratch.scheduler.TaskSchedulingService;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.widget.TextView;

public class TaskListActivity extends FragmentActivity {

	// For debug
	private Logger mLogger;
		
	TaskListAdapter mAdapter;
    ViewPager mViewPager;
    DataSetChangeReceiver mDataSetChangeReceiver;


	public TaskListActivity() {
		mLogger = Logger.getLogger(this.getClass().getName());
	}
	
    public void onCreate(Bundle savedInstanceState) {
    	mLogger.log(Level.INFO, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_pager_layout);
        
        // Start TaskSchedulingService
        startService(new Intent(TaskListActivity.this, TaskSchedulingService.class));
        
        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        mAdapter =
                new TaskListAdapter(
                        getSupportFragmentManager());
        mViewPager = (ViewPager)findViewById(R.id.pager);
        mViewPager.setAdapter(mAdapter);
        PagerTitleStrip strip = (PagerTitleStrip)findViewById(R.id.pager_title_strip);
        Typeface font = Typeface.createFromAsset(getAssets(), 
        		getString(R.string.default_font));
        
        for (int counter = 0 ; counter < strip.getChildCount(); counter++) {
            if (strip.getChildAt(counter) instanceof TextView) {
                ((TextView)strip.getChildAt(counter)).setTypeface(font);
            }
        }
        
        mDataSetChangeReceiver = new DataSetChangeReceiver();
        IntentFilter filter = new IntentFilter();
		filter.addAction(IDataStorage.DATASET_CHANGE);
		this.registerReceiver(mDataSetChangeReceiver, filter);
    }    
    
    /**
     * BroadcastReceiver for data base updates. Notify task lists
     * that the data has changed in the database.
     * 
     * @author ab
     *
     */
    private final class DataSetChangeReceiver extends BroadcastReceiver {

		public void onReceive(Context pContext, Intent pIntent) {
			mLogger.log(Level.INFO, "DataSetChangeReceiver onReceive called");
			mAdapter.notifyDataSetChanged();
		}
	}
}
