package com.example.rtse.domain.util

import android.content.ContentUris
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import com.example.rtse.domain.AudioFile
import com.example.rtse.domain.Constants

object ReadFilesUtil {


    fun Context.loadAudioFiles(): ArrayList<AudioFile> {
        Log.d(Constants.TAG, "loadAudioFiles(): ")

        val resolver = applicationContext.contentResolver
        val audioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

//        Log.d(Constants.TAG, "loadAudioFiles(): audioCollectionURI = $audioCollection")

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
        )

        // Define a filter if needed (e.g., only certain file types)
          val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
          val selectionArgs = arrayOf("%/Music/TestAudioFiles/%.wav")

        val audioFiles = mutableListOf<AudioFile>()
        // Sort order (if needed)
        val sortOrder = null

        // Perform the query
        resolver.query(
            audioCollection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            // Iterate through the cursor to retrieve audio file information
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val displayName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                val filePath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val duration =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val audioUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                // Do something with the retrieved information (e.g., add it to a list)
                // You can create a data class or structure to store this information.
                val audioFile = AudioFile(id, displayName, filePath, duration, audioUri)
                audioFiles.add(audioFile)
                Log.d(Constants.TAG, "loadAudioFiles(): $audioFile")
                // Add audioFile to your list or perform any other action needed.
            }
        }

        return ArrayList(audioFiles)

    }

    fun Context.loadSNRFiles(snrRange:String): ArrayList<AudioFile> {
        Log.d(Constants.TAG, "loadSNRFiles(): ")

        val resolver = applicationContext.contentResolver
        val audioCollection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL_PRIMARY
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }

//        Log.d(Constants.TAG, "loadAudioFiles(): audioCollectionURI = $audioCollection")

        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
        )

        // Define a filter if needed (e.g., only certain file types)
        val selection = "${MediaStore.Audio.Media.DATA} LIKE ?"
        val selectionArgs = arrayOf("%/Music/InferenceTest_${snrRange}/%.wav")

        val audioFiles = mutableListOf<AudioFile>()
        // Sort order (if needed)
        val sortOrder = null

        // Perform the query
        resolver.query(
            audioCollection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )?.use { cursor ->
            // Iterate through the cursor to retrieve audio file information
            while (cursor.moveToNext()) {
                val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                val displayName =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME))
                val filePath =
                    cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
                val duration =
                    cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION))
                val audioUri = ContentUris.withAppendedId(
                    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    id
                )
                // Do something with the retrieved information (e.g., add it to a list)
                // You can create a data class or structure to store this information.
                val audioFile = AudioFile(id, displayName, filePath, duration, audioUri)
                audioFiles.add(audioFile)
                Log.d(Constants.TAG, "loadAudioFiles(): $audioFile")
                // Add audioFile to your list or perform any other action needed.
            }
        }

        return ArrayList(audioFiles)

    }
}