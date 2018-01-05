package com.scratch.gui;

import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.R;
import com.scratch.data.storage.DbStorage;
import com.scratch.data.storage.IDataStorage;
import com.scratch.data.storage.TaskContract;
import com.scratch.data.storage.TaskContract.TaskEntry;
import com.scratch.data.types.Operation;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;
import com.scratch.scheduler.TaskSchedulingService;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.PagerTitleStrip;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;

// This is the abstract base class for fragments that display a list
// of Task objects.
//
// IncompleteTaskListFragment, AllTaskListFragment, CompletedTaskLitFragment
// are expected sub-classes.

public abstract class TaskListFragment extends Fragment implements
View.OnLongClickListener {

	private static final String DELETE_CONFIRMATION_TAG = "DELETE_TASK";
	private static int sIdCounter = 0;

	// The ID of the cursor loader for the task list view
	private int mTaskListLoaderId;

	// The ID of the cursor loader for the recurring task list view
	// private int mRecurringTaskListLoaderId;

	// For debug
	private Logger mLogger;

	// Used to retrieve Task objects from the database
	protected static IDataStorage mDbStorage = null;

	/** Flag indicating whether we have called bind on the service. */
	protected boolean mBound;

	// This is the Adapter being used to display the task list data
	protected SimpleCursorAdapter mTaskListAdapter = null;

	// This is the Adapter being used to display the recurring task list data
	// protected SimpleCursorAdapter mRecurringTaskListAdapter = null;

	// View binder for the task list
	protected TaskViewBinder mTaskViewBinder;

	// For the cursor adapter, specify which columns go into which Task views
	protected String[] mFromTaskColumnsList = {
			TaskContract.TaskEntry.COLUMN_NAME_TASK_COMPLETED,
			TaskContract.TaskEntry.COLUMN_NAME_NAME,
			TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE,
			TaskContract.TaskEntry._ID };

	// For the cursor adapter, specify which columns go into which RecurringTask
	// views
	// protected String[] mFromRecurringTaskColumnsList = {
	// TaskContract.TaskEntry.COLUMN_NAME_TASK_COMPLETED,
	// TaskContract.RecurringTaskEntry.COLUMN_NAME_RECURRENCE_TYPE,
	// TaskContract.TaskEntry.COLUMN_NAME_NAME,
	// TaskContract.TaskEntry.COLUMN_NAME_DUE_DATE,
	// TaskContract.RecurringTaskEntry._ID};

	// The IDs for the Task view
	protected int[] mToTaskViewsList = { com.scratch.R.id.setcomplete,
			com.scratch.R.id.name, com.scratch.R.id.duedate,
			com.scratch.R.id.key };

	// The IDs for the RecurringTask view
	// protected int[] mToRecurringTaskViewsList = {
	// com.scratch.R.id.setcomplete,
	// com.scratch.R.id.tasktype,
	// com.scratch.R.id.name,
	// com.scratch.R.id.duedate};

	// The Task list view
	protected ListView mTaskListView = null;

	// The RecurringTask list view
	// protected ListView mRecurringTaskListView = null;

	/** Messenger for communicating with the service. */
	protected Messenger mService = null;

	// Maps key to Task objects.
	protected Hashtable<String, Task> mTaskHash;

	protected ClickListener mClickListener;

	protected ITaskHashRetrieval mTaskHashRetrieval;

	protected CursorLoaderCallback mCursorLoaderCallback;

	protected FragmentPagerAdapter mPagerAdapter;

	/**
	 * Class for interacting with the main interface of the service.
	 */
	protected ServiceConnection mConnection = new ServiceConnection() {
		public void onServiceConnected(ComponentName pClassName,
				IBinder pService) {
			// This is called when the connection with the service has been
			// established, giving us the object we can use to
			// interact with the service. We are communicating with the
			// service using a Messenger, so here we get a client-side
			// representation of that from the raw IBinder object.
			mService = new Messenger(pService);
			mBound = true;
		}

		public void onServiceDisconnected(ComponentName pClassName) {
			// This is called when the connection with the service has been
			// unexpectedly disconnected -- that is, its process crashed.
			mService = null;
			mBound = false;
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle pSavedInstanceState) {
		super.onCreate(pSavedInstanceState);
		if (mDbStorage == null) {
			mDbStorage = new DbStorage(this.getActivity()
					.getApplicationContext());
		}

		mTaskHash = new Hashtable<String, Task>();
		setHasOptionsMenu(true);

		PagerTitleStrip pagerTitleStrip = (PagerTitleStrip) getActivity()
				.findViewById(R.id.pager_title_strip);
		pagerTitleStrip.setBackgroundColor(Color.GREEN);
		//pagerTitleStrip.setTextColor(Color.BLACK);

		ViewPager viewPager = (ViewPager) this.getActivity().findViewById(
				R.id.pager);
		mPagerAdapter = (FragmentPagerAdapter) viewPager.getAdapter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater,
	 * android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater pInflater, ViewGroup pContainer,
			Bundle pSavedInstanceState) {
		super.onCreateView(pInflater, pContainer, pSavedInstanceState);

		View rootView = pInflater.inflate(R.layout.task_list_fragment_layout,
				pContainer, false);

		mTaskListView = (ListView) rootView
				.findViewById(com.scratch.R.id.tasklist);

		// mRecurringTaskListView = (ListView) rootView.findViewById(
		// com.scratch.R.id.recurringtasklist);

		// Create an empty adapter we will use to display the loaded data.
		// We pass null for the cursor, then update it in onLoadFinished()
		mTaskListAdapter = new SimpleCursorAdapter(mTaskListView.getContext(),
				com.scratch.R.layout.task_fragment_layout, null,
				mFromTaskColumnsList, mToTaskViewsList, 0);

		// mRecurringTaskListAdapter = new SimpleCursorAdapter(
		// mRecurringTaskListView.getContext(),
		// com.scratch.R.layout.task_fragment_layout, null,
		// mFromRecurringTaskColumnsList, mToRecurringTaskViewsList, 0);

		mTaskViewBinder = new TaskViewBinder(mClickListener, mTaskHashRetrieval);
		mTaskListAdapter.setViewBinder(mTaskViewBinder);

		// Sets the data behind this view to the task_fragment_layout
		mTaskListView.setAdapter(mTaskListAdapter);
		// mRecurringTaskListView.setAdapter(mRecurringTaskListAdapter);

		mTaskListLoaderId = getNextId();

		// Prepare the loader. Either re-connect with an existing one,
		// or start a new one.
		getLoaderManager().initLoader(mTaskListLoaderId, null,
				mCursorLoaderCallback);
		return rootView;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onPause()
	 */
	@Override
	public void onPause() {
		super.onPause();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onResume()
	 */
	@Override
	public void onResume() {
		super.onResume();
		mLogger.log(Level.INFO, "onResume called");
	}

	public void onStart() {
		super.onStart();
		// Bind to the service
		this.getActivity().bindService(
				new Intent(this.getActivity(), TaskSchedulingService.class),
				mConnection, Context.BIND_AUTO_CREATE);
	}

	@Override
	public void onStop() {
		super.onStop();
		mLogger.log(Level.INFO, "onStop called");

		// Unbind from the service
		if (mBound) {
			this.getActivity().unbindService(mConnection);
			mBound = false;
		}

		if (mDbStorage != null) {
			mDbStorage.shutdown();
		}
	}

	public boolean onLongClick(View pView) {
		TextView idTextView = (TextView) pView.findViewById(R.id.name);
		String id = idTextView.getText().toString();
		Intent editIntent = new Intent(pView.getContext(),
				EditTaskActivity.class);
		editIntent.setAction(EditTaskActivity.EDIT_TASK);
		Task task = mTaskHash.get(id);

		if (task == null) {
			mLogger.log(Level.WARNING, "no task found in task hash for id: "
					+ id);
			return false;
		} else {
			mLogger.log(Level.INFO,
					"Long click detected for task: " + task.getName()
					+ " with id: " + id);

			editIntent.putExtra(task.getClass().getName(), task);
			editIntent.putExtra(Operation.class.getName(),
					Operation.UPDATE.ordinal());
			startActivityForResult(editIntent, Operation.UPDATE.ordinal());

			return true;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public void onCreateOptionsMenu(Menu pMenu, MenuInflater pInflater) {
		// Inflate the menu items for use in the action bar
		pInflater.inflate(R.menu.task_list_activity_actions, pMenu);
		super.onCreateOptionsMenu(pMenu, pInflater);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem pItem) {
		mLogger.log(Level.INFO, "ActionBar menu item selected");

		// Handle presses on the action bar items
		switch (pItem.getItemId()) {
		case R.id.action_new:
			addNewTask();
			return true;
		default:
			return super.onOptionsItemSelected(pItem);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onCreateContextMenu(android.view.ContextMenu
	 * , android.view.View, android.view.ContextMenu.ContextMenuInfo)
	 */
	@Override
	public void onCreateContextMenu(ContextMenu pMenu, View pView,
			ContextMenuInfo pMenuInfo) {
		super.onCreateContextMenu(pMenu, pView, pMenuInfo);

		AdapterContextMenuInfo info = (AdapterContextMenuInfo) pMenuInfo;
		TextView idTextView = (TextView) info.targetView.findViewById(R.id.key);
		String id = idTextView.getText().toString();
		Task task = mTaskHash.get(id);

		if (task == null) {
			TextView nameTextView = (TextView) info.targetView
					.findViewById(R.id.name);
			String name = nameTextView.getText().toString();
			task = (RecurringTask) mTaskHash.get(name);

			if (task == null) {
				mLogger.log(Level.WARNING,
						"task retrieved from ViewTaskHash is null!");
				return;
			}
		}

		if (task instanceof RecurringTask) {
			getActivity().getMenuInflater().inflate(R.menu.recurring_task_menu,
					pMenu);
		} else {
			getActivity().getMenuInflater().inflate(R.menu.task_menu, pMenu);
		}

		pMenu.setHeaderTitle(task.getName());
		TextView headerView = new TextView(pView.getContext());
		headerView.setTextAlignment(TextView.TEXT_ALIGNMENT_CENTER);
		headerView.setBackgroundColor(Color.GREEN);
		headerView.setText(task.getName());
		headerView.setTextSize(20);
		pMenu.setHeaderView(headerView);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * android.support.v4.app.Fragment#onContextItemSelected(android.view.MenuItem
	 * )
	 */
	@Override
	public boolean onContextItemSelected(MenuItem pItem) {

		if (this.getUserVisibleHint()){
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) pItem
					.getMenuInfo();

			if (info == null) {
				mLogger.log(Level.WARNING, "MenuItem has null MenuInfo!");
				return false;
			}

			Cursor cursor;
			String id;
			String name;
			Task task;
			RecurringTask recurringTask;

			switch (pItem.getItemId()) {
			case R.id.edit:
				mLogger.log(Level.INFO, "Edit task");
				cursor = (Cursor) mTaskListView.getItemAtPosition(info.position);
				id = cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry._ID));
				name = cursor.getString(cursor
						.getColumnIndexOrThrow(TaskEntry.COLUMN_NAME_NAME));
				task = mTaskHash.get(id);
				recurringTask = (RecurringTask) mTaskHash.get(name);

				if (recurringTask != null) {
					editTask(task, recurringTask);
				} else {
					editTask(task);
				}
				return true;
			case R.id.delete:
				mLogger.log(Level.INFO, "Delete task");
				cursor = (Cursor) mTaskListView.getItemAtPosition(info.position);
				id = cursor.getString(cursor.getColumnIndexOrThrow(TaskEntry._ID));
				task = mTaskHash.get(id);
				deleteTask(task);
				return true;
				/*
				 * case R.id.edit_recurring: mLogger.log(Level.INFO,
				 * "Edit recurring task"); // cursor =
				 * (Cursor)mRecurringTaskListView.getItemAtPosition(info.position);
				 * cursor = (Cursor)mTaskListView.getItemAtPosition(info.position);
				 * id = cursor.getString(cursor.getColumnIndexOrThrow(
				 * TaskEntry.COLUMN_NAME_NAME)); task = mTaskHash.get(id);
				 * editTask(task); return true; case R.id.delete_recurring:
				 * mLogger.log(Level.INFO, "Delete recurring task"); // cursor =
				 * (Cursor)mRecurringTaskListView.getItemAtPosition(info.position);
				 * cursor = (Cursor)mTaskListView.getItemAtPosition(info.position);
				 * id = cursor.getString(cursor.getColumnIndexOrThrow(
				 * TaskEntry.COLUMN_NAME_NAME)); task = mTaskHash.get(id);
				 * deleteTask(task); return true;
				 */
			default:
				return super.onContextItemSelected(pItem);
			}
		} else {
			return false;
		}
	}

	private void addNewTask() {
		mLogger.log(Level.INFO, "add new task called");
		Intent newTaskIntent = new Intent(this.getActivity()
				.getApplicationContext(), NewTaskActivity.class);
		newTaskIntent.setAction(NewTaskActivity.NEW_TASK);
		startActivityForResult(newTaskIntent, Operation.ADD.ordinal());
	}

	private void editTask(Task pTask) {
		mLogger.log(Level.INFO, "edit task called");

		Intent editTaskIntent = new Intent(this.getActivity()
				.getApplicationContext(), EditTaskActivity.class);
		editTaskIntent.setAction(EditTaskActivity.EDIT_TASK);
		editTaskIntent.putExtra(Task.class.getName(), pTask);

		startActivityForResult(editTaskIntent, Operation.UPDATE.ordinal());
	}

	private void editTask(Task pTask, RecurringTask pRecurringTask) {
		mLogger.log(Level.INFO, "edit recurring task called");

		Intent editTaskIntent = new Intent(this.getActivity()
				.getApplicationContext(), EditTaskActivity.class);
		editTaskIntent.setAction(EditTaskActivity.EDIT_TASK);
		editTaskIntent.putExtra(RecurringTask.class.getName(), pRecurringTask);
		editTaskIntent.putExtra(Task.class.getName(), pTask);

		startActivityForResult(editTaskIntent, Operation.UPDATE.ordinal());
	}

	private void deleteTask(Task pTask) {
		mLogger.log(Level.INFO, "delete task called");
		String title = getResources().getString(
				com.scratch.R.string.delete_confirmation_message);
		title += " " + pTask.getName();
		ConfirmationDialogFragment confirmDlg = ConfirmationDialogFragment
				.newInstance(title, Task.class.getName(), pTask);
		confirmDlg.setTargetFragment(this, Operation.REMOVE.ordinal());
		confirmDlg.show(getFragmentManager(), DELETE_CONFIRMATION_TAG);
	}

	protected int getNextId() {
		return sIdCounter++;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.support.v4.app.Fragment#onActivityResult(int, int,
	 * android.content.Intent)
	 */
	public void onActivityResult(int pRequestCode, int pResultCode, Intent pData) {
		mLogger.log(Level.INFO, "onActivityResult called");

		if (pResultCode == Activity.RESULT_OK) {
			Task task = null;

			if (pData.hasExtra(Task.class.getName())) {
				mLogger.log(Level.INFO, "Activity returned Task object");
				task = pData.getParcelableExtra(Task.class.getName());
			} else if (pData.hasExtra(RecurringTask.class.getName())) {
				mLogger.log(Level.INFO,
						"Activity returned RecurringTask object");
				task = pData.getParcelableExtra(RecurringTask.class.getName());
			}

			if (task != null) {
				Intent intent = null;
				if ((pRequestCode == Operation.UPDATE.ordinal() || pRequestCode == Operation.ADD
						.ordinal())) {
					intent = new Intent(TaskSchedulingService.UPDATE);
					intent.putExtra(task.getClass().getName(), task);
					// TODO delete this
					if (task instanceof RecurringTask){
						// TODO
						mLogger.log(Level.INFO, "TaskListFragment onActivityResult " + ((RecurringTask)task).getRecurrence().toString());
					}
				} else if (pRequestCode == Operation.REMOVE.ordinal()) {
					intent = new Intent(TaskSchedulingService.REMOVE);

					if (task instanceof RecurringTask){
						intent.putExtra(RecurringTask.class.getName(), 
								(RecurringTask)task);						
					} else {
						intent.putExtra(Task.class.getName(), task);
					}

					RecurringTask recurringTask = (RecurringTask) mTaskHash
							.get(task.getName());

					// remove the task from the recurring task if needed
					if (recurringTask != null) {
						recurringTask.removeTask(task);
					}

					// Delete the task from the database
					mDbStorage.delete(task);
				} else {
					mLogger.log(Level.WARNING, "Unknown request code!");
					return;
				}

				reloadData();
				mPagerAdapter.notifyDataSetChanged();

				LocalBroadcastManager.getInstance(
						TaskListFragment.this.getActivity()).sendBroadcast(
								intent);
			} else {
				mLogger.log(Level.INFO, "No task object found in intent");
				reloadData();
				mPagerAdapter.notifyDataSetChanged();
			}
		} else {
			mLogger.log(Level.INFO, "Activity cancelled");
		}
	}

	public TaskListFragment() {
		mLogger = Logger.getLogger(this.getClass().getName());
		mBound = false;
		mCursorLoaderCallback = new CursorLoaderCallback();
		mClickListener = new ClickListener();
		mTaskHashRetrieval = new TaskHashRetrieval();
	}

	protected abstract String getTaskSelection();

	protected abstract String getRecurringTaskSelection();

	protected abstract String getTaskSortOrder();

	protected abstract String getRecurringTaskSortOrder();

	protected abstract String[] getTaskSelectionArgs();

	protected abstract String[] getRecurringTaskSelectionArgs();

	protected abstract Cursor getTaskCursor();

	protected abstract Cursor getRecurringTaskCursor();

	protected void reloadData() {

		// new Thread(new Runnable() {
		// public void run() {
		mTaskHash.clear();
		mLogger.log(Level.INFO, "Calling reloadDataCallback in background");
		Vector<Task> taskVec = reloadDataCallback();
		mLogger.log(Level.INFO, "reloadDataCallback returned " + taskVec.size()
				+ " task(s)");
		mLogger.log(Level.INFO,
				"TaskListView contains " + mTaskListView.getChildCount()
				+ " children");
		for (Task task : taskVec) {
			if (task instanceof RecurringTask) {
				mLogger.log(Level.INFO, "putting " + task.getName()
						+ " recurring task in ViewTaskHash");
				mLogger.log(Level.INFO, ((RecurringTask)task).getRecurrence().toString());
				mTaskHash.put(task.getName(), (RecurringTask) task);

				for (Task subTask : ((RecurringTask)task).getTasks()){
					mLogger.log(Level.INFO, "putting " + task.getName() 
							+ " task in ViewTaskHash with key: " + 
							subTask.getKey());
					mTaskHash.put(Long.toString(subTask.getKey()), subTask);
				}
			} else {
				mLogger.log(Level.INFO, "putting " + task.getName()
						+ " with key \"" + task.getKey() + "\" in ViewTaskHash");
				mTaskHash.put(Long.toString(task.getKey()), task);
			}
		}
	}

	public void restartLoader() {
		mLogger.log(Level.INFO, "restartLoader called");
		getLoaderManager().restartLoader(mTaskListLoaderId, null,
				mCursorLoaderCallback);
		// getLoaderManager().restartLoader(mRecurringTaskListLoaderId,
		// null, mCursorLoaderCallback);
	}

	// This method should retrieve Task data from the database
	protected abstract Vector<Task> reloadDataCallback();

	private class CursorLoaderCallback implements
	LoaderManager.LoaderCallbacks<Cursor> {

		// Called when a new Loader needs to be created
		public Loader<Cursor> onCreateLoader(int pId, Bundle pArgs) {
			mLogger.log(Level.INFO, "onCreateLoader called " + pId);
			// Now create and return a CursorLoader that will take care of
			// creating a Cursor for the data being displayed.
			if (pId == mTaskListLoaderId) {
				return new CursorLoader(mTaskListView.getContext(),
						Uri.parse(DbStorage.TASK_URI), mFromTaskColumnsList,
						getTaskSelection(), getTaskSelectionArgs(),
						getTaskSortOrder());
				// } else if (pId == mRecurringTaskListLoaderId){
				// return new CursorLoader(
				// mRecurringTaskListView.getContext(),
				// Uri.parse(DbStorage.RECURRING_TASK_URI),
				// mFromRecurringTaskColumnsList, getRecurringTaskSelection(),
				// getRecurringTaskSelectionArgs(),
				// getRecurringTaskSortOrder());
			} else {
				mLogger.log(Level.WARNING,
						"OnCreateLoader received an unknown ID (" + pId + ")");
				return null;
			}
		}

		// Called when a previously created loader has finished loading
		public void onLoadFinished(Loader<Cursor> pLoader, Cursor pData) {
			int numCursors = 0;
			if (pData != null) {
				numCursors = pData.getColumnCount();
			}

			mLogger.log(Level.INFO, "Load Finished with " + numCursors
					+ " cursors");

			// Clear the View-Task hashtable
			mLogger.log(Level.INFO, "clearing the ViewTaskHash");
			mTaskHash.clear();
			reloadData();

			if (pLoader.getId() == mTaskListLoaderId) {
				// Swap the new cursor in. (The framework will take care of
				// closing the
				// old cursor once we return.)
				mLogger.log(Level.INFO, "swapping task cursor");

				mTaskListAdapter.swapCursor(pData);
				mTaskListAdapter.notifyDataSetChanged();
				// } else if (pLoader.getId() == mRecurringTaskListLoaderId){
				// mLogger.log(Level.INFO, "swapping recurring task cursor");
				// mRecurringTaskListAdapter.swapCursor(pData);
				// mRecurringTaskListAdapter.notifyDataSetChanged();
			} else {
				mLogger.log(Level.WARNING,
						"Loader has unknown ID: " + pLoader.getId());
			}
		}

		// Called when a previously created loader is reset, making the data
		// unavailable
		public void onLoaderReset(Loader<Cursor> pLoader) {
			mLogger.log(Level.INFO, "onLoaderReset called");
			// This is called when the last Cursor provided to onLoadFinished()
			// above is about to be closed. We need to make sure we are no
			// longer using it.

			if (pLoader.getId() == mTaskListLoaderId) {
				mTaskListAdapter.swapCursor(null);
				mTaskListAdapter.notifyDataSetChanged();
				// } else if (pLoader.getId() == mRecurringTaskListLoaderId){
				// mRecurringTaskListAdapter.swapCursor(null);
				// mRecurringTaskListAdapter.notifyDataSetChanged();
			} else {
				mLogger.log(
						Level.WARNING,
						"onLoaderReset Loader has unknown ID: "
								+ pLoader.getId());
			}
		}
	}

	private class ClickListener implements OnClickListener {

		@Override
		public void onClick(View pView) {
			mLogger.log(Level.INFO, "onClick called");
			ViewGroup parent = (ViewGroup) pView.getParent();
			TextView idTextView = (TextView) parent.findViewById(R.id.key);
			String id = idTextView.getText().toString();
			Task task = mTaskHash.get(id);

			if (task == null) {
				mLogger.log(Level.WARNING,
						"task retrieved from ViewTaskHash is null!");
				return;
			}

			RecurringTask recurringTask = (RecurringTask) mTaskHash.get(task
					.getName());

			if (pView instanceof CheckBox) {
				// Is the view now checked?
				CheckBox checkBox = (CheckBox) pView;
				boolean saveNeeded = true;
				mLogger.log(Level.INFO, id + " task checkbox has been set to "
						+ checkBox.isChecked());

				if (checkBox.isChecked() != task.isTaskCompleted()) {
					task.setTaskCompleted(checkBox.isChecked());
				} else {
					saveNeeded = false;
				}

				if (saveNeeded) {
					if (recurringTask != null) {
						// Add task will update the task
						recurringTask.addTask(task);

						if (mDbStorage.save(recurringTask)) {
							mLogger.log(Level.WARNING, recurringTask.getName()
									+ " recurring task saved successfully");
							reloadData();
							mPagerAdapter.notifyDataSetChanged();
						} else {
							mLogger.log(
									Level.WARNING,
									"failed to save recurring task "
											+ task.getName());
						}
					} else if (mDbStorage.save(task)) {
						mLogger.log(Level.WARNING, task.getName()
								+ " task saved successfully");
						reloadData();
						mPagerAdapter.notifyDataSetChanged();
					} else {
						mLogger.log(Level.WARNING, "failed to save task "
								+ task.getName());
					}

					// Send an intent if the task was completed, otherwise
					// just send a Message to the TaskSchedulingService
					if (saveNeeded && task.isTaskCompleted()){
						Intent setCompleteIntent = 
								new Intent(TaskSchedulingService.SET_TASK_COMPLETE);
						setCompleteIntent.putExtra(Task.class.getName(), task);

						if (recurringTask != null) {
							setCompleteIntent.putExtra(RecurringTask.class.getName(), 
									recurringTask);
						}

						mLogger.log(Level.INFO, "broadcasting TASK_COMPLETE intent for " + task.getName());
						LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
										setCompleteIntent);
					} else {
						Intent updateIntent = 
								new Intent(TaskSchedulingService.UPDATE);
						updateIntent.putExtra(Task.class.getName(), task);

						mLogger.log(Level.INFO, "broadcasting UPDATE intent for " + task.getName());
						LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(
										updateIntent);
					}

					return;
				} else {
					mLogger.log(Level.INFO,
							"Checkbox state unchanged, no save needed");
				}
			}

			mLogger.log(Level.INFO, "Show Menu");
			TaskListFragment.this.registerForContextMenu(mTaskListView);
			pView.showContextMenu();
		}
	}

	private class TaskHashRetrieval implements ITaskHashRetrieval {
		public Task getTask(String pKey) {
			return mTaskHash.get(pKey);
		}
	}
}
