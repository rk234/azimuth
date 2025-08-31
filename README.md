# Azimuth 📡

Azimuth is a desktop NEXRAD Level 2 weather radar viewer and weather monitoring suite. It leverages hardware accelerated graphics with OpenGL to render high-resolution NEXRAD level 2 weather radar data in real time on an interactive map. Azimuth also displays live weather warnings from the NWS.

## 📸 Screenshots

_Coming Soon_

## 📂 File Structure

```
/
├── lib/ (dependencies)
├── src/
│   └── main/
│       ├── kotlin/
│       │   ├── data/ (data models, state management, and services)
│       │   ├── map/ (map rendering and controls)
│       │   ├── meteo/ (weather data processing)
│       │   ├── rendering/ (OpenGL rendering pipeline)
│       │   ├── utils/ (utility functions)
│       │   ├── views/ (Swing UI components)
│       │   └── Main.kt (application entry point)
│       └── resources/ (application resources)
└── README.md
```

## 🛠️ Tech Stack

*   **Core:** Kotlin
*   **UI:** Java Swing, FlatLaf
*   **Graphics:** OpenGL (via LWJGL)
*   **Weather Data:** UCAR NetCDF
*   **Concurrency:** Kotlin Coroutines