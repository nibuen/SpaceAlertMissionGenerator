package com.boarbeard.audio;

import android.content.res.Resources;
import android.text.Html;

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

    /**
     * Returns a new MissionLog entry formatted for a mission introduction
     * message.
     *
     * @param res might be used for getting color or style information.  I said
     *            <i>might.</i>
     * @param message the text to display.
     */
    public static MissionLog formatIntro(Resources res, String message) {
        return new MissionLog(Html.fromHtml("<b><i><span style=\"color:#C7EBFC;\">" +
                message + "</span></i></b>"));
    }
}
