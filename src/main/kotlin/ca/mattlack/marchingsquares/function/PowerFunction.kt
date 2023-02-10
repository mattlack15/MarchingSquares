package ca.mattlack.marchingsquares.function

import kotlin.math.pow

class PowerFunction(val power: Double) : DifferentiableFunction() {
    override fun invoke(x: Double): Double {
        return x.pow(power)
    }

    override fun derivative(): DifferentiableFunction {
        return PowerFunction(power - 1) * ConstantFunction(power)
    }
}