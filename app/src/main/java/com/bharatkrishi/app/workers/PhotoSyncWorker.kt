package com.bharatkrishi.app.workers

import android.content.Context
import android.net.Uri
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bharatkrishi.app.AppDatabase
import com.bharatkrishi.app.data.FirebaseManager
import com.bharatkrishi.app.data.local.PhotoEntity

class PhotoSyncWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val database = AppDatabase.getDatabase(applicationContext)
        val photoDao = database.photoDao()
        val firebaseManager = FirebaseManager()

        val unsyncedPhotos = photoDao.getUnsyncedPhotos()

        if (unsyncedPhotos.isEmpty()) {
            return Result.success()
        }

        var hasFailure = false
        unsyncedPhotos.forEach { photo ->
            try {
                val downloadUrl = firebaseManager.uploadImage(Uri.parse(photo.uri))
                if (downloadUrl != null) {
                    photoDao.update(photo.copy(isSynced = true))
                } else {
                    hasFailure = true
                }
            } catch (e: Exception) {
                if (com.bharatkrishi.app.BuildConfig.DEBUG) {
                    android.util.Log.e("PhotoSyncWorker", "Error uploading ${photo.uri}", e)
                }
                hasFailure = true
            }
        }
        return if (hasFailure) Result.retry() else Result.success()
    }
}
