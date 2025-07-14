package views.sidebar

import data.warnings.Warning
import kotlinx.coroutines.Job
import java.awt.*
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.border.EmptyBorder

class WarningPanel(warnings: List<Warning>) : JScrollPane() {

    private val panel = JPanel()

    init {
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.background = UIManager.getColor("List.background")
        setViewportView(panel)
        verticalScrollBarPolicy = VERTICAL_SCROLLBAR_AS_NEEDED
        horizontalScrollBarPolicy = HORIZONTAL_SCROLLBAR_NEVER
        border = BorderFactory.createEmptyBorder()

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
                panel.add(WarningItem(warning))
                panel.add(Box.createRigidArea(Dimension(0, 5))) // separator
            }
        }
    }
}

class WarningItem(warning: Warning) : JPanel() {

    init {
        layout = BorderLayout(10, 0)
        border = EmptyBorder(5, 0, 5, 10)
        alignmentX = LEFT_ALIGNMENT

        val borderColor = when (warning.type) {
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

        val typeLabel = JLabel(warning.type).apply {
            putClientProperty("FlatLaf.styleClass", "h3")
            alignmentX = JLabel.LEFT_ALIGNMENT
        }
        val messageLabel = JTextArea(warning.message).apply {
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
        val expiresLabel = JLabel("Expires: ${warning.expires.format(DateTimeFormatter.ofPattern("HH:mm z"))}").apply {
            font = font.deriveFont(Font.ITALIC, 11f)
            alignmentX = JLabel.LEFT_ALIGNMENT
        }

        textPanel.add(typeLabel)
        textPanel.add(Box.createRigidArea(Dimension(0, 2)))
        textPanel.add(messageLabel)
        textPanel.add(Box.createRigidArea(Dimension(0, 5)))
        textPanel.add(expiresLabel)
        add(textPanel, BorderLayout.CENTER)

        val iconLabel = JLabel(UIManager.getIcon("OptionPane.warningIcon"))
        add(iconLabel, BorderLayout.EAST)
    }

    override fun getMaximumSize(): Dimension {
        val preferred = preferredSize
        return Dimension(Integer.MAX_VALUE, preferred.height)
    }
}