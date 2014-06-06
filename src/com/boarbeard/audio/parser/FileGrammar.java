package com.boarbeard.audio.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;

import org.xmlpull.v1.XmlPullParserException;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.boarbeard.audio.MediaInfo;

/**
 * File based Grammar
 * 
 * @author Chris
 * 
 */
public class FileGrammar extends Grammar {

	private static final String LOG_TAG = FileGrammar.class.getSimpleName();
	private final String name;

	public FileGrammar(File grammarXml, Context context) throws IOException {
		this(null, grammarXml, context);
	}

	public FileGrammar(Grammar parent, File grammarXml, Context context) {
		super(parent);

		File folder = grammarXml.getParentFile();
		if (folder == null) {
			throw new IllegalArgumentException("grammarXml may not be /");
		}

		// parse XML
		Map<Element, String> texts = FileGrammar.readTranscriptions(grammarXml);
		if (texts.isEmpty() && parent != null) {

			// grammar missing or corrupt assume same elements as parent
			texts = new HashMap<Element, String>();
			for (Element elem : parent.getSupportedElements()) {
				texts.put(elem, parent.getText(elem));
			}
		}

		addMedia(texts, folder, context);

		// by now texts only contains entries without media. Use media from
		// parent

		addParentMedia(texts, parent, context);

		this.name = folder.getName();
	}

	private void addParentMedia(Map<Element, String> texts, Grammar parent,
			Context context) {

		if (parent == null) {
			return;
		}

		for (Entry<Element, String> entry : texts.entrySet()) {
			MediaInfo parentInfo = parent.getMediaInfo(entry.getKey());
			if (parentInfo == null) {
				// text only
				addElement(entry.getKey(), entry.getValue(), null, null);
			} else {
				// (maybe) audio
				addElement(entry.getKey(), entry.getValue(),
						parentInfo.getResUri(), context);
			}
		}
	}

	/**
	 * Find and add media to text. This method removes found entrys from texts
	 * 
	 * @param texts
	 * @param folder
	 * @param context
	 */
	private void addMedia(Map<Element, String> texts, File folder,
			Context context) {
		SortedSet<String> files = new TreeSet<String>();
		Collections.addAll(files, folder.list());

		for (Iterator<Entry<Element, String>> it = texts.entrySet().iterator(); it
				.hasNext();) {
			final Entry<Element, String> entry = it.next();

			final Element elem = entry.getKey();

			final String prefix = elem.getFileName();

			if (prefix == null) {
				// no Audio for this element
				continue;
			}

			for (String file : files.tailSet(prefix)) {

				if (!file.startsWith(prefix)) {
					// as the set is ordered the next entry won't fit either
					Log.d(LOG_TAG, "no media found for " + prefix);
					break;
				}

				final int extStart = file.lastIndexOf('.');
				if (extStart != -1 && extStart != prefix.length()) {
					// file extension (if any) does not start right after
					// prefix
					continue;
				}

				try {
					final Uri mediaUri = Uri.fromFile(new File(folder, file));
					addElement(elem, entry.getValue(), mediaUri, context);
				} catch (IllegalArgumentException e) {
					Log.d(LOG_TAG, file + " is not supported");
					continue;
				}
				// "mark" entry as successful added
				it.remove();
				// continue with next element
				break;
			}

		}
	}

	@Override
	public String getName() {
		return name;
	}

	private static Map<Grammar.Element, String> readTranscriptions(
			File grammarXml) {

		try {
			FileInputStream in = new FileInputStream(grammarXml);
			try {
				Map<Grammar.Element, String> result = Grammar
						.parseGrammarXml(in);

				// this is for not missing an IOException if everything else is
				// ok. Closing a stream twice is fine
				in.close();

				return result;

			} catch (XmlPullParserException e) {
				Log.e(LOG_TAG, "cannot parse " + grammarXml, e);
				return Collections.emptyMap();

			} finally {
				try {
					in.close();
				} catch (IOException e) {
					// ignore
				}
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "cannot read " + grammarXml, e);
			return Collections.emptyMap();
		}

	}

}
