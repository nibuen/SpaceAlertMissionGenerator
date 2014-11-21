package com.boarbeard.audio;

public class MissionLog {
    private final CharSequence actionText;
    private final CharSequence clockText;

    public MissionLog(CharSequence actionText, CharSequence clockText) {
        this.actionText = actionText;
        this.clockText = clockText;
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

}
