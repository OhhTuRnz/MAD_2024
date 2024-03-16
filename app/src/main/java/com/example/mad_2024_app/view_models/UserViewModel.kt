package com.example.mad_2024_app.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.repositories.UserRepository
import com.example.mad_2024_app.database.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user
    // Function to insert a user
    fun insertUser(user: User) = viewModelScope.launch {
        userRepository.insert(user)
    }

    // Function to get a user by ID
    fun getUserById(userId: Int) = viewModelScope.launch {
        // Perform the operation and handle the result
        // For example, post value to a LiveData or handle in some other way
    }

    // Function to get a user by UUID
    fun getUserByUUID(userUUID: String){
        viewModelScope.launch {
            val fetchedUser = userRepository.getUserByUUID(userUUID)
            _user.postValue(fetchedUser)
        }
    }
}
