package com.scratch.gui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.support.v4.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;

public class ConfirmationDialogFragment extends DialogFragment {
	
	private static final String KEY = "key";
	
	private String mKey;
	
	private Parcelable mValue;

	public static ConfirmationDialogFragment newInstance(String pTitle) {
		ConfirmationDialogFragment f = new ConfirmationDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", pTitle);
		args.putString("key", "");
		f.setArguments(args);
		return f;
	}
	
	public static ConfirmationDialogFragment newInstance(String pTitle, String pKey, 
			Parcelable pValue) {
		ConfirmationDialogFragment f = new ConfirmationDialogFragment();
		Bundle args = new Bundle();
		args.putString("title", pTitle);
		args.putString(KEY, pKey);
		args.putParcelable(pKey, pValue);
		f.setArguments(args);
		return f;
	}

	public ConfirmationDialogFragment() {
	}

	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = getArguments().getString("title");
		
		mKey = getArguments().getString(KEY);
		
		if (!mKey.equals("")){
			mValue = getArguments().getParcelable(mKey);
		} else {
			mValue = null;
		}

		final Intent intent = new Intent();
		intent.putExtra(mKey, mValue);
		
		Dialog dialog = new AlertDialog.Builder(getActivity(), 
				com.scratch.R.style.dialog_style)
		.setIcon(0)
		.setTitle(title)
		.setPositiveButton(com.scratch.R.string.confirmation_dialog_ok,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pButton) {
				getTargetFragment().onActivityResult(getTargetRequestCode(), 
						Activity.RESULT_OK, intent);
			}
		})
		.setNegativeButton(com.scratch.R.string.confirmation_dialog_cancel,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface pDialog, int pButton) {
				getTargetFragment().onActivityResult(getTargetRequestCode(), 
						Activity.RESULT_CANCELED, intent);
			}
		}).create();

		return dialog;
	}
}