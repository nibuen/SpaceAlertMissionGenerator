package com.boarbeard.audio;

import android.content.res.Resources;
import android.text.Html;

import com.boarbeard.R;

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
        return new MissionLog(Html.fromHtml("<b><i><span style=\"color:" +
                colorToHTML(res.getColor(R.color.mission_card_intro_text_color)) +
                ";\">" + message + "</span></i></b>"));
    }

    /**
     * Takes a color in the form 0xAARRGGBB and returns a pound sign + the
     * left-padded RGB part, suitable for use in HTML.  For example, given
     * 0xffc7ebfc, this returns "#C7EBFC".
     *
     * <p>This is definitely the wrong place for this method, which <i>must</i>
     * already exist somewhere else.</p>
     */
    //  if you use this outside of this package, move it to some utility class!
    /*public*/ static String colorToHTML(int argb) {
        return String.format("#%06X", argb & 0xffffff);
    }
}
