package com.chesire.malime.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.chesire.malime.core.flags.Service
import com.chesire.malime.core.models.UserModel

@Dao
interface UserDao {
    @Delete
    suspend fun delete(user: UserModel)

    @Query("select * from usermodel where service == :service")
    suspend fun get(service: Service): UserModel?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserModel)
}
