package com.scratch.data.storage;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import com.scratch.data.storage.TaskContract.RecurringTaskEntry;
import com.scratch.data.storage.TaskContract.TaskEntry;
import com.scratch.data.types.RecurrenceType;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.ReminderLevel;
import com.scratch.data.types.Task;
import com.scratch.data.types.TaskRecurrence;
import com.scratch.data.types.TaskRecurrence.WeekNumber;

public class DbStorage extends ContentProvider implements IDataStorage, ITaskCursor {

	public static final String AUTHORITY = "com.scratch.provider";

	public static final String TASK_URI = "content://" + AUTHORITY + "/" 
			+ TaskEntry.TABLE_NAME;

	public static final String RECURRING_TASK_URI = "content://" + AUTHORITY + "/" 
			+ RecurringTaskEntry.TABLE_NAME;

	private static final String TASK_MIME = "vnd.android.cursor.item/vnd." + 
			AUTHORITY + "." + TaskEntry.TABLE_NAME;

	private static final String RECURRING_TASK_MIME = "vnd.android.cursor.item/vnd." + 
			AUTHORITY + "." + RecurringTaskEntry.TABLE_NAME;

	// Creates a UriMatcher object.
	private UriMatcher mUriMatcher;

	private static final int TASK_URI_CODE = 0;

	private static final int RECURRING_TASK_URI_CODE = 1;

	// column projections for a task
	public static final String sTaskProjection[] = { 
		TaskEntry._ID, 
		TaskEntry.COLUMN_NAME_NAME,
		TaskEntry.COLUMN_NAME_DETAILS,
		TaskEntry.COLUMN_NAME_DATE_PLANNED,
		TaskEntry.COLUMN_NAME_DUE_DATE,
		TaskEntry.COLUMN_NAME_REMINDER_DATE,
		TaskEntry.COLUMN_NAME_LAST_NOTIFY_DATE,
		TaskEntry.COLUMN_NAME_REMINDER_LEVEL,
		TaskEntry.COLUMN_NAME_COMPLETION_DATE,
		TaskEntry.COLUMN_NAME_TASK_COMPLETED };

	// column projections for a recurring task
	public static final String sRecurringTaskProjection[] = { 
		TaskEntry._ID,
		TaskEntry.COLUMN_NAME_NAME, 
		TaskEntry.COLUMN_NAME_DETAILS,
		TaskEntry.COLUMN_NAME_DATE_PLANNED,
		TaskEntry.COLUMN_NAME_DUE_DATE,
		TaskEntry.COLUMN_NAME_REMINDER_DATE,
		TaskEntry.COLUMN_NAME_LAST_NOTIFY_DATE,
		TaskEntry.COLUMN_NAME_REMINDER_LEVEL,
		TaskEntry.COLUMN_NAME_COMPLETION_DATE,
		TaskEntry.COLUMN_NAME_TASK_COMPLETED,
		RecurringTaskEntry.COLUMN_NAME_RECURRENCE_TYPE,
		RecurringTaskEntry.COLUMN_NAME_REGULARITY,
		RecurringTaskEntry.COLUMN_NAME_DAY_OF_MONTH,
		RecurringTaskEntry.COLUMN_NAME_ON_MONDAY,
		RecurringTaskEntry.COLUMN_NAME_ON_TUESDAY,
		RecurringTaskEntry.COLUMN_NAME_ON_WEDNESDAY,
		RecurringTaskEntry.COLUMN_NAME_ON_THURSDAY,
		RecurringTaskEntry.COLUMN_NAME_ON_FRIDAY,
		RecurringTaskEntry.COLUMN_NAME_ON_SATURDAY,
		RecurringTaskEntry.COLUMN_NAME_ON_SUNDAY,
		RecurringTaskEntry.COLUMN_NAME_WEEK_NUMBER,
		RecurringTaskEntry.COLUMN_NAME_MONTH,
		RecurringTaskEntry.COLUMN_NAME_USE_DAY_OF_MONTH,
		RecurringTaskEntry.COLUMN_NAME_TIME_OF_DAY,
		RecurringTaskEntry.COLUMN_NAME_TASKS };

	private Logger mLogger;
	private SQLiteDatabase mDb;
	private Boolean DATABASE_READY = false;
	private Context mContext;

	public DbStorage() {
		mLogger = Logger.getLogger(this.getClass().getName());
	}

	public DbStorage(final Context pContext) {
		mLogger = Logger.getLogger(this.getClass().getName());
		mContext = pContext;		
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		mUriMatcher.addURI(AUTHORITY, TaskEntry.TABLE_NAME, TASK_URI_CODE);
		mUriMatcher.addURI(AUTHORITY, RecurringTaskEntry.TABLE_NAME, 
				RECURRING_TASK_URI_CODE);

		new Thread(new Runnable() {
			public void run() {
				initialize();
			}
		}).start();
	}

	public boolean save(Task pTask) {
		mLogger.log(Level.INFO, "Saving " + pTask.getName());

		boolean retVal = false;
		
		if (pTask instanceof RecurringTask){
			// save tasks that it contains first
			for (Task task : ((RecurringTask) pTask).getTasks()){
				retVal = save(task);
				
				if (!retVal){
					return retVal;
				}
			}
			
			if (pTask.getKey() == 0) {
				retVal = create((RecurringTask)pTask);
			} else {
				retVal = update((RecurringTask)pTask);
			}
		}

		if (pTask.getKey() == 0) {
			retVal = create(pTask);
		} else {
			retVal = update(pTask);
		}

		return retVal;
	}

	public boolean delete(Task pTask) {
		mLogger.log(Level.INFO, "delete called");
		synchronized (DATABASE_READY) {
			boolean retVal = false;
			long rowId = pTask.getKey();

			synchronized (DATABASE_READY) {
				if (!DATABASE_READY){
					initialize();
				}			

				if (rowId != 0) {
					String selection = TaskEntry._ID + " LIKE ?";
					String[] selectionArgs = { String.valueOf(pTask.getKey()) };
					int count = 0;

					if (pTask instanceof com.scratch.data.types.RecurringTask) {
						count = delete(Uri.parse(RECURRING_TASK_URI),
								selection, selectionArgs);
					} else if (pTask instanceof com.scratch.data.types.Task) {
						count = delete(Uri.parse(TASK_URI), selection,
								selectionArgs);
					} else {
						mLogger.log(
								Level.WARNING,
								"Unable to delete unknown task type: "
										+ pTask.getName());
					}

					if (count != 1) {
						retVal = false;
						mLogger.log(Level.SEVERE, "Deleted " + count
								+ " rows for task (" + pTask.getName()
								+ ") in database");
					} else {
						retVal = true;
						mLogger.log(Level.INFO, "Succesful update of Task ("
								+ pTask.getName() + ") in database");
					}
				} else {
					mLogger.log(Level.WARNING, "Task (" + pTask.getName()
							+ ") is not in the database");
				}
			}

			return retVal;
		}
	}

	@Override
	public Vector<Task> getAllTasks() {
		mLogger.log(Level.INFO, "getAllTasks called");

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			Vector<Task> tasks = new Vector<Task>();
			Hashtable<Long, Task> taskHash = new Hashtable<Long, Task>();

			String sortOrder = TaskEntry._ID + " DESC";

			Cursor cursor = query(Uri.parse(TASK_URI), // The table to
					// query
					sTaskProjection, // The columns to return
					null, // The columns for the WHERE clause
					null, // The values for the WHERE clause
					sortOrder // The sort order
					);
			cursor.moveToFirst();

			mLogger.log(Level.INFO, cursor.getCount()
					+ " Task rows founds");
			
			for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
				Task task = getTaskFromCursor(cursor);

				if (task != null) {
					mLogger.log(Level.INFO, "Putting " + task.getName() + " : "
							+ task.getKey() + " in hashmap");
					taskHash.put(task.getKey(), task);
				} else {
					mLogger.log(Level.SEVERE,
							"Failed to extract Task from database");
				}
			}

			cursor = query(Uri.parse(RECURRING_TASK_URI), // The table to query
					sRecurringTaskProjection, // The columns to return
					null, // The columns for the WHERE clause
					null, // The values for the WHERE clause
					sortOrder // The sort order
					);
			cursor.moveToFirst();

			for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
				RecurringTask task = getRecurringTaskFromCursor(cursor,
						taskHash);

				if (task != null) {
					mLogger.log(Level.INFO, "Adding " + task.getName()
							+ " to return vector");
					tasks.add(task);
				} else {
					mLogger.log(Level.SEVERE,
							"Failed to extract RecurringTask from database");
				}
			}

			Task task;

			// Add tasks from the hashmap to the vector to be returned
			for (Iterator<Task> it = taskHash.values().iterator(); it.hasNext(); it.remove()) {
				task = it.next();
				mLogger.log(Level.INFO, "Adding " + task.getName()
						+ " to return vector");
				tasks.add(task);
			}

			return tasks;
		}
	}

	@Override
	public Vector<Task> getAllIncompleteTasks() {
		mLogger.log(Level.INFO, "getAllIncompleteTasks called");

		Vector<Task> tasks = new Vector<Task>();
		Hashtable<Long, Task> taskHash = new Hashtable<Long, Task>();

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}

			String sortOrder = TaskEntry.COLUMN_NAME_DUE_DATE + " DESC";
			String selection = TaskEntry.COLUMN_NAME_TASK_COMPLETED + " LIKE ?";
			String[] selectionArgs = { String.valueOf(false) };

			Cursor cursor = query(Uri.parse(TASK_URI), // The table to query
					sTaskProjection, // The columns to return
					selection, // The columns for the WHERE clause
					selectionArgs, // The values for the WHERE clause
					sortOrder // The sort order
					);
			cursor.moveToFirst();

			mLogger.log(Level.INFO, cursor.getCount()
					+ " incomplete Task rows founds");
			for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
				Task task = getTaskFromCursor(cursor);

				if (task != null) {
					mLogger.log(Level.INFO, "Putting " + task.getName() + " : "
							+ task.getKey() + " in hash map");
					taskHash.put(task.getKey(), task);
				} else {
					mLogger.log(Level.SEVERE,
							"Failed to extract Incomplete Task from database");
				}		
			}

			cursor = query(Uri.parse(RECURRING_TASK_URI), // The table to query
					sRecurringTaskProjection, // The columns to return
					selection, // The columns for the WHERE clause
					selectionArgs, // The values for the WHERE clause
					sortOrder // The sort order
					);
			cursor.moveToFirst();

			mLogger.log(Level.INFO, cursor.getCount()
					+ " incomplete Recurring Task rows found");

			for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
				RecurringTask task = getRecurringTaskFromCursor(cursor,
						taskHash);

				if (task != null) {
					mLogger.log(Level.INFO, "Adding " + task.getName()
							+ " to return vector");
					tasks.add(task);
				} else {
					mLogger.log(Level.SEVERE,
							"Failed to extract RecurringTask from database");
				}
			}

			Task task;
			// Add tasks from the hashmap to the vector to be returned
			for (Iterator<Task> it = taskHash.values().iterator(); it.hasNext(); it.remove()) {
				task = it.next();
				mLogger.log(Level.INFO, "Adding " + task.getName()
						+ " to return vector");
				tasks.add(task);
			}
		}

		return tasks;
	}

	@Override
	public Vector<Task> getAllCompletedTasks() {
		mLogger.log(Level.INFO, "getAllCompletedTasks called");

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			Vector<Task> tasks = new Vector<Task>();
			Hashtable<Long, Task> taskHash = new Hashtable<Long, Task>();

			String sortOrder = TaskEntry.COLUMN_NAME_COMPLETION_DATE + " DESC";
			String selection = TaskEntry.COLUMN_NAME_TASK_COMPLETED + " LIKE ?";
			String[] selectionArgs = { String.valueOf(true) };

			Cursor cursor = query(Uri.parse(TASK_URI), // The table to query
					sTaskProjection, // The columns to return
					selection, // The columns for the WHERE clause
					selectionArgs, // The values for the WHERE clause
					sortOrder // The sort order
					);
			cursor.moveToFirst();

			mLogger.log(Level.INFO, cursor.getCount() + " complete Task rows found");

			for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
				Task task = getTaskFromCursor(cursor);

				if (task != null) {
					mLogger.log(Level.INFO, "Adding " + task.getName()
							+ " to return vector");
					tasks.add(task);
					mLogger.log(Level.INFO, "Putting " + task.getName() + " : "
							+ task.getKey() + " in hashmap");
					taskHash.put(task.getKey(), task);
				} else {
					mLogger.log(Level.SEVERE,
							"Failed to extract Completed Task from database");
				}
			}

			cursor = query(Uri.parse(RECURRING_TASK_URI), // The table to query
					sRecurringTaskProjection, // The columns to return
					selection, // The columns for the WHERE clause
					selectionArgs, // The values for the WHERE clause
					sortOrder // The sort order
					);
			cursor.moveToFirst();

			mLogger.log(Level.INFO, cursor.getCount()
					+ " Recurring Task rows found");

			for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
				RecurringTask task = getRecurringTaskFromCursor(cursor,
						taskHash);

				if (task != null) {
					mLogger.log(Level.INFO, "Adding " + task.getName()
							+ " to return vector");
					tasks.add(task);
				} else {
					mLogger.log(Level.SEVERE,
							"Failed to extract RecurringTask from database");
				}
			}

			return tasks;		
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.storage.ITaskCursor#getAllCompletedTasksCursor()
	 */
	@Override
	public Cursor getAllCompletedTasksCursor() {
		mLogger.log(Level.INFO, "getAllCompletedTasksCursor called");

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			String sortOrder = TaskEntry.COLUMN_NAME_COMPLETION_DATE + " DESC";
			String selection = TaskEntry.COLUMN_NAME_TASK_COMPLETED + " LIKE ?";
			String[] selectionArgs = { String.valueOf(true) };

			Cursor cursor = query(Uri.parse(TASK_URI), // The table to query
					sTaskProjection, // The columns to return
					selection, // The columns for the WHERE clause
					selectionArgs, // The values for the WHERE clause
					sortOrder // The sort order
					);

			cursor.moveToFirst();

			return cursor;
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.storage.ITaskCursor#getAllTasksCursor()
	 */
	@Override
	public Cursor getAllTasksCursor() {
		mLogger.log(Level.INFO, "getAllTasksCursor called");

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			String sortOrder = TaskEntry.COLUMN_NAME_DUE_DATE + " DESC";

			Cursor cursor = query(Uri.parse(TASK_URI), // The table to query
					sTaskProjection, // The columns to return
					null, // The columns for the WHERE clause
					null, // The values for the WHERE clause
					sortOrder // The sort order
					);

			cursor.moveToFirst();

			return cursor;
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.storage.ITaskCursor#getAllIncompleteTasksCursor()
	 */
	@Override
	public Cursor getAllIncompleteTasksCursor() {
		mLogger.log(Level.INFO, "getAllIncompleteTasksCursor called");

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			String sortOrder = TaskEntry.COLUMN_NAME_DUE_DATE + " DESC";
			String selection = TaskEntry.COLUMN_NAME_TASK_COMPLETED + " LIKE ?";
			String[] selectionArgs = { String.valueOf(false) };

			Cursor cursor = query(Uri.parse(TASK_URI), // The table to query
					sTaskProjection, // The columns to return
					selection, // The columns for the WHERE clause
					selectionArgs, // The values for the WHERE clause
					sortOrder // The sort order
					);

			cursor.moveToFirst();

			return cursor;
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.storage.ITaskCursor#getAllCompletedRecurringTasksCursor()
	 */
	@Override
	public Cursor getAllCompletedRecurringTasksCursor() {
		mLogger.log(Level.INFO, "getAllCompletedRecurringTasksCursor called");

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}					

			String sortOrder = TaskEntry.COLUMN_NAME_COMPLETION_DATE + " DESC";
			String selection = TaskEntry.COLUMN_NAME_TASK_COMPLETED + " LIKE ?";
			String[] selectionArgs = { String.valueOf(true) };

			Cursor cursor = query(Uri.parse(RECURRING_TASK_URI), // The table to query
					sRecurringTaskProjection, // The columns to return
					selection, // The columns for the WHERE clause
					selectionArgs, // The values for the WHERE clause
					sortOrder // The sort order
					);

			cursor.moveToFirst();

			return cursor;
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.storage.ITaskCursor#getAllRecurringTasksCursor()
	 */
	@Override
	public Cursor getAllRecurringTasksCursor() {
		mLogger.log(Level.INFO, "getAllRecurringTasksCursor called");

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			String sortOrder = TaskEntry.COLUMN_NAME_DUE_DATE + " DESC";

			Cursor cursor = query(Uri.parse(RECURRING_TASK_URI), // The table to query
					sRecurringTaskProjection, // The columns to return
					null, // The columns for the WHERE clause
					null, // The values for the WHERE clause
					sortOrder // The sort order
					);
			cursor.moveToFirst();

			return cursor;
		}
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.storage.ITaskCursor#getAllIncompleteRecurringTasksCursor()
	 */
	@Override
	public Cursor getAllIncompleteRecurringTasksCursor() {
		mLogger.log(Level.INFO, "getAllIncompleteRecurringTasksCursor called");

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			String sortOrder = TaskEntry.COLUMN_NAME_DUE_DATE + " DESC";
			String selection = TaskEntry.COLUMN_NAME_TASK_COMPLETED + " LIKE ?";
			String[] selectionArgs = { String.valueOf(false) };

			Cursor cursor = query(Uri.parse(RECURRING_TASK_URI), // The table to query
					sRecurringTaskProjection, // The columns to return
					selection, // The columns for the WHERE clause
					selectionArgs, // The values for the WHERE clause
					sortOrder // The sort order
					);

			cursor.moveToFirst();

			return cursor;
		}
	}
	
	private boolean create(RecurringTask pRecurringTask){
		mLogger.log(Level.INFO, "Creating Recurring Task : " + 
	       pRecurringTask.getName());
		boolean retVal = false;

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			ContentValues values = getContentValues(pRecurringTask);
			Uri uri = null;
			long rowId = -1;

			uri = insert(Uri.parse(RECURRING_TASK_URI), values);
		
			if (uri == null) {
				retVal = false;
				mLogger.log(Level.SEVERE,
						"Failed to create Task (" + pRecurringTask.getName()
						+ ") in database");
			} else {
				rowId = ContentUris.parseId(uri);
				pRecurringTask.setKey(rowId);
				retVal = true;
				mLogger.log(Level.INFO,
				   "Succesful insert of Recurring Task (" + 
				   pRecurringTask.getName() + ") in database");
			}
		}

		return retVal;
	}

	private boolean create(Task pTask) {
		mLogger.log(Level.INFO, "Creating " + pTask.getName());
		boolean retVal = false;

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			ContentValues values = getContentValues(pTask);
			Uri uri = null;
			long rowId = -1;

			uri = insert(Uri.parse(TASK_URI), values);
			
			if (uri == null) {
				retVal = false;
				mLogger.log(Level.SEVERE,
						"Failed to create Task (" + pTask.getName()
						+ ") in database");
			} else {
				rowId = ContentUris.parseId(uri);
				pTask.setKey(rowId);
				retVal = true;
				mLogger.log(Level.INFO,
						"Succesful insert of Task (" + pTask.getName()
						+ ") in database");
			}
		}

		return retVal;
	}

	private boolean update(RecurringTask pRecurringTask){
		mLogger.log(Level.INFO, "Updating Recurring Task " + 
	       pRecurringTask.getName());
		boolean retVal = false;

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			ContentValues values = getContentValues(pRecurringTask);
			String selection = RecurringTaskEntry._ID + " LIKE ?";
			String[] selectionArgs = { String.valueOf(pRecurringTask.getKey()) };
			int count = 0;

			count = update(Uri.parse(RECURRING_TASK_URI), values,
						selection, selectionArgs);
			
			if (count != 1) {
				retVal = false;
				mLogger.log(Level.SEVERE, "Updated " + count + 
						" rows for task ("
						+ pRecurringTask.getName() + ") in database");
			} else {
				retVal = true;
				mLogger.log(Level.INFO,
				   "Succesful update of Recurring Task (" + 
				   pRecurringTask.getName() + ") in database");
			}

			return retVal;
		}
	}
	
	private boolean update(Task pTask) {
		mLogger.log(Level.INFO, "Updating " + pTask.getName());
		boolean retVal = false;

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}			

			ContentValues values = getContentValues(pTask);
			String selection = TaskEntry._ID + " LIKE ?";
			String[] selectionArgs = { String.valueOf(pTask.getKey()) };
			int count = 0;

			if (pTask instanceof com.scratch.data.types.RecurringTask) {
				mLogger.log(Level.INFO, "Updating Recurring Task : " + pTask.getName());
				count = update(Uri.parse(RECURRING_TASK_URI), values,
						selection, selectionArgs);
			} else if (pTask instanceof com.scratch.data.types.Task) {
				mLogger.log(Level.INFO, "Updating Task : " + pTask.getName());
				count = update(Uri.parse(TASK_URI), values, selection,
						selectionArgs);
			} else {
				mLogger.log(Level.WARNING, "Unable to update unknown task type: "
						+ pTask.getName());
			}

			if (count != 1) {
				retVal = false;
				mLogger.log(Level.SEVERE, "Updated " + count + " rows for task ("
						+ pTask.getName() + ") in database");
			} else {
				retVal = true;
				mLogger.log(Level.INFO,
						"Succesful update of Task (" + pTask.getName()
						+ ") in database");
			}

			return retVal;
		}
	}

	private ContentValues getContentValues(Task pTask) {
		ContentValues values = new ContentValues();

		values.put(TaskEntry.COLUMN_NAME_NAME, pTask.getName());
		values.put(TaskEntry.COLUMN_NAME_DETAILS, pTask.getDetails());
		values.put(TaskEntry.COLUMN_NAME_DATE_PLANNED, pTask.getDatePlanned()
				.getTime());
		values.put(TaskEntry.COLUMN_NAME_REMINDER_DATE, pTask.getReminderDate()
				.getTime());
		values.put(TaskEntry.COLUMN_NAME_LAST_NOTIFY_DATE, pTask.getLastNotifyDate()
				.getTime());
		values.put(TaskEntry.COLUMN_NAME_REMINDER_LEVEL, pTask
				.getReminderLevel().name());
		values.put(TaskEntry.COLUMN_NAME_DUE_DATE, pTask.getDueDate()
				.getTime());
		values.put(TaskEntry.COLUMN_NAME_COMPLETION_DATE, pTask
				.getCompletionDate().getTime());
		values.put(TaskEntry.COLUMN_NAME_TASK_COMPLETED,
				Boolean.toString(pTask.isTaskCompleted()));

		if (pTask instanceof RecurringTask) {
			mLogger.log(Level.INFO, "Getting content values for recurring task : " + 
		       pTask.getName());
			RecurringTask rTask = (RecurringTask) pTask;
			Vector<Task> subTasks = rTask.getTasks();
			int size = subTasks.size();
			long subTaskKeys[] = new long[size + 1];
			subTaskKeys[0] = size;

			for (int i = 0; i < size; i++) {
				subTaskKeys[i + 1] = subTasks.elementAt(i).getKey();
			}

			ByteBuffer byteBuffer = ByteBuffer.allocate(
					subTaskKeys.length*(Long.SIZE/8));
			LongBuffer longBuffer = byteBuffer.asLongBuffer();
			longBuffer.put(subTaskKeys);

			values.put(RecurringTaskEntry.COLUMN_NAME_RECURRENCE_TYPE, 
					rTask.getRecurrence().getRecurrenceType().name());
			values.put(RecurringTaskEntry.COLUMN_NAME_REGULARITY, rTask
					.getRecurrence().getOccurenceRegularity());
			values.put(RecurringTaskEntry.COLUMN_NAME_DAY_OF_MONTH, rTask
					.getRecurrence().getDayOfMonth());
			values.put(RecurringTaskEntry.COLUMN_NAME_ON_MONDAY, 
					Boolean.toString(rTask.getRecurrence().isOnMonday()));
			values.put(RecurringTaskEntry.COLUMN_NAME_ON_TUESDAY, 
					Boolean.toString(rTask.getRecurrence().isOnTuesday()));
			values.put(RecurringTaskEntry.COLUMN_NAME_ON_WEDNESDAY, 
					Boolean.toString(rTask.getRecurrence().isOnWednesday()));
			values.put(RecurringTaskEntry.COLUMN_NAME_ON_THURSDAY, 
					Boolean.toString(rTask.getRecurrence().isOnThursday()));
			values.put(RecurringTaskEntry.COLUMN_NAME_ON_FRIDAY, 
					Boolean.toString(rTask.getRecurrence().isOnFriday()));
			values.put(RecurringTaskEntry.COLUMN_NAME_ON_SATURDAY, 
					Boolean.toString(rTask.getRecurrence().isOnSaturday()));
			values.put(RecurringTaskEntry.COLUMN_NAME_ON_SUNDAY, 
					Boolean.toString(rTask.getRecurrence().isOnSunday()));
			values.put(RecurringTaskEntry.COLUMN_NAME_WEEK_NUMBER, 
					rTask.getRecurrence().getWeekNum().name());
			values.put(RecurringTaskEntry.COLUMN_NAME_MONTH, 
					rTask.getRecurrence().getMonth());
			values.put(RecurringTaskEntry.COLUMN_NAME_USE_DAY_OF_MONTH, 
					Boolean.toString(rTask.getRecurrence().isUseDay()));
			values.put(RecurringTaskEntry.COLUMN_NAME_TIME_OF_DAY, 
					rTask.getRecurrence().getTimeOfDay());
			values.put(RecurringTaskEntry.COLUMN_NAME_TASKS, byteBuffer.array());
		}
mLogger.log(Level.INFO, values.toString());
		return values;
	}

	private Task getTaskFromCursor(Cursor pCursor) {

		try {
			long key = pCursor.getLong(pCursor
					.getColumnIndexOrThrow(TaskEntry._ID));
			String name = pCursor.getString(pCursor
					.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_NAME));
			String details = pCursor.getString(pCursor
					.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DETAILS));
			Date datePlanned = new Date(pCursor.getLong(pCursor
					.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DATE_PLANNED)));
			Date reminderDate = new Date(
					pCursor.getLong(pCursor
							.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_REMINDER_DATE)));
			Date lastNotifyDate = new Date(
					pCursor.getLong(pCursor.getColumnIndexOrThrow(
							TaskEntry.COLUMN_NAME_LAST_NOTIFY_DATE)));
			ReminderLevel reminderLevel = ReminderLevel
					.valueOf(pCursor.getString(pCursor
							.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_REMINDER_LEVEL)));
			Date dueDate = new Date(pCursor.getLong(pCursor
					.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DUE_DATE)));
			Date completionDate = new Date(
					pCursor.getLong(pCursor
							.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_COMPLETION_DATE)));
			boolean taskCompleted = Boolean
					.parseBoolean(pCursor.getString(pCursor
							.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_TASK_COMPLETED)));
			Task task = new Task(name, details, datePlanned, dueDate,
					reminderDate, lastNotifyDate, reminderLevel, completionDate, 
					taskCompleted);
			task.setKey(key);
			return task;
		} catch (IllegalArgumentException iae) {
			mLogger.log(Level.SEVERE,
					"Failed to extract from database: " + iae.getMessage());
			return null;
		}

	}

	private RecurringTask getRecurringTaskFromCursor(Cursor pCursor,
			Hashtable<Long, Task> pTaskHash) {
		mLogger.log(Level.INFO, "Getting recurring task from cursor");

		try {
			long key = pCursor.getLong(pCursor
					.getColumnIndexOrThrow(TaskEntry._ID));
			String name = pCursor.getString(pCursor
					.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_NAME));
			String details = pCursor.getString(pCursor
					.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DETAILS));
			Date datePlanned = new Date(pCursor.getLong(pCursor
					.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DATE_PLANNED)));
			Date reminderDate = new Date(
					pCursor.getLong(pCursor
							.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_REMINDER_DATE)));
			Date lastNotifyDate = new Date(
					pCursor.getLong(pCursor
							.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_LAST_NOTIFY_DATE)));
			ReminderLevel reminderLevel = ReminderLevel
					.valueOf(pCursor.getString(pCursor
							.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_REMINDER_LEVEL)));
			Date dueDate = new Date(pCursor.getLong(pCursor
					.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_DUE_DATE)));
			Date completionDate = new Date(
					pCursor.getLong(pCursor
							.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_COMPLETION_DATE)));
			boolean taskCompleted = Boolean
					.parseBoolean(pCursor.getString(pCursor
							.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_TASK_COMPLETED)));
			RecurrenceType type = RecurrenceType.valueOf(pCursor.getString(
					pCursor.getColumnIndexOrThrow(
							RecurringTaskEntry.COLUMN_NAME_RECURRENCE_TYPE)));
			int regularity = pCursor.getInt(pCursor.getColumnIndexOrThrow(
					RecurringTaskEntry.COLUMN_NAME_REGULARITY));
			int dayOfMonth = pCursor.getInt(pCursor.getColumnIndexOrThrow(
					RecurringTaskEntry.COLUMN_NAME_DAY_OF_MONTH));
			boolean onMonday = Boolean.valueOf(
					pCursor.getString(pCursor.getColumnIndexOrThrow(
							RecurringTaskEntry.COLUMN_NAME_ON_MONDAY)));
			boolean onTuesday = Boolean.valueOf(
					pCursor.getString(pCursor.getColumnIndexOrThrow(
							RecurringTaskEntry.COLUMN_NAME_ON_TUESDAY)));
			boolean onWednesday = Boolean.valueOf(
					pCursor.getString(pCursor.getColumnIndexOrThrow(
							RecurringTaskEntry.COLUMN_NAME_ON_WEDNESDAY)));
			boolean onThursday = Boolean.valueOf(
					pCursor.getString(pCursor.getColumnIndexOrThrow(
							RecurringTaskEntry.COLUMN_NAME_ON_THURSDAY)));
			boolean onFriday = Boolean.valueOf(
					pCursor.getString(pCursor.getColumnIndexOrThrow(
							RecurringTaskEntry.COLUMN_NAME_ON_FRIDAY)));
			boolean onSaturday = Boolean.valueOf(
					pCursor.getString(pCursor.getColumnIndexOrThrow(
							RecurringTaskEntry.COLUMN_NAME_ON_SATURDAY)));
			boolean onSunday = Boolean.valueOf(
					pCursor.getString(pCursor.getColumnIndexOrThrow(
							RecurringTaskEntry.COLUMN_NAME_ON_SUNDAY)));
			WeekNumber weekNum = WeekNumber.valueOf(pCursor.getString(
					pCursor.getColumnIndexOrThrow(
							RecurringTaskEntry.COLUMN_NAME_WEEK_NUMBER)));
			int month = pCursor.getInt(pCursor.getColumnIndexOrThrow(
					RecurringTaskEntry.COLUMN_NAME_MONTH));
			boolean useDayOfMonth = Boolean.valueOf(pCursor.getString(
					pCursor.getColumnIndexOrThrow(
							RecurringTaskEntry.COLUMN_NAME_USE_DAY_OF_MONTH)));
			long timeOfDay = pCursor.getLong(pCursor.getColumnIndexOrThrow(
					RecurringTaskEntry.COLUMN_NAME_TIME_OF_DAY));
			TaskRecurrence recurrence = new TaskRecurrence(type, regularity, 
					dayOfMonth, 
					onMonday, onTuesday, onWednesday, onThursday, onFriday, 
					onSaturday, onSunday, weekNum, month, useDayOfMonth, timeOfDay);
			byte[] buffer = pCursor
					.getBlob(pCursor
							.getColumnIndexOrThrow(RecurringTaskEntry.COLUMN_NAME_TASKS));
			ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
			LongBuffer longBuffer = byteBuffer.asLongBuffer();
			long[] taskKeys = new long[longBuffer.limit()];
			longBuffer.get(taskKeys);
			
			RecurringTask recurringTask = new RecurringTask(name, details,
					datePlanned, dueDate, reminderDate, lastNotifyDate, reminderLevel,
					recurrence, completionDate, taskCompleted);
			recurringTask.setKey(key);

			for (int i = 1; i <= taskKeys[0]; i++) {
				Task task = pTaskHash.remove(taskKeys[i]);
				
				if (task != null) {
				   recurringTask.addTask(task);
				}
			}

			return recurringTask;
		} catch (IllegalArgumentException iae) {
			mLogger.log(Level.SEVERE,
					"Failed to extract from database: " + iae.getMessage());
			return null;
		}
	}

	public void initialize() {
		mLogger.log(Level.INFO, "initialize database");

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				TaskDbHelper dbHelper = new TaskDbHelper(mContext);
				mDb = dbHelper.getWritableDatabase();
				DATABASE_READY = true;
			}
		}
	}

	public void shutdown() {
		mLogger.log(Level.INFO, "shutting down");
		synchronized (DATABASE_READY) {
			if (DATABASE_READY) {
				mDb.close();
				DATABASE_READY=false;
			}
		}
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

		mUriMatcher.addURI(AUTHORITY, TaskEntry.TABLE_NAME, TASK_URI_CODE);
		mUriMatcher.addURI(AUTHORITY, RecurringTaskEntry.TABLE_NAME, 
				RECURRING_TASK_URI_CODE);
		mContext = this.getContext();

		return true;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri pUri, String[] pProjection, String pSelection,
			String[] pSelectionArgs, String pSortOrder) {
		Cursor cursor = null;

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}

			switch (mUriMatcher.match(pUri)){

			case TASK_URI_CODE:
				cursor = mDb.query(TaskEntry.TABLE_NAME, // The table to
						// query
						pProjection, // The columns to return
						pSelection, // The columns for the WHERE clause
						pSelectionArgs, // The values for the WHERE clause
						null, // don't group the rows
						null, // don't filter by row groups
						pSortOrder // The sort order
						);
				cursor.moveToFirst();
				break;
			case RECURRING_TASK_URI_CODE:
				cursor = mDb.query(RecurringTaskEntry.TABLE_NAME, // The table to
						// query
						pProjection, // The columns to return
						pSelection, // The columns for the WHERE clause
						pSelectionArgs, // The values for the WHERE clause
						null, // don't group the rows
						null, // don't filter by row groups
						pSortOrder // The sort order
						);
				cursor.moveToFirst();
				break;
			default:
				mLogger.log(Level.WARNING, "Unknown URI: "
						+ pUri.toString());
			}
		}

		return cursor;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri pUri) {
		String retString = "";

		switch (mUriMatcher.match(pUri)){

		case TASK_URI_CODE:
			retString = TASK_MIME;
			break;
		case RECURRING_TASK_URI_CODE:
			retString = RECURRING_TASK_MIME;
			break;
		default:
			mLogger.log(Level.WARNING, "Unknown URI: "
					+ pUri.toString());
		}

		return retString;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri pUri, ContentValues pValues) {
		long rowId = 0;
		Uri retUri = null;
		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}

			switch (mUriMatcher.match(pUri)){

			case TASK_URI_CODE:
				rowId = mDb.insert(TaskEntry.TABLE_NAME, null, pValues);
				break;
			case RECURRING_TASK_URI_CODE:
				rowId = mDb.insert(RecurringTaskEntry.TABLE_NAME, null, pValues);
				break;
			default:
				mLogger.log(Level.WARNING, "Unknown URI: "
						+ pUri.toString());
			}
		}

		if (rowId < 0) {
			mLogger.log(Level.SEVERE,
					"Insert of ContentValues: " + pValues + 
					" into database failed for URI: " + pUri);
		} else {
			retUri = ContentUris.withAppendedId(pUri, rowId);
			mLogger.log(Level.INFO,
					"Succesfully inserted ContentValues: " + pValues + 
					" into database for URI: " + pUri);
		}

		return retUri;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri pUri, String pSelection, String[] pSelectionArgs) {
		int count = 0;

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}

			switch (mUriMatcher.match(pUri)){

			case TASK_URI_CODE:
				count = mDb.delete(TaskEntry.TABLE_NAME,
						pSelection, pSelectionArgs);
				break;
			case RECURRING_TASK_URI_CODE:
				count = mDb.delete(RecurringTaskEntry.TABLE_NAME,
						pSelection, pSelectionArgs);
				break;
			default:
				mLogger.log(Level.WARNING, "Unknown URI: "
						+ pUri.toString());
			}
		}

		if (count == 0) {
			mLogger.log(Level.WARNING,
					"Now rows deleted from database " + 
							"for URI: " + pUri);
		} else {
			mLogger.log(Level.INFO,
					"Succesfully deleted from " + 
							"database for URI: " + pUri);
		}

		return count;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri pUri, ContentValues pValues, String pSelection,
			String[] pSelectionArgs) {
		int count = 0;

		synchronized (DATABASE_READY) {
			if (!DATABASE_READY){
				initialize();
			}

			switch (mUriMatcher.match(pUri)){

			case TASK_URI_CODE:
				count = mDb.update(TaskEntry.TABLE_NAME, pValues,
						pSelection, pSelectionArgs);
				break;
			case RECURRING_TASK_URI_CODE:
				count = mDb.update(RecurringTaskEntry.TABLE_NAME, pValues,
						pSelection, pSelectionArgs);
				break;
			default:
				mLogger.log(Level.WARNING, "Unknown URI: "
						+ pUri.toString());
			}
		}

		if (count == 0) {
			mLogger.log(Level.WARNING,
					"No rows updated in database " + 
							"for URI: " + pUri);
		} else {
			mLogger.log(Level.INFO,
					"Succesfully updated in " + 
							"database for URI: " + pUri);
		}

		return count;
	}
}