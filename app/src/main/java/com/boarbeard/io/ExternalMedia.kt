package com.boarbeard.io

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import com.boarbeard.R
import java.io.File
import java.io.FileOutputStream

import java.util.*

object ExternalMedia {
    lateinit var media_context: Context

    @JvmStatic
	fun init(context: Context) {
        media_context = context
        try {

            if (!File(context.getExternalFilesDir(null), "readme.txt").exists()) {


                // Write read me
                var src = context.resources.openRawResource(
                        R.raw.readme)
                val readme = FileOutputStream(File(context.getExternalFilesDir(null), "readme.txt"))

                var buffer = ByteArray(src.available())
                src.read(buffer)
                readme.write(buffer)
                readme.close()
                src.close()



                // Write grammar.xml example
                src = context.resources.openRawResource(R.raw.grammar)
                val grammar = FileOutputStream(File(context.getExternalFilesDir(null), "grammar.xml"))
                buffer = ByteArray(src.available())
                src.read(buffer)
                grammar.write(buffer)
                src.close()
                grammar.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Something else is wrong. It may be one of many other states, but all we need
    // to know is we can neither read nor write
    // We can only read the media
    // We can read and write the media
    val isReadable: Boolean
        get() {
            var mExternalStorageAvailable = false
            val state = Environment.getExternalStorageState()
            mExternalStorageAvailable = if (Environment.MEDIA_MOUNTED == state) {
                // We can read and write the media
                true
            } else Environment.MEDIA_MOUNTED_READ_ONLY == state
            return mExternalStorageAvailable
        }

    @JvmStatic
	fun getMediaFolders(context: Context?): List<Uri> {
        val folders: MutableList<Uri> = ArrayList()
        if (isReadable) {
            try {
                val dir = context?.getExternalFilesDir(null)

                if (dir != null) {
                    for (file in dir.listFiles()) {
                        if (file.isDirectory) folders.add(Uri.fromFile(file))
                    }
                }
            } catch (e: Exception) {
            }
        }
        return folders
    }

    fun getMediaFiles(directory: File): List<Uri> {
        val medias: MutableList<Uri> = ArrayList()
        if (isReadable) {
            try {
                if (directory.listFiles() != null) {
                    for (file in directory.listFiles()) {
                        medias.add(Uri.fromFile(file))
                    }
                }
            } catch (e: Exception) {
            }
        }
        return medias
    }

    @JvmStatic
	fun getMediaTextFile(folder: String): File? {
        if (isReadable) {
            try {
                val file = File(File(media_context.getExternalFilesDir(null), folder), "grammar.xml")
                if (file.isFile) return file
            } catch (e: Exception) {
            }
        }
        return null
    }
}