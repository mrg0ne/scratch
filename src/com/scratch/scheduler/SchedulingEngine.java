package com.scratch.scheduler;

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
			RecurringTask pRecurringTask, Task pTask) {

		mLogger.log(Level.INFO, "Generating next recurring task for : " + 
				pRecurringTask);
		// If there are no task instances, 
		if (pRecurringTask.getTasks().size() == 0) {
			mLogger.log(Level.WARNING, "No sub-tasks exist for : " + 
					pRecurringTask.getName());
		} else {

			switch (pRecurringTask.getRecurrence().getRecurrenceType()) {
			case DAILY:
				generateNextRecurringDailyTask(pRecurringTask, pTask);
				break;
			case WEEKLY:
				generateNextRecurringWeeklyTask(pRecurringTask, pTask);
				break;
			case MONTHLY:
				generateNextRecurringMonthlyTask(pRecurringTask, pTask);
				break;
			case YEARLY:
				generateNextRecurringYearlyTask(pRecurringTask, pTask);
				break;
			default :
				mLogger.log(Level.WARNING, "Can't generate next task for " + 
						"recurrence: " + pRecurringTask.getRecurrence());
			}
		}

		mLogger.log(Level.INFO, "Recurring task updated : " + pRecurringTask);
		return pRecurringTask;
	}

	private void generateNextRecurringDailyTask(
			RecurringTask pRecurringTask, Task pTask){
		mLogger.log(Level.INFO, "generateNextRecurringDailyTask called");
		Date now = new Date();
		Date oldDueDate = pTask.getDueDate();
		TaskRecurrence recurrence = pRecurringTask.getRecurrence();

		// If recurring task does not have any days set, return
		if (!recurrence.isOnMonday() && !recurrence.isOnTuesday() && 
				!recurrence.isOnWednesday() && !recurrence.isOnThursday() 
				&& !recurrence.isOnFriday() && !recurrence.isOnSaturday() 
				&& !recurrence.isOnSunday()) {
			mLogger.log(Level.INFO, pTask.getName() + 
					" does not have any days set");
			return;
		}

		Calendar nowCal = Calendar.getInstance();
		nowCal.setFirstDayOfWeek(Calendar.MONDAY);
		nowCal.setTime(now);
		Calendar newCal = Calendar.getInstance();
		newCal.setTime(oldDueDate);
		newCal.setFirstDayOfWeek(Calendar.MONDAY);
		//		newCal.set(Calendar.YEAR, nowCal.get(Calendar.YEAR));		
		//		newCal.set(Calendar.DAY_OF_YEAR, nowCal.get(Calendar.DAY_OF_YEAR));
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
				pRecurringTask.setReminderDate(newReminderDate);
			}

			pRecurringTask.addTask(newTask);		
			// TODO check if this is necessary
			pRecurringTask.setDueDate(newCal.getTime());
		} else {
			mLogger.log(Level.WARNING, pTask.getName() + " task is not complete");
		}
	}

	private void generateNextRecurringWeeklyTask(
			RecurringTask pRecurringTask, Task pTask){
		Date now = new Date();
		Date oldDueDate = pTask.getDueDate();
		TaskRecurrence recurrence = pRecurringTask.getRecurrence();
		int regularity = recurrence.getOccurenceRegularity();
		Calendar newCal = Calendar.getInstance();
		newCal.setFirstDayOfWeek(Calendar.MONDAY);
		newCal.setTime(oldDueDate);
		mLogger.log(Level.SEVERE, newCal.getTime().toString());
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

			// Check if this is still part of the same week that the old task was due on
			if ((oldCal.get(Calendar.WEEK_OF_YEAR) == nowCal.get(Calendar.WEEK_OF_YEAR))
					&&
					(oldCal.get(Calendar.YEAR) == nowCal.get(Calendar.YEAR))) {
				// Set new cal to the same day as now cal but keep the hours,minutes,seconds
				newCal.set(Calendar.DAY_OF_YEAR, nowCal.get(Calendar.DAY_OF_YEAR));

				newCal.set(Calendar.YEAR, nowCal.get(Calendar.YEAR));

				// If now cal is less than new cal, check if the task should occur today
				if (newCal.getTime().getTime() > nowCal.getTime().getTime()) {
					if (newCal.isSet(Calendar.MONDAY) && recurrence.isOnMonday()) {
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
			}

			while (!newDueDateFound){			
				if (newCal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY){
					// If this is not Sunday, move to the next day
					newCal.add(Calendar.DAY_OF_WEEK, 1);
				} else {
					// Move to the next week that a due date should fall on
					newCal.add(Calendar.WEEK_OF_YEAR, regularity);
					newCal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
				}

				// Only check the weekdays once new cal is in the future
				//if (newCal.getTime().getTime() > nowCal.getTime().getTime()) {
				if (newCal.getTime().getTime() > oldCal.getTime().getTime()) {
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
				pRecurringTask.setReminderDate(newReminderDate);
			}

			pRecurringTask.addTask(newTask);
			pRecurringTask.setDueDate(newCal.getTime());
		} else {
			mLogger.log(Level.WARNING, pTask.getName() + " task is not complete");
		}
	}

	private void generateNextRecurringMonthlyTask(
			RecurringTask pRecurringTask, Task pTask){
		Date now = new Date();
		Date oldDueDate = pTask.getDueDate();
		TaskRecurrence recurrence = pRecurringTask.getRecurrence();

		// Check recurrence validity.
		// If recurring task does not have any days set, return
		if (!recurrence.isUseDay() && !recurrence.isOnMonday() 
				&& !recurrence.isOnTuesday() && 
				!recurrence.isOnWednesday() && !recurrence.isOnThursday() 
				&& !recurrence.isOnFriday() && !recurrence.isOnSaturday() 
				&& !recurrence.isOnSunday()) {
			mLogger.log(Level.INFO, pTask.getName() + 
					" does not have any days set");
			return;
		}

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

				// Set new calendar date
				if (recurrence.isUseDay()){
					newCal.set(Calendar.DAY_OF_MONTH, recurrence.getDayOfMonth());
				} else {
					if (recurrence.getWeekNum() == WeekNumber.LAST) {
						newCal.set(Calendar.WEEK_OF_MONTH, 
								newCal.getActualMaximum(Calendar.WEEK_OF_MONTH));
					} else {
						newCal.set(Calendar.WEEK_OF_MONTH, recurrence.getWeekNum().ordinal());
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
						mLogger.log(Level.WARNING, pRecurringTask.getName() + 
								" has no day of week set");
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
				pRecurringTask.setReminderDate(newReminderDate);
			}

			pRecurringTask.addTask(newTask);		
			pRecurringTask.setDueDate(newCal.getTime());
		} else {
			mLogger.log(Level.WARNING, pTask.getName() + " task is not complete");
		}
	}

	private void generateNextRecurringYearlyTask(
			RecurringTask pRecurringTask, Task pTask){
		Date now = new Date();
		Date oldDueDate = pTask.getDueDate();
		TaskRecurrence recurrence = pRecurringTask.getRecurrence();
		int regularity = recurrence.getOccurenceRegularity();
		Calendar newCal = Calendar.getInstance();
		newCal.setFirstDayOfWeek(Calendar.MONDAY);
		newCal.setTime(oldDueDate);
		newCal.set(Calendar.MONTH, recurrence.getMonth());

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

			if (newCal.getTime().getTime() > nowCal.getTime().getTime()) {				
				newDueDateFound = true;
			}

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
						mLogger.log(Level.WARNING, pRecurringTask.getName() + 
								" has no day of week set");
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
				pRecurringTask.setReminderDate(newReminderDate);
			}

			pRecurringTask.addTask(newTask);		
			pRecurringTask.setDueDate(newCal.getTime());
		} else {
			mLogger.log(Level.WARNING, pTask.getName() + " task is not complete");
		}
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
