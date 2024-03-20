package com.example.mad_2024_app.Activities

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.mad_2024_app.R
import com.example.mad_2024_app.database.Address
import com.example.mad_2024_app.database.Shop
import com.example.mad_2024_app.view_models.AddressViewModel

class FavoriteShopsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)
    }

    class FavoriteShopAdapter(private val context: Context, private val addressViewModel: AddressViewModel) : BaseAdapter() {
        private var shops: MutableList<Shop> = mutableListOf()

        private val TAG = "ShopAdapter"
        fun setShops(newShops: List<Shop>) {
            Log.d(TAG, "Adding Shops")
            Log.d(TAG, "Added ${shops.size} shop(s)")
            shops.clear()
            shops.addAll(newShops)
            notifyDataSetChanged()
        }
        override fun getCount(): Int = shops.size

        override fun getItem(position: Int): Any = shops[position]

        override fun getItemId(position: Int): Long = position.toLong()

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var listItemView = convertView
                ?: LayoutInflater.from(context).inflate(R.layout.shop_list_item, parent, false)

            val shop = getItem(position) as Shop
            listItemView.findViewById<TextView>(R.id.shop_name).text = shop.name

            // Fetch and display the address
            shop.addressId?.let { addressId ->
                addressViewModel.getAddressById(addressId) { address ->
                    address?.let {
                        // Update the UI with the address
                        listItemView.findViewById<TextView>(R.id.shop_address).text = formatAddressString(it)
                    }
                }
            }

            return listItemView
        }

        private fun formatAddressString(address: Address): String {
            return "${address.street}, ${address.number}\n${address.city}, ${address.zipCode}\n${address.country}"
        }
    }
}