package com.ginogipsy.sanmartinoapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.ginogipsy.sanmartinoapp.data.local.entity.CantinaEntity
import com.ginogipsy.sanmartinoapp.data.local.entity.CantinaWithMenuItems
import com.ginogipsy.sanmartinoapp.data.local.entity.MenuItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CantinaDao {

    @Transaction
    @Query("SELECT * FROM cantine ORDER BY number ASC")
    fun getAllCantine(): Flow<List<CantinaWithMenuItems>>

    @Transaction
    @Query("SELECT * FROM cantine WHERE id = :id")
    suspend fun getCantinaById(id: String): CantinaWithMenuItems?

    @Query("SELECT * FROM menu_items WHERE standId = :standId")
    suspend fun getMenuItemsByStandId(standId: String): List<MenuItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCantine(cantine: List<CantinaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(menuItems: List<MenuItemEntity>)

    @Query("DELETE FROM cantine")
    suspend fun clearAllCantine()

    @Query("DELETE FROM menu_items WHERE standId = :standId")
    suspend fun clearMenuItemsForStand(standId: String)

    /**
     * Cerca cantine che hanno piatti con determinati ingredienti o tipi (keywords),
     * oppure per nome della cantina stessa.
     */
    @Transaction
    @Query("""
        SELECT DISTINCT c.* FROM cantine c
        LEFT JOIN menu_items m ON c.id = m.standId
        WHERE c.name LIKE '%' || :query || '%'
        OR m.name LIKE '%' || :query || '%'
        OR m.keywords LIKE '%' || :query || '%'
    """)
    fun searchCantine(query: String): Flow<List<CantinaEntity>>
}
