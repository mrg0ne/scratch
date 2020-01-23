package com.scratch.scheduler;
// ADB wuz here
import java.util.Calendar;
import java.util.Date;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.scratch.data.settings.AbstractSettingsManager;
import com.scratch.data.types.RecurrenceType;
import com.scratch.data.types.RecurringTask;
import com.scratch.data.types.Task;
import com.scratch.data.types.TaskRecurrence;
import com.scratch.data.types.TaskRecurrence.WeekNumber;

public class SchedulingEngine {

    public static final long sDayInMs = 24*60*60*1000;

    private AbstractSettingsManager mSettingsMgr;

    private Logger mLogger;

    public SchedulingEngine(AbstractSettingsManager pSettingsMgr) {
        mSettingsMgr = pSettingsMgr;
        mLogger = Logger.getLogger(this.getClass().getName());
    }

    public RecurringTask makeSchedule(RecurringTask pTask) {
        RecurringTask rRecurringTask = new RecurringTask(pTask);

        // compare due dates of tasks
        Vector<Date> dueDates = new Vector<Date>();

        for (Task task : pTask.getTasks()) {
            dueDates.add(task.getDueDate());
        }

        mLogger.log(Level.INFO, "Determine task recurrence from due dates for " +
                "task: " + pTask.getName());
        RecurrenceType recurrenceType = determineRecurrence(dueDates);

        if (recurrenceType != RecurrenceType.NONE) {
            mLogger.log(Level.INFO, "Using due dates to set recurrence for task: "
                    + pTask.getName() + " to " + recurrenceType);
            rRecurringTask.getRecurrence().setRecurrenceType(recurrenceType);
        } else {
            // compare completion dates of tasks
            Vector<Date> completionDates = new Vector<Date>();

            for (Task task : pTask.getTasks()) {
                completionDates.add(task.getCompletionDate());
            }

            mLogger.log(Level.INFO, "Determine task recurrence from completion " +
                    "dates for task: " + pTask.getName());
            recurrenceType = determineRecurrence(completionDates);

            if (recurrenceType != RecurrenceType.NONE) {
                mLogger.log(Level.INFO, "Using completion dates to set recurrence for task: "
                        + pTask.getName() + " to " + recurrenceType);
                rRecurringTask.getRecurrence().setRecurrenceType(recurrenceType);
            } else {
                // compare planned dates of tasks
                Vector<Date> plannedDates = new Vector<Date>();

                for (Task task : pTask.getTasks()) {
                    plannedDates.add(task.getDatePlanned());
                }

                mLogger.log(Level.INFO, "Determine task recurrence from planned " +
                        "dates for task: " + pTask.getName());
                recurrenceType = determineRecurrence(completionDates);

                if (recurrenceType != RecurrenceType.NONE) {
                    mLogger.log(Level.INFO, "Using planned dates to set recurrence "
                            + "for task: " + pTask.getName() + " to " + recurrenceType);
                    rRecurringTask.getRecurrence().setRecurrenceType(recurrenceType);
                } else {
                    mLogger.log(Level.INFO, "Using default settings to set recurrence "
                            + "for task: " + pTask.getName() + " to " +
                            mSettingsMgr.getDefaultTaskRecurrence());
                    rRecurringTask.getRecurrence().setRecurrenceType(
                            mSettingsMgr.getDefaultTaskRecurrence());
                }
            }
        }

        return rRecurringTask;
    }

    public RecurringTask generateNextRecurringTask(
            RecurringTask pRecurringTask, Task pTask)
    throws SchedulingException {

        mLogger.log(Level.INFO, "Generating next recurring task for : " +
                pRecurringTask);
        // If there are no task instances,
        if (pRecurringTask.getTasks().size() == 0) {
            String errMsg = "No sub-tasks exist for : " +
                    pRecurringTask.getName();
            mLogger.log(Level.WARNING, errMsg);
            throw new SchedulingException(errMsg);
        } else {
            RecurringTask recurringTask;
            switch (pRecurringTask.getRecurrence().getRecurrenceType()) {
                case DAILY:
                    recurringTask = generateNextRecurringDailyTask(pRecurringTask, pTask);
                    break;
                case WEEKLY:
                    recurringTask = generateNextRecurringWeeklyTask(pRecurringTask, pTask);
                    break;
                case MONTHLY:
                    recurringTask = generateNextRecurringMonthlyTask(pRecurringTask, pTask);
                    break;
                case YEARLY:
                    recurringTask = generateNextRecurringYearlyTask(pRecurringTask, pTask);
                    break;
                default :
                    String errMsg = "Can't generate next task for " +
                            "recurrence: " + pRecurringTask.getRecurrence();
                    mLogger.log(Level.WARNING, errMsg);
                    throw new SchedulingException(errMsg);
            }

            mLogger.log(Level.INFO, "Recurring task updated : " + recurringTask);
            return recurringTask;
        }
    }

    private RecurringTask generateNextRecurringDailyTask(
            RecurringTask pRecurringTask, Task pTask)
    throws SchedulingException {
        mLogger.log(Level.INFO, "generateNextRecurringDailyTask called");
        Date now = new Date();
        Date oldDueDate = pTask.getDueDate();
        RecurringTask recurringTask = pRecurringTask;
        TaskRecurrence recurrence = recurringTask.getRecurrence();

        // If recurring task does not have any days set, return
        if (!recurrence.isOnMonday() && !recurrence.isOnTuesday() &&
                !recurrence.isOnWednesday() && !recurrence.isOnThursday()
                && !recurrence.isOnFriday() && !recurrence.isOnSaturday()
                && !recurrence.isOnSunday()) {
            String errMsg = pTask.getName() +
                    " does not have any days set";
            mLogger.log(Level.INFO, errMsg);
            throw new SchedulingException(errMsg);
        }

        Calendar nowCal = Calendar.getInstance();
        nowCal.setFirstDayOfWeek(Calendar.MONDAY);
        nowCal.setTime(now);
        Calendar newCal = Calendar.getInstance();
        newCal.setTime(oldDueDate);
        newCal.setFirstDayOfWeek(Calendar.MONDAY);

        Calendar oldCal = Calendar.getInstance();
        oldCal.setFirstDayOfWeek(Calendar.MONDAY);
        oldCal.setTime(oldDueDate);
        boolean newDueDateFound = false;

        if (pTask.isTaskCompleted()) {
            Task newTask = new Task(pTask);
            newTask.setTaskCompleted(false);

            // Check if the task is overdue
            if (newCal.getTime().getTime() < nowCal.getTime().getTime()) {
                // If so, set the date to today
                newCal.set(Calendar.YEAR, nowCal.get(Calendar.YEAR));
                newCal.set(Calendar.DAY_OF_YEAR, nowCal.get(Calendar.DAY_OF_YEAR));
            }

            while (!newDueDateFound){
                mLogger.log(Level.INFO, newCal.toString());
                mLogger.log(Level.INFO, recurrence.toString());
                newCal.add(Calendar.DAY_OF_YEAR, 1);

                // Check if the task should occur on this day
                if ((newCal.get(Calendar.DAY_OF_WEEK) == Calendar.MONDAY) && recurrence.isOnMonday()) {
                    newDueDateFound = true;
                } else if ((newCal.get(Calendar.DAY_OF_WEEK) == Calendar.TUESDAY) && recurrence.isOnTuesday()) {
                    newDueDateFound = true;
                } else if ((newCal.get(Calendar.DAY_OF_WEEK) == Calendar.WEDNESDAY) && recurrence.isOnWednesday()) {
                    newDueDateFound = true;
                } else if ((newCal.get(Calendar.DAY_OF_WEEK) == Calendar.THURSDAY) && recurrence.isOnThursday()) {
                    newDueDateFound = true;
                } else if ((newCal.get(Calendar.DAY_OF_WEEK) == Calendar.FRIDAY) && recurrence.isOnFriday()) {
                    newDueDateFound = true;
                } else if ((newCal.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY) && recurrence.isOnSaturday()) {
                    newDueDateFound = true;
                } else if ((newCal.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) && recurrence.isOnSunday()) {
                    newDueDateFound = true;
                }
            }

            newTask.setDueDate(newCal.getTime());

            // check if reminder date was set and adjust it if needed
            Date reminderDate = pTask.getReminderDate();
            long reminderDateTimeMs = reminderDate.getTime();

            if (reminderDateTimeMs > 0){
                long timeDiff = oldDueDate.getTime() - reminderDateTimeMs;
                mLogger.log(Level.INFO, "Reminder Date : " + reminderDate);
                mLogger.log(Level.INFO, "Old Reminder Date Time ms : " +
                        reminderDateTimeMs);
                long newDueDateMs = newTask.getDueDate().getTime();
                mLogger.log(Level.INFO, "New Due Date : " + newTask.getDueDate());
                mLogger.log(Level.INFO, "New due date time ms : " + newDueDateMs);
                Date newReminderDate = new Date(newDueDateMs - timeDiff);
                mLogger.log(Level.INFO, "New Reminder Date : " + newReminderDate);
                newTask.setReminderDate(newReminderDate);
                recurringTask.setReminderDate(newReminderDate);
            }

            recurringTask.addTask(newTask);
            // TODO check if this is necessary
            recurringTask.setDueDate(newCal.getTime());
        } else {
            String errMsg = pTask.getName() + " task is not complete";
            mLogger.log(Level.WARNING, errMsg);
            throw new SchedulingException(errMsg);
        }

        return recurringTask;
    }

    private RecurringTask generateNextRecurringWeeklyTask(
            RecurringTask pRecurringTask, Task pTask)
    throws SchedulingException {
        Date now = new Date();
        Date oldDueDate = pTask.getDueDate();
        RecurringTask recurringTask = pRecurringTask;
        TaskRecurrence recurrence = recurringTask.getRecurrence();

        // Check if the task recurrence is valid
        if (!recurrence.isValid()) {
            String errMsg = "Unable to generate next recurring weekly task for " +
                    pRecurringTask.getName() + ". The TaskRecurrence is invalid: " + recurrence;
            mLogger.log(Level.WARNING, errMsg);
            throw new SchedulingException(errMsg);
        }

        int regularity = recurrence.getOccurenceRegularity();
        Calendar newCal = Calendar.getInstance();
        newCal.setFirstDayOfWeek(Calendar.MONDAY);
        newCal.setTime(oldDueDate);
        Calendar nowCal = Calendar.getInstance();
        nowCal.setFirstDayOfWeek(Calendar.MONDAY);
        nowCal.setTime(now);
        Calendar oldCal = Calendar.getInstance();
        oldCal.setFirstDayOfWeek(Calendar.MONDAY);
        oldCal.setTime(oldDueDate);
        boolean newDueDateFound = false;

        if (pTask.isTaskCompleted()) {
            Task newTask = new Task(pTask);
            newTask.setTaskCompleted(false);

            // Iterate until a new due date is found
            while (!newDueDateFound) {
                int daysLeftInWeek = 0;
                Calendar tmpCal = Calendar.getInstance();
                tmpCal.setTime(newCal.getTime());

                // Move tmpCal to Sunday
                while (tmpCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
                    tmpCal.add(Calendar.DAY_OF_WEEK, 1);
                    tmpCal.getTimeInMillis();
                    daysLeftInWeek++;
                }

                // Check if we've found a week in the future
                if (tmpCal.getTimeInMillis() > nowCal.getTimeInMillis()) {

                    // Iterate over the days in this week to find the next available day
                    for (int i = 0; i <= daysLeftInWeek; i++) {
                        if (newCal.getTimeInMillis() > nowCal.getTimeInMillis()) {
                            int dayOfWeek = newCal.get(Calendar.DAY_OF_WEEK);

                            if ((newCal.getTimeInMillis() > oldCal.getTimeInMillis()) &&
                                    recurrence.occursOnDay(dayOfWeek)) {
                                newDueDateFound = true;
                                break;
                            } else {
                                newCal.add(Calendar.DAY_OF_WEEK, 1);
                                newCal.getTimeInMillis();
                            }
                        }
                    }
                }

                // If a new due date was not found go to Monday and iterate to the next week
                if (!newDueDateFound){
                    // Ensure the week starts on Monday
                    while (newCal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                        newCal.roll(Calendar.DAY_OF_WEEK, 1);
                        newCal.getTimeInMillis();
                    }

                    // Move to the next week that a due date should fall on
                    newCal.add(Calendar.WEEK_OF_YEAR, regularity);
                    newCal.getTimeInMillis();
                }
            }

            newTask.setDueDate(newCal.getTime());

            // check if reminder date was set and adjust it if needed
            Date reminderDate = pTask.getReminderDate();
            long reminderDateTimeMs = reminderDate.getTime();

            if (reminderDateTimeMs > 0){
                long timeDiff = oldDueDate.getTime() - reminderDateTimeMs;
                mLogger.log(Level.INFO, "Reminder Date : " + reminderDate);
                mLogger.log(Level.INFO, "Old Reminder Date Time ms : " +
                        reminderDateTimeMs);
                long newDueDateMs = newTask.getDueDate().getTime();
                mLogger.log(Level.INFO, "New Due Date : " + newTask.getDueDate());
                mLogger.log(Level.INFO, "New due date time ms : " + newDueDateMs);
                Date newReminderDate = new Date(newDueDateMs - timeDiff);
                mLogger.log(Level.INFO, "New Reminder Date : " + newReminderDate);
                newTask.setReminderDate(newReminderDate);
                recurringTask.setReminderDate(newReminderDate);
            }

            recurringTask.addTask(newTask);
            recurringTask.setDueDate(newCal.getTime());
        } else {
            String errMsg = "Unable to generate next recurring weekly task because " +
                    pTask.getName() + " task is not complete";
            mLogger.log(Level.WARNING, errMsg);
            throw new SchedulingException(errMsg);
        }

        return recurringTask;
    }

    private RecurringTask generateNextRecurringMonthlyTask(
            RecurringTask pRecurringTask, Task pTask)
    throws SchedulingException {
        Date now = new Date();
        Date oldDueDate = pTask.getDueDate();
        RecurringTask recurringTask = pRecurringTask;
        TaskRecurrence recurrence = recurringTask.getRecurrence();

        int regularity = recurrence.getOccurenceRegularity();

        Calendar nowCal = Calendar.getInstance();
        nowCal.setFirstDayOfWeek(Calendar.MONDAY);
        nowCal.setTime(now);
        Calendar newCal = Calendar.getInstance();
        newCal.setFirstDayOfWeek(Calendar.MONDAY);
        newCal.setTime(oldDueDate);

        Calendar oldCal = Calendar.getInstance();
        oldCal.setFirstDayOfWeek(Calendar.MONDAY);
        oldCal.setTime(oldDueDate);
        boolean newDueDateFound = false;

        if (pTask.isTaskCompleted()) {
            Task newTask = new Task(pTask);
            newTask.setTaskCompleted(false);

            while (!newDueDateFound){
                newCal.add(Calendar.MONTH, regularity);
                newCal.getTimeInMillis();

                // Set new calendar date
                if (recurrence.isUseDay()){
                    newCal.set(Calendar.DAY_OF_MONTH, recurrence.getDayOfMonth());
                    newCal.getTimeInMillis();
                } else {
                    // Set date to first day of the month
                    newCal.set(Calendar.DAY_OF_MONTH, 1);
                    newCal.getTimeInMillis();

                    WeekNumber weekNum = recurrence.getWeekNum();
                    int weeksToSkip = 0;

                    // get the day of the week
                    int dayOfWeek = newCal.get(Calendar.DAY_OF_WEEK);
                    if (recurrence.isOnMonday()) {
                        dayOfWeek = Calendar.MONDAY;
                    } else if (recurrence.isOnTuesday()) {
                        dayOfWeek = Calendar.TUESDAY;
                    } else if (recurrence.isOnWednesday()) {
                        dayOfWeek = Calendar.WEDNESDAY;
                    } else if (recurrence.isOnThursday()) {
                        dayOfWeek = Calendar.THURSDAY;
                    } else if (recurrence.isOnFriday()) {
                        dayOfWeek = Calendar.FRIDAY;
                    } else if (recurrence.isOnSaturday()) {
                        dayOfWeek = Calendar.SATURDAY;
                    } else if (recurrence.isOnSunday()) {
                        dayOfWeek = Calendar.SUNDAY;
                    } else {
                        String errMsg = pRecurringTask.getName() +
                                "Unable to generate next recurring monthly task because no day of week set";
                        mLogger.log(Level.WARNING, errMsg);
                        throw new SchedulingException(errMsg);
                    }

                    if (weekNum == WeekNumber.FIRST) {
                        weeksToSkip = 0;
                    } else if (weekNum == WeekNumber.SECOND) {
                        weeksToSkip = 1;
                    } else if (weekNum == WeekNumber.THIRD) {
                        weeksToSkip = 2;
                    } else if (weekNum == WeekNumber.FOURTH || weekNum == WeekNumber.LAST) {
                        weeksToSkip = 3;
                    } else {
                        String errMsg = recurringTask.getName() +
                                "Unable to generate next recurring monthly task because no week number set";
                        mLogger.log(Level.WARNING, errMsg);
                        throw new SchedulingException(errMsg);
                    }

                    // Convert to days and skip
                    int daysToSkip = 7*weeksToSkip;
                    newCal.add(Calendar.DAY_OF_MONTH, daysToSkip);
                    newCal.getTimeInMillis();

                    boolean dayOfWeekFound = false;

                    // Find the day of the week
                    while (!dayOfWeekFound) {
                        if (newCal.get(Calendar.DAY_OF_WEEK) != dayOfWeek) {
                            newCal.add(Calendar.DAY_OF_MONTH, 1);
                            newCal.getTimeInMillis();
                        } else {
                            dayOfWeekFound = true;
                        }
                    }

                    // If this is the last X of the month, add 7 more days
                    // and check if it is still the same month
                    if (weekNum == WeekNumber.LAST) {
                        Calendar tmpCal = Calendar.getInstance();
                        tmpCal.setTime(newCal.getTime());

                        // skip 7 days and check if this is the same month
                        tmpCal.add(Calendar.DAY_OF_MONTH, 7);
                        tmpCal.getTimeInMillis();

                        // If still within the same month set newCal to tmpCal
                        if (newCal.get(Calendar.MONTH) == tmpCal.get(Calendar.MONTH)) {
                            newCal = tmpCal;
                        }
                    }
                }

                // Check if the date found is in the future
                if (newCal.getTime().getTime() > nowCal.getTime().getTime()) {
                    newDueDateFound = true;
                }
            }

            newTask.setDueDate(newCal.getTime());

            // check if reminder date was set and adjust it if needed
            Date reminderDate = pTask.getReminderDate();
            long reminderDateTimeMs = reminderDate.getTime();

            if (reminderDateTimeMs > 0){
                long timeDiff = oldDueDate.getTime() - reminderDateTimeMs;
                mLogger.log(Level.INFO, "Reminder Date : " + reminderDate);
                mLogger.log(Level.INFO, "Old Reminder Date Time ms : " +
                        reminderDateTimeMs);
                long newDueDateMs = newTask.getDueDate().getTime();
                mLogger.log(Level.INFO, "New due date time ms : " + newDueDateMs);
                Date newReminderDate = new Date(newDueDateMs - timeDiff);
                mLogger.log(Level.INFO, "New Reminder Date : " + newReminderDate);
                newTask.setReminderDate(newReminderDate);
                recurringTask.setReminderDate(newReminderDate);
            }

            recurringTask.addTask(newTask);
            recurringTask.setDueDate(newCal.getTime());
        } else {
            String errMsg = "Unable to generate next recurring monthly task because " + pTask.getName() + " task is not complete";
            mLogger.log(Level.WARNING, errMsg);
            throw new SchedulingException(errMsg);
        }

        return recurringTask;
    }

    private RecurringTask generateNextRecurringYearlyTask(
            RecurringTask pRecurringTask, Task pTask)
    throws SchedulingException {
        Date now = new Date();
        Date oldDueDate = pTask.getDueDate();
        RecurringTask recurringTask = pRecurringTask;
        TaskRecurrence recurrence = recurringTask.getRecurrence();
        int regularity = recurrence.getOccurenceRegularity();
        Calendar newCal = Calendar.getInstance();
        newCal.setFirstDayOfWeek(Calendar.MONDAY);
        newCal.setTime(oldDueDate);
        //newCal.set(Calendar.MONTH, recurrence.getMonth());

        Calendar nowCal = Calendar.getInstance();
        nowCal.setFirstDayOfWeek(Calendar.MONDAY);
        nowCal.setTime(now);
        Calendar oldCal = Calendar.getInstance();
        oldCal.setFirstDayOfWeek(Calendar.MONDAY);
        oldCal.setTime(oldDueDate);
        boolean newDueDateFound = false;

        if (pTask.isTaskCompleted()) {
            Task newTask = new Task(pTask);
            newTask.setTaskCompleted(false);

            while (!newDueDateFound){
                newCal.add(Calendar.YEAR, regularity);
                newCal.set(Calendar.MONTH, recurrence.getMonth());

                // Set new calendar date
                if (recurrence.isUseDay()){
                    newCal.set(Calendar.DAY_OF_MONTH, recurrence.getDayOfMonth());
                } else {
                    if (recurrence.getWeekNum() == WeekNumber.LAST) {
                        newCal.set(Calendar.WEEK_OF_MONTH,
                                newCal.getActualMaximum(Calendar.WEEK_OF_MONTH));
                    } else {
                        newCal.set(Calendar.WEEK_OF_MONTH,
                                recurrence.getWeekNum().ordinal());
                    }

                    // set the day of the week
                    if (recurrence.isOnMonday()) {
                        newCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                    } else if (recurrence.isOnTuesday()) {
                        newCal.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
                    } else if (recurrence.isOnWednesday()) {
                        newCal.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
                    } else if (recurrence.isOnThursday()) {
                        newCal.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
                    } else if (recurrence.isOnFriday()) {
                        newCal.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
                    } else if (recurrence.isOnSaturday()) {
                        newCal.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
                    } else if (recurrence.isOnSunday()) {
                        newCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                    } else {
                        String errMsg = "Unable to generate next recurring yearly task because " +
                                recurringTask.getName() +
                                " has no day of week set";
                        mLogger.log(Level.WARNING, errMsg);
                        throw new SchedulingException(errMsg);
                    }
                }

                if (newCal.getTime().getTime() > nowCal.getTime().getTime()) {
                    newDueDateFound = true;
                }
            }

            newTask.setDueDate(newCal.getTime());

            // check if reminder date was set and adjust it if needed
            Date reminderDate = pTask.getReminderDate();
            long reminderDateTimeMs = reminderDate.getTime();

            if (reminderDateTimeMs > 0){
                long timeDiff = oldDueDate.getTime() - reminderDateTimeMs;
                mLogger.log(Level.INFO, "Reminder Date : " + reminderDate);
                mLogger.log(Level.INFO, "Old Reminder Date Time ms : " +
                        reminderDateTimeMs);
                long newDueDateMs = newTask.getDueDate().getTime();
                mLogger.log(Level.INFO, "New due date time ms : " + newDueDateMs);
                Date newReminderDate = new Date(newDueDateMs - timeDiff);
                mLogger.log(Level.INFO, "New Reminder Date : " + newReminderDate);
                newTask.setReminderDate(newReminderDate);
                recurringTask.setReminderDate(newReminderDate);
            }

            recurringTask.addTask(newTask);
            recurringTask.setDueDate(newCal.getTime());
        } else {
            String errMsg = "Unable to generate next recurring yearly task because " +
                    pTask.getName() + " task is not complete";
            mLogger.log(Level.WARNING, errMsg);
            throw new SchedulingException(errMsg);
        }

        return recurringTask;
    }


    private double getAverageDateDifference(Vector<Date> pDates) {
        double rAvg = 0;
        long timeDiff = 0;
        int numDates = pDates.size();

        for (int i = 0; i < numDates -1; i++){
            timeDiff += pDates.elementAt(i+1).getTime() -
                    pDates.elementAt(i).getTime();
        }

        rAvg = timeDiff/(numDates-1);

        return rAvg;
    }

    private double getVarianceOfDateDifference(Vector <Date> pDates, double pAverage) {
        double rVar = 0;
        long timeDiff = 0;
        int numDates = pDates.size();
        double diffMinusMean;

        for (int i = 0; i < numDates - 1; i++) {
            diffMinusMean = (pDates.elementAt(i+1).getTime() -
                    pDates.elementAt(i).getTime()) - pAverage;
            rVar += diffMinusMean*diffMinusMean;
        }

        rVar /= (numDates-1);
        return rVar;
    }

    private RecurrenceType determineRecurrence(Vector<Date> pDates) {
        RecurrenceType rRecurrence = RecurrenceType.NONE;
        double avg = 0;
        double var = 0;
        double stdDev = 0;

        mLogger.log(Level.INFO, "Comparing dates of " + pDates.size()
                + " tasks");
        avg = getAverageDateDifference(pDates);
        mLogger.log(Level.INFO, " Average date difference of is " + avg);
        var = getVarianceOfDateDifference(pDates, avg);
        mLogger.log(Level.INFO, "Variance of dates is " + var);

        // convert to hours
        avg /= (1000*60*60);
        mLogger.log(Level.INFO, "Average date difference converted to hours is "
                + avg);

        stdDev = Math.sqrt(var);
        stdDev /= (1000*60*60);
        mLogger.log(Level.INFO, "Standard deviation of date difference converted" +
                " to hours is " + stdDev);

        if (avg <= 72 && stdDev <= 48) {
            // Use a daily recurrence
            rRecurrence = RecurrenceType.DAILY;
            mLogger.log(Level.INFO, "Task recurrence is DAILY");
        } else if (avg >= 120 && avg <= 504 && stdDev <= 336) {
            // Use a weekly recurrence
            rRecurrence = RecurrenceType.WEEKLY;
            mLogger.log(Level.INFO, "Task recurrence is WEEKLY");
        } else if (avg > 504 && avg <= 1488 && stdDev <= 744) {
            // Use a monthly recurrence
            rRecurrence = RecurrenceType.MONTHLY;
            mLogger.log(Level.INFO, "Task recurrence is MONTHLY");
        } else if (avg >= 3720 && avg <= 5208 && stdDev <= 1080) {
            // Use a 6-month recurrence
            rRecurrence = RecurrenceType.SIX_MONTHS;
            mLogger.log(Level.INFO, "Task recurrence is SIX_MONTHS");
        } else if (avg < 13128) {
            // Use a yearly recurrence
            rRecurrence = RecurrenceType.YEARLY;
            mLogger.log(Level.INFO, "Task recurrence is YEARLY");
        } else {
            mLogger.log(Level.INFO, "Unable to determine task recurrence");
        }

        return rRecurrence;
    }
}
