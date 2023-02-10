package ca.mattlack.marchingsquares.function

class SumFunction(vararg val functions: DifferentiableFunction) : DifferentiableFunction() {
    override fun invoke(x: Double): Double {
        return functions.sumOf { it(x) }
    }

    override fun derivative(): DifferentiableFunction {
        return SumFunction(*functions.map { it.derivative() }.toTypedArray())
    }
}