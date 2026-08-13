package com.ginogipsy.sanmartinoapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ginogipsy.sanmartinoapp.data.local.dao.CantinaDao
import com.ginogipsy.sanmartinoapp.data.local.entity.CantinaEntity
import com.ginogipsy.sanmartinoapp.data.local.entity.MenuItemEntity

@Database(
    entities = [CantinaEntity::class, MenuItemEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cantinaDao(): CantinaDao
}
