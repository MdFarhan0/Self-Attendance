package `in`.hridayan.driftly.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import dagger.hilt.android.AndroidEntryPoint
import `in`.hridayan.driftly.core.domain.repository.ClassScheduleRepository
import `in`.hridayan.driftly.core.domain.repository.SubjectRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * BroadcastReceiver that handles device boot to reschedule all timetable alarms.
 * This ensures notifications survive device reboots.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "BootCompletedReceiver"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "com.htc.intent.action.QUICKBOOT_POWERON" ||
            intent.action == "android.app.action.SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED"
        ) {
            Log.d(TAG, "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━")
            Log.d(TAG, "🔄 Device boot/permission change detected - rescheduling alarms")
            Log.d(TAG, "   Action: ${intent.action}")

            // Use goAsync() for long-running work in receiver
            val pendingResult = goAsync()
            
            // Schedule alarms in background
            CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
                try {
                    rescheduleAllAlarms(context.applicationContext)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Failed to reschedule alarms", e)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }

    private suspend fun rescheduleAllAlarms(context: Context) {
        Log.d(TAG, "📚 Fetching all schedules from database...")

        // Access database directly (we can't use Hilt injection in standard BroadcastReceiver)
        val database = `in`.hridayan.driftly.core.data.database.SubjectDatabase.getDatabase(context)
        val classScheduleDao = database.classScheduleDao()
        val subjectDao = database.subjectDao()

        val allSchedules = classScheduleDao.getAllSchedulesOnce()
        Log.d(TAG, "📋 Found ${allSchedules.size} schedules to reschedule")

        var successCount = 0
        var failCount = 0

        for (schedule in allSchedules) {
            if (!schedule.isEnabled) {
                continue
            }

            // Get subject name
            val subject = try {
                subjectDao.getSubjectById(schedule.subjectId).first()
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to get subject for schedule ${schedule.id}", e)
                failCount++
                continue
            }

            val scheduled = TimetableAlarmScheduler.scheduleClassAlarms(
                context = context,
                scheduleId = schedule.id,
                subjectId = schedule.subjectId,
                subjectName = subject.subject,
                dayOfWeek = schedule.dayOfWeek,
                startTime = schedule.startTime,
                endTime = schedule.endTime,
                location = schedule.location
            )

            if (scheduled) {
                successCount++
            } else {
                failCount++
            }
        }

        Log.d(TAG, "📊 Boot/Permission Reschedule Summary: Success=$successCount, Failed=$failCount")
    }
}
