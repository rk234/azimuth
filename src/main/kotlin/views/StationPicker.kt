package views

import data.radar.RadarDataProvider
import data.radar.RadarDataRepository
import data.state.AppState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import utils.ProgressListener
import java.awt.Dimension
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

class StationPicker(radarDataProvider: RadarDataProvider) : JFrame("Choose a Station"), ProgressListener {
    private val stationList: JList<String>
    private val scrollPane: JScrollPane
    private val panel: JPanel = JPanel()
    private val stations: List<String>

    private val searchBar: JTextField = JTextField()

    private val applyBtn: JButton = JButton("Apply")
    private val cancelBtn: JButton = JButton("Cancel")

    private val progressBar = JProgressBar()
    private val progressMessage = JLabel()

    private val listeners = mutableSetOf<(String?) -> Unit>()

    val scope = MainScope()

    init {
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        contentPane = panel
        panel.border = BorderFactory.createEmptyBorder(8,8,8,8)

        val label = JLabel("Station List")
        label.alignmentX = JLabel.LEFT_ALIGNMENT

        stations = radarDataProvider.getStationList()

        stationList = JList(stations.toTypedArray())
        stationList.selectionMode = ListSelectionModel.SINGLE_SELECTION

        scrollPane = JScrollPane(stationList)
        scrollPane.alignmentX = JList.LEFT_ALIGNMENT
        scrollPane.maximumSize = Dimension(Integer.MAX_VALUE, 500)
        scrollPane.preferredSize = Dimension(400, 400)

        panel.add(label)
        add(Box.createVerticalStrut(8))

        searchBar.alignmentX = JTextField.LEFT_ALIGNMENT
        searchBar.maximumSize = Dimension(Integer.MAX_VALUE, 30)
        searchBar.document.addDocumentListener(object : DocumentListener {
            override fun insertUpdate(e: DocumentEvent?) {
                filterList()
            }
            override fun removeUpdate(e: DocumentEvent?) {
                filterList()
            }

            override fun changedUpdate(e: DocumentEvent?) {}
        })
        add(searchBar)

        panel.add(scrollPane)
        add(Box.createVerticalStrut(8))

        applyBtn.addActionListener {
            val listener = this
            val selectedStation = getSelectedStation()

            if(selectedStation != null) {
                AppState.activeStation.value = selectedStation
                AppState.window?.pauseAutoPoll()
                scope.launch(Dispatchers.IO) {
                    RadarDataRepository.loadInitialData(AppState.numLoopFrames.value, AppState.radarDataService, listener)
                    AppState.window?.resumeAutoPoll()
                    notifyListeners()
                    isVisible = false
                }
            }
        }

        cancelBtn.addActionListener {
            isVisible = false
        }

        add(JPanel().apply {
            layout = BoxLayout(this, BoxLayout.X_AXIS)
            maximumSize = Dimension(Int.MAX_VALUE, 50)
            alignmentX = JPanel.LEFT_ALIGNMENT
            add(Box.createHorizontalGlue())
            add(applyBtn)
            add(Box.createHorizontalStrut(8))
            add(cancelBtn)
        })

        add(Box.createVerticalStrut(8))

        progressBar.alignmentX = JProgressBar.LEFT_ALIGNMENT
        progressMessage.alignmentX = JLabel.LEFT_ALIGNMENT

        add(progressBar)
        add(progressMessage)

        progressBar.isVisible = false
        progressMessage.isVisible = false

        pack()
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

    override fun notifyProgress(progress: Double?, message: String) {
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