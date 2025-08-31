# Azimuth 📡⛈️

**az·i·muth** (noun)
> In radar meteorology, the direction of the radar beam, measured as a horizontal angle clockwise from true north.


Azimuth is a desktop NEXRAD Level 2 weather radar viewer and weather monitoring suite. It leverages hardware accelerated graphics with OpenGL to render high-resolution NEXRAD Level 2 weather radar data in real-time.

## Screenshots

*Note: some of these may be outdated*

<img width="1920" height="1157" alt="Screenshot From 2025-08-30 20-22-15" src="https://github.com/user-attachments/assets/cf556efa-a546-4d64-983b-a38b4301300d" />
<img width="1920" height="1157" alt="Screenshot From 2025-08-30 20-21-13" src="https://github.com/user-attachments/assets/8f69fcc6-3636-4fb9-a128-3ce3b1b82418" />

## Features

- Supports reflectivity, velocity, and cross correlation products.
- View up to 4 different radar panes at once.
- Severe weather warnings are overlayed on the map and can be viewed in detail in the warnings tab of the sidebar.
- Supports looping and scrubbing up to the last 50 radar frames.
- All data is automatically updated in real time.


## About NEXRAD Level 2 Data

**NEXRAD** (Next Generation Radar) is the U.S. network of weather surveillance radars operated by the National Weather Service. These Doppler radars provide critical weather information for forecasting and severe weather warnings.

### What Makes Level 2 Data Special

NEXRAD Level 2 data represents the **highest resolution** raw radar data available to the public, offering several key advantages over processed Level 3 products:

- **Full Angular Resolution**: Level 2 data maintains the radar's native 1° and 0.5° angular resolution, providing the most detailed view of weather phenomena
- **Higher Temporal Resolution**: Data updates every 4-10 minutes compared to longer intervals for processed products  
- **Unprocessed Precision**: Raw measurements without smoothing or averaging, preserving fine-scale weather features
- **Enhanced Detail**: Ability to detect and analyze smaller-scale weather features like mesocyclones, wind shear, and precipitation cores
- **Superior Storm Analysis**: Critical for identifying rotation signatures, hook echoes, and other severe weather indicators

This high-resolution data is essential for meteorologists, storm chasers, researchers, and weather enthusiasts who need the most detailed and timely radar information available. Azimuth brings this professional-grade data to your desktop with an intuitive, hardware-accelerated interface.


## File Structure

```
/
├── gradle/ (gradle wrapper)
├── build/ (build output)
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
├── build.gradle.kts (build script)
├── settings.gradle.kts (project settings)
├── gradlew (gradle wrapper executable)
└── README.md
```

## Tech Stack

*   **Core:** Kotlin
*   **UI:** Java Swing, FlatLaf
*   **Graphics:** OpenGL (via LWJGL)
*   **Weather Data:** UCAR NetCDF
*   **Concurrency:** Kotlin Coroutines

## Building and Running

To build and run the project, you will need to have a JDK 8 or higher installed.

1.  **Clone the repository:**
    ```bash
    git clone https://github.com/rk234/azimuth.git
    cd azimuth
    ```

2.  **Build the project:**
    ```bash
    ./gradlew build
    ```

3.  **Run the application:**
    ```bash
    ./gradlew run
    ```
> [!WARNING]
> This project is still under active development. Features may be incomplete or unstable, and there are still features planned.
