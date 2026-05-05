package com.example.trip_planner.data.repository

import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

/**
 * 用户仓库
 */
class UserRepository(private val database: TripDatabase) {

    private val userDao = database.userDao()

    val allUsers: Flow<List<UserEntity>> = userDao.getAllUsers()

    suspend fun register(username: String, email: String, password: String): Result<Long> {
        return try {
            if (userDao.getUserByUsername(username) != null) {
                return Result.failure(Exception("用户名已存在"))
            }
            if (email.isNotEmpty() && userDao.getUserByEmail(email) != null) {
                return Result.failure(Exception("邮箱已被注册"))
            }
            val passwordHash = hashPassword(password)
            val user = UserEntity(
                username = username,
                email = email,
                passwordHash = passwordHash
            )
            val userId = userDao.insertUser(user)
            Result.success(userId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun login(username: String, password: String): Result<UserEntity> {
        return try {
            val user = userDao.getUserByUsername(username)
            if (user == null) {
                return Result.failure(Exception("用户不存在"))
            }
            val passwordHash = hashPassword(password)
            if (user.passwordHash != passwordHash) {
                return Result.failure(Exception("密码错误"))
            }
            userDao.updateLastLogin(user.id, System.currentTimeMillis())
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getUserById(userId: Long): UserEntity? {
        return userDao.getUserById(userId)
    }

    suspend fun updateUser(user: UserEntity) {
        userDao.updateUser(user)
    }

    suspend fun changePassword(userId: Long, oldPassword: String, newPassword: String): Result<Unit> {
        return try {
            val user = userDao.getUserById(userId)
            if (user == null) {
                return Result.failure(Exception("用户不存在"))
            }
            val oldPasswordHash = hashPassword(oldPassword)
            if (user.passwordHash != oldPasswordHash) {
                return Result.failure(Exception("原密码错误"))
            }
            val newPasswordHash = hashPassword(newPassword)
            val updatedUser = user.copy(passwordHash = newPasswordHash)
            userDao.updateUser(updatedUser)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteUser(user: UserEntity) {
        userDao.deleteUser(user)
    }

    private fun hashPassword(password: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(password.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
