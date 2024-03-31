package com.example.mad_2024_app.Activities

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.mad_2024_app.App
import com.example.mad_2024_app.R
import com.example.mad_2024_app.RepositoryProvider
import com.example.mad_2024_app.database.Donut
import com.example.mad_2024_app.database.FavoriteDonuts
import com.example.mad_2024_app.repositories.DonutRepository
import com.example.mad_2024_app.repositories.FavoriteDonutsRepository
import com.example.mad_2024_app.view_models.DonutViewModel
import com.example.mad_2024_app.view_models.FavoriteDonutsViewModel
import com.example.mad_2024_app.view_models.ViewModelFactory
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth


class FavoriteDonutsActivity : AppCompatActivity() {
    private val TAG = "LogoGPSFavDonutActivity"
    private lateinit var latestLocation: Location
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var donutViewModel: DonutViewModel
    private lateinit var donutRepo: DonutRepository
    private lateinit var favoriteDonutsViewModel: FavoriteDonutsViewModel
    private lateinit var favoriteDonutRepo: FavoriteDonutsRepository

    private lateinit var donutAdapter: DonutAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check user login status before proceeding
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            // User is not logged in, redirect to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Close current activity
            return
        }

        val appContext = application as App

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        applyTheme(sharedPreferences)

        initializeViewModels()

        setContentView(R.layout.activity_favorite_donuts)

        toggleDrawer()

        setupList(appContext, sharedPreferences)
    }

    private fun setupList(appContext : Context, sharedPreferences: SharedPreferences) {
        val listView = findViewById<ListView>(R.id.lvDonuts)
        donutViewModel.allDonuts.observe(this, Observer { donuts ->
            if (donuts != null) {
                donutAdapter = DonutAdapter(this, donuts, favoriteDonutsViewModel)

                setupDonutObserverForFavoriteDonuts(appContext, donutAdapter = donutAdapter, sharedPreferences = sharedPreferences)

                listView.adapter = donutAdapter
            } else {
                Log.d(TAG, "No donuts found")
            }
        })
    }

    private fun setupDonutObserverForFavoriteDonuts(appContext: Context, donutAdapter : DonutAdapter, sharedPreferences: SharedPreferences) {
        val uuid = sharedPreferences.getString("userId", null)

        if (uuid != null) {
            favoriteDonutsViewModel.getFavoriteDonutsByUser(uuid)
            favoriteDonutsViewModel.favoriteDonuts.observe(this, Observer { favoriteDonutsList ->
                if (favoriteDonutsList != null) {
                    val favoriteDonutsIds = favoriteDonutsList.map { it.donutId }.toSet()
                    donutAdapter.setFavoriteDonutsIds(favoriteDonutsIds)
                }
            })
        } else {
            Log.e(TAG, "User ID not found in SharedPreferences.")
        }
    }



    class DonutAdapter(context: Context, private val donuts: List<Donut>, private val favoriteDonutViewModel: FavoriteDonutsViewModel) :
        ArrayAdapter<Donut>(context, 0,donuts) {
        private val inflater: LayoutInflater = LayoutInflater.from(context)

        private val TAG = "DonutAdapter"

        private var favoriteDonutsIds: Set<Int> = emptySet()

        fun setFavoriteDonutsIds(newFavoriteDonutsIds: Set<Int>) {
            this.favoriteDonutsIds = newFavoriteDonutsIds

            Log.d(TAG, "Favorite donuts updated: ${favoriteDonutsIds}")

            notifyDataSetChanged()
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.donut_list_item, parent, false)

            val sharedPreferences =
                context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

            val donut = donuts[position]

            val imageView = view.findViewById<ImageView>(R.id.image_view)
            Glide.with(context).load(donut.image).into(imageView)

            val nameTextView = view.findViewById<TextView>(R.id.donut_name)
            nameTextView.text = donut.name

            val typeTextView = view.findViewById<TextView>(R.id.donut_type)
            typeTextView.text = donut.type

            val likeButton = view.findViewById<CheckBox>(R.id.like_button_donut)

            val isFavorite = donut.donutId in favoriteDonutsIds

            Log.d(TAG, "like button is checked: $isFavorite for donut: ${donut.donutId}" +
                    " in list of favorite donuts: ${favoriteDonutsIds.toString()}")

            likeButton.isChecked = isFavorite

            likeButton.setOnClickListener { view ->
                val isChecked = likeButton.isChecked
                val uuid = sharedPreferences.getString("userId", null) ?: return@setOnClickListener
                handleFavoriteDonutToggle(donut.donutId, isChecked, uuid)
            }

            return view
        }

        private fun handleFavoriteDonutToggle(donutId: Int, isChecked: Boolean, uuid: String) {
            val updatedFavoriteDonutsIds = favoriteDonutsIds.toMutableSet()

            if (isChecked) {
                updatedFavoriteDonutsIds.add(donutId)
                favoriteDonutViewModel.upsertFavoriteDonut(FavoriteDonuts(uuid = uuid, donutId = donutId))
            } else {
                updatedFavoriteDonutsIds.remove(donutId)
                favoriteDonutViewModel.removeFavoriteDonutById(uuid = uuid, donutId = donutId)
            }

            setFavoriteDonutsIds(updatedFavoriteDonutsIds)
        }

    }

    private fun initializeViewModels(){
        donutRepo = RepositoryProvider.getDonutRepository()
        val donutFactory = ViewModelFactory(donutRepo)
        donutViewModel = ViewModelProvider(this, donutFactory)[DonutViewModel::class.java]

        favoriteDonutRepo = RepositoryProvider.getFavoriteDonutsRepository()
        val favoriteDonutFactory = ViewModelFactory(favoriteDonutRepo)
        favoriteDonutsViewModel = ViewModelProvider(this, favoriteDonutFactory)[FavoriteDonutsViewModel::class.java]
    }

    private fun applyTheme(sharedPreferences: SharedPreferences) {
        val isDarkModeEnabled = sharedPreferences.getBoolean("darkModeEnabled", false)

        // Apply the appropriate theme
        if (isDarkModeEnabled) {
            setTheme(R.style.AppTheme_Dark)
        } else {
            setTheme(R.style.AppTheme_Light)
        }
    }

    private fun toggleDrawer() {
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view_drawer)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close)
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.nav_home -> {
                    // Handle nav_home click (Home)
                    val rootView = findViewById<View>(android.R.id.content)
                    goHome(rootView)
                    true
                }

                R.id.nav_maps -> {
                    // Handle nav_maps click (OpenStreetMaps)
                    Toast.makeText(applicationContext, "OpenStreetMaps", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goMaps(rootView)
                    true
                }

                R.id.nav_settings -> {
                    // Handle nav_settings click (Settings)
                    Toast.makeText(applicationContext, "Settings", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goSettings(rootView)
                    true
                }

                R.id.nav_login -> {
                    // Handle nav_login click (Login)
                    val rootView = findViewById<View>(android.R.id.content)
                    goLogin(rootView)
                    true
                }

                R.id.nav_profile -> {
                    // Handle nav_profile click (Profile)
                    Toast.makeText(applicationContext, "Profile", Toast.LENGTH_SHORT).show()
                    val rootView = findViewById<View>(android.R.id.content)
                    goProfile(rootView)
                    true
                }

                R.id.nav_logout -> {
                    logoutUser()
                    true
                }

                else -> false
            }
        }
    }

    fun onPrevButtonClick(view: View) {
        // go to another activity
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }

    fun goHome(view: View) {
        // go to Main
        val intent = Intent(this, MainActivity::class.java)

        startActivity(intent)
    }

    fun goMaps(view: View) {
        // go to OpenStreetMaps
        if (::latestLocation.isInitialized) {
            val intent = Intent(this, OpenStreetMap::class.java).apply {
                putExtra("locationBundle", Bundle().apply {
                    putParcelable("location", latestLocation)
                })
            }
            startActivity(intent)
        } else {
            Toast.makeText(this, "Location not available yet.", Toast.LENGTH_SHORT).show()
        }
    }

    fun goSettings(view: View) {
        // go to Settings
        val intent = Intent(this, SettingsActivity::class.java)
        startActivity(intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    fun goProfile(view: View) {
        // go to Settings
        val intent = Intent(this, ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun goLogin(view: View) {
        // Check if the user is already logged in
        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // User is already logged in, show a toast message and do not navigate
            Toast.makeText(this, "Already logged in", Toast.LENGTH_SHORT).show()
        } else {
            // User is not logged in, navigate to LoginActivity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            // Optionally, you can remove this toast to avoid redundancy
            Toast.makeText(applicationContext, "Login", Toast.LENGTH_SHORT).show()
        }
    }

    private fun logoutUser() {
        val auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            // User is logged in, proceed with logout
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()

            // Redirect to login screen or another appropriate activity
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // Optionally, close the current activity
        } else {
            // User is not logged in
            Toast.makeText(this, "No user is logged in", Toast.LENGTH_SHORT).show()
        }
    }
}