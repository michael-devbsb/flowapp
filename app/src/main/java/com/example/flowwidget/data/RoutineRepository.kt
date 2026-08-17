package com.example.flowwidget.data

import android.content.Context
import com.example.flowwidget.NotificationScheduler
import com.example.flowwidget.data.local.RoutineBlock
import com.example.flowwidget.data.local.RoutineDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    @ApplicationContext private val context: Context
) {
    suspend fun getAllBlocks() = routineDao.getAllBlocks()

    suspend fun getBlocksForDay(dayOfWeek: Int, date: String) = 
        routineDao.getBlocksForDay(dayOfWeek, date)

    fun getActiveBlock(currentTime: String, dayOfWeek: Int, date: String): Flow<RoutineBlock?> =
        routineDao.getActiveBlock(currentTime, dayOfWeek, date)

    suspend fun getActiveBlockNow(currentTime: String, dayOfWeek: Int, date: String): RoutineBlock? =
        routineDao.getActiveBlock(currentTime, dayOfWeek, date).firstOrNull()

    suspend fun insertBlock(block: RoutineBlock): Long {
        val id = routineDao.insertBlock(block)
        val blockWithId = if (block.id == 0) block.copy(id = id.toInt()) else block
        NotificationScheduler.scheduleAlarm(context, blockWithId)
        return id
    }

    suspend fun deleteBlock(block: RoutineBlock) {
        NotificationScheduler.cancelAlarm(context, block)
        routineDao.deleteBlock(block)
    }

    suspend fun deleteAllBlocks() {
        val blocks = routineDao.getAllBlocks()
        blocks.forEach { NotificationScheduler.cancelAlarm(context, it) }
        routineDao.deleteAllBlocks()
    }

    suspend fun getConflictingFixedBlocks(dayOfWeek: Int, start: String, end: String) =
        routineDao.getConflictingFixedBlocks(dayOfWeek, start, end)
}
