package com.scratch.gui;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.R;
import com.scratch.data.types.RecurrenceType;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;
import com.scratch.data.types.TaskRecurrence;
import com.scratch.data.types.TaskRecurrence.WeekNumber;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

public class MakeMonthlyRecurringTaskActivity extends Activity {

    RecurringTask mTask = null;
	
	NumberPicker mRegularityPicker;
	
	RadioButton mDayRadioButton;
	
	NumberPicker mDayNumberPicker;
	
	RadioButton mWeekNumRadioButton;
	
	NumberPicker mWeekNumPicker;
	
	NumberPicker mWeekDayPicker;
	
    TimePicker mTimeDuePicker;
    
    TextView mDayText;
    
    TextView mTheText;
    
    TextView mOfTheMonthText;
	
	// For debug
	protected Logger mLogger;
	
	public MakeMonthlyRecurringTaskActivity() {
		super();
		mLogger = Logger.getLogger(this.getClass().getName());
		mTask = new RecurringTask();
	}

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

		setContentView(R.layout.make_monthly_recurring_task_layout);
		
		mRegularityPicker = (NumberPicker)findViewById(com.scratch.R.id.regularity);
		mRegularityPicker.setMinValue(1);
		mRegularityPicker.setMaxValue(12);
		mRegularityPicker.setValue(mTask.getRecurrence().getOccurenceRegularity());
		
		mDayRadioButton = (RadioButton)findViewById(
				com.scratch.R.id.day_picker_radiobutton);
		
		mDayText = (TextView)findViewById(com.scratch.R.id.day);
	
		mDayNumberPicker = (NumberPicker)findViewById(
				com.scratch.R.id.day_number_picker);		
		mDayNumberPicker.setMinValue(1);
		mDayNumberPicker.setMaxValue(31);
		mDayNumberPicker.setValue(mTask.getRecurrence().getDayOfMonth());
		
		mWeekNumRadioButton = (RadioButton)findViewById(
				com.scratch.R.id.weeknum_picker_radiobutton);
		
		mTheText = (TextView)findViewById(com.scratch.R.id.the);
		
		mWeekNumPicker = (NumberPicker)findViewById(
				com.scratch.R.id.weeknum_picker);
		mWeekNumPicker.setDisplayedValues(new String[]{
				getString(com.scratch.R.string.first),
				getString(com.scratch.R.string.second),
				getString(com.scratch.R.string.third),
				getString(com.scratch.R.string.fourth),
				getString(com.scratch.R.string.last)});
		mWeekNumPicker.setMinValue(0);
		mWeekNumPicker.setMaxValue(4);
		mWeekNumPicker.setValue(mTask.getRecurrence().getWeekNum().ordinal());
		
		mWeekDayPicker = (NumberPicker)findViewById(
				com.scratch.R.id.weekday_picker);
		mWeekDayPicker.setMinValue(0);
		mWeekDayPicker.setMaxValue(6);
		mWeekDayPicker.setDisplayedValues(new String[]{
				getString(com.scratch.R.string.monday),
				getString(com.scratch.R.string.tuesday),
				getString(com.scratch.R.string.wednesday),
				getString(com.scratch.R.string.thursday),
				getString(com.scratch.R.string.friday),
				getString(com.scratch.R.string.saturday),
				getString(com.scratch.R.string.sunday)});
		
		if (mTask.getRecurrence().isOnMonday()) {
			mWeekDayPicker.setValue(0);
		} else if (mTask.getRecurrence().isOnTuesday()) {
			mWeekDayPicker.setValue(1);
		} else if (mTask.getRecurrence().isOnWednesday()) {
			mWeekDayPicker.setValue(2);
		} else if (mTask.getRecurrence().isOnThursday()) {
			mWeekDayPicker.setValue(3);
		} else if (mTask.getRecurrence().isOnFriday()) {
			mWeekDayPicker.setValue(4);
		} else if (mTask.getRecurrence().isOnSaturday()) {
			mWeekDayPicker.setValue(5);
		} else if (mTask.getRecurrence().isOnSunday()) {
			mWeekDayPicker.setValue(6);
		} else {
			mWeekDayPicker.setValue(0);
		}
		
		mOfTheMonthText = (TextView)findViewById(com.scratch.R.id.of_the_month);
		
		TaskRecurrence recurrence = mTask.getRecurrence();
		
		mTimeDuePicker = (TimePicker)findViewById(com.scratch.R.id.time_due);
		
		Date dueDate = mTask.getDueDate();
		mTimeDuePicker.setCurrentHour(dueDate.getHours());
		mTimeDuePicker.setCurrentMinute(dueDate.getMinutes());
		
		if (recurrence.isUseDay()){
			mDayRadioButton.setChecked(true);
			mDayText.setEnabled(true);
			mDayNumberPicker.setEnabled(true);
			mWeekNumRadioButton.setChecked(false);
			mTheText.setEnabled(false);
			mWeekNumPicker.setEnabled(false);
			mWeekDayPicker.setEnabled(false);
			mOfTheMonthText.setEnabled(false);
		} else {
			mDayRadioButton.setChecked(false);
			mDayText.setEnabled(false);
			mDayNumberPicker.setEnabled(false);
			mWeekNumRadioButton.setChecked(true);
			mTheText.setEnabled(true);
			mWeekNumPicker.setEnabled(true);
			mWeekDayPicker.setEnabled(true);
			mOfTheMonthText.setEnabled(true);
		}
	}
	
	public void onRadioButtonClicked(View pView) {
		
		switch (pView.getId()){
		case com.scratch.R.id.day_picker_radiobutton:
			mLogger.log(Level.INFO, "Day Picker radio button checked");
			mDayRadioButton.setChecked(true);
			mDayText.setEnabled(true);
			mDayNumberPicker.setEnabled(true);
			mWeekNumRadioButton.setChecked(false);
			mTheText.setEnabled(false);
			mWeekNumPicker.setEnabled(false);
			mWeekDayPicker.setEnabled(false);
			mOfTheMonthText.setEnabled(false);
			break;
			
		case com.scratch.R.id.weeknum_picker_radiobutton:
			mLogger.log(Level.INFO, "Week number radio button checked");
			mDayRadioButton.setChecked(false);
			mDayText.setEnabled(false);
			mDayNumberPicker.setEnabled(false);
			mWeekNumRadioButton.setChecked(true);
			mTheText.setEnabled(true);
			mWeekNumPicker.setEnabled(true);
			mWeekDayPicker.setEnabled(true);
			mOfTheMonthText.setEnabled(true);
			break;
			
		default:
			mLogger.log(Level.WARNING, "onRadioButton called with unknown view ID: " 
		       + pView.getId());
			break;
		}
	}

	// Callback when "Done" button is clicked
	public void doneButtonClicked(View pView){
		mLogger.log(Level.INFO, "Done button has been clicked");
		
		TaskRecurrence recurrence = new TaskRecurrence();
		recurrence.setRecurrenceType(RecurrenceType.MONTHLY);
		recurrence.setOccurenceRegularity(mRegularityPicker.getValue());
		
		if (mDayRadioButton.isChecked()) {
			recurrence.setUseDay(true);
			recurrence.setDayOfMonth(mDayNumberPicker.getValue());			
		} else if (mWeekNumRadioButton.isChecked()) {
			recurrence.setUseDay(false);
			recurrence.setWeekNum(WeekNumber.values()[mWeekNumPicker.getValue()]);
			mLogger.log(Level.INFO, mWeekDayPicker.getDisplayedValues()
					[mWeekDayPicker.getValue()] + 
					" selected from the Weekday picker");
			
			switch (mWeekDayPicker.getValue()) {
			case 0:
				recurrence.setOnMonday(true);
				break;
			case 1:
				recurrence.setOnTuesday(true);
				break;
			case 2:
				recurrence.setOnWednesday(true);
				break;
			case 3:
				recurrence.setOnThursday(true);
				break;
			case 4:
				recurrence.setOnFriday(true);
				break;
			case 5:
				recurrence.setOnSaturday(true);
				break;
			case 6:
				recurrence.setOnSunday(true);
				break;
			default:
				mLogger.log(Level.WARNING, 
						"Unknown day selected from the Weekday picker");
					break;
			}
		} else {
			mLogger.log(Level.WARNING, "No radio buttons have been checked");
		}
		
		recurrence.setTimeOfDay((mTimeDuePicker.getCurrentMinute().intValue()*1000) 
				+ (mTimeDuePicker.getCurrentHour()*60000));
		
		mTask.setRecurrence(recurrence);		
		
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
