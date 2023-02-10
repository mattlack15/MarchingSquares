package ca.mattlack.marchingsquares.function

class RationalFunction(
    val numerator: DifferentiableFunction,
    val denominator: DifferentiableFunction
) : DifferentiableFunction() {
    override fun invoke(x: Double): Double {
        return numerator(x) / denominator(x)
    }

    override fun derivative(): DifferentiableFunction {
        return (numerator.derivative() * denominator - numerator * denominator.derivative()) / (denominator * denominator)
    }
}