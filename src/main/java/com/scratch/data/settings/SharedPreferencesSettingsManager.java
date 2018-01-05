package com.scratch.data.settings;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.R;
import android.content.Context;
import android.content.SharedPreferences;

import com.scratch.data.types.RecurrenceType;
import com.scratch.data.types.ReminderLevel;
import com.scratch.data.types.SortOrder;

public class SharedPreferencesSettingsManager extends AbstractSettingsManager {

	private SharedPreferences.Editor mEditor;
	private Context mContext;
	private Logger mLogger;

	public SharedPreferencesSettingsManager(Context pContext) {
		super();
		mContext = pContext;
		mLogger = Logger.getLogger(this.getClass().getName());
		SharedPreferences prefs = pContext.getSharedPreferences(
				pContext.getString(R.string.preferences_file_key), 
				Context.MODE_PRIVATE);
		mEditor = prefs.edit();
		mRecurringTaskHistorySize = prefs.getInt(pContext.getString(
				R.string.recurring_task_history_size), 100);
		mDefaultTimeDue = prefs.getInt(pContext.getString(
				R.string.default_time_due), 1000*60*60*10); //10am
		mDefaultReminderTime = prefs.getInt(pContext.getString(
				R.string.default_reminder_time), 1000*60*60*24); //24hrs
		mToolTipsOn = prefs.getBoolean(pContext.getString(
				R.string.tool_tips_on), true);
		mAutoSchedulerOn = prefs.getBoolean(pContext.getString(
				R.string.auto_scheduler_on), true);
		mDefaultNotificationEmailAddr = prefs.getString(
				pContext.getString(R.string.default_notification_email_addr), "");		
		mDefaultReminderLevel = ReminderLevel.valueOf(prefs.getString(
				pContext.getString(
				R.string.default_reminder_level), ReminderLevel.MED.name()));
		mDefaultTaskRecurrence = RecurrenceType.valueOf(prefs.getString(
				pContext.getString(
				R.string.default_task_recurrence), RecurrenceType.WEEKLY.name()));
		mSortOrderIncompleteTasks = SortOrder.valueOf(prefs.getString(
				pContext.getString(R.string.sort_order_complete_tasks), 
				SortOrder.DUE_DATE.name()));
		mSortOrderCompleteTasks = SortOrder.valueOf(prefs.getString(
				pContext.getString(R.string.sort_order_incomplete_tasks), 
				SortOrder.COMPLETE_DATE.name()));
		mSortOrderAllTasks = SortOrder.valueOf(prefs.getString(
				pContext.getString(R.string.sort_order_all_tasks), 
				SortOrder.DUE_DATE.name()));
		mTimeForShowingFutureTasks = prefs.getLong(pContext.getString(
				R.string.time_for_showing_future_tasks), 1000*60*60*24*31); //31 days
	}
	
	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setRecurringTaskHistorySize(int)
	 */
	@Override
	public void setRecurringTaskHistorySize(int pRecurringTaskHistorySize) {
		super.setRecurringTaskHistorySize(pRecurringTaskHistorySize);
		mLogger.log(Level.INFO, "setRecurringTaskHistorySize called: " 
		   + pRecurringTaskHistorySize);
		mEditor.putInt(mContext.getString(R.string.recurring_task_history_size), 
				mRecurringTaskHistorySize);
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit recurring task history size");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setDefaultTimeDue(int)
	 */
	@Override
	public void setDefaultTimeDue(long pDefaultTimeDue) {
		super.setDefaultTimeDue(pDefaultTimeDue);
		mLogger.log(Level.INFO, "setDefaultTimeDue called: " + pDefaultTimeDue);
		mEditor.putLong(mContext.getString(R.string.default_time_due), 
				mDefaultTimeDue);
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit default time due");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setDefaultReminderTime(int)
	 */
	@Override
	public void setDefaultReminderTime(long pDefaultReminderTime) {
		super.setDefaultReminderTime(pDefaultReminderTime);
		mLogger.log(Level.INFO, "setDefaultReminderTime called: "
		   + pDefaultReminderTime);
		mEditor.putLong(mContext.getString(R.string.default_reminder_time), 
				mDefaultReminderTime);
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit default reminder time");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setToolTipsOn(boolean)
	 */
	@Override
	public void setToolTipsOn(boolean pToolTipsOn) {
		super.setToolTipsOn(pToolTipsOn);
		mLogger.log(Level.INFO, "setToolTipsOn called: " + pToolTipsOn);
		mEditor.putBoolean(mContext.getString(R.string.tool_tips_on), 
				mToolTipsOn);
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit tool tips on");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setAutoSchedulerOn(boolean)
	 */
	@Override
	public void setAutoSchedulerOn(boolean pAutoSchedulerOn) {
		super.setAutoSchedulerOn(pAutoSchedulerOn);
		mLogger.log(Level.INFO, "setAutoSchedulerOn called: " + pAutoSchedulerOn);
		mEditor.putBoolean(mContext.getString(R.string.auto_scheduler_on), 
				mAutoSchedulerOn);
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit auto scheduler on");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setDefaultNotificationEmailAddr(java.lang.String)
	 */
	@Override
	public void setDefaultNotificationEmailAddr(
			String pDefaultNotificationEmailAddr) {
		super.setDefaultNotificationEmailAddr(pDefaultNotificationEmailAddr);
		mLogger.log(Level.INFO, "setDefaultNotificationEmailAddr called : " 
		   + pDefaultNotificationEmailAddr);
		mEditor.putString(mContext.getString(R.string.default_notification_email_addr), 
				mDefaultNotificationEmailAddr);
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, 
					"failed to commit default notification email address");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setDefaultReminderLevel(com.scratch.data.types.ReminderLevel)
	 */
	@Override
	public void setDefaultReminderLevel(ReminderLevel pDefaultReminderLevel) {
		super.setDefaultReminderLevel(pDefaultReminderLevel);
		mLogger.log(Level.INFO, "setDefaultReminderLevel called : " + 
		   pDefaultReminderLevel.name());
		mEditor.putString(mContext.getString(R.string.default_reminder_level), 
				mDefaultReminderLevel.name());
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit default reminder level");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setDefaultTaskRecurrence(com.scratch.data.types.RecurrenceType)
	 */
	@Override
	public void setDefaultTaskRecurrence(RecurrenceType pDefaultTaskRecurrence) {
		super.setDefaultTaskRecurrence(pDefaultTaskRecurrence);
		mLogger.log(Level.INFO, "setDefaultTaskRecurrence called : " + 
		   pDefaultTaskRecurrence.name());
		mEditor.putString(mContext.getString(R.string.default_task_recurrence), 
				mDefaultTaskRecurrence.name());
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit default task recurrence");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setSortOrderIncompleteTasks(com.scratch.data.types.SortOrder)
	 */
	@Override
	public void setSortOrderIncompleteTasks(SortOrder pSortOrderIncompleteTasks) {
		super.setSortOrderIncompleteTasks(pSortOrderIncompleteTasks);
		mLogger.log(Level.INFO, "setSortOrderIncompleteTasks called : " + 
		   pSortOrderIncompleteTasks.name());
		mEditor.putString(mContext.getString(R.string.sort_order_incomplete_tasks), 
				mSortOrderIncompleteTasks.name());
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit sort order incomplete tasks");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setSortOrderCompleteTasks(com.scratch.data.types.SortOrder)
	 */
	@Override
	public void setSortOrderCompleteTasks(SortOrder pSortOrderCompleteTasks) {
		super.setSortOrderCompleteTasks(pSortOrderCompleteTasks);
		mLogger.log(Level.INFO, "setSortOrderCompleteTasks called : " + 
		   pSortOrderCompleteTasks.name());
		mEditor.putString(mContext.getString(R.string.sort_order_complete_tasks), 
				mSortOrderCompleteTasks.name());
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit sort order complete tasks");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setSortOrderAllTasks(com.scratch.data.types.SortOrder)
	 */
	@Override
	public void setSortOrderAllTasks(SortOrder pSortOrderAllTasks) {
		super.setSortOrderAllTasks(pSortOrderAllTasks);
		mLogger.log(Level.INFO, "setSortOrderAllTasks called : " + pSortOrderAllTasks.name());
		mEditor.putString(mContext.getString(R.string.sort_order_all_tasks), 
				mSortOrderAllTasks.name());
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit sort order all tasks");
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.settings.AbstractSettingsManager#setTimeForShowingFutureTasks(long)
	 */
	@Override
	public void setTimeForShowingFutureTasks(long pTimeForShowingFutureTasks) {
		super.setTimeForShowingFutureTasks(pTimeForShowingFutureTasks);
		mLogger.log(Level.INFO, "setTimeForShowingFutureTasks called : " + 
		   pTimeForShowingFutureTasks);
		mEditor.putLong(mContext.getString(R.string.time_for_showing_future_tasks), 
				mTimeForShowingFutureTasks);
		if (!mEditor.commit()) {
			mLogger.log(Level.WARNING, "failed to commit time for showing future tasks");
		}
	}

	

}
