package com.example.mad_2024_app.DAOs

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Delete
import androidx.room.OnConflictStrategy
import com.example.mad_2024_app.database.Address

@Dao
interface AddressDAO {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(address: Address) : Long

    @Query("SELECT * FROM Address")
    fun getAllAddresses(): LiveData<List<Address>>

    @Query("SELECT * FROM Address WHERE addressId = :addressId")
    fun getAddressById(addressId: Int): Address

    @Delete
    suspend fun delete(address: Address)

    // Optionally, if you need to delete by ID:
    @Query("DELETE FROM Address WHERE addressId = :addressId")
    fun deleteById(addressId: Int)

    // Other database operations as needed
}
