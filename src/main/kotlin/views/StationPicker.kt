package views

import data.radar.RadarDataProvider
import data.radar.RadarDataRepository
import data.state.AppState
import kotlinx.coroutines.*
import utils.ProgressListener
import java.awt.Dimension
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class StationPicker(private val parent: JComponent, radarDataProvider: RadarDataProvider) : JPopupMenu(), ProgressListener {
    private val stationList: JList<String>
    private val scrollPane: JScrollPane
    private val stations: List<String>

    private val searchBar: JTextField = JTextField()

    private val applyBtn: JButton = JButton("Apply")
    private val cancelBtn: JButton = JButton("Cancel")

    private val progressBar = JProgressBar()
    private val progressMessage = JLabel()

    private val listeners = mutableSetOf<(String?) -> Unit>()

    private val scope = MainScope()

    private var loadJob: Job? = null

    init {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = BorderFactory.createEmptyBorder(8,8,8,8)
        panel.preferredSize = Dimension(350, 400)

        val label = JLabel("Station List")
        label.alignmentX = JLabel.LEFT_ALIGNMENT

        stations = radarDataProvider.getStationList()

        stationList = JList(stations.toTypedArray())
        stationList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        scrollPane = JScrollPane(stationList)
        scrollPane.alignmentX = JList.LEFT_ALIGNMENT
        scrollPane.maximumSize = Dimension(Integer.MAX_VALUE, 300)
        scrollPane.preferredSize = Dimension(330, 250)

        panel.add(label)
        panel.add(Box.createVerticalStrut(8))

        searchBar.alignmentX = JTextField.LEFT_ALIGNMENT
        searchBar.maximumSize = Dimension(Integer.MAX_VALUE, 25)
        searchBar.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                filterList()
            }
            override fun removeUpdate(e: DocumentEvent?) {
                filterList()
            }

            override fun changedUpdate(e: DocumentEvent?) {}
        })
        panel.add(searchBar)
        panel.add(Box.createVerticalStrut(8))

        panel.add(scrollPane)
        panel.add(Box.createVerticalStrut(8))

        applyBtn.addActionListener {
            val listener = this
            val selectedStation = getSelectedStation()

            if(selectedStation != null) {
                disableBtns()

                runBlocking {
                    loadJob?.cancelAndJoin()
                }

                val prevStation = AppState.activeStation.value

                loadJob = scope.launch(Dispatchers.IO) {
                    try {
                        AppState.activeStation.value = selectedStation
                        AppState.window?.pauseAutoPoll()

                        RadarDataRepository.loadInitialData(
                            AppState.numLoopFrames.value,
                            AppState.radarDataService,
                            listener
                        )
                        AppState.window?.resumeAutoPoll()
                        notifyListeners()
                        this@StationPicker.isVisible = false
                    } catch(e: CancellationException) {
                        println("Load job cancelled")
                        AppState.window?.resumeAutoPoll()
                        AppState.activeStation.value = prevStation
                    }
                }
            }
        }

        cancelBtn.addActionListener {
            isVisible = false
        }

        val buttonPanel = JPanel()
        buttonPanel.layout = BoxLayout(buttonPanel, BoxLayout.X_AXIS)
        buttonPanel.maximumSize = Dimension(Int.MAX_VALUE, 30)
        buttonPanel.alignmentX = JPanel.LEFT_ALIGNMENT
        buttonPanel.add(Box.createHorizontalGlue())
        buttonPanel.add(applyBtn)
        buttonPanel.add(Box.createHorizontalStrut(8))
        buttonPanel.add(cancelBtn)

        panel.add(buttonPanel)
        panel.add(Box.createVerticalStrut(8))

        progressBar.alignmentX = JProgressBar.LEFT_ALIGNMENT
        progressBar.maximumSize = Dimension(Int.MAX_VALUE, 20)
        progressMessage.alignmentX = JLabel.LEFT_ALIGNMENT

        panel.add(progressBar)
        panel.add(progressMessage)

        progressBar.isVisible = false
        progressMessage.isVisible = false

        add(panel)
    }

    private fun filterList() {
        val filter = searchBar.text
        val filtered = stations.filter { it.contains(filter, ignoreCase = true) }
        stationList.setListData(filtered.toTypedArray())
    }

    fun addStationSelectListener(listener: (String?) -> Unit) {
        listeners.add(listener)
    }

    private fun notifyListeners() {
        println("Selected ${getSelectedStation()}")
        listeners.forEach { it(getSelectedStation()) }
    }

    fun getSelectedStation(): String? {
        return stationList.selectedValue
    }

    fun disableBtns() {
        applyBtn.isEnabled = false
        cancelBtn.isEnabled = false
    }

    fun showUnder(component: JComponent) {
        val location = component.locationOnScreen
        val componentSize = component.size
        show(component, 0, componentSize.height)

        // Request focus on the search bar when the popup opens
        SwingUtilities.invokeLater {
            searchBar.requestFocusInWindow()
        }
    }

    override fun notifyProgress(progress: Double?, message: String) {
        SwingUtilities.invokeLater {
            progressBar.isVisible = true
            progressMessage.isVisible = true
            if(progress == null) {
                progressBar.isIndeterminate = true
            } else {
                progressBar.isIndeterminate = false
                progressBar.value = (progress * 100).toInt()
                progressMessage.text = message
            }
        }
    }
}