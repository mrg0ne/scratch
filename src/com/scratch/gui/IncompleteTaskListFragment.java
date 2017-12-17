package com.scratch.gui;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.database.Cursor;
import android.support.v4.app.FragmentPagerAdapter;

import com.scratch.data.storage.DbStorage;
import com.scratch.data.storage.TaskContract.RecurringTaskEntry;
import com.scratch.data.storage.TaskContract.TaskEntry;
import com.scratch.data.types.Task;

public class IncompleteTaskListFragment extends TaskListFragment{

	private Logger mLogger;
		
	public static final IncompleteTaskListFragment newInstance(){
		return new IncompleteTaskListFragment();
	}
	
	public IncompleteTaskListFragment(){
		super();
		mLogger = Logger.getLogger(this.getClass().getName());
	}

	@Override
	protected Vector<Task> reloadDataCallback() {
		Vector<Task> taskVec = mDbStorage.getAllIncompleteTasks();
		mLogger.log(Level.INFO, "reloadDataCallback returning " + 
		   taskVec.size() + " incomplete tasks");
		return taskVec;
	}

	@Override
	protected String getTaskSelection() {
		return TaskEntry.COLUMN_NAME_TASK_COMPLETED + " LIKE ?";
	}

	@Override
	protected String getRecurringTaskSelection() {
		return RecurringTaskEntry.COLUMN_NAME_TASK_COMPLETED + " LIKE ?";
	}

	@Override
	protected String getTaskSortOrder() {
		return TaskEntry.COLUMN_NAME_DUE_DATE + " ASC";
	}

	@Override
	protected String getRecurringTaskSortOrder() {
		return RecurringTaskEntry.COLUMN_NAME_DUE_DATE + " ASC";
	}

	@Override
	protected String[] getTaskSelectionArgs() {
		return new String[]{ String.valueOf(false) };
	}

	@Override
	protected String[] getRecurringTaskSelectionArgs() {
		return new String[]{ String.valueOf(false) };
	}

	/* (non-Javadoc)
	 * @see com.scratch.gui.TaskListFragment#getTaskCursor()
	 */
	@Override
	protected Cursor getTaskCursor() {
       Cursor cursor = ((DbStorage)mDbStorage).getAllIncompleteTasksCursor();
       return cursor;
	}

	/* (non-Javadoc)
	 * @see com.scratch.gui.TaskListFragment#getRecurringTaskCursor()
	 */
	@Override
	protected Cursor getRecurringTaskCursor() {
		Cursor cursor = ((DbStorage)mDbStorage).getAllIncompleteRecurringTasksCursor();
	    return cursor;
	}
}
