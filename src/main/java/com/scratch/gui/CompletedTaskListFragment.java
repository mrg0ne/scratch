package com.scratch.gui;

import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.database.Cursor;
import androidx.fragment.app.FragmentPagerAdapter;

import com.scratch.data.storage.DbStorage;
import com.scratch.data.storage.TaskContract.RecurringTaskEntry;
import com.scratch.data.storage.TaskContract.TaskEntry;
import com.scratch.data.types.Task;

public class CompletedTaskListFragment extends TaskListFragment {

	private Logger mLogger;
	
	public static final CompletedTaskListFragment newInstance(){
		return new CompletedTaskListFragment();
	}
	
	public CompletedTaskListFragment(){
		super();
		mLogger = Logger.getLogger(this.getClass().getName());
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
		return TaskEntry.COLUMN_NAME_DUE_DATE + " DESC";
	}

	@Override
	protected String getRecurringTaskSortOrder() {
		return RecurringTaskEntry.COLUMN_NAME_DUE_DATE + " DESC";
	}

	@Override
	protected String[] getTaskSelectionArgs() {
		return new String[]{ String.valueOf(true) };
	}

	@Override
	protected String[] getRecurringTaskSelectionArgs() {
		return new String[]{ String.valueOf(false) };
	}

	@Override
	protected Vector<Task> reloadDataCallback() {
		Vector<Task> taskVec = mDbStorage.getAllCompletedTasks();
		mLogger.log(Level.INFO, "reloadDataCallback returning " + 
		   taskVec.size() + " complete tasks");
		return taskVec;
	}

	/* (non-Javadoc)
	 * @see com.scratch.gui.TaskListFragment#getTaskCursor()
	 */
	@Override
	protected Cursor getTaskCursor() {
		Cursor cursor = ((DbStorage)mDbStorage).getAllCompletedTasksCursor();
	    return cursor;
	}

	/* (non-Javadoc)
	 * @see com.scratch.gui.TaskListFragment#getRecurringTaskCursor()
	 */
	@Override
	protected Cursor getRecurringTaskCursor() {
		Cursor cursor = ((DbStorage)mDbStorage).getAllCompletedRecurringTasksCursor();
	    return cursor;
	}

}
