package com.boarbeard.audio

import android.content.res.Resources
import android.text.Html
import com.boarbeard.R

class MissionLog @JvmOverloads constructor(
    val actionText: CharSequence,
    val clockText: CharSequence? = null
) {

    companion object {
        /**
         * Returns a new MissionLog entry formatted for a mission introduction
         * message.
         *
         * @param res might be used for getting color or style information.
         * @param message the text to display.
         */
        fun formatIntro(res: Resources, message: String?): MissionLog {
            return MissionLog(
                Html.fromHtml(
                    "<b><i><span style=\"color:" +
                            colorToHTML(res.getColor(R.color.mission_card_intro_text_color)) +
                            ";\">" + message + "</span></i></b>", 0
                )
            )
        }

        /**
         * Takes a color in the form 0xAARRGGBB and returns a pound sign + the
         * left-padded RGB part, suitable for use in HTML.  For example, given
         * 0xffc7ebfc, this returns "#C7EBFC".
         *
         *
         * Considering eventually replacing this with a ForegroundColorSpan.
         */
        //  if you use this outside of this package, move it to some utility class!
        /*public*/
        @JvmStatic
        fun colorToHTML(argb: Int): String {
            return String.format("#%06X", argb and 0xffffff)
        }
    }
}