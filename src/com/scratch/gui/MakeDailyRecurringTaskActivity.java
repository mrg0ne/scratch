package com.scratch.gui;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.R;
import com.scratch.data.types.RecurrenceType;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;
import com.scratch.data.types.TaskRecurrence;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TimePicker;

public class MakeDailyRecurringTaskActivity extends Activity {

	RecurringTask mTask = null;

	CheckBox mMondayCheckBox;

	CheckBox mTuesdayCheckBox;

	CheckBox mWednesdayCheckBox;

	CheckBox mThursdayCheckBox;

	CheckBox mFridayCheckBox;

	CheckBox mSaturdayCheckBox;

	CheckBox mSundayCheckBox;

	TimePicker mTimeDuePicker;
	
	// For debug
	protected Logger mLogger;

	public MakeDailyRecurringTaskActivity() {
		super();
		mLogger = Logger.getLogger(this.getClass().getName());
		mTask = new RecurringTask();
	}
	
	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Get the Task object from the intent
		Intent intent = getIntent();

		if (intent.hasExtra(Task.class.getName())) {
			mTask = new RecurringTask((Task)(intent.getParcelableExtra(
					Task.class.getName())));
		} else if (intent.hasExtra(RecurringTask.class.getName())){
			mTask = intent.getParcelableExtra(RecurringTask.class.getName());
		} else {
			mTask = new RecurringTask();
			mLogger.log(Level.WARNING, "Intent does not contain " 
					+ Task.class.getName() + " or " 
					+ RecurringTask.class.getName() + " keys.");
		}

		setContentView(R.layout.make_daily_recurring_task_layout);
		
		mMondayCheckBox = (CheckBox)findViewById(com.scratch.R.id.checkbox_monday);
		mTuesdayCheckBox = (CheckBox)findViewById(com.scratch.R.id.checkbox_tuesday);
		mWednesdayCheckBox = (CheckBox)findViewById(com.scratch.R.id.checkbox_wednesday);
		mThursdayCheckBox = (CheckBox)findViewById(com.scratch.R.id.checkbox_thursday);
		mFridayCheckBox = (CheckBox)findViewById(com.scratch.R.id.checkbox_friday);
		mSaturdayCheckBox = (CheckBox)findViewById(com.scratch.R.id.checkbox_saturday);
		mSundayCheckBox = (CheckBox)findViewById(com.scratch.R.id.checkbox_sunday);
		
		TaskRecurrence recurrence = mTask.getRecurrence();
		mMondayCheckBox.setChecked(recurrence.isOnMonday());
		mTuesdayCheckBox.setChecked(recurrence.isOnTuesday());
		mWednesdayCheckBox.setChecked(recurrence.isOnWednesday());
		mThursdayCheckBox.setChecked(recurrence.isOnThursday());
		mFridayCheckBox.setChecked(recurrence.isOnFriday());
		mSaturdayCheckBox.setChecked(recurrence.isOnSaturday());
		mSundayCheckBox.setChecked(recurrence.isOnSunday());
		
		mTimeDuePicker = (TimePicker)findViewById(com.scratch.R.id.time_due);
		
		Date dueDate = mTask.getDueDate();
		mTimeDuePicker.setCurrentHour(dueDate.getHours());
		mTimeDuePicker.setCurrentMinute(dueDate.getMinutes());
	}

	// Callback when "Done" button is clicked
	public void doneButtonClicked(View pView){
		mLogger.log(Level.INFO, "Done button has been clicked");
		
		TaskRecurrence recurrence = new TaskRecurrence();
		recurrence.setRecurrenceType(RecurrenceType.DAILY);
		recurrence.setOnMonday(mMondayCheckBox.isChecked());
		recurrence.setOnTuesday(mTuesdayCheckBox.isChecked());
		recurrence.setOnWednesday(mWednesdayCheckBox.isChecked());
		recurrence.setOnThursday(mThursdayCheckBox.isChecked());
		recurrence.setOnFriday(mFridayCheckBox.isChecked());
		recurrence.setOnSaturday(mSaturdayCheckBox.isChecked());
		recurrence.setOnSunday(mSundayCheckBox.isChecked());
		recurrence.setTimeOfDay((mTimeDuePicker.getCurrentMinute().intValue()*1000) 
				+ (mTimeDuePicker.getCurrentHour()*60000));
		
		mTask.setRecurrence(recurrence);		
		mLogger.log(Level.INFO, mTask.getRecurrence().toString());
		Intent intent = new Intent();

		// Put the task object in the intent to be returned
		intent.putExtra(mTask.getClass().getName(), mTask);

		if (getParent() == null) {
			setResult(Activity.RESULT_OK, intent);
		} else {
			getParent().setResult(Activity.RESULT_OK, intent);
		}

		finish();
	}


}
