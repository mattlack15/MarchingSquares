package ca.mattlack.marchingsquares.function

class ConstantFunction(val constant: Double) : DifferentiableFunction() {
    override fun invoke(x: Double): Double {
        return constant
    }

    override fun derivative(): DifferentiableFunction {
        return ConstantFunction(0.0)
    }
}