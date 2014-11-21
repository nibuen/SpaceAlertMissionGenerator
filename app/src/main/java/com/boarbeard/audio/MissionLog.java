package com.boarbeard.audio;

import android.graphics.Color;

public class MissionLog {
    private final int color;
    private final CharSequence actionText;
    private final CharSequence clockText;

    public MissionLog(int color, CharSequence actionText, CharSequence clockText) {
        this.color = color;
        this.actionText = actionText;
        this.clockText = clockText;
    }

    public MissionLog(CharSequence actionText, CharSequence clockText) {
        this(Color.WHITE, actionText, clockText);
    }

    public MissionLog(CharSequence actionText) {
        this(actionText, null);
    }

    public CharSequence getActionText() {
        return actionText;
    }

    public CharSequence getClockText() {
        return clockText;
    }

    public int getColor() {
        return color;
    }
}
