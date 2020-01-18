package com.scratch.gui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;

import com.scratch.R;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class NewTaskActivity extends AbstractTaskActivity {

	public static final String NEW_TASK = "com.scratch.intent.NEW_TASK";

	public NewTaskActivity() {
		super();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		mLogger.log(Level.INFO, "onCreate called:" + pSavedInstanceState);
		super.onCreate(pSavedInstanceState);

		if (mTask == null) {
			mTask = new Task();
		}

		updateViews();
		
		if (mTimeDue != null){
			// Set the default time due
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(mSettingsManager.getDefaultTimeDue());
			SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
			mTimeDue.setText(timeFormat.format(cal.getTime()));
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onSaveInstanceState(android.os.Bundle)
	 */
	@Override
	protected void onSaveInstanceState(Bundle pOutState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(pOutState);
	}

	protected void updateViews() {
		mLogger.log(Level.INFO, "updateViews called");
		
		setContentView(R.layout.edit_task_layout);

		mTitle = (TextView)findViewById(com.scratch.R.id.title);
		mTitle.setText(com.scratch.R.string.title_new_task);
		mName = (EditText)findViewById(com.scratch.R.id.edit_name);
		mName.setText(mTask.getName());
		mName.setTextColor(Color.BLACK);
		mDetails = (EditText)findViewById(com.scratch.R.id.edit_details);
		mDetails.setText(mTask.getDetails());
		mDetails.setTextColor(Color.BLACK);
		mDueDateButton = (Button)findViewById(com.scratch.R.id.due_date_button);				
		mDueDate = (TextView)findViewById(com.scratch.R.id.due_date_text);
		mDueDate.setTextColor(Color.BLACK);
		mCancelDueDateButton = (ImageButton)findViewById(
				com.scratch.R.id.cancel_due_date_button);

		mDoneButton = (Button)findViewById(com.scratch.R.id.done_button);
		
		// If a due date has been set, inflate the view stub
		if (mTask.getDueDate().getTime() != 0){
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);
			mLogger.log(Level.INFO, "Due date has been set to: " 
					+ mDueDate.getText() 
					+ ", inflating the recurrence and reminder views");
			((ViewStub)findViewById(R.id.cancel_due_date_button_stub)).inflate();
			mCancelDueDateButton = (ImageButton)findViewById(
					com.scratch.R.id.cancel_due_date_button);
			ViewStub stub = (ViewStub)findViewById(R.id.make_recurring_stub);

			if (stub != null) {
				mLogger.log(Level.INFO, "inflating make_recurring_stub");
				stub.inflate();
			} else {
				mLogger.log(Level.INFO, "not inflating make_recurring_stub" +
						" because it is null");
			}
			
			mDueDate.setText(dateFormat.format(mTask.getDueDate().getTime()));
			mTimeDueButton = (Button)findViewById(
					com.scratch.R.id.time_due_button);
			mTimeDue = (TextView)findViewById(
					com.scratch.R.id.time_due_text);
			mTimeDue.setTextColor(Color.BLACK);
			mTimeDue.setText(timeFormat.format(mTask.getDueDate().getTime()));
			mRecurrenceButton = (Button)findViewById(
					com.scratch.R.id.recurrence_button);
			mRecurrenceText = (TextView)findViewById(
					com.scratch.R.id.recurrence_text);
			mRecurrenceText.setTextColor(Color.BLACK);
			
			if (mRecurringTask == null) {
				mRecurrenceButton.setText(
						com.scratch.R.string.make_recurring_button);
				mRecurrenceText.setText(com.scratch.R.string.one_time);
			} else {
				mRecurrenceButton.setText(com.scratch.R.string.make_one_time_button);

				switch (mRecurringTask.getRecurrence().getRecurrenceType()) {
				case DAILY :
					mRecurrenceText.setText(com.scratch.R.string.daily);
					break;
				case WEEKLY :
					mRecurrenceText.setText(com.scratch.R.string.weekly);
					break;
				case MONTHLY :
					mRecurrenceText.setText(com.scratch.R.string.monthly);
					break;
				case YEARLY :
					mRecurrenceText.setText(com.scratch.R.string.yearly);
					break;
				default :
					mLogger.log(Level.WARNING, "Unknown recurrence type");
					mRecurrenceText.setText(com.scratch.R.string.unknown);
					break;
				}
			}
			
			mReminderDateButton = (Button)findViewById(
					com.scratch.R.id.reminder_date_button);
			mReminderDateText = (TextView)findViewById(
					com.scratch.R.id.reminder_date_text);
			mReminderDateText.setTextColor(Color.BLACK);
			mReminderTimeButton = (Button)findViewById(
					com.scratch.R.id.reminder_time_button);
			mReminderTimeText = (TextView)findViewById(
					com.scratch.R.id.reminder_time_text);
			mReminderTimeText.setTextColor(Color.BLACK);
			
			if (mTask.getReminderDate().getTime() != 0) {
				mReminderDateText.setText(dateFormat.format(
						mTask.getReminderDate()));
				((ViewStub)findViewById(
						com.scratch.R.id.cancel_reminder_date_button_stub)).inflate();
				mCancelReminderDateButton = (ImageButton)findViewById(
					com.scratch.R.id.cancel_reminder_date_button);
				mReminderTimeText.setText(timeFormat.format(
						mTask.getReminderDate()));
			}
			/*
			// Set the recurrence button label and text
			if (mRecurrenceText.getText().toString().isEmpty()){
				mRecurrenceButton.setText(
						com.scratch.R.string.make_recurring_button);
				mRecurrenceText.setText(com.scratch.R.string.one_time);
			} else if (!mRecurrenceText.getText().equals(
					com.scratch.R.string.one_time)) {
				mRecurrenceButton.setText(com.scratch.R.string.make_one_time_button);	        	
			}	        */
		}
	}

}
