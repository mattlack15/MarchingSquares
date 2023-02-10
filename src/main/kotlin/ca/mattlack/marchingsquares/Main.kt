package ca.mattlack.marchingsquares

import ca.mattlack.marchingsquares.ball.MetaBall
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import kotlin.math.*


/**
 * Author: Matthew Lack
 * Date: February 9, 2023
 *
 * This is an implementation of the marching squares algorithm
 * to render the function for meta balls. They are coloured for
 * aesthetics. It's quite fun to watch.
 *
 * This is an algorithm used to draw contours of arbitrary functions.
 * I've chosen to draw the function for meta balls because it looks cool,
 * but it can be used to draw any function and be applied to many things,
 * one example might be a graphing calculator. I've actually already created
 * a small library for creating differentiable functions, which can be found
 * in the function package.
 *
 * I may in the future expand this project into a simple graphing calculator.
 */
fun main() {
    val renderer = Renderer()
//    renderer.showDots = true

    val radiusBase = 200.0
    val variance = 80.0


    val balls = listOf(
        MetaBall(300.0, 300.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(450.0, 450.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(150.0, 150.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(150.0, 450.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(450.0, 150.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(290.0, 340.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(130.0, 300.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(400.0, 189.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(430.0, 535.0, radiusBase + variance * (Math.random() - 0.5)),
    )

    val fps = 500.0
    var phase = 0.0

    var tick = 0L

    var frames = 0
    var lastTime = System.currentTimeMillis()

    var pause = false

    renderer.frame.addKeyListener(object : KeyAdapter() {
        override fun keyPressed(e: KeyEvent) {
            if (e.keyCode == java.awt.event.KeyEvent.VK_SPACE) {
                pause = true
            }
        }

        override fun keyReleased(e: KeyEvent?) {
            if (e?.keyCode == java.awt.event.KeyEvent.VK_SPACE) {
                pause = false
            }
        }
    })

    while (true) {

        val bpm = 184.0

        if (pause) {
            Thread.sleep(1)
            continue
        }

        val time = System.currentTimeMillis()

        phase += Math.PI * 2 / (100.0 / (60.0 / bpm)) * 150.0

        tick++

        val scalar = sin(tick / 100.0 * Math.PI * 2.0 / (60.0 / bpm * 2.0))

        renderer.marcher?.function = { x, y -> balls.sumOf { it.function(x, y) } }
//
//        renderer.marcher?.function = { x, y ->
//            sin((x - phase) / 100.0) * scalar - (y-400) / 100.0
//        }


        if (System.currentTimeMillis() - lastTime >= 1000) {
            println("FPS: $frames")
            frames = 0
            lastTime = System.currentTimeMillis()
        }

        frames++

        val ns = 1000000000.0 / fps

        // Render
        renderer.render()

        balls.forEach {
            it.move()

            // Bounce off walls
            if (it.x + it.radius / 2.0 > renderer.frame.width || it.x - it.radius / 2.0 < 0) {
                it.velX *= -1
                it.move()
            }
            if (it.y + it.radius / 2.0 > renderer.frame.height || it.y - it.radius / 2.0 < 0) {
                it.velY *= -1
                it.move()
            }

            // Attract all other balls
            balls.forEach others@{ other ->
                if (other == it) return@others

                val dx = other.x - it.x
                val dy = other.y - it.y
                val dist = sqrt(dx * dx + dy * dy)

                val dirX = dx / dist
                val dirY = dy / dist

                val force = 0.00005 * it.radius / ((dist / 100.0) * (dist / 100.0)).coerceAtLeast(1.0)

                it.velX += dirX * force
                it.velY += dirY * force
            }
        }

        val timeTaken = System.currentTimeMillis() - time

        val sleepNs = (ns - timeTaken * 1000000.0).toLong()
        val sleepMs = sleepNs / 1000000L
        val remainder = sleepNs % 1000000L

        Thread.sleep(sleepMs.coerceAtLeast(0), remainder.toInt().coerceAtLeast(0))
    }

}