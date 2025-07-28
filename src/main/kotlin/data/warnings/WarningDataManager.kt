package data.warnings

import javax.swing.Timer

class WarningDataManager(
    private val warningService: WarningDataService
) {
    private var warnings: MutableList<Warning> = mutableListOf()
    private var listeners: MutableSet<(List<Warning>) -> Unit> = mutableSetOf()
    private val pollTimer: Timer = Timer(30000, { pollWarnings() })

    fun addListener(listener: (List<Warning>) -> Unit) {
        listeners.add(listener)
    }

    fun init() {
        try {
            println("WarningDataManager init")
            warnings.clear()
            warnings.addAll(warningService.poll() ?: emptyList())
        } catch (e: Exception) {
            println("Error initializing WarningDataManager: ${e.message}")
        }
    }

    fun startPolling() {
        println("Started polling for warnings...")
        pollTimer.start()
    }

    fun stopPolling() {
        println("Stopped polling for warnings.")
        pollTimer.stop()
    }

    fun pollWarnings() {
        try {
            val newWarns = warningService.poll()
            if(newWarns != null && newWarns != warnings) {
                println("New warnings received: ${newWarns.size}")
                warnings.clear()
                warnings.addAll(newWarns)
                notifyListeners()
            }
        } catch (e: Exception) {
            println("Error polling warnings: ${e.message}")
        }
    }

    fun getWarnings(): List<Warning> {
        return warnings
    }

    private fun notifyListeners() {
        for(listener in listeners) {
            listener(warnings)
        }
    }
}