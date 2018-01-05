package com.scratch.gui.custom;

import com.scratch.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.EditText;

public class CustomEditText extends EditText {

	public CustomEditText(Context pContext, AttributeSet pAttrs, int pDefStyle) {
		super(pContext, pAttrs, pDefStyle);
		init(pAttrs);
	}

	public CustomEditText(Context pContext, AttributeSet pAttrs) {
		super(pContext, pAttrs);
		init(pAttrs);

	}

	public CustomEditText(Context pContext) {
		super(pContext);
		init(null);
	}

	private void init(AttributeSet pAttrs) {
		if (pAttrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(pAttrs,
					R.styleable.CustomFont);
			String fontName = a.getString(R.styleable.CustomFont_name);

			if (fontName != null) {
				Typeface myTypeface = Typeface.createFromAsset(getContext()
						.getAssets(), "fonts/" + fontName);
				setTypeface(myTypeface);
			}

			a.recycle();
		}
	}
}
