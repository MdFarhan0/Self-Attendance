package `in`.hridayan.driftly.core.di.entry

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import `in`.hridayan.driftly.core.domain.repository.AttendanceRepository

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WorkerEntryPoint {
    fun attendanceRepository(): AttendanceRepository
}
