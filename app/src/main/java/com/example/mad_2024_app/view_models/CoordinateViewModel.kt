package com.example.mad_2024_app.view_models

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Coordinate
import com.example.mad_2024_app.repositories.CoordinateRepository
import kotlinx.coroutines.launch

class CoordinateViewModel(private val coordinateRepository: CoordinateRepository) : ViewModel() {

    private val _nearCoordinates = MutableLiveData<List<Coordinate>?>()
    val nearCoordinates: LiveData<List<Coordinate>?> = _nearCoordinates
    private val TAG = "CoordinateViewModel"

    fun getCoordinateById(coordinateId: Int, callback: (Coordinate?) -> Unit) = viewModelScope.launch {
        coordinateRepository.getAddressById(coordinateId).collect { coordinate ->
            callback(coordinate)
        }
    }

    fun insertCoordinate(coordinate: Coordinate) = viewModelScope.launch {
        coordinateRepository.insertAddress(coordinate)
    }

    fun deleteCoordinate(coordinate: Coordinate) = viewModelScope.launch {
        coordinateRepository.deleteAddress(coordinate)
    }

    fun deleteCoordinateById(coordinateId: Int) = viewModelScope.launch {
        coordinateRepository.deleteAddressById(coordinateId)
    }

    // Additional methods as needed
}