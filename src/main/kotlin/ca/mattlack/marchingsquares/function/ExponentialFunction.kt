package ca.mattlack.marchingsquares.function

import kotlin.math.ln
import kotlin.math.pow

class ExponentialFunction(val base: Double): DifferentiableFunction() {
    override fun invoke(x: Double): Double {
        return base.pow(x)
    }

    override fun derivative(): DifferentiableFunction {
        return ExponentialFunction(base) * ConstantFunction(ln(base))
    }
}