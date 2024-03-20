package com.example.mad_2024_app.view_models

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.repositories.AddressRepository
import kotlinx.coroutines.launch

class AddressViewModel(private val addressRepository: AddressRepository) : ViewModel() {

    private val _nearAddresses = MutableLiveData<List<Address>?>()
    val nearAddresses: LiveData<List<Address>?> = _nearAddresses
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