package com.scratch.data.storage;

import android.database.Cursor;

// Cursor interface to Task data storage
public interface ITaskCursor {

	// Return a cursor containing all completed tasks
	public Cursor getAllCompletedTasksCursor();
	
	// Return a cursor containing all tasks
	public Cursor getAllTasksCursor();
	
	// Return a cursor containing all incomplete tasks
	public Cursor getAllIncompleteTasksCursor();
	
	// Return a cursor containing all completed recurring tasks
    public Cursor getAllCompletedRecurringTasksCursor();
	
    // Return a cursor containing all recurring tasks
	public Cursor getAllRecurringTasksCursor();
	
	// Return a cursor containing all incomplete recurring tasks
	public Cursor getAllIncompleteRecurringTasksCursor();
}
