# Azimuth 📡⛈️

**az·i·muth** (noun)
> In radar meteorology, the direction of the radar beam, measured as a horizontal angle clockwise from true north.

> [!WARNING]
> This project is still under active development. Features may be incomplete or unstable, and there are still features planned.


Azimuth is a desktop NEXRAD Level 2 weather radar viewer and weather monitoring suite. It leverages hardware accelerated graphics with OpenGL to render high-resolution NEXRAD Level 2 weather radar data in real-time on an interactive map. Azimuth also displays live weather warnings from the NWS.

## 📸 Screenshots

*Note: some of these may be outdated*

<img width="1920" height="1157" alt="Screenshot From 2025-08-30 20-22-15" src="https://github.com/user-attachments/assets/cf556efa-a546-4d64-983b-a38b4301300d" />
<img width="1920" height="1157" alt="Screenshot From 2025-08-30 20-21-13" src="https://github.com/user-attachments/assets/8f69fcc6-3636-4fb9-a128-3ce3b1b82418" />

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

## 🚀 Building and Running

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
