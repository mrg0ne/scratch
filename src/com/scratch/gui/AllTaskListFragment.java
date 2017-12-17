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

public class AllTaskListFragment extends TaskListFragment {

	private Logger mLogger;
	
	public static final AllTaskListFragment newInstance(){
		return new AllTaskListFragment();
	}
	
	public AllTaskListFragment(){
		super();
		mLogger = Logger.getLogger(this.getClass().getName());
	}

	@Override
	protected String getTaskSelection() {
		return "";
	}

	@Override
	protected String getRecurringTaskSelection() {
		return "";
	}

	@Override
	protected String getTaskSortOrder() {
		return TaskEntry.COLUMN_NAME_DUE_DATE + " DESC";
	}

	@Override
	protected String getRecurringTaskSortOrder() {
		return RecurringTaskEntry.COLUMN_NAME_DUE_DATE + " DESC";
	}

	@Override
	protected String[] getTaskSelectionArgs() {
		return new String[]{};
	}

	@Override
	protected String[] getRecurringTaskSelectionArgs() {
		return new String[]{};
	}

	@Override
	protected Vector<Task> reloadDataCallback() {
		Vector<Task> taskVec = mDbStorage.getAllTasks();
		mLogger.log(Level.INFO, "reloadDataCallback returning " + 
		   taskVec.size() + " tasks");
		return taskVec;
	}

	/* (non-Javadoc)
	 * @see com.scratch.gui.TaskListFragment#getTaskCursor()
	 */
	@Override
	protected Cursor getTaskCursor() {
		Cursor cursor = ((DbStorage)mDbStorage).getAllTasksCursor();
	    return cursor;
	}

	/* (non-Javadoc)
	 * @see com.scratch.gui.TaskListFragment#getRecurringTaskCursor()
	 */
	@Override
	protected Cursor getRecurringTaskCursor() {
		Cursor cursor = ((DbStorage)mDbStorage).getAllRecurringTasksCursor();
	    return cursor;
	}

}
