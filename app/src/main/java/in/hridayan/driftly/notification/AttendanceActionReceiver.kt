package `in`.hridayan.driftly.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf

/**
 * Handles attendance marking from notification actions
 */
class AttendanceActionReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val subjectId = intent.getIntExtra("subjectId", -1)
        val notificationId = intent.getIntExtra("notificationId", -1)
        val action = intent.getStringExtra("action")
        val workTag = intent.getStringExtra("workTag")
        
        if (subjectId == -1 || action == null) return
        
        // Cancel the notification (Standard Mode)
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(notificationId)

        // Cancel the Foreground Worker (Persistent Mode)
        if (workTag != null) {
            WorkManager.getInstance(context).cancelAllWorkByTag(workTag)
        }

        // If action is CLEAR, just dismiss (which is done above), don't mark anything
        if (action == "CLEAR") {
            return
        }
        
        // Mark attendance via background worker
        val workData = workDataOf(
            "subjectId" to subjectId,
            "action" to action
        )

        val markAttendanceWork = OneTimeWorkRequestBuilder<MarkAttendanceWorker>()
            .setInputData(workData)
            .build()

        WorkManager.getInstance(context).enqueue(markAttendanceWork)
    }
}
