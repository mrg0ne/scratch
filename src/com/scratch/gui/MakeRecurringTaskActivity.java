package com.scratch.gui;

import java.util.logging.Level;
import java.util.logging.Logger;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import com.scratch.R;
import com.scratch.data.types.RecurrenceType;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;
import com.scratch.data.types.TaskRecurrence;

public class MakeRecurringTaskActivity extends Activity{

	public static final int CHANGE_RECURRENCE = 1;

	public static final int DAILY_RECURRENCE = 2;

	public static final int WEEKLY_RECURRENCE = 3;

	public static final int MONTHLY_RECURRENCE = 4;

	public static final int YEARLY_RECURRENCE = 5;

	RecurringTask mRecurringTask = null;

	RadioButton mOneTimeRadioButton;

	RadioButton mDailyRadioButton;

	RadioButton mWeeklyRadioButton;

	RadioButton mMonthlyRadioButton;

	RadioButton mYearlyRadioButton;

	// For debug
	protected Logger mLogger;

	public MakeRecurringTaskActivity() {
		super();
		mLogger = Logger.getLogger(this.getClass().getName());
		mRecurringTask = new RecurringTask();
	}

	// Callback when "Done" button is clicked
	public void doneButtonClicked(View pView){
		mLogger.log(Level.INFO, "Done button has been clicked");

		if(mOneTimeRadioButton.isChecked()){
			TaskRecurrence recurrence = new TaskRecurrence();
			recurrence.setRecurrenceType(RecurrenceType.NONE);
			mRecurringTask.setRecurrence(recurrence);

			Intent intent = new Intent();

			// Put the task object in the intent to be returned
			intent.putExtra(RecurringTask.class.getName(), mRecurringTask);

			if (getParent() == null) {
				setResult(Activity.RESULT_OK, intent);
			} else {
				getParent().setResult(Activity.RESULT_OK, intent);
			}

			finish();
		} else if (mDailyRadioButton.isChecked()) {
			Intent dailyIntent = new Intent(pView.getContext(), 
					MakeDailyRecurringTaskActivity.class);
			dailyIntent.putExtra(mRecurringTask.getClass().getName(), mRecurringTask);
			startActivityForResult(dailyIntent, DAILY_RECURRENCE);
		} else if (mWeeklyRadioButton.isChecked()){
			Intent weeklyIntent = new Intent(pView.getContext(), 
					MakeWeeklyRecurringTaskActivity.class);
			weeklyIntent.putExtra(RecurringTask.class.getName(), mRecurringTask);
			startActivityForResult(weeklyIntent, WEEKLY_RECURRENCE);
		} else if (mMonthlyRadioButton.isChecked()){
			Intent monthlyIntent = new Intent(pView.getContext(), 
					MakeMonthlyRecurringTaskActivity.class);
			monthlyIntent.putExtra(mRecurringTask.getClass().getName(), mRecurringTask);
			startActivityForResult(monthlyIntent, MONTHLY_RECURRENCE);
		} else if (mYearlyRadioButton.isChecked()){
			Intent yearlyIntent = new Intent(pView.getContext(), 
					MakeYearlyRecurringTaskActivity.class);
			yearlyIntent.putExtra(mRecurringTask.getClass().getName(), mRecurringTask);
			startActivityForResult(yearlyIntent, YEARLY_RECURRENCE);
		} else {
			mLogger.log(Level.WARNING, "No radio buttons are selected");
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
	 */
	@Override
	protected void onActivityResult(int pRequestCode, int pResultCode, Intent pData) {
		super.onActivityResult(pRequestCode, pResultCode, pData);
		if (pResultCode != Activity.RESULT_OK){
			mLogger.log(Level.INFO, "bad result, result code = " + pResultCode);
			return;
		}

		if (pData.hasExtra(RecurringTask.class.getName())){
			mRecurringTask = (RecurringTask)pData.getParcelableExtra(
					RecurringTask.class.getName());
		} else {
			mLogger.log(Level.WARNING, "Unknown data in onActivityResult");
			return;
		}			

		Intent intent = new Intent();

		// Put the task object in the intent to be returned
		intent.putExtra(mRecurringTask.getClass().getName(), mRecurringTask);

		if (getParent() == null) {
			setResult(Activity.RESULT_OK, intent);
		} else {
			getParent().setResult(Activity.RESULT_OK, intent);
		}

		finish();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		// Get the Task object from the intent
		Intent intent = getIntent();

		if (intent.hasExtra(RecurringTask.class.getName())){
			mRecurringTask = intent.getParcelableExtra(RecurringTask.class.getName());
		} else if (intent.hasExtra(Task.class.getName())) {
			mRecurringTask = new RecurringTask(
					(Task)intent.getParcelableExtra(Task.class.getName()));		
		} else {
			mLogger.log(Level.WARNING, "Intent does not contain " 
					+ Task.class.getName() + " or " 
					+ RecurringTask.class.getName() + " keys.");
		}

		setContentView(R.layout.make_recurring_task_layout);

		mOneTimeRadioButton = (RadioButton)findViewById(
				com.scratch.R.id.radiobutton_one_time_task);
		mDailyRadioButton = (RadioButton)findViewById(
				com.scratch.R.id.radiobutton_daily_make_recurring_task);
		mWeeklyRadioButton = (RadioButton)findViewById(
				com.scratch.R.id.radiobutton_weekly_make_recurring_task);
		mMonthlyRadioButton = (RadioButton)findViewById(
				com.scratch.R.id.radiobutton_monthly_make_recurring_task);
		mYearlyRadioButton = (RadioButton)findViewById(
				com.scratch.R.id.radiobutton_yearly_make_recurring_task);

		mOneTimeRadioButton.setChecked(false);
		mDailyRadioButton.setChecked(false);
		mWeeklyRadioButton.setChecked(false);
		mMonthlyRadioButton.setChecked(false);
		mYearlyRadioButton.setChecked(false);

		if (mRecurringTask.getRecurrence().getRecurrenceType() 
				== RecurrenceType.NONE){
			mOneTimeRadioButton.setChecked(true);
		} else if (mRecurringTask.getRecurrence().getRecurrenceType() 
				== RecurrenceType.DAILY){
			mDailyRadioButton.setChecked(true);
		} else if (mRecurringTask.getRecurrence().getRecurrenceType() 
				== RecurrenceType.WEEKLY){
			mWeeklyRadioButton.setChecked(true);
		} else if (mRecurringTask.getRecurrence().getRecurrenceType() 
				== RecurrenceType.MONTHLY){
			mMonthlyRadioButton.setChecked(true);
		} else if (mRecurringTask.getRecurrence().getRecurrenceType() 
				== RecurrenceType.YEARLY){
			mYearlyRadioButton.setChecked(true);
		} else {
			mLogger.log(Level.WARNING, "Unknown recurrence type, " + 
					"radio buttons not set");
		}		
	}

	public void onRadioButtonClicked(View pView) {
		mOneTimeRadioButton.setChecked(false);
		mDailyRadioButton.setChecked(false);
		mWeeklyRadioButton.setChecked(false);
		mMonthlyRadioButton.setChecked(false);
		mYearlyRadioButton.setChecked(false);

		switch (pView.getId()){
		case com.scratch.R.id.radiobutton_one_time_task:
			mLogger.log(Level.INFO, "One-Time radio button checked");
			mOneTimeRadioButton.setChecked(true);
			break;

		case com.scratch.R.id.radiobutton_daily_make_recurring_task:
			mLogger.log(Level.INFO, "Daily radio button checked");
			mDailyRadioButton.setChecked(true);
			break;

		case com.scratch.R.id.radiobutton_weekly_make_recurring_task:
			mLogger.log(Level.INFO, "Weekly radio button checked");
			mWeeklyRadioButton.setChecked(true);
			break;

		case com.scratch.R.id.radiobutton_monthly_make_recurring_task:
			mLogger.log(Level.INFO, "Monthly radio button checked");
			mMonthlyRadioButton.setChecked(true);
			break;

		case com.scratch.R.id.radiobutton_yearly_make_recurring_task:
			mYearlyRadioButton.setChecked(true);
			break;

		default:
			mLogger.log(Level.WARNING, "onRadioButton called with unknown view ID: " 
					+ pView.getId());
			break;
		}
	}

}
