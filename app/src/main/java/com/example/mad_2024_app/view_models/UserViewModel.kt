package com.example.mad_2024_app.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.test.core.app.ActivityScenario.launch
import com.example.mad_2024_app.database.User
import com.example.mad_2024_app.repositories.UserRepository

class UserViewModel {
    private val _data = MutableLiveData<String>()
    val data: LiveData<String> = _data

}