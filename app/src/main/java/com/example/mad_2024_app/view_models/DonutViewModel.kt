package com.example.mad_2024_app.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.database.Donut
import com.example.mad_2024_app.repositories.DonutRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class DonutViewModel(private val donutsRepository: DonutRepository) : ViewModel() {

    private val _allDonuts = MutableLiveData<List<Donut>?>()
    val allDonuts: LiveData<List<Donut>> = donutsRepository.getAllDonuts()

    private val _donutById = MutableLiveData<Donut?>()
    val donutById: LiveData<Donut?> = _donutById

    private val TAG = "DonutViewModel"

    // Function to insert or update a donut
    fun upsertDonut(donut: Donut) = viewModelScope.launch {
        donutsRepository.upsertDonut(donut)
    }

    // Function to get all donuts
    fun getAllDonuts() = viewModelScope.launch {
        donutsRepository.getAllDonuts() // No collect needed here
    }


    // Function to get a donut by ID (pre-collect version)
    suspend fun getDonutByIdPreCollect(donutId: Int): Flow<Donut?> {
        return donutsRepository.getDonutById(donutId)
    }

    // Function to get a donut by ID (alternative using LiveData)
    fun getDonutByIdLiveData(donutId: Int) = viewModelScope.launch {
        donutsRepository.getDonutById(donutId).collect { donut ->
            _donutById.postValue(donut)
        }
    }

    // Function to delete a donut
    fun deleteDonut(donut: Donut) = viewModelScope.launch {
        donutsRepository.deleteDonut(donut)
    }

    // Additional methods as needed (e.g., filtering by specific criteria)
}
