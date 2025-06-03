# Nearby-Explorer-Android-Dev.
Nearby Explorer is an Android application that helps users discover nearby points of interest using Mapbox.

## Technology Stack

**Language:** Kotlin
**Minimum SDK:** 21 (Android 5.0 Lollipop)
**Target SDK:** 33 (Android 13)
**Map:** Mapbox Maps SDK for Android
**Location:** Google Play Services Location API
**Navigation:** Jetpack Navigation Component
**UI:** Material Design Components, ConstraintLayout, RecyclerView
**Async:** Kotlin Coroutines
**Network/Search:** Mapbox Search SDK

## Core Components

**MainActivity:** Hosts NavHostFragment
**MapFragment:** Shows the map and search results
**PlaceDetailFragment:** Displays detailed place info
**MainViewModel:** Handles UI state and logic
**MapboxSearchService:** Wraps Mapbox search API
**Place:** Data class for place information
**PlaceAdapter:** RecyclerView adapter
**MarkerUtils:** Helper for custom map markers
