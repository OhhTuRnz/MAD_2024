package com.example.mad_2024_app.view_models

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.repositories.UserRepository
import com.example.mad_2024_app.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _user = MediatorLiveData<User?>()
    private val TAG = "UserViewModel"

    val user: LiveData<User?> = _user
    // Function to insert a user
    fun insertUser(user: User) = viewModelScope.launch {
        userRepository.insert(user)
    }

    // Function to get a user by ID
    fun getUserById(userId: Int) = viewModelScope.launch {
        val liveData = userRepository.getUserById(userId)
        _user.addSource(liveData) { userData ->
            _user.value = userData
        }
    }

    // Function to get a user by UUID
    fun getUserByUUID(userUUID: String) {
        val liveData = userRepository.getUserByUUID(userUUID)
        _user.addSource(liveData) { userData ->
            _user.value = userData
        }
    }

    fun checkAndStoreUser(userUUID: String, lifecycleOwner: LifecycleOwner) {
        userRepository.getUserByUUID(userUUID).observe(lifecycleOwner) { user ->
            // Perform actions with the user data here
            if (user == null) {
                // User does not exist, perform insertion
                Log.d(TAG, "Creating user with uuid: $userUUID")
                viewModelScope.launch(Dispatchers.IO) {
                    userRepository.insert(User(uuid = userUUID))
                }
            } else {
                // User exists, update LiveData
                _user.value = user
            }
        }
    }
}
