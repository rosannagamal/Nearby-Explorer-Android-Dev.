package com.example.cs3130_bonusassignment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.cs3130_bonusassignment.model.OverpassResponse
import com.example.cs3130_bonusassignment.network.ApiClient
import com.example.cs3130_bonusassignment.repository.PoiRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapViewFragment : Fragment() {
    // MAP ELEMENTS
    private lateinit var mapView: MapView
    private lateinit var locationOverlay: MyLocationNewOverlay

    // LAYOUT ELEMENTS
    private lateinit var btnRestaurant: Button
    private lateinit var btnGasStation: Button
    private lateinit var btnGarage: Button
    private lateinit var btnParks: Button
    private lateinit var btnSupermarket: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // API ELEMENTS
    private val poiRepository = PoiRepository(ApiClient.overpassService)
    private val poiMarkers = mutableListOf<org.osmdroid.views.overlay.Marker>()
    private lateinit var poiRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_map_view, container, false)

        Configuration.getInstance().load(
            requireContext(),
            PreferenceManager.getDefaultSharedPreferences(requireContext())
        )
        Configuration.getInstance().userAgentValue = requireContext().packageName

        mapView = view.findViewById(R.id.mapView)
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.setMultiTouchControls(true)

        poiRecyclerView = view.findViewById(R.id.recyclerPoiList)
        poiRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        checkLocationPermission()

        view.findViewById<Button>(R.id.btnRecenter).setOnClickListener {
            centerMapOnUser()
        }
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnRestaurant = view.findViewById(R.id.ButtonResturants)
        btnGasStation = view.findViewById(R.id.ButtonGas)
        btnGarage = view.findViewById(R.id.ButtonGarages)
        btnParks = view.findViewById(R.id.ButtonParks)
        btnSupermarket = view.findViewById(R.id.ButtonSupermarkets)
        progressBar = view.findViewById(R.id.progressBar)
        progressBar.visibility = View.GONE

        // SETTING UP BUTTONS
        btnRestaurant.setOnClickListener {fetchPOIs("restaurant")}
        btnGasStation.setOnClickListener {fetchPOIs("fuel")}
        btnGarage.setOnClickListener {fetchPOIs("car_repair")}
        btnParks.setOnClickListener {fetchPOIs("park")}
        btnSupermarket.setOnClickListener {fetchPOIs("supermarket")}
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1001) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                enableMyLocation()
            } else {
                Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun enableMyLocation() {
        val locationProvider = GpsMyLocationProvider(requireContext())
        locationOverlay = MyLocationNewOverlay(locationProvider, mapView)
        locationOverlay.enableMyLocation()
        mapView.overlays.add(locationOverlay)
        centerMapOnUser()
    }

    private fun centerMapOnUser() {
        val myLocation = locationOverlay.myLocation
        if (myLocation != null) {
            mapView.controller.setZoom(16.0)
            mapView.controller.animateTo(GeoPoint(myLocation.latitude, myLocation.longitude))
        } else {
            Toast.makeText(requireContext(), "Waiting for location...", Toast.LENGTH_SHORT).show()
        }
    }

    private fun fetchPOIs(POIName: String) {
        val boundingBox = mapView.boundingBox
        val boundingBoxString = "${boundingBox.latSouth},${boundingBox.lonWest},${boundingBox.latNorth},${boundingBox.lonEast}"
        progressBar.visibility = View.VISIBLE

        val call: Call<OverpassResponse> = poiRepository.fetchCategory(POIName, boundingBoxString)

        call.enqueue(object : Callback<OverpassResponse> {
            override fun onResponse(
                call: Call<OverpassResponse>,
                response: Response<OverpassResponse>
            ) {
                if (response.isSuccessful) {
                    val elements = response.body()?.elements.orEmpty()
                    Log.d("MapViewFragment", "Got ${elements.size} POIs")
                    Toast.makeText(
                        requireContext(),
                        "Fetched ${elements.size} $POIName(s)",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE

                    // REMOVING OLD MARKERS
                    poiMarkers.forEach { mapView.overlays.remove(it) }
                    poiMarkers.clear()

                    // NEW MARKERS
                    for (element in elements) {
                        val marker = org.osmdroid.views.overlay.Marker(mapView)
                        marker.icon =
                            ContextCompat.getDrawable(requireContext(), R.drawable.ic_marker)
                        marker.position = GeoPoint(element.lat, element.lon)
                        marker.setAnchor(
                            org.osmdroid.views.overlay.Marker.ANCHOR_CENTER,
                            org.osmdroid.views.overlay.Marker.ANCHOR_BOTTOM
                        )
                        val name = element.tags?.name ?: "Unnamed Place"
                        marker.title = name
                        mapView.overlays.add(marker)
                        poiMarkers.add(marker)
                    }

                    mapView.invalidate()

                    // RECYCLER VIEW SET UP
                    poiRecyclerView.adapter = ListAdapter(elements, POIName) { selectedPOI ->
                        val geoPoint = GeoPoint(selectedPOI.lat, selectedPOI.lon)
                        mapView.controller.setZoom(20.0)
                        mapView.controller.animateTo(geoPoint)
                    }
                    poiRecyclerView.visibility = View.VISIBLE

                } else {
                    Log.e("MapViewFragment", "API error: ${response.code()}")
                    Toast.makeText(
                        requireContext(),
                        "API error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressBar.visibility = View.GONE
                }
            }

            override fun onFailure(call: Call<OverpassResponse>, t: Throwable) {
                Log.e("MapViewFragment", "Network failure", t)
                Toast.makeText(
                    requireContext(),
                    "Network error: ${t.localizedMessage}",
                    Toast.LENGTH_SHORT
                ).show()
                progressBar.visibility = View.GONE
            }
        })
    }

    override fun onResume() {
        super.onResume()
        if (::locationOverlay.isInitialized) {
            locationOverlay.enableMyLocation()
        }
    }

    override fun onPause() {
        super.onPause()
        if (::locationOverlay.isInitialized) {
            locationOverlay.disableMyLocation()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDetach()
    }
}