package com.boarbeard.ui.widget

import android.content.Context
import android.preference.DialogPreference
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.boarbeard.R
import com.google.android.material.slider.RangeSlider
import kotlin.math.roundToInt

class SeekBarPreference(context: Context?, attrs: AttributeSet) : DialogPreference(context, attrs) {
    private val mMinValue: Int
    private val mMaxValue: Int
    private val mDefaultLeftValue: Int
    private val mDefaultRightValue: Int

    private var leftValue: Int = -1
    private var rightValue: Int = -1
    private val mLineSpacing: Int

    private lateinit var mValueText: TextView

    private val isRange: Boolean get() = mDefaultRightValue != DEFAULT_RIGHT_VALUE

    override fun onCreateDialogView(): View {
        val settings = PreferenceManager.getDefaultSharedPreferences(this.context)

        // Inflate layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_slider, null)
        mValueText = view.findViewById<View>(R.id.current_value) as TextView

        val slider = view.findViewById<RangeSlider>(R.id.seek_bar)
        slider.valueFrom = mMinValue.toFloat()
        slider.valueTo = mMaxValue.toFloat()

        // Get current value from settings, clamped so out-of-range saved values can't crash the slider
        leftValue = clamp(getPersistedInt(mDefaultLeftValue))

        if (isRange) {
            rightValue = clamp(settings.getInt(key + RIGHT_VALUE_SUFFIX, mDefaultRightValue))
            if (rightValue < leftValue) {
                leftValue = rightValue.also { rightValue = leftValue }
            }
            slider.setValues(leftValue.toFloat(), rightValue.toFloat())
        } else {
            slider.setValues(leftValue.toFloat())
        }
        updateValueText()

        slider.addOnChangeListener { s, _, _ ->
            val values = s.values
            leftValue = values[0].roundToInt()
            if (isRange) {
                rightValue = values[1].roundToInt()
            }
            updateValueText()
        }
        return view
    }

    private fun clamp(value: Int): Int = value.coerceIn(mMinValue, mMaxValue)

    private fun updateValueText() {
        mValueText.text = if (isRange) intervalText(leftValue, rightValue) else leftValue.toString()
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)
        if (!positiveResult) {
            return
        }
        if (shouldPersist()) {
            persistInt(leftValue)
            val settings = PreferenceManager.getDefaultSharedPreferences(this.context)
            settings.edit().putInt(key + RIGHT_VALUE_SUFFIX, rightValue).apply()
        }
        notifyChanged()
    }

    override fun getTitle(): CharSequence {
        var title = super.getTitle().toString()
        val value = getPersistedInt(mDefaultLeftValue)
        val settings = PreferenceManager.getDefaultSharedPreferences(this.context)

        title = if (mDefaultRightValue != DEFAULT_RIGHT_VALUE) {
            val secondValue = settings.getInt(key + RIGHT_VALUE_SUFFIX, mDefaultRightValue)
            if (secondValue < value) {
                String.format(title, secondValue, value)
            } else {
                String.format(title, value, secondValue)
            }
        } else {
            String.format(title, value)
        }
        return title
    }

    private fun intervalText(min: Int, max: Int): String {
        return "$min - $max"
    }

    companion object {
        private const val PREFERENCE_NS = "http://schemas.android.com/apk/res-auto"
        private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

        const val RIGHT_VALUE_SUFFIX = "RightValue";

        private const val ATTR_DEFAULT_VALUE = "defaultValue"
        private const val ATTR_DEFAULT_RIGHT_VALUE = "defaultSecondValue"
        private const val ATTR_MIN_VALUE = "minValue"
        private const val ATTR_MAX_VALUE = "maxValue"
        private const val ATTR_LINE_SPACING = "seekBarLineSpacing"

        private const val DEFAULT_MIN_VALUE = 0
        private const val DEFAULT_MAX_VALUE = 100
        private const val DEFAULT_CURRENT_VALUE = 50
        private const val DEFAULT_RIGHT_VALUE = -1
        private const val DEFAULT_LINE_SPACING = 1
    }

    init {
        mMinValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MIN_VALUE, DEFAULT_MIN_VALUE)
        mMaxValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MAX_VALUE, DEFAULT_MAX_VALUE)
        mLineSpacing = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_LINE_SPACING, DEFAULT_LINE_SPACING)
        mDefaultRightValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_DEFAULT_RIGHT_VALUE, DEFAULT_RIGHT_VALUE)
        mDefaultLeftValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE)
    }
}