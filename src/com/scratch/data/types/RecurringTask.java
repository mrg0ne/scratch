package com.scratch.data.types;

import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.os.Parcel;
import android.os.Parcelable;

public class RecurringTask extends Task {

	// Recurrence setting
	private TaskRecurrence mRecurrence;
	
	// List of the last x tasks
	Vector<Task> mTasks;
	
	private Logger mLogger;
	
	/**
	 * Default constructor
	 */
	public RecurringTask() {
		super();
		mRecurrence = new TaskRecurrence();
		mLogger = Logger.getLogger(this.getClass().getName());
	}
	
	/**
	 * Copy constructor
	 * @param pRecurringTask
	 */
	public RecurringTask(RecurringTask pRecurringTask) {
		super(pRecurringTask);
		mRecurrence = pRecurringTask.getRecurrence();
		mTasks = new Vector<Task>();
		
		// Copy the contained tasks
		for(Task task : pRecurringTask.getTasks()) {
			Task taskCopy = new Task(task);
			mTasks.add(taskCopy);
		}
		
		mLogger = Logger.getLogger(this.getClass().getName());
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
	public RecurringTask(String pName, String pDetails, Date pDatePlanned, 
			Date pDueDate, Date pReminderDate, Date pLastNotifyDate, 
			ReminderLevel pReminderLevel, TaskRecurrence pRecurrence,
			Date pCompletionDate, boolean pTaskCompleted) {
		super(pName, pDetails, pDatePlanned, pDueDate, pReminderDate, 
				pLastNotifyDate, pReminderLevel, pCompletionDate, 
				pTaskCompleted);
		mRecurrence = pRecurrence;
		mTasks = new Vector<Task>();
		mLogger = Logger.getLogger(this.getClass().getName());
	}
	
	public RecurringTask(Task pTask, TaskRecurrence pRecurrence, Date pDatePlanned,
			Date pDueDate,
			long pTimeDue, Date pReminderDate, long pReminderTime) {
		this(pTask.getName(), pTask.getDetails(), pDatePlanned, pDueDate, 
				pReminderDate, pTask.getLastNotifyDate(), pTask.getReminderLevel(),
				pRecurrence, pTask.getCompletionDate(), 
				false);
		mRecurrence = pRecurrence;
		mTasks.add(pTask);
	}
	
	public RecurringTask(Task pTask){
		this(pTask.getName(), pTask.getDetails(), pTask.getDatePlanned(), 
				pTask.getDueDate(), pTask.getReminderDate(),
				pTask.getLastNotifyDate(), pTask.getReminderLevel(),
				new TaskRecurrence(), pTask.getCompletionDate(), 
				false);
		mTasks.add(pTask);
	}
	
	/**
	 * @return the mRecurrence
	 */
	public TaskRecurrence getRecurrence() {
		return mRecurrence;
	}

	/**
	 * @param pRecurrence
	 *            the mRecurrence to set
	 */
	public void setRecurrence(TaskRecurrence pRecurrence) {
		this.mRecurrence = pRecurrence;
	}
	
	/* (non-Javadoc)
	 * @see com.scratch.data.types.Task#setName(java.lang.String)
	 */
	@Override
	public void setName(String pName) {
		super.setName(pName);
		
		// Update the name of all sub-tasks
		for (Task task : mTasks){
			task.setName(pName);
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.types.Task#setDetails(java.lang.String)
	 */
	@Override
	public void setDetails(String pDetails) {
        super.setDetails(pDetails);
		
		// Update the details of all sub-tasks
		for (Task task : mTasks){
			task.setDetails(pDetails);
		}
	}

	public Vector<Task> getTasks() {
	   return mTasks;
	}
	
	public void addTask(Task pTask) {
		removeTask(pTask);
		mTasks.add(pTask);
	}
	
	public boolean containsTask(Task pTask){
		boolean retVal = false;
		
		for (Task task : mTasks){
			if (pTask.getKey() == task.getKey()){
				retVal = true;
				break;
			}
		}
		
		return retVal;
	}
	
	public void removeTask(Task pTask) {
		for (int i = 0; i < mTasks.size(); i++) {
			if (mTasks.elementAt(i).getKey() == pTask.getKey()) {
				mTasks.remove(i);
				mLogger.log(Level.INFO, this.getName() + 
						": removing task " + pTask.getKey());
				break;
			}
		}
	}
	
	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel pDest, int pFlags) {
		super.writeToParcel(pDest, pFlags);
		mRecurrence.writeToParcel(pDest, pFlags);
		pDest.writeInt(mTasks.size());	
		
		for (Task task : mTasks) {
			task.writeToParcel(pDest, pFlags);
		}
	}

	public static final Parcelable.Creator<RecurringTask> CREATOR = 
			new Parcelable.Creator<RecurringTask>() {
        public RecurringTask createFromParcel(Parcel pIn) {
            return new RecurringTask(pIn);
        }

        public RecurringTask[] newArray(int pSize) {
            return new RecurringTask[pSize];
        }
    };

    protected RecurringTask(Parcel pIn) {
    	super(pIn);
    	mRecurrence = new TaskRecurrence(pIn);
    	int numTasks = pIn.readInt();
    	mTasks = new Vector<Task>(numTasks);
    	
    	for (int i = 0; i < numTasks; i++){
    		Task task = new Task(pIn);
    		mTasks.add(i, task);
    	}
    	
		mLogger = Logger.getLogger(this.getClass().getName());
    }
}
