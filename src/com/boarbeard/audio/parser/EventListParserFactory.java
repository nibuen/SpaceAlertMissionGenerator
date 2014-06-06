package com.boarbeard.audio.parser;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.boarbeard.io.ExternalMedia;

public class EventListParserFactory {

	private static int MAX_CACHE_SIZE = 2;

	private static EventListParserFactory instance;

	public static EventListParserFactory getInstance() {
		if (instance == null) {
			instance = new EventListParserFactory();
		}
		return instance;
	}

	private final Map<String, EventListParser> parserCache;

	/**
	 * Access only through {@link #defaultGrammar(Context)}
	 */
	private DefaultGrammar defaultGrammar;

	private EventListParserFactory() {
		super();
		parserCache = Collections
				.synchronizedMap(new LinkedHashMap<String, EventListParser>(16,
						0.75f, true) {

					private static final long serialVersionUID = -801208327526246125L;

					@Override
					protected boolean removeEldestEntry(
							Entry<String, EventListParser> eldest) {

						return size() > MAX_CACHE_SIZE;
					}

				});

	}

	private synchronized Grammar defaultGrammar(Context context) {

		if (defaultGrammar == null) {
			defaultGrammar = new DefaultGrammar(context);
		}
		return defaultGrammar;
	}

	public EventListParser getParser(Context context) {
		SharedPreferences preferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		String grammarName = preferences.getString("voice_choices",
				DefaultGrammar.NAME);
		return getParser(grammarName, context, false);
	}

	public EventListParser getParser(String grammarName, Context context,
			boolean forceCreation) {

		EventListParser result = forceCreation ? null : parserCache
				.get(grammarName);
		if (result != null) {
			return result;
		}

		final Grammar grammar = newGrammar(grammarName, context);
		// choose parser

		if (grammar.getSupportedElements().containsAll(
				GermanParser.REQUIRED_ELEMENTS)) {
			result = new GermanParser(grammar);
		} else {
			// English is a fine default cause the default grammar supports all
			// required elements
			result = new EnglishParser(grammar);
		}

		parserCache.put(grammarName, result);
		return result;
	}

	private Grammar newGrammar(String grammarName, Context context) {

		Grammar def = defaultGrammar(context);
		if (def.getName().equals(grammarName)) {
			return def;
		}

		File grammarXml = ExternalMedia.getMediaTextFile(grammarName);
		if (grammarXml == null) {
			return def;
		}

		return new FileGrammar(def, grammarXml, context);
	}
}
