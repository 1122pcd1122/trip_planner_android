package com.example.trip_planner.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.trip_planner.data.local.TripDatabase
import com.example.trip_planner.data.local.entity.UserEntity
import com.example.trip_planner.data.repository.AuthRepository
import com.example.trip_planner.data.repository.UserRepository
import com.example.trip_planner.network.CloudTripDetail
import com.example.trip_planner.network.CloudTripInfo
import com.example.trip_planner.network.LoginResult
import com.example.trip_planner.utils.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class UserViewModel(application: Application) : AndroidViewModel(application) {

    private val database = TripDatabase.getDatabase(application)
    private val userRepository = UserRepository(database)
    private val authRepository = AuthRepository()
    private val context = application.applicationContext

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser

    val currentUserId: StateFlow<Long> = _currentUser.map { it?.id ?: 0L }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val isLoggedIn: StateFlow<Boolean> = _currentUser.map { it != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val allUsers: StateFlow<List<UserEntity>> = userRepository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        restoreLoginState()
    }

    private fun restoreLoginState() {
        viewModelScope.launch {
            val token = UserPreferences.getToken(context)
            if (token.isNotEmpty()) {
                authRepository.verifyToken(token)
                    .onSuccess { result ->
                        syncLocalUser(result)
                    }
                    .onFailure {
                        UserPreferences.clearLoginState(context)
                    }
            } else if (UserPreferences.isLoggedIn(context)) {
                val userId = UserPreferences.getLoggedInUserId(context)
                val user = userRepository.getUserById(userId)
                if (user != null) {
                    _currentUser.value = user
                } else {
                    UserPreferences.clearLoginState(context)
                }
            }
        }
    }

    fun register(username: String, email: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.register(username, password, email)
                .onSuccess { result ->
                    syncLocalUser(result)
                    onSuccess()
                }
                .onFailure {
                    userRepository.register(username, email, password)
                        .onSuccess { userId ->
                            val user = userRepository.getUserById(userId)
                            if (user != null) {
                                _currentUser.value = user
                                UserPreferences.saveLoginState(context, user.id, username, username, email)
                                onSuccess()
                            }
                        }
                        .onFailure { localError ->
                            onError(localError.message ?: "注册失败")
                        }
                }
        }
    }

    fun login(username: String, password: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            authRepository.login(username, password)
                .onSuccess { result ->
                    syncLocalUser(result)
                    onSuccess()
                }
                .onFailure {
                    userRepository.login(username, password)
                        .onSuccess { user ->
                            _currentUser.value = user
                            UserPreferences.saveLoginState(context, user.id, username, user.nickname ?: username, user.email)
                            onSuccess()
                        }
                        .onFailure { localError ->
                            onError(localError.message ?: "登录失败")
                        }
                }
        }
    }

    private fun syncLocalUser(result: LoginResult) {
        viewModelScope.launch {
            var user = userRepository.getUserById(result.userId)
            if (user == null) {
                val newUser = UserEntity(
                    id = result.userId,
                    username = result.username,
                    email = result.email,
                    passwordHash = "",
                    nickname = result.nickname
                )
                userRepository.updateUser(newUser)
                user = newUser
            }
            _currentUser.value = user
            UserPreferences.saveLoginState(
                context,
                result.userId,
                result.username,
                result.nickname,
                result.email,
                result.token
            )
        }
    }

    fun logout() {
        _currentUser.value = null
        UserPreferences.clearLoginState(context)
    }

    fun updateProfile(
        nickname: String? = null,
        avatar: String? = null,
        phone: String? = null,
        bio: String? = null,
        gender: Int? = null,
        birthday: String? = null
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val updatedUser = user.copy(
                nickname = nickname ?: user.nickname,
                avatar = avatar ?: user.avatar,
                phone = phone ?: user.phone,
                bio = bio ?: user.bio,
                gender = gender ?: user.gender,
                birthday = birthday ?: user.birthday
            )
            userRepository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun changePassword(oldPassword: String, newPassword: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val user = _currentUser.value
            if (user == null) {
                onError("未登录")
                return@launch
            }
            userRepository.changePassword(user.id, oldPassword, newPassword)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "修改密码失败") }
        }
    }

    fun saveTripToCloud(
        tripId: String,
        destination: String,
        days: Int,
        startDate: String,
        endDate: String,
        preferences: String,
        tripData: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val token = UserPreferences.getToken(context)
            if (token.isEmpty()) {
                onError("未登录")
                return@launch
            }
            authRepository.saveTripToCloud(
                token, tripId, destination, days, startDate, endDate, preferences, tripData
            )
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "保存失败") }
        }
    }

    fun getCloudTripList(onSuccess: (List<CloudTripInfo>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val token = UserPreferences.getToken(context)
            if (token.isEmpty()) {
                onError("未登录")
                return@launch
            }
            authRepository.getCloudTripList(token)
                .onSuccess { onSuccess(it) }
                .onFailure { onError(it.message ?: "获取失败") }
        }
    }

    fun getCloudTrip(tripId: String, onSuccess: (CloudTripDetail) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val token = UserPreferences.getToken(context)
            if (token.isEmpty()) {
                onError("未登录")
                return@launch
            }
            authRepository.getCloudTrip(token, tripId)
                .onSuccess { onSuccess(it) }
                .onFailure { onError(it.message ?: "获取失败") }
        }
    }

    fun deleteCloudTrip(tripId: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            val token = UserPreferences.getToken(context)
            if (token.isEmpty()) {
                onError("未登录")
                return@launch
            }
            authRepository.deleteCloudTrip(token, tripId)
                .onSuccess { onSuccess() }
                .onFailure { onError(it.message ?: "删除失败") }
        }
    }
}
