package com.example.mad_2024_app.Controller

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import com.example.mad_2024_app.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore

class ShopDetailsFragment : DialogFragment() {

    private var lastSubmissionTime: Long = 0
    private val submissionDelay: Long = 30000

    companion object {
        private const val SHOP_NAME_KEY = "shop_name"
        private const val SHOP_ADDRESS_KEY = "shop_address"
        private const val SHOP_LATITUDE_KEY = "shop_latitude"
        private const val SHOP_LONGITUDE_KEY = "shop_longitude"

        fun newInstance(shopName: String, shopAddress: String, latitude: Double, longitude: Double): ShopDetailsFragment {
            val fragment = ShopDetailsFragment()
            val bundle = Bundle().apply {
                putString(SHOP_NAME_KEY, shopName)
                putString(SHOP_ADDRESS_KEY, shopAddress)
                putDouble(SHOP_LATITUDE_KEY, latitude)
                putDouble(SHOP_LONGITUDE_KEY, longitude)
            }
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_shop_details, container, false)

        val shopName = arguments?.getString(SHOP_NAME_KEY) ?: ""
        val shopAddress = arguments?.getString(SHOP_ADDRESS_KEY) ?: ""
        val latitude = arguments?.getDouble(SHOP_LATITUDE_KEY) ?: Double.POSITIVE_INFINITY
        val longitude = arguments?.getDouble(SHOP_LONGITUDE_KEY) ?: Double.POSITIVE_INFINITY

        if(latitude == Double.POSITIVE_INFINITY || longitude == Double.POSITIVE_INFINITY) {
            dismiss()
        }

        // Use these values to populate your views
        view.findViewById<TextView>(R.id.shop_name_text_view).text = shopName
        view.findViewById<TextView>(R.id.shop_address_text_view).text = shopAddress
        val ratingBar = view.findViewById<RatingBar>(R.id.rating_bar)
        val commentEditText = view.findViewById<EditText>(R.id.comment_edit_text)
        val submitButton = view.findViewById<Button>(R.id.submit_button)

        submitButton.setOnClickListener {
            val currentUser = Firebase.auth.currentUser
            if (currentUser != null) {
                val currentTime = System.currentTimeMillis()
                if (currentTime - lastSubmissionTime >= submissionDelay) {
                    val rating = ratingBar.rating
                    val comment = commentEditText.text.toString()
                    submitCommentToFirestore(shopName, latitude, longitude, rating, comment, currentUser.uid)
                    lastSubmissionTime = currentTime
                } else {
                    Toast.makeText(context, "Please wait before submitting another comment.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "You must be logged in to leave a comment.", Toast.LENGTH_SHORT).show()
            }
        }


        return view
    }

    private fun submitCommentToFirestore(shopName: String, latitude: Double, longitude: Double, rating : Float, comment: String, userId: String) {
        val commentShopName = shopName.replace(" ", "_" )
        val commentData = hashMapOf(
            "shopId" to "$commentShopName@${latitude};${longitude}",
            "userUuid" to userId,
            "comment" to comment,
            "rating" to rating.toDouble(),
            "timestamp" to FieldValue.serverTimestamp()
        )

        val db = Firebase.firestore
        db.collection("comments")
            .add(commentData)
            .addOnSuccessListener {
                Toast.makeText(context, "Comment submitted successfully", Toast.LENGTH_SHORT).show()
                dismiss()  // Close the fragment
            }
            .addOnFailureListener {
                Toast.makeText(context, "Failed to submit comment", Toast.LENGTH_SHORT).show()
            }
    }
}