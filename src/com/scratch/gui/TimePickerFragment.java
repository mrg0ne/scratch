package com.scratch.gui;

import com.scratch.data.settings.SharedPreferencesSettingsManager;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.os.Bundle;
import android.widget.TimePicker;

public class TimePickerFragment extends DialogFragment {

	public static final String DEFAULT_HOUR_KEY = "DEFAULT_HOUR_KEY";

	public static final String DEFAULT_MINUTE_KEY = "DEFAULT_MINUTE_KEY";

	OnTimeSetListener mTimeSetListener;
	
	/**
	 * Create a new instance of TimePickerFragment, providing "hour"
	 * and "minute" as arguments.
	 */
	static TimePickerFragment newInstance(int pHour, int pMinute, 
			OnTimeSetListener pTimeSetListener) {
		TimePickerFragment f = new TimePickerFragment();

		// Put arguments in the Bundle
		Bundle args = new Bundle();
		args.putInt(TimePickerFragment.DEFAULT_HOUR_KEY, pHour);
		args.putInt(TimePickerFragment.DEFAULT_MINUTE_KEY, pMinute);
		f.setTimeSetListener(pTimeSetListener);
		f.setArguments(args);

		return f;
	}

	public Dialog onCreateDialog(Bundle pSavedInstanceState) {
		SharedPreferencesSettingsManager settings = 
				new SharedPreferencesSettingsManager(getActivity().getBaseContext());
		int hour = getArguments().getInt(DEFAULT_HOUR_KEY);
		int minute = getArguments().getInt(DEFAULT_MINUTE_KEY);
		/*
		int hour = (int)settings.getDefaultTimeDue()/(60*60*1000);
		int minute = (int)settings.getDefaultTimeDue()% (1000*60);
*/

		Activity a = getActivity();

		// Create a new instance of TimePickerDialog and return it
		TimePickerDialog dialog = new TimePickerDialog(a, mTimeSetListener, 
				hour, minute, false);
		TimePicker picker = new TimePicker(a);
		dialog.onTimeChanged(picker, hour, minute);
				
		return dialog;
	}

	public void setTimeSetListener(OnTimeSetListener pTimeSetListener){
		mTimeSetListener = pTimeSetListener;
	}
}
