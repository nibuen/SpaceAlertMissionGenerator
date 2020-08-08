package com.boarbeard.ui.widget;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.boarbeard.R;

public class SeekBarPreference extends DialogPreference implements com.boarbeard.ui.widget.DualSliderView.KnobValuesChangedListener {
	private static final String PREFERENCE_NS = "http://schemas.android.com/apk/res-auto";
	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

	private static final String ATTR_DEFAULT_VALUE = "defaultValue";
	private static final String ATTR_DEFAULT_SECOND_VALUE = "defaultSecondValue";
	private static final String ATTR_MIN_VALUE = "minValue";
	private static final String ATTR_MAX_VALUE = "maxValue";
	private static final String ATTR_LINE_SPACING = "seekBarLineSpacing";
	
	private static final int DEFAULT_MIN_VALUE = 0;
	private static final int DEFAULT_MAX_VALUE = 100;
	private static final int DEFAULT_CURRENT_VALUE = 50;
	private static final int DEFAULT_SECOND_VALUE = -1;
	private static final int DEFAULT_LINE_SPACING = 1;
	
	private int mMinValue;
	private int mMaxValue;
	private int mDefaultValue;
	private int mDefaultSecondValue;
	private int mCurrentValue;
	private int mCurrentSecondValue;
	private int mLineSpacing;
	
	private DualSliderView mSeekBar;
	private TextView mValueText;
	
	
	
	public SeekBarPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
				
		mMinValue = attrs.getAttributeIntValue
			(PREFERENCE_NS, ATTR_MIN_VALUE, DEFAULT_MIN_VALUE);
		mMaxValue = attrs.getAttributeIntValue
			(PREFERENCE_NS, ATTR_MAX_VALUE, DEFAULT_MAX_VALUE);
		mLineSpacing = attrs.getAttributeIntValue
			(PREFERENCE_NS, ATTR_LINE_SPACING, DEFAULT_LINE_SPACING);		
		mDefaultSecondValue = attrs.getAttributeIntValue
		(PREFERENCE_NS, ATTR_DEFAULT_SECOND_VALUE, DEFAULT_SECOND_VALUE);
		
		mDefaultValue = attrs.getAttributeIntValue
			(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE);
	}
	
	@Override
	protected View onCreateDialogView() {

		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		
	    // Get current value from settings
	    mCurrentValue = getPersistedInt(mDefaultValue);
	    mCurrentSecondValue = settings.getInt(getKey() + "SecondValue", mDefaultSecondValue);

	    // Inflate layout
	    LayoutInflater inflater = 
		(LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    View view = inflater.inflate(R.layout.dialog_slider, null);

	    ((TextView) view.findViewById(R.id.current_value)).setText(Integer.toString(mMinValue));

	    // Setup SeekBar
	    mSeekBar = (DualSliderView) view.findViewById(R.id.seek_bar);
	    mSeekBar.setRange(mMinValue, mMaxValue);
	    mSeekBar.setSecondThumbEnabled(mCurrentSecondValue != -1);
	    mSeekBar.setStartKnobValue(mCurrentSecondValue);
	    mSeekBar.setEndKnobValue(mCurrentValue);	    
	    mSeekBar.setLineSpacing(mLineSpacing);
	    mSeekBar.setOnKnobValuesChangedListener(this);
	    
	    // Put current value
	    mValueText = (TextView) view.findViewById(R.id.current_value);
	    mValueText.setText(Integer.toString(mCurrentValue));

	    return view;
	}
	
	@Override
	protected void onDialogClosed(boolean positiveResult) {
	    super.onDialogClosed(positiveResult);

	    if (!positiveResult) {
	        return;
	    }
	    if (shouldPersist()) {	    	
	        persistInt(mCurrentValue);
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getContext());
			settings.edit().putInt(getKey() + "SecondValue", mCurrentSecondValue).commit();
	    }

	    notifyChanged();
	}
		
	@Override
	public CharSequence getTitle() {
	    String title = super.getTitle().toString();
	    int value = getPersistedInt(mDefaultValue);
		SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this.getContext());
		int secondValue = settings.getInt(getKey() + "SecondValue", mDefaultSecondValue);
	    
	    if(secondValue != -1) {
	    	if(secondValue < value) {
	    		title = String.format(title, secondValue, value);
	    	} else {
	    		title = String.format(title, value, secondValue);
	    	}
	    } else {
	    	title = String.format(title, value);
	    } 
	    
	    return title;
	}

	public void onValuesChanged(boolean knobStartChanged,
			boolean knobEndChanged, int knobStart, int knobEnd) {
		
		if(knobEnd > knobStart) {
			mCurrentValue = knobEnd;
			mCurrentSecondValue = knobStart;
		} else {
			mCurrentValue = knobStart;
			mCurrentSecondValue = knobEnd;	
		}
		
		if (!mSeekBar.isSecondThumbEnabled() && knobEndChanged) {
			mValueText.setText("" + knobEnd);
		} else if (knobStartChanged || knobEndChanged) {
			if (knobStart == knobEnd) {
				mValueText.setText("" + knobEnd);
			} else if (knobStart < knobEnd) {
				mValueText.setText(intervalText(knobStart, knobEnd));
			} else {
				mValueText.setText(intervalText(knobEnd, knobStart));
			}
		}
	}
	
	private String intervalText(int min, int max) {
		return min + " - " + max;
	}

}
