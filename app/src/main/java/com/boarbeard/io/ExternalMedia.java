package com.boarbeard.io;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import com.boarbeard.R;

public class ExternalMedia {

	public static void init(Context context) {
		try {
			File dir = new File(Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/com.boarbeard");
			if (!dir.exists()) {
				dir.mkdirs();

				// Write read me
				InputStream src = context.getResources().openRawResource(
						R.raw.readme);
				FileOutputStream dst = new FileOutputStream(new File(
						dir.getAbsolutePath() + "/readme.txt"));
				byte[] buffer = new byte[src.available()];
				src.read(buffer);
				dst.write(buffer);
				dst.close();
				src.close();

				// Write grammar.xml example
				src = context.getResources().openRawResource(R.raw.grammar);
				dst = new FileOutputStream(new File(dir.getAbsolutePath()
						+ "/grammar.xml"));
				buffer = new byte[src.available()];
				src.read(buffer);
				dst.write(buffer);
				src.close();
				dst.close();
			}
		} catch (Exception e) {
		}
	}

	public static boolean isReadable() {
		boolean mExternalStorageAvailable = false;
		String state = Environment.getExternalStorageState();

		if (Environment.MEDIA_MOUNTED.equals(state)) {
			// We can read and write the media
			mExternalStorageAvailable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			// We can only read the media
			mExternalStorageAvailable = true;
		} else {
			// Something else is wrong. It may be one of many other states, but
			// all we need
			// to know is we can neither read nor write
			mExternalStorageAvailable = false;
		}

		return mExternalStorageAvailable;
	}

	public static List<Uri> getMediaFolders(Context context) {
		List<Uri> folders = new ArrayList<Uri>();

		if (ExternalMedia.isReadable()) {
			try {
				File dir = new File(Environment.getExternalStorageDirectory()
						.getAbsolutePath() + "/com.boarbeard");
				for (File file : dir.listFiles()) {
					if (file.isDirectory())
						folders.add(Uri.fromFile(file));
				}
			} catch (Exception e) {
			}
		}

		return folders;
	}

	public static List<Uri> getMediaFiles(File directory) {
		List<Uri> medias = new ArrayList<Uri>();
		if (ExternalMedia.isReadable()) {
			try {
				for (File file : directory.listFiles()) {
					medias.add(Uri.fromFile(file));
				}
			} catch (Exception e) {

			}
		}

		return medias;
	}

	public static File getMediaTextFile(String folder) {
		if (ExternalMedia.isReadable()) {
			try {
				File file = new File(Environment.getExternalStorageDirectory()
						.getAbsolutePath()
						+ "/com.boarbeard/"
						+ folder
						+ "/grammar.xml");
				if (file.isFile())
					return file;
			} catch (Exception e) {
			}
		}

		return null;
	}
}
