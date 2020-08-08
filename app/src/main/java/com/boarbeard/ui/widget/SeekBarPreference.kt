package com.boarbeard.ui.widget

import android.content.Context
import android.preference.DialogPreference
import android.preference.PreferenceManager
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.boarbeard.R
import com.jaygoo.widget.OnRangeChangedListener
import com.jaygoo.widget.RangeSeekBar
import timber.log.Timber
import kotlin.math.roundToInt

class SeekBarPreference(context: Context?, attrs: AttributeSet) : DialogPreference(context, attrs), OnRangeChangedListener {
    private val mMinValue: Int
    private val mMaxValue: Int
    private val mDefaultValue: Int
    private val mDefaultSecondValue: Int

    private var leftValue: Int = -1
    private var rightValue: Int = -1
    private val mLineSpacing: Int

    private lateinit var mValueText: TextView

    private lateinit var rangeSeekBar: RangeSeekBar

    override fun onCreateDialogView(): View {
        val settings = PreferenceManager.getDefaultSharedPreferences(this.context)

        // Inflate layout
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view = inflater.inflate(R.layout.dialog_slider, null)
        mValueText = view.findViewById<View>(R.id.current_value) as TextView

        // Setup SeekBar
        rangeSeekBar = view.findViewById<View>(R.id.seek_bar) as RangeSeekBar
        rangeSeekBar.setRange(mMinValue.toFloat(), mMaxValue.toFloat(), 1f)

        // Get current value from settings
        leftValue = getPersistedInt(mDefaultValue)

        if (mDefaultSecondValue != DEFAULT_SECOND_VALUE) {
            rightValue = settings.getInt(key + "SecondValue", mDefaultSecondValue)
            rangeSeekBar.seekBarMode = RangeSeekBar.SEEKBAR_MODE_RANGE
            rangeSeekBar.setProgress(leftValue.toFloat(), rightValue.toFloat())
            mValueText.text = intervalText(leftValue, rightValue)
        } else {
            rangeSeekBar.seekBarMode = RangeSeekBar.SEEKBAR_MODE_SINGLE
            rangeSeekBar.setProgress(leftValue.toFloat())
            mValueText.text = leftValue.toString()
        }

        rangeSeekBar.setOnRangeChangedListener(this)
        return view
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        super.onDialogClosed(positiveResult)
        if (!positiveResult) {
            return
        }
        if (shouldPersist()) {
            persistInt(leftValue)
            val settings = PreferenceManager.getDefaultSharedPreferences(this.context)
            settings.edit().putInt(key + "SecondValue", rightValue).apply()
        }
        notifyChanged()
    }

    override fun getTitle(): CharSequence {
        var title = super.getTitle().toString()
        val value = getPersistedInt(mDefaultValue)
        val settings = PreferenceManager.getDefaultSharedPreferences(this.context)

        title = if (mDefaultSecondValue != DEFAULT_SECOND_VALUE) {
            val secondValue = settings.getInt(key + "SecondValue", mDefaultSecondValue)
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

    override fun onRangeChanged(view: RangeSeekBar, leftValue: Float, rightValue: Float, isFromUser: Boolean) {
        //Timber.i("leftValue: $leftValue and rightValue: $rightValue")

        this.leftValue = leftValue.roundToInt()
        this.rightValue = rightValue.roundToInt()

        if (rangeSeekBar.seekBarMode == RangeSeekBar.SEEKBAR_MODE_SINGLE) {
            mValueText.text = this.leftValue.toString()
        } else {
            mValueText.text = intervalText(this.leftValue, this.rightValue)
        }
    }

    override fun onStartTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {}
    override fun onStopTrackingTouch(view: RangeSeekBar, isLeft: Boolean) {}

    companion object {
        private const val PREFERENCE_NS = "http://schemas.android.com/apk/res-auto"
        private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"
        private const val ATTR_DEFAULT_VALUE = "defaultValue"
        private const val ATTR_DEFAULT_SECOND_VALUE = "defaultSecondValue"
        private const val ATTR_MIN_VALUE = "minValue"
        private const val ATTR_MAX_VALUE = "maxValue"
        private const val ATTR_LINE_SPACING = "seekBarLineSpacing"
        private const val DEFAULT_MIN_VALUE = 0
        private const val DEFAULT_MAX_VALUE = 100
        private const val DEFAULT_CURRENT_VALUE = 50
        private const val DEFAULT_SECOND_VALUE = -1
        private const val DEFAULT_LINE_SPACING = 1
    }

    init {
        mMinValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MIN_VALUE, DEFAULT_MIN_VALUE)
        mMaxValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_MAX_VALUE, DEFAULT_MAX_VALUE)
        mLineSpacing = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_LINE_SPACING, DEFAULT_LINE_SPACING)
        mDefaultSecondValue = attrs.getAttributeIntValue(PREFERENCE_NS, ATTR_DEFAULT_SECOND_VALUE, DEFAULT_SECOND_VALUE)
        mDefaultValue = attrs.getAttributeIntValue(ANDROID_NS, ATTR_DEFAULT_VALUE, DEFAULT_CURRENT_VALUE)
    }
}