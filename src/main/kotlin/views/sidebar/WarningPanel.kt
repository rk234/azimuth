package views.sidebar

import data.state.AppState
import data.warnings.Warning
import java.awt.*
import java.time.Duration
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.border.EmptyBorder

class WarningPanel(private val warnings: MutableList<Warning>) : JScrollPane() {

    private val panel = JPanel()

    init {
        AppState.warningDataManager.addListener {
            println("New warnings received: ${it.size}")
            warnings.clear()
            warnings.addAll(it)
        }

        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.background = UIManager.getColor("List.background")
        setViewportView(panel)
        verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED
        horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER
        border = BorderFactory.createEmptyBorder()

        verticalScrollBar.unitIncrement = 10

        if (warnings.isEmpty()) {
            val noWarningsLabel = JLabel("No active warnings").apply {
                horizontalAlignment = SwingConstants.CENTER
                foreground = Color.GRAY
            }
            val centerPanel = JPanel(GridBagLayout())
            centerPanel.add(noWarningsLabel)
            panel.add(centerPanel)
        } else {
            warnings.forEach { warning ->
                val item = WarningItem(warning)
                // Add mouse listener for popup
                item.addMouseListener(object : java.awt.event.MouseAdapter() {
                    override fun mouseClicked(e: java.awt.event.MouseEvent?) {
                        WarningPopup.show(warning)
                    }
                })
                panel.add(item)
                panel.add(Box.createRigidArea(Dimension(0, 5))) // separator
            }
        }
    }
}

class WarningItem(warning: Warning) : JPanel() {

    private val expiresLabel: JLabel

    init {
        layout = BorderLayout(10, 0)
        border = EmptyBorder(5, 0, 5, 10)
        alignmentX = LEFT_ALIGNMENT

        val borderColor = when (warning.name) {
            "Tornado Warning" -> Color.RED
            "Severe Thunderstorm Warning" -> Color.ORANGE
            "Flash Flood Warning" -> Color.GREEN
            else -> Color.GRAY
        }

        val colorStripe = JPanel().apply {
            background = borderColor
            preferredSize = Dimension(5, 0)
        }
        add(colorStripe, BorderLayout.WEST)

        val textPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            isOpaque = false
        }

        val typeLabel = JLabel(warning.name).apply {
            putClientProperty("FlatLaf.styleClass", "h3")
            alignmentX = JLabel.LEFT_ALIGNMENT
        }
        val messageLabel = JTextArea(warning.headline).apply {
            putClientProperty("FlatLaf.styleClass", "p")
            wrapStyleWord = true
            lineWrap = true
            isOpaque = false
            isEditable = false
            isFocusable = false
            background = UIManager.getColor("Label.background")
            foreground = UIManager.getColor("Label.foreground")
            border = UIManager.getBorder("Label.border")
            alignmentX = JLabel.LEFT_ALIGNMENT
        }

        expiresLabel = JLabel().apply {
            alignmentX = JLabel.LEFT_ALIGNMENT
            // Set tooltip with expiry time in local timezone
            toolTipText = "Expires at: " + warning.expires.withZoneSameInstant(java.time.ZoneId.systemDefault())
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z"))
        }
        updateExpiresLabel(warning)

        // Timer to update the label every second
        val timer = Timer(1000) {
            updateExpiresLabel(warning)
        }
        timer.start()

        textPanel.add(typeLabel)
        textPanel.add(Box.createRigidArea(Dimension(0, 2)))
        textPanel.add(messageLabel)
        textPanel.add(Box.createRigidArea(Dimension(0, 5)))
        textPanel.add(expiresLabel)
        add(textPanel, BorderLayout.CENTER)

        val iconLabel = JLabel(UIManager.getIcon("OptionPane.warningIcon"))
        add(iconLabel, BorderLayout.EAST)
    }

    private fun updateExpiresLabel(warning: Warning) {
        val now = ZonedDateTime.now()
        val expires = warning.expires
        val duration = Duration.between(now, expires)
        val seconds = duration.seconds
        val label = if (seconds > 0) {
            val hours = seconds / 3600
            val minutes = (seconds % 3600) / 60
            val secs = seconds % 60
            buildString {
                append("Expires in ")
                if (hours > 0) append("${hours}h ")
                if (minutes > 0 || hours > 0) append("${minutes}m ")
                append("${secs}s")
            }
        } else {
            "Expired!"
        }
        expiresLabel.text = label
    }

    override fun getMaximumSize(): Dimension {
        val preferred = preferredSize
        return Dimension(Integer.MAX_VALUE, preferred.height)
    }
}

// Add this class at the end of the file
object WarningPopup {
    fun show(warning: Warning) {
        val frame = JFrame(warning.name)
        frame.defaultCloseOperation = JFrame.DISPOSE_ON_CLOSE
        frame.layout = BorderLayout()
        frame.isAlwaysOnTop = true

        // Header panel (reuse WarningItem, but disable mouse interaction)
        val header = WarningItem(warning)
        header.isEnabled = false

        // Full message area
        val textArea = JTextArea(warning.message).apply {
            lineWrap = true
            wrapStyleWord = true
            isEditable = false
            isFocusable = false
            border = BorderFactory.createEmptyBorder(10, 10, 10, 10)
        }

        frame.add(header, BorderLayout.NORTH)
        frame.add(JScrollPane(textArea), BorderLayout.CENTER)
        frame.setSize(400, 300)
        frame.setLocationRelativeTo(null)
        frame.isVisible = true
    }
}
