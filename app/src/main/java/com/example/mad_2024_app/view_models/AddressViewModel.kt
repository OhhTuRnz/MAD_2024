package com.example.mad_2024_app.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.repositories.AddressRepository
import kotlinx.coroutines.launch

class AddressViewModel(private val addressRepository: AddressRepository) : ViewModel() {

    private val _address = MutableLiveData<Address?>()
    val address: LiveData<Address?> = _address
    private val TAG = "AddressViewModel"

    fun getAddressById(addressId: Int, callback: (Address?) -> Unit) = viewModelScope.launch {
        addressRepository.getAddressById(addressId).collect { address ->
            Log.d(TAG, "Retrieving address for ID: $addressId")
            callback(address)
        }
    }
    fun insertAddress(address: Address) = viewModelScope.launch {
        addressRepository.insertAddress(address)
    }

    fun deleteAddress(address: Address) = viewModelScope.launch {
        addressRepository.deleteAddress(address)
    }

    fun deleteAddressById(addressId: Int) = viewModelScope.launch {
        addressRepository.deleteAddressById(addressId)
    }

    // Additional methods as needed
}