package com.scratch.data.storage;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import android.content.Context;

import com.scratch.data.types.Task;

public class Cache implements IDataStorage {

	private DbStorage mStorage;
	private Vector<Long> mAllCompletedTasks;
	private Vector<Long> mAllIncompleteTasks;
	private Vector<Long> mAllTasks;
	private Hashtable<Long, Task> mTaskHash;
	private Logger mLogger;

	public Cache(Context pContext) {
		mStorage = new DbStorage(pContext);
		mTaskHash = new Hashtable<Long, Task>();
		mAllTasks = new Vector<Long>();
		mAllIncompleteTasks = new Vector<Long>();
		mAllCompletedTasks = new Vector<Long>();
		mLogger = Logger.getLogger(this.getClass().getName());
	}

	public boolean save(Task pTask) {
		mLogger.log(Level.INFO, "save called for task : " + pTask);
		boolean retVal = false;

		retVal = mStorage.save(pTask);

		if (retVal) {
			if (pTask.getKey() == 0) {
				mLogger.log(Level.WARNING, pTask.getName()
						+ " has Key value 0!");
			}

			mTaskHash.put(pTask.getKey(), pTask);
			mAllTasks.add(pTask.getKey());
		} else {
			mLogger.log(Level.WARNING,
					"save failed for task : " + pTask.getName());
		}

		return retVal;
	}

	public boolean delete(Task pTask) {
		mLogger.log(Level.INFO, "delete called for task : " + pTask);
		boolean retVal = false;

		retVal = mStorage.delete(pTask);

		mTaskHash.remove(pTask.getKey());
		mAllTasks.remove(pTask.getKey());

		if (pTask.isTaskCompleted()) {
			mAllCompletedTasks.remove(pTask.getKey());
		} else {
			mAllIncompleteTasks.remove(pTask.getKey());
		}
		if (!retVal) {
			mLogger.log(Level.WARNING,
					"delete failed for task : " + pTask.getName());
		}

		return retVal;
	}

	public Vector<Task> getAllTasks() {
		mLogger.log(Level.INFO, "getAllTasks called");
		Vector<Task> tasks = new Vector<Task>();

		if (mAllTasks.isEmpty()) {
			mLogger.log(Level.INFO, "Empty cache, using DB storage");
			tasks = mStorage.getAllTasks();

			for (int i = 0; i < tasks.size(); i++) {
				Task task = tasks.elementAt(i);
				mTaskHash.put(task.getKey(), task);

				if (task.isTaskCompleted()
						&& !mAllCompletedTasks.contains(task.getKey())) {
					mLogger.log(Level.INFO, "Adding to completed tasks cache");
					mAllCompletedTasks.add(task.getKey());
				} else if (!task.isTaskCompleted()
						&& !mAllIncompleteTasks.contains(task.getKey())) {
					mLogger.log(Level.INFO, "Adding to incomplete tasks cache");
					mAllIncompleteTasks.add(task.getKey());
				}
			}

			mLogger.log(Level.INFO, "returning " + tasks.size() + " tasks");
		} else {

			for (int i = 0; i < mAllTasks.size(); i++) {
				tasks.add(mTaskHash.get(mAllTasks.elementAt(i)));
			}

			mLogger.log(Level.INFO, "Using cache, returning " + tasks.size()
					+ " tasks");
		}

		return tasks;
	}

	public Vector<Task> getAllIncompleteTasks() {
		mLogger.log(Level.INFO, "getAllIncompleteTasks called");
		Vector<Task> tasks = new Vector<Task>();

		if (mAllIncompleteTasks.isEmpty()) {
			mLogger.log(Level.INFO, "Empty cache, using DB storage");
			tasks = mStorage.getAllIncompleteTasks();

			for (int i = 0; i < tasks.size(); i++) {
				Task task = tasks.elementAt(i);
				mTaskHash.put(task.getKey(), task);
				mAllIncompleteTasks.add(task.getKey());
			}

			mLogger.log(Level.INFO, "returning " + tasks.size() + " tasks");
		} else {

			for (int i = 0; i < mAllIncompleteTasks.size(); i++) {
				tasks.add(mTaskHash.get(mAllIncompleteTasks.elementAt(i)));
			}

			mLogger.log(Level.INFO, "Using cache, returning " + tasks.size()
					+ " tasks");
		}

		return tasks;
	}

	public Vector<Task> getAllCompletedTasks() {
		mLogger.log(Level.INFO, "getAllCompletedTasks called");
		Vector<Task> tasks = new Vector<Task>();

		if (mAllCompletedTasks.isEmpty()) {
			mLogger.log(Level.INFO, "Empty cache, using DB storage");
			tasks = mStorage.getAllCompletedTasks();

			for (int i = 0; i < tasks.size(); i++) {
				Task task = tasks.elementAt(i);
				mTaskHash.put(task.getKey(), task);
				mAllCompletedTasks.add(task.getKey());
			}

			mLogger.log(Level.INFO, "returning " + tasks.size() + " tasks");
		} else {

			for (int i = 0; i < mAllCompletedTasks.size(); i++) {
				tasks.add(mTaskHash.get(mAllCompletedTasks.elementAt(i)));
			}

			mLogger.log(Level.INFO, "Using cache, returning " + tasks.size()
					+ " tasks");
		}

		return tasks;
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.storage.IDataStorage#initialize()
	 */
	@Override
	public void initialize() {
		mStorage.initialize();
	}

	/* (non-Javadoc)
	 * @see com.scratch.data.storage.IDataStorage#shutdown()
	 */
	@Override
	public void shutdown() {
		mStorage.shutdown();
	}

}
