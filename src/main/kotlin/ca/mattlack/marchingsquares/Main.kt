package ca.mattlack.marchingsquares

import ca.mattlack.marchingsquares.ball.MetaBall
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.*
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

    val radiusBase = 240.0
    val variance = 60.0


    val balls = mutableListOf<MetaBall>()

    repeat(6) {
        val radius = radiusBase + Random().nextDouble() * variance - variance / 2
        val x = Random().nextDouble() * renderer.frame.width / 2.0 + renderer.frame.width / 4.0
        val y = Random().nextDouble() * renderer.frame.height / 2.0 + renderer.frame.height / 4.0
        balls.add(MetaBall(x, y, radius))
    }

    val fps = 4000.0
    var phase = 0.0

    var tick = 0L

    var frames = 0
    var lastTime = System.currentTimeMillis()

    var pause = false

    renderer.frame.addKeyListener(object : KeyAdapter() {
        override fun keyPressed(e: KeyEvent?) {
            if (e?.keyCode == KeyEvent.VK_SPACE) {
                pause = true
            }
        }

        override fun keyReleased(e: KeyEvent?) {
            if (e?.keyCode == KeyEvent.VK_SPACE) {
                pause = false
            }
        }
    })

    while (true) {

        val bpm = 180.0

        if (pause) {
            Thread.sleep(1)
            continue
        }

        val time = System.currentTimeMillis()

//        phase += Math.PI * 2 / (fps / (60.0 / bpm)) * 150.0

        tick++

        val scalar = sin(tick / fps * Math.PI * 2.0 / (60.0 / bpm * 2.0))

        renderer.marcher?.function = { x, y -> balls.sumOf { it.function(x, y) } }

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
        }

        val timeTaken = System.currentTimeMillis() - time

        val sleepNs = (ns - timeTaken * 1000000.0).toLong()
        val sleepMs = sleepNs / 1000000L - 1
        val remainder = sleepNs % 1000000L

        Thread.sleep(sleepMs.coerceAtLeast(0), remainder.toInt().coerceAtLeast(0))
    }

}
