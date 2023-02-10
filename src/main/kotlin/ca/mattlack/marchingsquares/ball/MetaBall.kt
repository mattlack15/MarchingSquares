package ca.mattlack.marchingsquares.ball

import java.util.*

class MetaBall(var x: Double, var y: Double, var radius: Double) {

    var velX = (Random().nextDouble() * 2 - 1) * 1.6
    var velY = (Random().nextDouble() * 2 - 1) * 1.6

    val function = { x: Double, y: Double ->
        val dx = x - this.x
        val dy = y - this.y
        val r = radius
        r * r / (dx * dx + dy * dy) - 1.0
    }

    fun move() {
        x += velX
        y += velY
    }

}