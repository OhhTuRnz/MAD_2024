package com.example.mad_2024_app.DAOs

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Upsert
import com.example.mad_2024_app.database.Address
import kotlinx.coroutines.flow.Flow

@Dao
interface AddressDAO {
    @Upsert
    suspend fun upsert(address: Address) : Long

    @Query("SELECT * FROM Address")
    fun getAllAddresses(): Flow<List<Address>>

    @Query("SELECT * FROM Address WHERE addressId = :addressId")
    fun getAddressById(addressId: Int): Flow<Address>

    @Delete
    suspend fun delete(address: Address)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM Address WHERE addressId = :addressId")
    fun deleteById(addressId: Int)

    // Other database operations as needed
}
