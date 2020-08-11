package com.boarbeard.audio;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestMissionLog {

    @Test
    public void testColorToHTML() {
        assertEquals("#C7EBFC", MissionLog.colorToHTML(0xffc7ebfc));
        assertEquals("#C7EBFC", MissionLog.colorToHTML(0xc7ebfc));
        assertEquals("#07EBFC", MissionLog.colorToHTML(0xff07ebfc));
        assertEquals("#07EBFC", MissionLog.colorToHTML(0x7ebfc));
        assertEquals("#00EBFC", MissionLog.colorToHTML(0xff00ebfc));
        assertEquals("#00EBFC", MissionLog.colorToHTML(0xebfc));
        assertEquals("#0000FC", MissionLog.colorToHTML(0xff0000fc));
        assertEquals("#0000FC", MissionLog.colorToHTML(0xfc));
    }
}
