package ca.mattlack.marchingsquares

import ca.mattlack.marchingsquares.ball.MetaBall
import ca.mattlack.marchingsquares.function.ConstantFunction
import ca.mattlack.marchingsquares.function.IdentityFunction
import ca.mattlack.marchingsquares.march.SquareMarcher
import java.awt.*
import java.util.concurrent.ForkJoinPool
import javax.swing.JFrame

class Renderer {

    val frame = JFrame("Marching Squares")
    var marcher: SquareMarcher? = null

    var showDots = false

    val pool = ForkJoinPool(Runtime.getRuntime().availableProcessors())

    init {
        frame.defaultCloseOperation = JFrame.EXIT_ON_CLOSE

        val screenSize = Toolkit.getDefaultToolkit().getScreenSize()

        frame.setSize((screenSize.width * 0.8).toInt(), (screenSize.height * 0.8).toInt())
        frame.isVisible = true

        // Center
        frame.setLocationRelativeTo(null)


        frame.createBufferStrategy(2)

        marcher = SquareMarcher(
            { _, _ -> 0.0 },
            Pair(0.0, 0.0),
            Pair(frame.width.toDouble(), frame.height.toDouble()),
            Pair(frame.width / 8, frame.height / 8),
            pool
        )

    }

    fun render() {
        val buffer = frame.bufferStrategy.drawGraphics

        buffer.clearRect(0, 0, frame.width, frame.height)
        draw(buffer)

        buffer.dispose()
        frame.bufferStrategy.show()
    }

    private fun draw(g: Graphics) {
        val backgroundColour = Color.DARK_GRAY.darker().darker().darker()
        g.color = backgroundColour
        g.fillRect(0, 0, frame.width, frame.height)

        // Rainbow gradient
        val g2d = g as Graphics2D
        g2d.paint = GradientPaint(
            0f, 0f, Color.RED,
            frame.width.toFloat(), frame.height.toFloat(), Color.BLUE
        )

        val lines = marcher?.march() ?: emptyList()

        val thickness = 6
        // Rounded stroke
        (g as Graphics2D).stroke = BasicStroke(thickness.toFloat(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)

        // Draw the contour lines in parallel
        for (line in lines) {
            pool.submit {
                g.drawLine(
                    line.start.first.toInt(),
                    line.start.second.toInt(),
                    line.end.first.toInt(),
                    line.end.second.toInt()
                )
            }
        }

        pool.awaitQuiescence(1, java.util.concurrent.TimeUnit.SECONDS)

        if (marcher == null || !showDots) return

        // Draw the dot map
        val dotMap = marcher!!.computeDotMap()

        val min = marcher!!.min
        val max = marcher!!.max
        val step = Pair(
            (max.first - min.first) / marcher!!.resolution.first,
            (max.second - min.second) / marcher!!.resolution.second
        )

        val dotSize = 2

        for (x in dotMap.indices) {
            for (y in dotMap[x].indices) {
                val realX = min.first + x * step.first
                val realY = min.second + y * step.second

                if (dotMap[x][y]) {
                    g.color = Color(Color.GREEN.rgb and 0xFFFFFF or (0x66 shl 24), true)
                } else {
                    g.color = Color(Color.GRAY.rgb and 0xFFFFFF or (0x66 shl 24), true)
                }

                g.fillArc(
                    realX.toInt() - dotSize,
                    realY.toInt() - dotSize,
                    dotSize * 2,
                    dotSize * 2,
                    0,
                    360
                )
            }
        }
    }
}