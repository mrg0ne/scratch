package com.scratch.data.storage;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.data.storage.TaskContract.RecurringTaskEntry;
import com.scratch.data.storage.TaskContract.TaskEntry;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TaskDbHelper extends SQLiteOpenHelper {
	
	public static final int DATABASE_VERSION = 1;
	public static final String DATABASE_NAME = "scratch.db";
	

	private static final String TEXT_TYPE = " TEXT";
	private static final String INTEGER_TYPE = " INTEGER";
	private static final String NULL_TYPE = " NULL";
	private static final String BLOB_TYPE = " BLOB";
	private static final String COMMA_SEP = ",";
	private static final String SQL_CREATE_TASK_ENTRIES =
	   "CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
	   TaskEntry._ID + " INTEGER PRIMARY KEY," + 
	   TaskEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
	   TaskEntry.COLUMN_NAME_DETAILS + TEXT_TYPE + COMMA_SEP +
	   TaskEntry.COLUMN_NAME_DATE_PLANNED + INTEGER_TYPE + COMMA_SEP +
	   TaskEntry.COLUMN_NAME_REMINDER_DATE + INTEGER_TYPE + COMMA_SEP +
	   TaskEntry.COLUMN_NAME_LAST_NOTIFY_DATE + INTEGER_TYPE + COMMA_SEP +
	   TaskEntry.COLUMN_NAME_REMINDER_LEVEL + INTEGER_TYPE + COMMA_SEP +
	   TaskEntry.COLUMN_NAME_DUE_DATE + INTEGER_TYPE + COMMA_SEP +
	   TaskEntry.COLUMN_NAME_TASK_COMPLETED + TEXT_TYPE + COMMA_SEP +
	   TaskEntry.COLUMN_NAME_COMPLETION_DATE + INTEGER_TYPE +
	   " )";
	
	private static final String SQL_DELETE_TASK_ENTRIES =
	   "DROP TABLE IF EXISTS " + TaskEntry.TABLE_NAME;
	
	private static final String SQL_CREATE_RECURRING_TASK_ENTRIES =
	   "CREATE TABLE " + RecurringTaskEntry.TABLE_NAME + " (" +
	   RecurringTaskEntry._ID + " INTEGER PRIMARY KEY," + 
	   RecurringTaskEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_DETAILS + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_DATE_PLANNED + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_REMINDER_DATE + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_LAST_NOTIFY_DATE + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_REMINDER_LEVEL + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_DUE_DATE + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_TASK_COMPLETED + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_COMPLETION_DATE + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_RECURRENCE_TYPE + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_REGULARITY + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_DAY_OF_MONTH + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_ON_MONDAY + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_ON_TUESDAY + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_ON_WEDNESDAY + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_ON_THURSDAY + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_ON_FRIDAY + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_ON_SATURDAY + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_ON_SUNDAY + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_WEEK_NUMBER + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_MONTH + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_USE_DAY_OF_MONTH + TEXT_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_TIME_OF_DAY + INTEGER_TYPE + COMMA_SEP +
	   RecurringTaskEntry.COLUMN_NAME_TASKS + BLOB_TYPE +
	   " )";
	
	private static final String SQL_DELETE_RECURRING_TASK_ENTRIES =
	   "DROP TABLE IF EXISTS " + RecurringTaskEntry.TABLE_NAME;

	private Logger mLogger;
	
	public TaskDbHelper(Context pContext) {
		super(pContext, DATABASE_NAME, null, DATABASE_VERSION);
		mLogger = Logger.getLogger(this.getClass().getName());
	}

	@Override
	public void onCreate(SQLiteDatabase pDb) {
	   mLogger.log(Level.INFO, "Creating databases");
	   pDb.execSQL(SQL_CREATE_TASK_ENTRIES);
	   pDb.execSQL(SQL_CREATE_RECURRING_TASK_ENTRIES);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase pDb, int pOldVersion, int pNewVersion) {
	   mLogger.log(Level.WARNING, "onUpgrade called");

	}
	
}
