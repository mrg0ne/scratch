package com.scratch.data.storage;

import com.scratch.data.types.Task;

import java.util.Vector;

public interface IDataStorage {

	public static final String DATASET_CHANGE = "com.scratch.DATASET_CHANGE";
	
	boolean save(Task pTask);
	
	boolean delete(Task pTask);
	
	Vector<Task> getAllTasks();
	
	Vector<Task> getAllIncompleteTasks();
	
	Vector<Task> getAllCompletedTasks();
	
	void initialize();
	
	void shutdown();
}
