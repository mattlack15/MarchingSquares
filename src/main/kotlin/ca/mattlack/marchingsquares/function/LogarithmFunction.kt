package ca.mattlack.marchingsquares.function

import kotlin.math.ln

class LogarithmFunction(val base: Double) : DifferentiableFunction() {
    override fun invoke(x: Double): Double {
        return kotlin.math.ln(x) / kotlin.math.ln(base)
    }

    override fun derivative(): DifferentiableFunction {
        return RationalFunction(ConstantFunction(1.0), IdentityFunction() * ConstantFunction(ln(base)))
    }
}