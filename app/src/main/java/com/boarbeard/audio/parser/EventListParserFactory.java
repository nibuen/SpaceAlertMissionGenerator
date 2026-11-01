package com.boarbeard.audio.parser;

import android.content.Context;

/**
 * Hands out the parser for the built-in English narration. The grammar loads
 * every audio clip to read its duration, so it is created once and reused.
 */
public class EventListParserFactory {

    private static EventListParserFactory instance;

    public static synchronized EventListParserFactory getInstance() {
        if (instance == null) {
            instance = new EventListParserFactory();
        }
        return instance;
    }

    private EventListParser parser;

    private EventListParserFactory() {
    }

    public synchronized EventListParser getParser(Context context) {
        if (parser == null) {
            parser = new EnglishParser(new DefaultGrammar(context.getApplicationContext()));
        }
        return parser;
    }
}
