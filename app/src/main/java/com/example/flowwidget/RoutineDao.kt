package com.example.flowwidget

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface RoutineDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBlock(block: RoutineBlock)

    @Delete
    suspend fun deleteBlock(block: RoutineBlock)

    @Query("SELECT * FROM routine_blocks")
    suspend fun getAllBlocks(): List<RoutineBlock>

    @Query("""
        SELECT * FROM routine_blocks 
        WHERE startTime <= :currentTime AND endTime > :currentTime 
        AND ((isFixed = 1 AND selectedDays LIKE '%' || :dayOfWeek || '%') OR (isFixed = 0 AND date = :date))
        ORDER BY isFixed ASC LIMIT 1
    """)
    suspend fun getActiveBlock(currentTime: String, dayOfWeek: Int, date: String): RoutineBlock?

    @Query("SELECT * FROM routine_blocks WHERE (isFixed = 1 AND selectedDays LIKE '%' || :dayOfWeek || '%') OR (isFixed = 0 AND date = :date)")
    suspend fun getBlocksForDay(dayOfWeek: Int, date: String): List<RoutineBlock>

    @Query("""
        SELECT * FROM routine_blocks 
        WHERE isFixed = 1 AND selectedDays LIKE '%' || :dayOfWeek || '%'
        AND ((startTime >= :start AND startTime < :end) OR (endTime > :start AND endTime <= :end))
    """)
    suspend fun getConflictingFixedBlocks(dayOfWeek: Int, start: String, end: String): List<RoutineBlock>
}
