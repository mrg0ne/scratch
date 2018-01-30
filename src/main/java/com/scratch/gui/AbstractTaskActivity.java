package com.scratch.gui;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.data.settings.AbstractSettingsManager;
import com.scratch.data.settings.SharedPreferencesSettingsManager;
import com.scratch.data.storage.DbStorage;
import com.scratch.data.storage.IDataStorage;
import com.scratch.data.types.Operation;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;
import com.scratch.scheduler.TaskSchedulingService;

import android.R;
import android.app.Activity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

public abstract class AbstractTaskActivity extends FragmentActivity {

	public static final String DUE_DATE_TAG = "DUE_DATE_TAG";

	public static final String TIME_DUE_TAG = "TIME_DUE_TAG";

	public static final String REMINDER_DATE_TAG = "REMINDER_DATE_TAG";

	public static final String REMINDER_TIME_TAG = "REMINDER_TIME_TAG";

	public static final String DATE_FORMAT = "MM-dd-yyyy";

	public static final String TIME_FORMAT = "h:mm a";

	/** Messenger for communicating with the service. */
	protected Messenger mService = null;

	/** Flag indicating whether we have called bind on the service. */
	protected boolean mBound;

	// For debug
	protected Logger mLogger;

	// Used to retrieve Task objects from the database
	protected static IDataStorage mDbStorage = null;

	protected static AbstractSettingsManager mSettingsManager = null;

	protected TextView mTitle;

	protected EditText mName;

	protected EditText mDetails;

	protected Button mDueDateButton;

	protected TextView mDueDate;
	
	protected ImageButton mCancelDueDateButton;

	protected Button mTimeDueButton;

	protected TextView mTimeDue;

	protected Button mRecurrenceButton;

	protected TextView mRecurrenceText;

	protected Button mReminderDateButton;

	protected TextView mReminderDateText;
	
	protected ImageButton mCancelReminderDateButton;

	protected Button mReminderTimeButton;

	protected TextView mReminderTimeText;

	protected Button mDoneButton;

	protected Task mTask = null;
	
	protected RecurringTask mRecurringTask = null;

	/**
	 * Class for interacting with the main interface of the service.
	 */
	protected ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName pClassName, IBinder pService) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service.  We are communicating with the
			// service using a Messenger, so here we get a client-side
			// representation of that from the raw IBinder object.
			mService = new Messenger(pService);
			mBound = true;
		}

		public void onServiceDisconnected(ComponentName pClassName) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			mBound = false;
		}
	};

	public AbstractTaskActivity() {
		super();
		mLogger = Logger.getLogger(this.getClass().getName());
		mBound = false;
		mTask = null;
		mRecurringTask = null;
	}

	// Callback when "Done" button is clicked
	public void doneButtonClicked(View pView){
		mLogger.log(Level.INFO, "Done button clicked");
		
		// Show a toast error message if the name was not set.
		if (mName.getText().toString() == null ||
				mName.getText().toString().isEmpty()) {
			Toast.makeText(getApplicationContext(), 
					com.scratch.R.string.invalid_name_toast, 
					Toast.LENGTH_SHORT).show();
			return;
		}
		
		mTask.setName(mName.getText().toString());
		mTask.setDetails(mDetails.getText().toString());
		
		if (!mDueDate.getText().toString().isEmpty()){
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

			try {
				Date date = dateFormat.parse((String) mDueDate.getText());
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int day = cal.get(Calendar.DAY_OF_MONTH);

				if (mTimeDue != null && !mTimeDue.getText().toString().isEmpty()) {
					mLogger.log(Level.INFO, "Time due has been set");
					date = timeFormat.parse((String)mTimeDue.getText());
					cal.setTime(date);
				} else {
					mLogger.log(Level.INFO, "No time due set, using default");
					cal.setTime(new Date(mSettingsManager.getDefaultTimeDue()));
				}

				int hour = cal.get(Calendar.HOUR_OF_DAY);
				int minute = cal.get(Calendar.MINUTE);
				int second = cal.get(Calendar.SECOND);
				cal.set(year, month, day, hour, minute, second);
				mTask.setDueDate(cal.getTime());
			} catch (ParseException pe) {
				mLogger.log(Level.WARNING, "Failed to parse Due Date string: " 
						+ mDueDate.getText() + " " + mTimeDue.getText());
				pe.printStackTrace();
			}
		}

		if (mReminderDateText != null && !mReminderDateText.getText().toString().isEmpty()){
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
			SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);

			try {
				Date date = dateFormat.parse((String) mReminderDateText.getText());
				Calendar cal = Calendar.getInstance();
				cal.setTime(date);
				int year = cal.get(Calendar.YEAR);
				int month = cal.get(Calendar.MONTH);
				int day = cal.get(Calendar.DAY_OF_MONTH);
				date = timeFormat.parse((String)mReminderTimeText.getText());
				cal.setTime(date);
				int hour = cal.get(Calendar.HOUR_OF_DAY);
				int minute = cal.get(Calendar.MINUTE);
				int second = cal.get(Calendar.SECOND);
				cal.set(year, month, day, hour, minute, second);
				mTask.setReminderDate(cal.getTime());
			} catch (ParseException pe) {
				mLogger.log(Level.WARNING, "Failed to parse Reminder Date string: " 
						+ mReminderDateText.getText() + " " + mReminderTimeText.getText());
				pe.printStackTrace();
			}
		}
				
		Intent intent = new Intent();

		// Put the task object in the intent to be returned
		//intent.putExtra(mTask.getClass().getName(), mTask);

		
		if (mRecurringTask != null){
			mRecurringTask.setName(mTask.getName());
			mRecurringTask.setDetails(mTask.getDetails());
			mRecurringTask.addTask(mTask);
			
			if (mDbStorage.save(mRecurringTask)){
				mLogger.log(Level.INFO, "RecurringTask saved");
				intent.putExtra(mRecurringTask.getClass().getName(), mRecurringTask);
				// TODO delete debug
				mLogger.log(Level.INFO, "doneButtonClicked " + mRecurringTask.getRecurrence().toString());
			} else {
				mLogger.log(Level.WARNING, "RecurringTask save failed");
			}
		} else if (mDbStorage.save(mTask)){
			mLogger.log(Level.INFO, "Task saved");
			intent.putExtra(mTask.getClass().getName(), mTask);
		} else {
			mLogger.log(Level.WARNING, "Task save failed");
		}

		if (getParent() == null) {
			setResult(Activity.RESULT_OK, intent);
		} else {
			getParent().setResult(Activity.RESULT_OK, intent);
		}
		
		
		// Send the new task to the TaskSchedulingService
		Message msg = Message.obtain();
		msg.obj = mTask;
		msg.arg1 = Operation.UPDATE.ordinal();

		try {
			mService.send(msg);
		} catch(RemoteException e) {
			mLogger.log(Level.WARNING, 
					"Failed to send the message to the TaskSchedulingService");
			e.printStackTrace();
		}

		finish();
	}

	// Callback when "Set Time Due" button is clicked
	public void timeDueButtonClicked(View pView){
		
		mTask.setName(mName.getText().toString());
		mTask.setDetails(mDetails.getText().toString());
		
		// Launch picker to select a time

		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);		
		hour++;
		int minute = cal.get(Calendar.MINUTE);

		if (minute < 30 && minute != 0) {
			minute = 30;
		} else if (minute > 30) {
			minute = 0;
			hour++;
		}

		// If time due is set, initialize the picker to the time that is set
		if (!mTimeDue.getText().toString().isEmpty()){			
			SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);
				Date date = mTask.getDueDate();
				cal.setTime(date);
				hour = cal.get(Calendar.HOUR_OF_DAY);
				minute = cal.get(Calendar.MINUTE);
		}

		TimePickerFragment timePicker = 
				TimePickerFragment.newInstance(hour, minute, 
						new TimeDueListener());
		timePicker.show(getFragmentManager(), AbstractTaskActivity.TIME_DUE_TAG);
	}

	// Callback when Recurrence button is clicked
	public void recurrenceButtonClicked(View pView){
		mLogger.log(Level.INFO, "Change Recurrence button clicked, " + 
				"launching MakeRecurringTaskActivity");

		mTask.setName(mName.getText().toString());
		mTask.setDetails(mDetails.getText().toString());
		
		Intent makeRecurringTaskIntent = 
				new Intent(pView.getContext(), MakeRecurringTaskActivity.class);

		if (mRecurringTask != null){
			mRecurringTask.setName(mTask.getName());
			mRecurringTask.setDetails(mTask.getDetails());
			mLogger.log(Level.INFO, "Putting Recurring Task : (" + 
			   mRecurringTask.getName() + ") in the Intent");
			makeRecurringTaskIntent.putExtra(RecurringTask.class.getName(), mRecurringTask);
		} else {
			mLogger.log(Level.INFO, "Putting Task : (" + mTask.getName() + 
					") in the Intent");
			makeRecurringTaskIntent.putExtra(mTask.getClass().getName(), mTask);
		}
		
		startActivityForResult(makeRecurringTaskIntent, 
				MakeRecurringTaskActivity.CHANGE_RECURRENCE);
	}

	// Callback when "Set Reminder Date" button is clicked
	public void reminderDateButtonClicked(View pView){
		// Launch picker to select a reminder date
		mTask.setName(mName.getText().toString());
		mTask.setDetails(mDetails.getText().toString());

		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);


		// If reminder date is set, initialize the picker to the date that is set
		if (!mReminderDateText.getText().toString().isEmpty() 
				&& !mReminderDateText.getText().equals("0")){			
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);

			try {
				Date date = dateFormat.parse((String) mReminderDateText.getText());
				cal.setTime(date);
				year = cal.get(Calendar.YEAR);
				month = cal.get(Calendar.MONTH);
				day = cal.get(Calendar.DAY_OF_MONTH);
			} catch (ParseException pe) {
				mLogger.log(Level.WARNING, "Failed to parse Reminder Date string: " 
						+ mReminderDateText.getText());
				pe.printStackTrace();
			}
		}

		DatePickerFragment datePicker = 
				DatePickerFragment.newInstance(year, month, day, 
						new ReminderDateListener() );
		datePicker.show(getFragmentManager(), AbstractTaskActivity.REMINDER_DATE_TAG);
	}

	// Callback when "Set Reminder Time" button is clicked
	public void reminderTimeButtonClicked(View pView){
		
		mTask.setName(mName.getText().toString());
		mTask.setDetails(mDetails.getText().toString());
		
		// Launch picker to select a time

		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);		
		int minute = cal.get(Calendar.MONTH);



		// If reminder time is set, initialize the picker to the time that is set
		if (!mReminderTimeText.getText().toString().isEmpty()){
			Date date = mTask.getReminderDate();

			cal.setTime(date);
			hour = cal.get(Calendar.HOUR_OF_DAY);
			minute = cal.get(Calendar.MINUTE);
		} else {
		    // Round minute to a half/hour and advance by one hour
            if (minute < 30 && minute != 0) {
                minute = 30;
            } else if (minute > 30) {
                minute = 0;
                hour++;
            }
        }

		TimePickerFragment timePicker = 
				TimePickerFragment.newInstance(hour, minute, 
						new ReminderTimeListener());
		timePicker.show(getFragmentManager(), AbstractTaskActivity.REMINDER_TIME_TAG);
	}

	// Callback when "Set Due Date" button is clicked
	public void dueDateButtonClicked(View pView){
		// Launch picker to select a due date

		mTask.setName(mName.getText().toString());
		mTask.setDetails(mDetails.getText().toString());
		
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);


		// If due date is set, initialize the picker to the date that is set
		if (!mDueDate.getText().toString().isEmpty() && 
				!mDueDate.getText().equals("0")){
			Date date = mTask.getDueDate();

			cal.setTime(date);
			year = cal.get(Calendar.YEAR);
			month = cal.get(Calendar.MONTH);
			day = cal.get(Calendar.DAY_OF_MONTH);
		}

		DatePickerFragment datePicker = 
				DatePickerFragment.newInstance(year, month, day, 
						new DateDueListener());
		datePicker.show(getFragmentManager(), AbstractTaskActivity.DUE_DATE_TAG);
	}

	public void cancelDueDateButtonClicked(View pView){
		mDueDate.setText("");
		mTimeDue.setText("");
		mTask.setDueDate(new Date(0));
		mRecurrenceText.setText(com.scratch.R.string.one_time);
		mReminderDateText.setText("");
		mReminderTimeText.setText("");
		mTask.setReminderDate(new Date(0));
		mTask.setName(mName.getText().toString());
		mTask.setDetails(mDetails.getText().toString());
		updateViews();
	}
	
	public void cancelReminderDateButtonClicked(View pView){
		mReminderDateText.setText("");
		mReminderTimeText.setText("");
		mTask.setReminderDate(new Date(0));
		mTask.setName(mName.getText().toString());
		mTask.setDetails(mDetails.getText().toString());
		updateViews();
	}
	
	public void onActivityResult(int pRequestCode, int pResultCode, Intent pData) {
		mLogger.log(Level.INFO, "onActivityResult called request code = " + pRequestCode);
		if (pResultCode != Activity.RESULT_OK){
			mLogger.log(Level.INFO, "result code = " + pResultCode + 
					", no change to task");
			return;
		}

		if (pRequestCode == MakeRecurringTaskActivity.CHANGE_RECURRENCE){
			if (pData.hasExtra(Task.class.getName())){
				mLogger.log(Level.INFO, "MakeRecurringTaskActivity returned Task object");
				mTask = pData.getParcelableExtra(Task.class.getName());				
			}
			
			if (pData.hasExtra(RecurringTask.class.getName())){
				mLogger.log(Level.INFO, "MakeRecurringTaskActivity returned RecurringTask object");
				mRecurringTask = (RecurringTask)pData.getParcelableExtra(
						RecurringTask.class.getName());
				mLogger.log(Level.INFO, "AbstractTaskActivity " + mRecurringTask.getRecurrence().toString());
			} 		
		}

		updateViews();
	}

	private class DateDueListener implements OnDateSetListener {
		// Call back when a DatePickerFragment is used
		public void onDateSet(DatePicker pView, int pYear, int pMonth, int pDay) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(mTask.getDueDate());
			cal.set(pYear, pMonth, pDay);
			mTask.setDueDate(cal.getTime());
			updateViews();
		}
	}

	private class ReminderDateListener implements OnDateSetListener {
		// Call back when a DatePickerFragment is used
		public void onDateSet(DatePicker pView, int pYear, int pMonth, int pDay) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(mTask.getReminderDate());
			cal.set(pYear, pMonth, pDay);
			mTask.setReminderDate(cal.getTime());
			updateViews();
		}
	}

	private class TimeDueListener implements OnTimeSetListener {
		// Call back when a TimePickerFragment is used
		public void onTimeSet(TimePicker pView, int pHourOfDay, int pMinute) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(mTask.getDueDate());
			cal.set(Calendar.HOUR_OF_DAY, pHourOfDay);
			cal.set(Calendar.MINUTE, pMinute);
			mTask.setDueDate(cal.getTime());/*
			SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);

			mTimeDue.setText(dateFormat.format(cal.getTime()));*/
			updateViews();
		}
	}

	private class ReminderTimeListener implements OnTimeSetListener {
		// Call back when a TimePickerFragment is used
		public void onTimeSet(TimePicker pView, int pHourOfDay, int pMinute) {
			Calendar cal = Calendar.getInstance();
			cal.setTime(mTask.getReminderDate());
			cal.set(Calendar.HOUR_OF_DAY, pHourOfDay);
			cal.set(Calendar.MINUTE, pMinute);
			mTask.setReminderDate(cal.getTime());/*
			SimpleDateFormat dateFormat = new SimpleDateFormat(TIME_FORMAT);

			mReminderTimeText.setText(dateFormat.format(cal.getTime()));*/
			updateViews();
		}
	}

	protected void onStart() {
		super.onStart();
		if (mDbStorage == null) {
			mDbStorage = new DbStorage(getApplicationContext());
		}

		if (mSettingsManager == null){
			mSettingsManager= new SharedPreferencesSettingsManager(
					getApplicationContext());
		}
		// Bind to the service
		bindService(new Intent(this, TaskSchedulingService.class), mConnection,
				Context.BIND_AUTO_CREATE);
	}

	@Override
	protected void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			unbindService(mConnection);
			mBound = false;
		}

		if (mDbStorage != null){
			mDbStorage.shutdown();
		}
	}	    

	protected abstract void updateViews(); 
}
