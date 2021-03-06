package com.scratch.scheduler;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.R;
import com.scratch.data.settings.AbstractSettingsManager;
import com.scratch.data.settings.SharedPreferencesSettingsManager;
import com.scratch.data.storage.DbStorage;
import com.scratch.data.storage.IDataStorage;
import com.scratch.data.types.Operation;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;
import com.scratch.gui.EditTaskActivity;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.Process;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class TaskSchedulingService extends Service {

    public static final String SET_TASK_COMPLETE = "com.scratch.SET_TASK_COMPLETE";
    public static final String UPDATE = "com.scratch.UPDATE";
    public static final String ADD = "com.scratch.ADD";
    public static final String REMOVE = "com.scratch.REMOVE";

    private IDataStorage mStorage;

    private Logger mLogger;

    private boolean SHUTDOWN;

    private AbstractSettingsManager mSettingsMgr;

    private SchedulingEngine mScheduler;

    // Allows message based communication with the ServiceHandler
    private Messenger mMessenger;

    private LinkedList<ScheduledNotification> mTasks;

    private Looper mServiceLooper;

    private ServiceHandler mServiceHandler;

    private Thread mWorkerThread;

    private BroadcastReceiver mReceiver;

    private NotificationChannel mSchedulingServiceChannel;

    private NotificationManager mNotificationManager;

    private int mNotificationId;

    private String mSchedulingServiceChannelName;
    private String mSchedulingServiceChannelId;
    private String mSchedulingServiceChannelDescription;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mLogger.log(Level.INFO, "onStartCommand called :" + intent + " " + flags + " " + startId);
        Intent notificationIntent = new Intent(this, TaskSchedulingService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
                new Notification.Builder(this, mSchedulingServiceChannelId)
                        .setContentTitle("TaskSchedulingService is running in background")
                        .setContentText("TaskSchedulingService is running in background")
                        .setContentIntent(pendingIntent)
                        .build();

        mLogger.log(Level.INFO, "Before startForeground");
        startForeground(mNotificationId++, notification);
        mLogger.log(Level.INFO, "After startForeground");

        return super.onStartCommand(intent, flags, startId);
    }

    public TaskSchedulingService() {
        super();
        mLogger = Logger.getLogger(this.getClass().getName());
        SHUTDOWN = false;
        mTasks = new LinkedList<ScheduledNotification>();
        mServiceHandler = null;
        mServiceLooper = null;
        mWorkerThread = null;
        mNotificationId = 0;
        mSchedulingServiceChannelId = this.getClass().getName();
    }

    /* (non-Javadoc)
     * @see android.app.Service#onCreate()
     */
    public void onCreate() {
        super.onCreate();
        mLogger.log(Level.INFO, "onCreate called");

		SHUTDOWN = false;

		mSchedulingServiceChannelName = getString(R.string.scheduling_svc_channel_name);
		mSchedulingServiceChannelDescription = getString(R.string.scheduling_svc_channel_description);

		mSchedulingServiceChannel = new NotificationChannel(mSchedulingServiceChannelId,
                mSchedulingServiceChannelName, NotificationManager.IMPORTANCE_DEFAULT);

		// Configure the notification channel.
		mSchedulingServiceChannel.setDescription(mSchedulingServiceChannelDescription);

        mNotificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.createNotificationChannel(mSchedulingServiceChannel);

        Intent notificationIntent = new Intent(this, TaskSchedulingService.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification =
        new Notification.Builder(this, mSchedulingServiceChannelId)
                        .setContentTitle("TaskSchedulingService is running in background")
                        .setContentText("TaskSchedulingService is running in background")
                        .setContentIntent(pendingIntent)
                        .build();
        mLogger.log(Level.INFO, "Before startForeground");
        startForeground(mNotificationId++, notification);
        mLogger.log(Level.INFO, "After startForeground");

        mSettingsMgr = new SharedPreferencesSettingsManager(this);
        mScheduler = new SchedulingEngine(mSettingsMgr);

		mStorage = new DbStorage(this);

		Vector<Task> tasks = mStorage.getAllIncompleteTasks();

		for (Task task : tasks){
			addTask(task);
		}

		// Start up the worker thread
		mWorkerThread = new Thread(new WorkerThread(), 
				"TaskSchedulingWorkerThread");
		mWorkerThread.start();

		// Start up the handler thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		HandlerThread handlerThread = new HandlerThread(
				"TaskSchedulingHandlerThread",
				Process.THREAD_PRIORITY_BACKGROUND);
		handlerThread.start();

		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = handlerThread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
		mMessenger = new Messenger(mServiceHandler);

		mReceiver = new IntentReceiver();

		IntentFilter filter = new IntentFilter();
		filter.addAction(SET_TASK_COMPLETE);
		filter.addAction(UPDATE);
		filter.addAction(ADD);
		filter.addAction(REMOVE);

		LocalBroadcastManager.getInstance(this).registerReceiver(
				mReceiver, filter);
    }

    /* (non-Javadoc)
     * @see android.app.Service#onDestroy()
     */
    @Override
    public void onDestroy() {
        mLogger.log(Level.INFO, "onDestroy called");
        super.onDestroy();
        SHUTDOWN = true;

        if (mWorkerThread != null) {
            mWorkerThread.interrupt();
            mWorkerThread = null;
        }

        mStorage.shutdown();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mReceiver);

        Intent restartIntent = new Intent();
        restartIntent.setAction("com.scratch.RESTART_TASK_SCHEDULING_SERVICE");
        restartIntent.setClass(this, TaskSchedulingRestarter.class);
        sendBroadcast(restartIntent);
    }

    public IBinder onBind(Intent pIntent) {
        return mMessenger.getBinder();
    }

    private synchronized void addTask(RecurringTask pRecurringTask){
        mLogger.log(Level.INFO, "Adding Recurring Task : " +
                pRecurringTask.getName());

        for (Task task: pRecurringTask.getTasks()){
            if (!task.isTaskCompleted()){
                addTask(task);
            }
        }
    }

    private synchronized void addTask(final Task pTask) {
        if (pTask.isTaskCompleted()){
            return;
        }

        mLogger.log(Level.INFO, "Adding " + pTask.getName()
                + " to scheduled task list");
        ScheduledNotification schedTask = new ScheduledNotification(pTask);
        mTasks.add(schedTask);
        try {
            Collections.sort(mTasks);
        } catch (ClassCastException e) {
            mLogger.log(Level.WARNING, e.getMessage());
        }
    }

    private synchronized void removeTask(RecurringTask pRecurringTask){
        mLogger.log(Level.INFO, "Removing recurring task : " +
                pRecurringTask.getName());

        for (Task task : pRecurringTask.getTasks()){
            removeTask(task);
        }
    }

    private synchronized void removeTask(Task pTask) {
        for (ScheduledNotification schedTask : mTasks) {
            if (schedTask.getTask().getKey() == pTask.getKey()) {
                mLogger.log(Level.INFO, "Removing " + pTask.getName()
                        + " from scheduled task list");
                mTasks.remove(schedTask);

                try {
                    Collections.sort(mTasks);
                } catch (ClassCastException e) {
                    mLogger.log(Level.WARNING, e.getMessage());
                }

                break;
            }
        }
    }

    private void setTaskComplete(Task pTask){
        mLogger.log(Level.INFO, "Setting task complete to TRUE for task : " + pTask.getName());

        pTask.setTaskCompleted(true);
        mStorage.save(pTask);

        RecurringTask rTask = findRecurringTask(pTask);

        if (rTask != null){
            setRecurringTaskComplete(rTask, pTask);
        } else {
            mLogger.log(Level.INFO, "No RecurringTask found for task : " + pTask.getName());
            Message msg = mServiceHandler.obtainMessage();
            msg.obj = pTask;
            if (!mServiceHandler.sendMessage(msg)) {
                mLogger.log(Level.WARNING, "Failed to place the Message for task : "
                        + pTask.getName() + " onto the message queue");
            }
        }

        sendDataSetChangeNotification();
    }

    private void setTaskComplete(RecurringTask pRecurringTask){
        mLogger.log(Level.INFO, "Setting Recurring Task complete : " +
                pRecurringTask.getName());

        pRecurringTask.setTaskCompleted(true);
        boolean saveNeeded = false;

        for (Task task : pRecurringTask.getTasks()){
            if (!task.isTaskCompleted()){
                task.setTaskCompleted(true);
                saveNeeded = true;
            }

            updateTask(task);
        }

        if (saveNeeded){
            mStorage.save(pRecurringTask);
        }
    }

    private void setRecurringTaskComplete(
            RecurringTask pRecurringTask, Task pTask){
        mLogger.log(Level.INFO, "setRecurringTaskComplete for "
                + pRecurringTask.getName());
        boolean allTasksComplete = true;

        for (Task task : pRecurringTask.getTasks()){
            if (!task.isTaskCompleted()){
                allTasksComplete = false;
                break;
            }
        }

        // tasks are complete
        if (allTasksComplete) {
            mLogger.log(Level.INFO, "All sub-tasks complete, " +
                    "generating new task instance");

            try {
                // Generate next recurring task
                RecurringTask recurringTask = mScheduler.generateNextRecurringTask(
                        pRecurringTask, pTask);
                mStorage.save(recurringTask);
                Message msg = mServiceHandler.obtainMessage();
                msg.obj = recurringTask.getTasks().lastElement();
                msg.arg1 = Operation.ADD.ordinal();
                mServiceHandler.sendMessage(msg);
            } catch (SchedulingException se) {
                mLogger.log(Level.WARNING, "Failed to Generate the next recurring task: \n" + se);
                se.printStackTrace();
            }
        }
    }

    private void sendReminderNotification(Task pTask) {
        mLogger.log(Level.INFO, "Sending Reminder notification");
        Calendar cal = Calendar.getInstance();
        cal .setTime(pTask.getDueDate());
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, mSchedulingServiceChannelId)
                        //  .setSmallIcon(R.drawable.reminder_icon)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Task Reminder")
                        .setContentText(pTask.getName() + " due on "
                                + cal.get(Calendar.MONTH) + "/" +
                                cal.get(Calendar.DAY_OF_MONTH) + "/" +
                                cal.get(Calendar.YEAR));
        sendNotification(builder, pTask);
    }

    /**
     * This method finds and returns a RecurringTask that contains
     * the given Task object if it exists, otherwise it returns null.
     * @param pTask
     * @return
     */
    private RecurringTask findRecurringTask(Task pTask){

        for (Task task : mStorage.getAllTasks()){
            if (task instanceof RecurringTask){
                if (((RecurringTask)task).containsTask(pTask)){
                    return (RecurringTask)task;
                }
            }
        }

        return null;
    }

    /**
     * Send a dataset change notification to the GUI
     */
    private void sendDataSetChangeNotification(){
        mLogger.log(Level.INFO, "Sending dataset change notification");
        Intent dataSetChangeIntent = new Intent(IDataStorage.DATASET_CHANGE);
        sendBroadcast(dataSetChangeIntent);
    }

    private void sendDueNotification(final Task pTask) {
        mLogger.log(Level.INFO, "Sending Due notification for task: " + pTask.getName());
        Calendar cal = Calendar.getInstance();
        cal .setTime(pTask.getDueDate());
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, mSchedulingServiceChannelId)
                        //.setSmallIcon(R.drawable.due_icon)
                        .setSmallIcon(R.drawable.ic_launcher)
                        .setContentTitle("Task Due")
                        .setContentText(pTask.getName() + " due on "
                                + cal.get(Calendar.MONTH) + "/" +
                                cal.get(Calendar.DAY_OF_MONTH) + "/" +
                                cal.get(Calendar.YEAR));
        sendNotification(builder, pTask);
    }

    private void sendOverDueNotification(final Task pTask) {
        mLogger.log(Level.INFO, "Sending Overdue notification");
        Calendar cal = Calendar.getInstance();
        cal .setTime(pTask.getDueDate());
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, mSchedulingServiceChannelId)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentTitle("Task Overdue")
                .setContentText(pTask.getName() + " due on "
                        + cal.get(Calendar.MONTH) + "/" +
                        cal.get(Calendar.DAY_OF_MONTH) + "/" +
                        cal.get(Calendar.YEAR));

        sendNotification(builder, pTask);
    }

    private void sendNotification(NotificationCompat.Builder pBuilder, final Task pTask){
        Intent setCompleteIntent = new Intent(this, TaskBroadcastReceiver.class);
        setCompleteIntent.setAction(SET_TASK_COMPLETE);
        setCompleteIntent.putExtra(pTask.getClass().getName(), pTask);
        PendingIntent setCompletePendingIntent =
                PendingIntent.getBroadcast(this, mNotificationId++, setCompleteIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

        Intent editIntent = new Intent(this, EditTaskActivity.class);
        editIntent.putExtra(pTask.getClass().getName(), pTask);
        editIntent.putExtra(Operation.class.getName(), Operation.UPDATE.ordinal());

        PendingIntent editPendingIntent = PendingIntent.getActivity(this,
                mNotificationId++,	editIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        pBuilder.setContentIntent(editPendingIntent);
        pBuilder.addAction(R.drawable.ic_action_accept, getString(R.string.set_task_complete), setCompletePendingIntent);

        mNotificationManager.notify((int)pTask.getKey(), pBuilder.build());
    }

    private synchronized void updateTask(RecurringTask pRecurringTask){
        mLogger.log(Level.INFO, "Updating Recurring Task : " +
                pRecurringTask.getName());

        for (Task task : pRecurringTask.getTasks()){
            updateTask(task);
        }
    }

    private synchronized void updateTask(Task pTask) {
        if (pTask.isTaskCompleted()) {
            // Remove scheduled task
            removeTask(pTask);
        } else {
            for (ScheduledNotification schedTask : mTasks) {
                if (schedTask.getTask().getKey() == pTask.getKey()) {
                    mLogger.log(Level.INFO, "Updating " + pTask.getName()
                            + " in scheduled task list");
                    schedTask.setTask(pTask);

                    try {
                        Collections.sort(mTasks);
                    } catch (ClassCastException e) {
                        mLogger.log(Level.WARNING, e.getMessage());
                    }

                    // leave this method if a task was updated
                    return;
                }
            }

            // If all scheduled tasks were checked and no match was found,
            // add the task
            addTask(pTask);
        }
    }

    private final class WorkerThread implements Runnable {

        public void run() {
            while (!SHUTDOWN) {
                try {
                    if (mTasks.isEmpty()) {
                        mLogger.log(Level.INFO, "Task list is empty");
                        Thread.sleep(Long.MAX_VALUE);
                    } else {
                        ScheduledNotification schedTask = mTasks.getFirst();
                        Date now = new Date();
                        long alarmTime = schedTask.getNextNotification().getTime();
                        long sleepTime = alarmTime - now.getTime();

                        if (sleepTime < 0) {
                            mLogger.log(Level.WARNING, "Error occured, negative sleep time!, " +
                                    "current time is " + now + " and next notification time is "
                                    + schedTask.getNextNotification());
                            mLogger.log(Level.WARNING, schedTask.getTask().toString());
                            schedTask = mTasks.removeFirst();
                            addTask(schedTask.getTask());
                        } else {
                            long time = sleepTime;
                            long days = time/(1000*60*60*24);
                            time -= days*1000*60*60*24;
                            long hours = time/(1000*60*60);
                            time -= hours*1000*60*60;
                            long minutes = time/(1000*60);
                            time -= minutes*1000*60;
                            long seconds = time/1000;
                            mLogger.log(Level.INFO, "Notification thread sleeping for " + days +
                                    " days, " + hours + " hours, " + minutes +
                                    " minutes, and " + seconds +
                                    " seconds for task: " + schedTask.getTask().getName());
                            Thread.sleep(sleepTime);
                            schedTask = mTasks.removeFirst();
                            mLogger.log(Level.INFO, "Updating last notify date and " +
                                    "saving the Task to storage");
                            now = new Date();
                            Task task = schedTask.getTask();
                            task.setLastNotifyDate(now);
                            mStorage.save(task);

                            if (now.getTime() > (task.getDueDate().getTime()
                                    + 24*60*60*1000)){
                                sendOverDueNotification(task);
                            } else if ((task.getDueDate().getTime()
                                    - now.getTime()) < 24*60*60*1000){
                                sendDueNotification(task);
                            } else if (now.getTime() < task.getDueDate().getTime()
                                    && now.getTime() > task.getReminderDate().getTime()
                                    && task.getReminderDate().getTime() > 0){
                                sendReminderNotification(task);
                            } else {
                                mLogger.log(Level.WARNING, "Failed to determine"
                                        + " type of notification to send for: " + task);
                            }

                            addTask(task);
                        }
                    }
                } catch (InterruptedException ie) {
                    mLogger.log(Level.INFO, "Worker Thread has been interrupted");
                }
            }

            mLogger.log(Level.INFO, "SHUTDOWN flag is TRUE, " +
                    "stopping TaskSchedulingService");
            stopSelf();
        }

    }

    // Handler that receives messages from the thread
    private final class ServiceHandler extends Handler {

        /* (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        public void handleMessage(Message pMsg) {
            mLogger.log(Level.FINE, "handleMessage called");

            if (pMsg.obj instanceof Task) {
                Task task = (Task)pMsg.obj;
                if (pMsg.arg1 == Operation.ADD.ordinal()) {
                    addTask(task);
                } else if (pMsg.arg1 == Operation.UPDATE.ordinal()) {
                    updateTask(task);
                } else if (pMsg.arg1 == Operation.REMOVE.ordinal()) {
                    removeTask(task);
                } else {
                    mLogger.log(Level.WARNING, "Unknown message type.  "
                            + "Ordinal value is " + pMsg.arg1);
                    return;
                }

                // Wake up the worker thread to react to the changes from
                // this message
                mWorkerThread.interrupt();

            } else {
                mLogger.log(Level.WARNING, "Message object does not contain a Task");
            }
        }

        /* (non-Javadoc)
         * @see android.os.Handler#toString()
         */
        @Override
        public String toString() {
            String taskList = new String();

            for (ScheduledNotification task : mTasks) {
                taskList += task.getTask().getName() + " " +
                        task.getNextNotification() + "\n";
            }

            return taskList;
        }

        public ServiceHandler(Looper looper) {
            super(looper);
        }

    }

    private final class IntentReceiver extends BroadcastReceiver {

        public void onReceive(Context pContext, Intent pIntent) {
            mLogger.log(Level.INFO, "BroadcastReceiver onReceive called");
            Task task = null;
            RecurringTask rTask = null;

            // TODO handle intents with recurring task only
            // The Intent must contain a Task object, RecurringTask is
            // optional
            if (pIntent.hasExtra(Task.class.getName())) {
                task = pIntent.getParcelableExtra(Task.class.getName());
            }

            if (pIntent.hasExtra(RecurringTask.class.getName())) {
                rTask = pIntent.getParcelableExtra(
                        RecurringTask.class.getName());
                mLogger.log(Level.INFO, "onReceive " + rTask.getRecurrence().toString());
            }

            if (pIntent.getAction().equals(SET_TASK_COMPLETE)) {
                mLogger.log(Level.INFO, "Setting task complete to TRUE");
                if (task != null){
                    setTaskComplete(task);
                } else if (rTask != null){
                    setTaskComplete(rTask);
                } else {
                    mLogger.log(Level.WARNING, "Intent does not contain " +
                            "a Task or RecurringTask object");
                }

                mNotificationManager.cancel((int)task.getKey());
            } else if (pIntent.getAction().equals(UPDATE)) {
                mLogger.log(Level.INFO, "UPDATE Intent received");
                if (task != null){
                    updateTask(task);
                } else if (rTask != null){
                    updateTask(rTask);
                } else {
                    mLogger.log(Level.WARNING, "Intent does not contain " +
                            "a Task or RecurringTask object");
                }
            } else if (pIntent.getAction().equals(ADD)){
                mLogger.log(Level.INFO, "ADD Intent received");
                if (task != null){
                    addTask(task);
                } else if (rTask != null){
                    addTask(rTask);
                } else {
                    mLogger.log(Level.WARNING, "Intent does not contain " +
                            "a Task or RecurringTask object");
                }
            } else if (pIntent.getAction().equals(REMOVE)){
                mLogger.log(Level.INFO, "REMOVE Intent received");
                if (task != null){
                    removeTask(task);
                } else if (rTask != null){
                    removeTask(rTask);
                } else {
                    mLogger.log(Level.WARNING, "Intent does not contain " +
                            "a Task or RecurringTask object");
                }
            } else {
                mLogger.log(Level.WARNING, "Unknown intent received by BroadcastReceiver: " +
                        pIntent.getAction());
            }

            // Wake up the worker thread to react to the changes from
            // this message
            mWorkerThread.interrupt();
        }
    }
}
