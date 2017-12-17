package com.scratch.data.storage;

import android.provider.BaseColumns;

public final class TaskContract {
	
	public TaskContract() {}
	
	public static abstract class TaskEntry implements BaseColumns {
		public static final String TABLE_NAME = "task";
		public static final String COLUMN_NAME_NAME = "name";
		public static final String COLUMN_NAME_DETAILS = "details";
		public static final String COLUMN_NAME_DATE_PLANNED = "date_planned";
		public static final String COLUMN_NAME_DUE_DATE = "due_date";
		public static final String COLUMN_NAME_REMINDER_DATE = "reminder_date";
		public static final String COLUMN_NAME_LAST_NOTIFY_DATE = "last_notify_date";
		public static final String COLUMN_NAME_REMINDER_LEVEL = "reminder_level";
		public static final String COLUMN_NAME_COMPLETION_DATE = "completion_date";
		public static final String COLUMN_NAME_TASK_COMPLETED = "is_task_complete";
	}
	
	public static abstract class RecurringTaskEntry extends TaskEntry {
		public static final String TABLE_NAME = "recurring_task";
		public static final String COLUMN_NAME_TASKS = "tasks";
		public static final String COLUMN_NAME_RECURRENCE_TYPE = "recurrence_type";
		public static final String COLUMN_NAME_REGULARITY = "regularity";
		public static final String COLUMN_NAME_DAY_OF_MONTH = "day_of_month";
		public static final String COLUMN_NAME_ON_MONDAY = "on_monday";
		public static final String COLUMN_NAME_ON_TUESDAY = "on_tuesday";
		public static final String COLUMN_NAME_ON_WEDNESDAY = "on_wednesday";
		public static final String COLUMN_NAME_ON_THURSDAY = "on_thursday";
		public static final String COLUMN_NAME_ON_FRIDAY = "on_friday";
		public static final String COLUMN_NAME_ON_SATURDAY = "on_saturday";
		public static final String COLUMN_NAME_ON_SUNDAY = "on_sunday";
		public static final String COLUMN_NAME_WEEK_NUMBER = "week_number";
		public static final String COLUMN_NAME_MONTH = "month";
		public static final String COLUMN_NAME_USE_DAY_OF_MONTH = "use_day_of_month";
		public static final String COLUMN_NAME_TIME_OF_DAY = "time_of_day";
	}
	
}
