package com.example.mad_2024_app.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.repositories.UserRepository
import com.example.mad_2024_app.database.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    private val TAG = "UserViewModel"

    // Function to insert a user
    fun upsertUser(user: User) = viewModelScope.launch {
        userRepository.upsert(user)
    }

    // Function to get a user by ID
    fun getUserById(userId: Int) = viewModelScope.launch {
        userRepository.getUserById(userId).collect { userData ->
            _user.postValue(userData)
        }
    }

    // Function to get a user by UUID
    fun getUserByUUID(userUUID: String) = viewModelScope.launch {
        userRepository.getUserByUUID(userUUID).collect { userData ->
            _user.postValue(userData)
        }
    }

    suspend fun getUserByUUIDPreCollect(userUUID: String) : Flow<User?> {
        return userRepository.getUserByUUID(userUUID)
    }

    fun checkAndStoreUser(userUUID: String) = viewModelScope.launch {
        val user = userRepository.getUserByUUID(userUUID).firstOrNull()

        // Perform actions with the user data here
        if (user == null) {
            // User does not exist, perform insertion
            Log.d(TAG, "Creating user with uuid: $userUUID")
            upsertUser(User(uuid = userUUID))
        } else {
            // User exists, update LiveData
            Log.d(TAG, "User Retrieved in Model View")
            _user.postValue(user)
        }
    }
}
