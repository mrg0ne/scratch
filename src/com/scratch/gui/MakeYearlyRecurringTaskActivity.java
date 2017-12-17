package com.scratch.gui;

import java.util.Calendar;
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
import android.widget.DatePicker;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.TimePicker;

public class MakeYearlyRecurringTaskActivity extends Activity {

	RecurringTask mTask = null;

	NumberPicker mRegularityPicker;

	RadioButton mDayRadioButton;

	DatePicker mDayDatePicker;

	RadioButton mWeekNumRadioButton;

	NumberPicker mWeekNumPicker;

	NumberPicker mWeekDayPicker;

	NumberPicker mMonthPicker;

	TimePicker mTimeDuePicker;

	TextView mDayText;

	TextView mTheText;

	TextView mOf;

	// For debug
	protected static final Logger mLogger = Logger.getLogger(MakeYearlyRecurringTaskActivity.class.getName());

	public MakeYearlyRecurringTaskActivity() {
		super();
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

		setContentView(R.layout.make_yearly_recurring_task_layout);

		mRegularityPicker = (NumberPicker)findViewById(com.scratch.R.id.regularity);
		mRegularityPicker.setMinValue(1);
		mRegularityPicker.setMaxValue(100);
		mRegularityPicker.setValue(mTask.getRecurrence().getOccurenceRegularity());

		mDayRadioButton = (RadioButton)findViewById(
				com.scratch.R.id.day_picker_radiobutton);

		mDayText = (TextView)findViewById(com.scratch.R.id.day);

		mDayDatePicker = (DatePicker)findViewById(
				com.scratch.R.id.day_date_picker);	
		Date dueDate = mTask.getDueDate();
		Calendar cal = Calendar.getInstance();
		
		// If a due date hasn't been set, initialize to today
		if (dueDate.getTime() == 0){
			mDayDatePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		} else {
			// Otherwise, use the date from the task
			cal.setTime(dueDate);
			mDayDatePicker.updateDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
		}

		mWeekNumRadioButton = (RadioButton)findViewById(
				com.scratch.R.id.weeknum_picker_radiobutton);

		mTheText = (TextView)findViewById(com.scratch.R.id.the);

		mWeekNumPicker = (NumberPicker)findViewById(
				com.scratch.R.id.weeknum_picker);
		mWeekNumPicker.setMinValue(0);
		mWeekNumPicker.setMaxValue(4);
		mWeekNumPicker.setDisplayedValues(new String[]{
				getString(com.scratch.R.string.first),
				getString(com.scratch.R.string.second),
				getString(com.scratch.R.string.third),
				getString(com.scratch.R.string.fourth),
				getString(com.scratch.R.string.last)});
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

		mOf = (TextView)findViewById(com.scratch.R.id.of);

		mMonthPicker = (NumberPicker)findViewById(com.scratch.R.id.month_picker);
		mMonthPicker.setMinValue(0);
		mMonthPicker.setMaxValue(11);
		mMonthPicker.setDisplayedValues(new String[]{
				getString(com.scratch.R.string.january),
				getString(com.scratch.R.string.february),
				getString(com.scratch.R.string.march),
				getString(com.scratch.R.string.april),
				getString(com.scratch.R.string.may),
				getString(com.scratch.R.string.june),
				getString(com.scratch.R.string.july),
				getString(com.scratch.R.string.august),
				getString(com.scratch.R.string.september),
				getString(com.scratch.R.string.october),
				getString(com.scratch.R.string.november),
				getString(com.scratch.R.string.december)});

		mMonthPicker.setValue(cal.get(Calendar.MONTH));

		TaskRecurrence recurrence = mTask.getRecurrence();

		mTimeDuePicker = (TimePicker)findViewById(com.scratch.R.id.time_due);

		mTimeDuePicker.setCurrentHour(dueDate.getHours());
		mTimeDuePicker.setCurrentMinute(dueDate.getMinutes());

		if (recurrence.isUseDay()){
			mDayRadioButton.setChecked(true);
			mDayText.setEnabled(true);
			mDayDatePicker.setEnabled(true);
			mWeekNumRadioButton.setChecked(false);
			mTheText.setEnabled(false);
			mWeekNumPicker.setEnabled(false);
			mWeekDayPicker.setEnabled(false);
			mOf.setEnabled(false);
			mMonthPicker.setEnabled(false);
		} else {
			mDayRadioButton.setChecked(false);
			mDayText.setEnabled(false);
			mDayDatePicker.setEnabled(false);
			mWeekNumRadioButton.setChecked(true);
			mTheText.setEnabled(true);
			mWeekNumPicker.setEnabled(true);
			mWeekDayPicker.setEnabled(true);
			mOf.setEnabled(true);
			mMonthPicker.setEnabled(true);
		}
	}

	public void onRadioButtonClicked(View pView) {

		switch (pView.getId()){
		case com.scratch.R.id.day_picker_radiobutton:
			mLogger.log(Level.INFO, "Day Picker radio button checked");
			mDayRadioButton.setChecked(true);
			mDayText.setEnabled(true);
			mDayDatePicker.setEnabled(true);
			mWeekNumRadioButton.setChecked(false);
			mTheText.setEnabled(false);
			mWeekNumPicker.setEnabled(false);
			mWeekDayPicker.setEnabled(false);
			mOf.setEnabled(false);
			mMonthPicker.setEnabled(false);
			break;

		case com.scratch.R.id.weeknum_picker_radiobutton:
			mLogger.log(Level.INFO, "Week number radio button checked");
			mDayRadioButton.setChecked(false);
			mDayText.setEnabled(false);
			mDayDatePicker.setEnabled(false);
			mWeekNumRadioButton.setChecked(true);
			mTheText.setEnabled(true);
			mWeekNumPicker.setEnabled(true);
			mWeekDayPicker.setEnabled(true);
			mOf.setEnabled(true);
			mMonthPicker.setEnabled(true);
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
		recurrence.setRecurrenceType(RecurrenceType.YEARLY);
		recurrence.setOccurenceRegularity(mRegularityPicker.getValue());

		if (mDayRadioButton.isChecked()) {
			recurrence.setUseDay(true);
			Calendar cal = Calendar.getInstance();
			cal.set(Calendar.YEAR, mDayDatePicker.getYear());
			cal.set(Calendar.MONTH, mDayDatePicker.getMonth());
			cal.set(Calendar.DAY_OF_MONTH, mDayDatePicker.getDayOfMonth());
			Date dueDate = cal.getTime();
			mTask.setDueDate(dueDate);
			recurrence.setMonth(mDayDatePicker.getMonth());
			recurrence.setDayOfMonth(mDayDatePicker.getDayOfMonth());			
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

			recurrence.setMonth(mDayDatePicker.getMonth());
		} else {
			mLogger.log(Level.WARNING, "No radio buttons have been checked");
		}

		recurrence.setTimeOfDay((mTimeDuePicker.getCurrentMinute().intValue()*1000) 
				+ (mTimeDuePicker.getCurrentHour()*60000));
		Date dueDate = mTask.getDueDate();
		dueDate.setTime(dueDate.getTime()+recurrence.getTimeOfDay());
		mTask.setDueDate(dueDate);

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
