package com.example.trilogic.model

import com.example.trilogic.model.db.UserDao

object UserRepository {
    
    suspend fun addUser(user: User, dao: UserDao): Boolean {
        val result = dao.insert(user)
        return result != -1L
    }

    suspend fun getUser(username: String, dao: UserDao): User? {
        return dao.getUser(username)
    }

    suspend fun updateHighScore(user: User, newScore: Int, dao: UserDao) {
        if (newScore > user.highScore) {
            val updatedUser = user.copy(highScore = newScore)
            dao.update(updatedUser)
        }
    }
}
