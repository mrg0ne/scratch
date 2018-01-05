package com.scratch.gui;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.R;
import com.scratch.data.storage.TaskContract.TaskEntry;
import com.scratch.data.types.RecurrenceType;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;
import com.scratch.data.types.TaskRecurrence;

import android.annotation.SuppressLint;
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebView.FindListener;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("SimpleDateFormat")
public class TaskViewBinder implements ViewBinder {

	// For debug
	private Logger mLogger;

	private SimpleDateFormat mDateFormat;

	private SimpleDateFormat mTimeFormat;

	private OnClickListener mOnClickListener;

	private ITaskHashRetrieval mTaskHash;

	@SuppressLint("SimpleDateFormat")
	public TaskViewBinder(
			android.view.View.OnClickListener pOnClickListener,
			ITaskHashRetrieval pTaskHash){
		mLogger = Logger.getLogger(this.getClass().getName());
		mDateFormat = new SimpleDateFormat(AbstractTaskActivity.DATE_FORMAT);
		mTimeFormat = new SimpleDateFormat(AbstractTaskActivity.TIME_FORMAT);
		mOnClickListener = pOnClickListener;
		mTaskHash = pTaskHash;
	}

	public boolean setViewValue(View pView, Cursor pCursor, int pColumnIndex) {
		mLogger.log(Level.INFO, "setViewValue called for column : " + pColumnIndex);
		boolean retVal = true;

		if (!pView.hasOnClickListeners()){
			pView.setOnClickListener(mOnClickListener);
			mLogger.log(Level.INFO, "setting OnClickListener");
		} else {
			mLogger.log(Level.INFO, "already has on click listeners");
		}

		if (pView.getId() == com.scratch.R.id.setcomplete){
			CheckBox checkbox = (CheckBox)pView;			
			boolean taskCompleted = Boolean
					.parseBoolean(pCursor.getString(pColumnIndex));

			mLogger.log(Level.INFO, "setcomplete value is : " + taskCompleted);

			if (taskCompleted != checkbox.isChecked()){
				mLogger.log(Level.INFO, "check box value changed to " + taskCompleted);
				checkbox.setChecked(taskCompleted);
			}
			
		} else if (pView.getId() == com.scratch.R.id.name){
			TextView textView = (TextView)pView;
			String name = pCursor.getString(pColumnIndex);
			mLogger.log(Level.INFO, "name value is : " + name);
			textView.setText(name);
			
			textView.setTextColor(getTextColor(pCursor));

			Task task = mTaskHash.getTask(name);

			if (task == null) {
				String id = pCursor.getString(pCursor.getColumnIndexOrThrow(TaskEntry._ID));
				task = mTaskHash.getTask(id);
			}
			
			ViewGroup parent = (ViewGroup)pView.getParent();
			ImageView taskType = 
					(ImageView)parent.findViewById(R.id.tasktype);

			if (task != null && task instanceof RecurringTask){
				mLogger.log(Level.INFO, name + " is a RecurringTask");
				TaskRecurrence recurrence = 
						((RecurringTask)task).getRecurrence();

				if (recurrence.getRecurrenceType() != RecurrenceType.NONE){
					mLogger.log(Level.INFO, "getting image resource for " + name);
					taskType.setImageResource(R.drawable.ic_action_repeat);	
				} else {
					mLogger.log(Level.INFO, "setting image resource to 0 for " + name);
					taskType.setImageResource(0);
				}
			} else {	
				mLogger.log(Level.INFO, "setting image resource to 0 for " + name);
				taskType.setImageResource(0);
			}
		} else if (pView.getId() == com.scratch.R.id.duedate){
			TextView textView = (TextView)pView;
			long ms = pCursor.getLong(pColumnIndex);

			// Only display due date if value is greater than zero
			if (ms > 0){
				Date dueDate = new Date(ms);
				String date = mDateFormat.format(dueDate);
				String time = mTimeFormat.format(dueDate);
				mLogger.log(Level.INFO, "duedate value is : " + date + " " + time);
				textView.setText(date + "   \n" + time + "   ");
				textView.setTextAlignment(TextView.TEXT_ALIGNMENT_VIEW_END);
				textView.setTextColor(getTextColor(pCursor));
			} else {
				textView.setText("");
			}
		} else {
			mLogger.log(Level.INFO, "Unknown view ID: " + pView.getId());
			retVal = false;
		}

		return retVal;
	}	
	
	/**
	 * Get the text color to the cursor's task
	 * @param pCursor
	 * @return color of the text
	 */
	private int getTextColor(Cursor pCursor){
		int retColor = Color.BLACK;
		String name = pCursor.getString(pCursor.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_NAME));
		Task task = mTaskHash.getTask(name);

		if (task == null) {
			String id = pCursor.getString(pCursor.getColumnIndexOrThrow(TaskEntry._ID));
			task = mTaskHash.getTask(id);
		}

		Calendar now = Calendar.getInstance();

		// Set text to RED if due date has passed
		if (task != null && !task.isTaskCompleted() 
				&& (task.getDueDate().getTime() > 0) 
				&& (task.getDueDate().getTime() < now.getTimeInMillis())){
			retColor = Color.RED;
		} else {
			retColor = Color.BLACK;
		}
		
		return retColor;
	}
}
