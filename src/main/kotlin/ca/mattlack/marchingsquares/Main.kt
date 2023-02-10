package ca.mattlack.marchingsquares

import ca.mattlack.marchingsquares.ball.MetaBall


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
        MetaBall(300.0, 300.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(300.0, 300.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(300.0, 300.0, radiusBase + variance * (Math.random() - 0.5)),
        MetaBall(300.0, 300.0, radiusBase + variance * (Math.random() - 0.5)),
    )

    val fps = 100.0

    while (true) {

        renderer.marcher?.function = { x, y -> balls.sumOf { it.function(x, y) } }

        val ms = 1000.0 / fps

        val time = System.currentTimeMillis()

        // Render
        renderer.render()

        // Update ball positions/velocities
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

        Thread.sleep((ms - timeTaken).toLong().coerceAtLeast(0))
    }

}