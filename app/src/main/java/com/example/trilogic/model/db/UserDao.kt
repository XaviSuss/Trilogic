package com.example.trilogic.model.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trilogic.model.User

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(user: User): Long

    @Query("SELECT * FROM users WHERE username = :username")
    suspend fun getUser(username: String): User?

    @Query("SELECT * FROM users ORDER BY highScore DESC LIMIT 10")
    suspend fun getTopUsers(): List<User>

    @Update
    suspend fun update(user: User)
}
