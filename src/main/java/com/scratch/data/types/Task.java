package com.scratch.data.types;

import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

public class Task implements Parcelable {

	private String mName;

	private String mDetails;
	
	private Date mDatePlanned;

	private Date mDueDate;

	private Date mReminderDate;
	
	private Date mLastNotifyDate;

	private ReminderLevel mReminderLevel;

	private Date mCompletionDate;

	private boolean mTaskCompleted;
	
	private long mKey;

	public Task() {
		mName = "";
		mDetails = "";
		mReminderLevel = ReminderLevel.OFF;
		mTaskCompleted = false;
		mKey = 0;
		mCompletionDate = new Date(0);
		mDatePlanned = new Date();
		mDueDate = new Date(0);
		mReminderDate = new Date(0);
		mLastNotifyDate = new Date(0);
	}
	
	/**
	 * Copy constructor
	 * @param pTask
	 */
	public Task(Task pTask) {
		mCompletionDate = pTask.getCompletionDate();
		mDatePlanned = pTask.getDatePlanned();
		mDetails = pTask.getDetails();
		mDueDate = pTask.getDueDate();
		// copy constructor, don't copy the key
		mKey = 0;
		mName = pTask.getName();
		mReminderDate = pTask.getReminderDate();
		mLastNotifyDate = pTask.getLastNotifyDate();
		mReminderLevel = pTask.getReminderLevel();
		mTaskCompleted = pTask.isTaskCompleted();
	}
	

	/**
	 * @param pName
	 * @param pDetails
	 * @param pDueDate
	 * @param pTimeDue
	 * @param pReminderDate
	 * @param pReminderTime
	 * @param pReminderLevel
	 * @param pRecurrence
	 * @param pCompletionDate
	 * @param pCompletionTime
	 * @param pTaskCompleted
	 */
	public Task(String pName, String pDetails, Date pDatePlanned, Date pDueDate, 
			Date pReminderDate, Date pLastNotifyDate,
			ReminderLevel pReminderLevel,
			Date pCompletionDate, boolean pTaskCompleted) {
		this.mName = pName;
		this.mDetails = pDetails;
		this.mDatePlanned = pDatePlanned;
		this.mDueDate = pDueDate;
		this.mReminderDate = pReminderDate;
		this.mLastNotifyDate = pLastNotifyDate;
		this.mReminderLevel = pReminderLevel;
		this.mCompletionDate = pCompletionDate;
		this.mTaskCompleted = pTaskCompleted;
		this.mKey = 0;
	}

	protected Task(Parcel pIn) {
    	mCompletionDate = new Date(pIn.readLong());
    	mDatePlanned = new Date(pIn.readLong());
    	mDetails = pIn.readString();
    	mDueDate = new Date(pIn.readLong());
    	mKey = pIn.readLong();
    	mLastNotifyDate = new Date(pIn.readLong());
    	mName = pIn.readString();
    	mReminderDate = new Date(pIn.readLong());
    	mReminderLevel = ReminderLevel.valueOf(pIn.readString());
    	mTaskCompleted = Boolean.valueOf(pIn.readString());
    }
	
	public String toString() {
		String retVal = new String("");
		retVal = "Name:\t\t" + mName + "\n" + "Details:\t\t" + mDetails + "\n"
				+ "Date Planned:\t\t" + mDatePlanned + "\n"
				+ "Due Date:\t\t" + mDueDate + "\n"
				+ "Reminder Date:\t\t" + mReminderDate + "\n"
				+ "Last Notify Date:\t\t" + mLastNotifyDate + "\n"
				+ "Reminder Level:\t\t" + mReminderLevel + "\n"
				+ "Task Completed:\t\t" + mTaskCompleted + "\n"
				+ "Completion Date:\t\t" + mCompletionDate + "\n"
				+ "Primary Key:\t\t" + mKey + "\n";

		return retVal;
	}

	/**
	 * @return the mName
	 */
	public String getName() {
		return mName;
	}

	/**
	 * @param mName
	 *            the mName to set
	 */
	public void setName(String pName) {
		this.mName = pName;
	}

	/**
	 * @return the mDetails
	 */
	public String getDetails() {
		return mDetails;
	}

	/**
	 * @param pDetails
	 *            the mDetails to set
	 */
	public void setDetails(String pDetails) {
		this.mDetails = pDetails;
	}
	
	public Date getDatePlanned() {
		return mDatePlanned;
	}
	
	public void setDatePlanned(Date pDatePlanned) {
		this.mDatePlanned = pDatePlanned;
	}

	/**
	 * @return the mDueDate
	 */
	public Date getDueDate() {
		return mDueDate;
	}

	/**
	 * @param pDueDate
	 *            the mDueDate to set
	 */
	public void setDueDate(Date pDueDate) {
		this.mDueDate = pDueDate;
	}

	/**
	 * @return the mReminderDate
	 */
	public Date getReminderDate() {
		return mReminderDate;
	}
	
	/**
	 * 
	 * @return the mLastNotifyDate
	 */
	public Date getLastNotifyDate() {
		return mLastNotifyDate;
	}

	/**
	 * @param pReminderDate
	 *            the mReminderDate to set
	 */
	public void setReminderDate(Date pReminderDate) {
		this.mReminderDate = pReminderDate;
	}

	/**
	 * 
	 * @param pLastNotifyDate the mLastNotifyDate to set
	 */
	public void setLastNotifyDate(Date pLastNotifyDate) {
		this.mLastNotifyDate = pLastNotifyDate;
	}
	
	/**
	 * @return the mReminderLevel
	 */
	public ReminderLevel getReminderLevel() {
		return mReminderLevel;
	}

	/**
	 * @param pReminderLevel
	 *            the mReminderLevel to set
	 */
	public void setReminderLevel(ReminderLevel pReminderLevel) {
		this.mReminderLevel = pReminderLevel;
	}

	/**
	 * @return the mCompletionDate
	 */
	public Date getCompletionDate() {
		return mCompletionDate;
	}

	/**
	 * @param pCompletionDate
	 *            the mCompletionDate to set
	 */
	public void setCompletionDate(Date pCompletionDate) {
		this.mCompletionDate = pCompletionDate;
	}

	/**
	 * @return the mTaskCompleted
	 */
	public boolean isTaskCompleted() {
		return mTaskCompleted;
	}

	/**
	 * @param pTaskCompleted
	 *            the mTaskCompleted to set
	 */
	public void setTaskCompleted(boolean pTaskCompleted) {
		this.mTaskCompleted = pTaskCompleted;
	}

	public long getKey() {
		return this.mKey;
	}
	
	public void setKey(long pKey) {
	   mKey = pKey;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel pDest, int pFlags) {
		pDest.writeLong(mCompletionDate.getTime());
		pDest.writeLong(mDatePlanned.getTime());
		pDest.writeString(mDetails);
		pDest.writeLong(mDueDate.getTime());
		pDest.writeLong(mKey);
		pDest.writeLong(mLastNotifyDate.getTime());
		pDest.writeString(mName);
		pDest.writeLong(mReminderDate.getTime());
		pDest.writeString(mReminderLevel.name());
		pDest.writeString(Boolean.toString(mTaskCompleted));
	}

	public static final Parcelable.Creator<Task> CREATOR = 
			new Parcelable.Creator<Task>() {
        public Task createFromParcel(Parcel pIn) {
            return new Task(pIn);
        }

        public Task[] newArray(int pSize) {
            return new Task[pSize];
        }
    };

    
}
