package com.boarbeard.io

import android.content.Context
import android.net.Uri
import android.os.Environment
import com.boarbeard.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ExternalMedia {
    lateinit var media_context: Context

    @JvmStatic
    fun init(context: Context) {
        media_context = context

        if (isExternalStorageWritable() && !File(context.getExternalFilesDir(null), "readme.txt").exists()) {
            // Write read me
            try {
                context.resources.openRawResource(R.raw.readme).use { input ->
                    FileOutputStream(File(context.getExternalFilesDir(null), "readme.xml")).use { output ->
                        input.buffered().copyTo(out = output)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }

            // write grammar.xml
            try {
                context.resources.openRawResource(R.raw.grammar).use { input ->
                    FileOutputStream(File(context.getExternalFilesDir(null), "grammar.xml")).use { output ->
                        input.buffered().copyTo(out = output)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

    }

    // Checks if a volume containing external storage is available
    // for read and write.
    private fun isExternalStorageWritable(): Boolean {
        return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
    }

    // Checks if a volume containing external storage is available to at least read.
    private fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    @JvmStatic
    fun getMediaFolders(context: Context?): List<Uri> {
        if (isExternalStorageReadable()) {
            try {
                return context?.getExternalFilesDir(null)?.listFiles()?.map { file ->
                    Uri.fromFile(file)
                } ?: emptyList()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return emptyList()
    }

    @JvmStatic
    fun getMediaTextFile(folder: String): File? {
        if (isExternalStorageReadable()) {
            try {
                val file = File(File(media_context.getExternalFilesDir(null), folder), "grammar.xml")
                if (file.isFile) return file
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return null
    }
}