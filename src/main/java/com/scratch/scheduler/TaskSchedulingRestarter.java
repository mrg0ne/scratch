package com.scratch.scheduler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.scratch.data.storage.IDataStorage;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;

import java.util.logging.Level;
import java.util.logging.Logger;

public final class TaskSchedulingRestarter extends BroadcastReceiver {

    private Logger mLogger;

    public TaskSchedulingRestarter() {
        super();
        mLogger = Logger.getLogger(this.getClass().getName());
    }

    public void onReceive(Context pContext, Intent pIntent) {

        mLogger.log(Level.INFO, "TaskSchedulingRestarter onReceive called");

        if (pIntent.getAction().equals("com.scratch.RESTART_TASK_SCHEDULING_SERVICE")) {
            pContext.startForegroundService(new Intent(pContext, TaskSchedulingService.class));
        } else {
            mLogger.log(Level.WARNING, "Unknown intent received by BroadcastReceiver: " +
                    pIntent.getAction());
        }
    }
}
