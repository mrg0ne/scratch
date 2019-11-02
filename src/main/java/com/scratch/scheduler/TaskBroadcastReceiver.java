package com.scratch.scheduler;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.scratch.R;
import com.scratch.data.storage.DbStorage;
import com.scratch.data.storage.IDataStorage;
import com.scratch.data.types.Operation;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class TaskBroadcastReceiver extends BroadcastReceiver {

    private IDataStorage mStorage;

    private Logger mLogger;

    public TaskBroadcastReceiver() {
        super();
        mLogger = Logger.getLogger(this.getClass().getName());
    }

    public void onReceive(Context pContext, Intent pIntent) {

        mLogger.log(Level.INFO, "TaskBroadcastReceiver onReceive called");
        Task task = null;
        RecurringTask rTask = null;

        // TODO handle intents with recurring task only
        // The Intent must contain a Task object, RecurringTask is
        // optional
        if (pIntent.hasExtra(Task.class.getName())) {
            task = pIntent.getParcelableExtra(Task.class.getName());
            mLogger.log(Level.INFO, "onReceive Task" + task.toString());

            if (pIntent.hasExtra(RecurringTask.class.getName())) {
                rTask = pIntent.getParcelableExtra(RecurringTask.class.getName());
                mLogger.log(Level.INFO, "onReceive RecurringTask" + rTask.toString());
            }
        }

        if (pIntent.getAction().equals(TaskSchedulingService.SET_TASK_COMPLETE)) {
            mLogger.log(Level.INFO, "Setting task complete to TRUE");
            if (task != null){
                task.setTaskCompleted(true);
                Intent setCompleteIntent =
                        new Intent(TaskSchedulingService.SET_TASK_COMPLETE);
                setCompleteIntent.putExtra(Task.class.getName(), task);

                if (rTask != null) {
                    setCompleteIntent.putExtra(RecurringTask.class.getName(),
                            rTask);
                }

                mLogger.log(Level.INFO, "broadcasting SET_TASK_COMPLETE intent for " + task.getName());
                LocalBroadcastManager.getInstance(pContext).sendBroadcast(
                        setCompleteIntent);
            } else {
                mLogger.log(Level.WARNING, "Intent does not contain " +
                        "a Task object");
            }
        } else {
            mLogger.log(Level.WARNING, "Unknown intent received by BroadcastReceiver: " +
                    pIntent.getAction());
        }
    }
}
