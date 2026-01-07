package `in`.hridayan.driftly.core.data.database


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import `in`.hridayan.driftly.core.data.model.AttendanceEntity
import `in`.hridayan.driftly.core.data.model.ClassScheduleEntity
import `in`.hridayan.driftly.core.data.model.SubjectEntity

@Database(
    entities = [SubjectEntity::class, AttendanceEntity::class, ClassScheduleEntity::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class SubjectDatabase : RoomDatabase() {
    abstract fun subjectDao(): SubjectDao
    abstract fun attendanceDao(): AttendanceDao
    abstract fun classScheduleDao(): ClassScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: SubjectDatabase? = null

        /**
         * Get database instance for non-DI contexts (e.g., BroadcastReceiver)
         */
        fun getDatabase(context: Context): SubjectDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SubjectDatabase::class.java,
                    "subject_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

