package com.example.mad_2024_app.Activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mad_2024_app.App
import com.example.mad_2024_app.R
import com.example.mad_2024_app.database.Donut
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.util.Locale


class FavoriteDonutsActivity : AppCompatActivity() {
    private val TAG = "LogoGPSFavDonutActivity"
    private lateinit var latestLocation: Location
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var searchView: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var donutsAdapter: DonutsAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPreferences = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        applyTheme(sharedPreferences)

        setContentView(R.layout.activity_favorite_donuts)

        toggleDrawer()

        if (!isLoggedIn) {
            // Redirigir al usuario a la MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // Continuar con la configuración de la actividad
            setupRecyclerView()
            setupSearchView()

            observeDonuts()
        }

    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.recyclerView)
        donutsAdapter = DonutsAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = donutsAdapter
    }

    private fun setupSearchView() {
        searchView = findViewById(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filtrar la lista de donuts con el texto de búsqueda
                donutsAdapter.filter.filter(newText)
                return true
            }
        })
    }

    private fun observeDonuts() {
        val donutFlow = (application as App).database.donutDao().getAllDonuts()
        lifecycleScope.launchWhenStarted {
            donutFlow
                .distinctUntilChanged() // Filtrar cambios para evitar actualizaciones innecesarias
                .map { it.toList() } // Convertir el flujo en una lista
                .collect { donutList ->
                    // Actualizar el adaptador con la nueva lista de donuts
                    donutsAdapter.updateDonuts(donutList)
                }
        }
    }

    class DonutsAdapter(private var originalDonutList: List<Donut>) :
        RecyclerView.Adapter<DonutsAdapter.DonutViewHolder>(), Filterable {

        private var filteredDonutList: List<Donut> = originalDonutList

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonutViewHolder {
            val itemView =
                LayoutInflater.from(parent.context).inflate(R.layout.activity_favorite_donuts, parent, false)
            return DonutViewHolder(itemView)
        }

        override fun onBindViewHolder(holder: DonutViewHolder, position: Int) {
            val currentDonut = filteredDonutList[position]
            holder.textViewName.text = currentDonut.name

            // Añade cualquier otra lógica para mostrar información adicional del donut
        }

        override fun getItemCount() = filteredDonutList.size

        inner class DonutViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val textViewName: TextView = itemView.findViewById(R.id.message)
            // Añade cualquier otra vista que necesites mostrar en el elemento del RecyclerView
        }

        // Implementa la lógica del filtro para el SearchView
        override fun getFilter(): Filter {
            return object : Filter() {
                override fun performFiltering(constraint: CharSequence?): FilterResults {
                    val filteredList = mutableListOf<Donut>()
                    if (constraint.isNullOrBlank()) {
                        filteredList.addAll(originalDonutList)
                    } else {
                        val filterPattern = constraint.toString().lowercase(Locale.ROOT).trim()
                        for (donut in originalDonutList) {
                            if (donut.name.lowercase(Locale.ROOT).contains(filterPattern)) {
                                filteredList.add(donut)
                            }
                        }
                    }
                    val filterResults = FilterResults()
                    filterResults.values = filteredList
                    return filterResults
                }

                @SuppressLint("NotifyDataSetChanged")
                override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                    @Suppress("UNCHECKED_CAST")
                    filteredDonutList = results?.values as List<Donut>
                    notifyDataSetChanged()
                }
            }
        }

        // Método para actualizar la lista de donuts en el adaptador
        @SuppressLint("NotifyDataSetChanged")
        fun updateDonuts(newDonutList: List<Donut>) {
            originalDonutList = newDonutList
            filteredDonutList = newDonutList
            notifyDataSetChanged()
        }
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
        val intent = Intent(this, Settings::class.java)
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
        val intent = Intent(this, Profile::class.java)
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