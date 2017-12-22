package com.scratch.data.settings;

import com.scratch.data.types.RecurrenceType;
import com.scratch.data.types.ReminderLevel;
import com.scratch.data.types.SortOrder;
// ADB wuz here
public abstract class AbstractSettingsManager {
	
	// The number of past recurring task instances to save
	protected int mRecurringTaskHistorySize;
	
	// The default time of day to use when setting a due date
	protected long mDefaultTimeDue;
	
	// The default amount of time before the due date to use when setting a reminder 
	protected long mDefaultReminderTime;
	
	// Switch that controls whether tool tips are displayed
	protected boolean mToolTipsOn;
	
	// Switch that controls whether the auto-scheduler runs or not
	protected boolean mAutoSchedulerOn;
	
	// The default email address to use when sending notifications
	protected String mDefaultNotificationEmailAddr;
	
	// the default reminder level to use when setting a reminder
	protected ReminderLevel mDefaultReminderLevel;
	
	// The default task recurrence setting to use
	protected RecurrenceType mDefaultTaskRecurrence;
	
	// The sort order to use for the incomplete tasks view
	protected SortOrder mSortOrderIncompleteTasks;
	
	// The sort order to use for the complete tasks view
	protected SortOrder mSortOrderCompleteTasks;
	
	// The sort order to use for the all tasks view
	protected SortOrder mSortOrderAllTasks;
	
	// Determines how far in the future the views should go when displaying tasks
	protected long mTimeForShowingFutureTasks;

	public AbstractSettingsManager() {
		mRecurringTaskHistorySize = 100;
		mDefaultTimeDue = 1000*60*60*10; // 10AM
		mDefaultReminderTime = 1000*60*60*24; // 24hrs
		mToolTipsOn = true;
		mAutoSchedulerOn = true;
		mDefaultNotificationEmailAddr = "";		
		mDefaultReminderLevel = ReminderLevel.MED;
		mDefaultTaskRecurrence = RecurrenceType.WEEKLY;
		mSortOrderIncompleteTasks = SortOrder.DUE_DATE;
		mSortOrderCompleteTasks = SortOrder.COMPLETE_DATE;
		mSortOrderAllTasks = SortOrder.DUE_DATE;
		mTimeForShowingFutureTasks = 1000*60*60*24*31; // 31days
	}

	/**
	 * @return the mRecurringTaskHistorySize
	 */
	public int getRecurringTaskHistorySize() {
		return mRecurringTaskHistorySize;
	}

	/**
	 * @param pRecurringTaskHistorySize the mRecurringTaskHistorySize to set
	 */
	public void setRecurringTaskHistorySize(int pRecurringTaskHistorySize) {
		this.mRecurringTaskHistorySize = pRecurringTaskHistorySize;
	}

	/**
	 * @return the mDefaultTimeDue
	 */
	public long getDefaultTimeDue() {
		return mDefaultTimeDue;
	}

	/**
	 * @param pDefaultTimeDue the mDefaultTimeDue to set
	 */
	public void setDefaultTimeDue(long pDefaultTimeDue) {
		this.mDefaultTimeDue = pDefaultTimeDue;
	}

	/**
	 * @return the mDefaultReminderTime
	 */
	public long getDefaultReminderTime() {
		return mDefaultReminderTime;
	}

	/**
	 * @param pDefaultReminderTime the mDefaultReminderTime to set
	 */
	public void setDefaultReminderTime(long pDefaultReminderTime) {
		this.mDefaultReminderTime = pDefaultReminderTime;
	}

	/**
	 * @return the mToolTipsOn
	 */
	public boolean areToolTipsOn() {
		return mToolTipsOn;
	}

	/**
	 * @param pToolTipsOn the mToolTipsOn to set
	 */
	public void setToolTipsOn(boolean pToolTipsOn) {
		this.mToolTipsOn = pToolTipsOn;
	}

	/**
	 * @return the mAutoSchedulerOn
	 */
	public boolean isAutoSchedulerOn() {
		return mAutoSchedulerOn;
	}

	/**
	 * @param pAutoSchedulerOn the mAutoSchedulerOn to set
	 */
	public void setAutoSchedulerOn(boolean pAutoSchedulerOn) {
		this.mAutoSchedulerOn = pAutoSchedulerOn;
	}

	/**
	 * @return the mDefaultNotificationEmailAddr
	 */
	public String getDefaultNotificationEmailAddr() {
		return mDefaultNotificationEmailAddr;
	}

	/**
	 * @param pDefaultNotificationEmailAddr the mDefaultNotificationEmailAddr to set
	 */
	public void setDefaultNotificationEmailAddr(
			String pDefaultNotificationEmailAddr) {
		this.mDefaultNotificationEmailAddr = pDefaultNotificationEmailAddr;
	}

	/**
	 * @return the mDefaultReminderLevel
	 */
	public ReminderLevel getDefaultReminderLevel() {
		return mDefaultReminderLevel;
	}

	/**
	 * @param pDefaultReminderLevel the mDefaultReminderLevel to set
	 */
	public void setDefaultReminderLevel(ReminderLevel pDefaultReminderLevel) {
		this.mDefaultReminderLevel = pDefaultReminderLevel;
	}

	/**
	 * @return the mDefaultTaskRecurrence
	 */
	public RecurrenceType getDefaultTaskRecurrence() {
		return mDefaultTaskRecurrence;
	}

	/**
	 * @param pDefaultTaskRecurrence the mDefaultTaskRecurrence to set
	 */
	public void setDefaultTaskRecurrence(RecurrenceType pDefaultTaskRecurrence) {
		this.mDefaultTaskRecurrence = pDefaultTaskRecurrence;
	}

	/**
	 * @return the mSortOrderIncompleteTasks
	 */
	public SortOrder getSortOrderIncompleteTasks() {
		return mSortOrderIncompleteTasks;
	}

	/**
	 * @param pSortOrderIncompleteTasks the mSortOrderIncompleteTasks to set
	 */
	public void setSortOrderIncompleteTasks(SortOrder pSortOrderIncompleteTasks) {
		this.mSortOrderIncompleteTasks = pSortOrderIncompleteTasks;
	}

	/**
	 * @return the mSortOrderCompleteTasks
	 */
	public SortOrder getSortOrderCompleteTasks() {
		return mSortOrderCompleteTasks;
	}

	/**
	 * @param pSortOrderCompleteTasks the mSortOrderCompleteTasks to set
	 */
	public void setSortOrderCompleteTasks(SortOrder pSortOrderCompleteTasks) {
		this.mSortOrderCompleteTasks = pSortOrderCompleteTasks;
	}

	/**
	 * @return the mSortOrderAllTasks
	 */
	public SortOrder getSortOrderAllTasks() {
		return mSortOrderAllTasks;
	}

	/**
	 * @param pSortOrderAllTasks the mSortOrderAllTasks to set
	 */
	public void setSortOrderAllTasks(SortOrder pSortOrderAllTasks) {
		this.mSortOrderAllTasks = pSortOrderAllTasks;
	}

	/**
	 * @return the mTimeForShowingFutureTasks
	 */
	public long getTimeForShowingFutureTasks() {
		return mTimeForShowingFutureTasks;
	}

	/**
	 * @param pTimeForShowingFutureTasks the mTimeForShowingFutureTasks to set
	 */
	public void setTimeForShowingFutureTasks(long pTimeForShowingFutureTasks) {
		this.mTimeForShowingFutureTasks = pTimeForShowingFutureTasks;
	}

}
