package com.scratch.gui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Level;

import com.scratch.R;
import com.scratch.data.types.Operation;
import com.scratch.data.types.RecurrenceType;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;
import com.scratch.data.types.TaskRecurrence;

import android.app.Activity;
import android.app.AlertDialog;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.os.RemoteException;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewStub;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class EditTaskActivity extends AbstractTaskActivity 
implements OnClickListener{

	public static final String EDIT_TASK = "com.scratch.intent.EDIT_TASK";	

	public static final String DELETE_CONFIRMATION_TAG = "DELETE_CONFIRMATION_TAG";

	public EditTaskActivity() {		
		super();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu pMenu) {
		return super.onCreateOptionsMenu(pMenu);
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem pItem) {
		// Handle presses on the action bar items
		switch (pItem.getItemId()) {
		case R.id.action_delete:
			String title = getResources().getString(
					com.scratch.R.string.delete_confirmation_message);
			title += " " + mTask.getName();
			ConfirmationDialogFragment confirmDlg = 
			ConfirmationDialogFragment.newInstance(
					title);
			confirmDlg.show(this.getSupportFragmentManager(), DELETE_CONFIRMATION_TAG);
			return true;
		default:
			return super.onOptionsItemSelected(pItem);
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);

		// Get the Task objects from the intent
		Intent intent = getIntent();

		
		if (intent.hasExtra(RecurringTask.class.getName())){
			mRecurringTask = intent.getParcelableExtra(
					RecurringTask.class.getName());
			mLogger.log(Level.INFO, "Recurring Task found in Intent : " + 
					mRecurringTask.getName());
			mLogger.log(Level.INFO, mRecurringTask.getRecurrence().toString());
		}
		
		if (intent.hasExtra(Task.class.getName())) {
			mTask = intent.getParcelableExtra(Task.class.getName());
			mLogger.log(Level.INFO, "Task found in Intent : " + 
					mTask.getName());
		} else {
			mLogger.log(Level.SEVERE, "Intent does not contain a " 
					+ Task.class.getName() + " key.");
		}
		
		updateViews();
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
		mTitle.setText(com.scratch.R.string.title_edit_task);
		mName = (EditText)findViewById(com.scratch.R.id.edit_name);
		mName.setText(mTask.getName());
		mDetails = (EditText)findViewById(com.scratch.R.id.edit_details);
		mDetails.setText(mTask.getDetails());
		mDueDateButton = (Button)findViewById(com.scratch.R.id.due_date_button);				
		mDueDate = (TextView)findViewById(com.scratch.R.id.due_date_text);		
		mDoneButton = (Button)findViewById(com.scratch.R.id.done_button);
		
		// If a due date has been set, inflate the view stubs
		if (mTask.getDueDate().getTime() != 0){
			Calendar cal = Calendar.getInstance();
			cal.setTime(mTask.getDueDate());
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);				
			mDueDate.setText(dateFormat.format(cal.getTime()));
			mLogger.log(Level.INFO, "Due date has been set to: " 
					+ mDueDate.getText() 
					+ ", inflating the recurrence and reminder views");

			((ViewStub) findViewById(R.id.cancel_due_date_button_stub)).inflate();
			mCancelDueDateButton = (ImageButton)findViewById(
					com.scratch.R.id.cancel_due_date_button);
			((ViewStub) findViewById(R.id.make_recurring_stub)).inflate();
			mTimeDueButton = (Button)findViewById(
					com.scratch.R.id.time_due_button);
			mTimeDue = (TextView)findViewById(
					com.scratch.R.id.time_due_text);

			SimpleDateFormat timeFormat = new SimpleDateFormat(TIME_FORMAT);			
			mTimeDue.setText(timeFormat.format(cal.getTime()));
			mRecurrenceButton = (Button)findViewById(
					com.scratch.R.id.recurrence_button);
			mRecurrenceText = (TextView)findViewById(
					com.scratch.R.id.recurrence_text);

			// If recurring task is null or the recurrence type is none,
			// display a "Make Recurring Task" button.
			// Otherwise, display a "Make One-Time Task" button.
			if (mRecurringTask == null || 
					mRecurringTask.getRecurrence().getRecurrenceType() 
					== RecurrenceType.NONE) {
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
				case NONE :
					mRecurrenceText.setText(com.scratch.R.string.one_time);
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
			
			mReminderTimeButton = (Button)findViewById(
					com.scratch.R.id.reminder_time_button);		
			mReminderTimeText = (TextView)findViewById(
					com.scratch.R.id.reminder_time_text);

			if (mTask.getReminderDate().getTime() != 0){
				cal.setTime(mTask.getReminderDate());
				mReminderDateText.setText(dateFormat.format(cal.getTime()));
				
				((ViewStub) findViewById(R.id.cancel_reminder_date_button_stub)).inflate();
				mCancelReminderDateButton = (ImageButton)findViewById(
						com.scratch.R.id.cancel_reminder_date_button);
				
				mReminderTimeText.setText(timeFormat.format(cal.getTime()));
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#startActivity(android.content.Intent)
	 */
	@Override
	public void startActivity(Intent pIntent) {
		super.startActivity(pIntent);
	}

	private void deleteTask(){
		Intent intent = new Intent();

		// Put task object in the intent to be returned
		intent.putExtra(mTask.getClass().getName(), mTask);

		if (getParent() == null) {
			setResult(Activity.RESULT_OK, intent);
		} else {
			getParent().setResult(Activity.RESULT_OK, intent);
		}

		if (mDbStorage.delete(mTask)){
			mLogger.log(Level.INFO, "Task deleted");
		} else {
			mLogger.log(Level.WARNING, "Task delete failed");
		}

		// Send the new task to the TaskSchedulingService
		Message msg = Message.obtain();
		msg.obj = mTask;
		msg.arg1 = Operation.REMOVE.ordinal();

		try {
			mService.send(msg);
		} catch(RemoteException e) {
			mLogger.log(Level.WARNING, 
					"Failed to send the message to the TaskSchedulingService");
			e.printStackTrace();
		}		
	}

	public void onClick(DialogInterface pDialog, int pWhichButton) {
		switch(pWhichButton){
		case AlertDialog.BUTTON_POSITIVE:
			// Delete the task
			mLogger.log(Level.INFO, "Received OK button click");
			deleteTask();
			mTask = null;
			pDialog.dismiss();
			finish();
			break;
		case AlertDialog.BUTTON_NEGATIVE:
			// Close the confirmation dialog
			mLogger.log(Level.INFO, "Received Cancel button click");
			pDialog.dismiss();
			break;
		default:
			mLogger.log(Level.WARNING, "Received click from unknown button: " 
					+ pWhichButton);
			break;
		}
	}
}
