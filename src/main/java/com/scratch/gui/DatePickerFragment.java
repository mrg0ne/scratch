package com.scratch.gui;

import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.DatePickerDialog.OnDateSetListener;
import android.os.Bundle;

public class DatePickerFragment extends DialogFragment {

	public final static String DEFAULT_YEAR_KEY = "DEFAULT_YEAR_KEY";
	public final static String DEFAULT_MONTH_KEY = "DEFAULT_MONTH_KEY";
	public final static String DEFAULT_DAY_KEY = "DEFAULT_DAY_KEY";

	private OnDateSetListener mDateSetListener;
	
	/**
	 * Create a new instance of DatePickerFragment, providing "year", "month"
	 * and "day" as arguments.
	 */
	static DatePickerFragment newInstance(int pYear, int pMonth, int pDay, 
			OnDateSetListener pDateSetListener) {
		DatePickerFragment f = new DatePickerFragment();

		// Put arguments in the Bundle
		Bundle args = new Bundle();
		args.putInt(DatePickerFragment.DEFAULT_YEAR_KEY, pYear);
		args.putInt(DatePickerFragment.DEFAULT_MONTH_KEY, pMonth);
		args.putInt(DatePickerFragment.DEFAULT_DAY_KEY, pDay);
		f.setDateSetListener(pDateSetListener);
		f.setArguments(args);

		return f;
	}


	public Dialog onCreateDialog(Bundle pSavedInstanceState) {
		Calendar cal = Calendar.getInstance();
		int year = cal.get(Calendar.YEAR);
		int month = cal.get(Calendar.MONTH);
		int day = cal.get(Calendar.DAY_OF_MONTH);

		if (pSavedInstanceState != null) {
			year = pSavedInstanceState.getInt(DEFAULT_YEAR_KEY);
			month = pSavedInstanceState.getInt(DEFAULT_MONTH_KEY);
			day = pSavedInstanceState.getInt(DEFAULT_DAY_KEY);
		}

		// Create a new instance of DatePickerDialog and return it
		Activity a = getActivity();
		DatePickerDialog dialog = new DatePickerDialog(a, mDateSetListener, 
				year, month, day);
		dialog.getDatePicker().setTag(this.getTag());
		return dialog;
	}
	
	public void setDateSetListener(OnDateSetListener pDateSetListener){
		mDateSetListener = pDateSetListener;
	}
}
