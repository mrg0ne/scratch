package com.scratch.scheduler;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.data.types.Task;

public class ScheduledNotification implements Comparable<ScheduledNotification> {

	private Task mTask;
	
	private Date mNextNotification;
	
	private Logger mLogger;
	
	public ScheduledNotification() {
		mLogger = Logger.getLogger(this.getClass().getName());
		mNextNotification = new Date(0);
	}

	public ScheduledNotification(Task pTask){
		this();
		mTask = pTask;
		mNextNotification = calculateNextNotification(pTask);
	}	

	/**
	 * @return the Task
	 */
	public Task getTask() {
		return mTask;
	}

	/**
	 * @param pTask the Task to set
	 */
	public void setTask(Task pTask) {
		this.mTask = pTask;
		mNextNotification = calculateNextNotification(pTask);
	}

	/**
	 * @return the NextNotification
	 */
	public Date getNextNotification() {
		return mNextNotification;
	}

	/**
	 * @param mNextNotification the NextNotification to set
	 */
	public void setNextNotification(Date pNextNotification) {
		this.mNextNotification = pNextNotification;
	}
	
	private Date calculateNextNotification(Task pTask) {
		Date rDate;
		Date now = new Date();
		long nowTime = now.getTime();
		long reminderTime = pTask.getReminderDate().getTime();
		long dueTime = pTask.getDueDate().getTime();
		long dayInMs = 24*60*60*1000;
		long rTime = 0;

		// Reminder time shouldn't be after due time
		if (reminderTime > dueTime) {
			mLogger.log(Level.WARNING, "Reminder time is after due time");
			reminderTime = dueTime;
		}
		
		if (nowTime < reminderTime) {
			rTime = reminderTime;
			mLogger.log(Level.INFO, "calculated next notification time for task : " 
			   + pTask + " at Reminder Date: " + new Date(rTime));
		} else if (nowTime < dueTime && reminderTime == 0) {
			rTime = dueTime;
			mLogger.log(Level.INFO, "calculated next notification time for task : " 
					   + pTask + " at Due Date: " + new Date(rTime));
		} else if (nowTime < dueTime && reminderTime > 0) {
			rTime = reminderTime;
			while (rTime < nowTime) {
				rTime += dayInMs;

				if (rTime > dueTime) {					
					rTime = dueTime;
					mLogger.log(Level.INFO, "calculated next notification time for" + 
					   " task : " + pTask + " after reminder date at Due Date: " + 
							new Date(rTime));
					break;
				}
			}
			
			mLogger.log(Level.INFO, "calculated next notification time for task : " 
					   + pTask + " after Reminder Date at : " + new Date(rTime));
		} else if (nowTime > dueTime) {
			rTime = dueTime;
			while (rTime < nowTime) {
				rTime += dayInMs;
			}
			mLogger.log(Level.INFO, "calculated next notification time for task : " 
					   + pTask + " after Due Date at : " + new Date(rTime));
		} else {
			mLogger.log(Level.WARNING, "Failed to calculate next " + 
					"notification time for task : " + pTask);
		}

		rDate = new Date(rTime);
		return rDate;
	}

	public int compareTo(ScheduledNotification pAnother) {
		int retVal = 0;
		long time0 = getNextNotification().getTime();
		long time1 = pAnother.getNextNotification().getTime();
		
		if (time0 < time1) {
			retVal = -1;
		} else if (time0 > time1) {
			retVal = 1;
		}
		
		return retVal;
	}
}
