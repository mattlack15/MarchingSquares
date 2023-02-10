package ca.mattlack.marchingsquares.function

class IdentityFunction : DifferentiableFunction() {
    override fun invoke(x: Double): Double {
        return x
    }

    override fun derivative(): DifferentiableFunction {
        return ConstantFunction(1.0)
    }
}