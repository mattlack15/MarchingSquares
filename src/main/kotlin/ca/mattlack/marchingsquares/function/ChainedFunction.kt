package ca.mattlack.marchingsquares.function

class ChainedFunction(
    val parent: DifferentiableFunction,
    val child: DifferentiableFunction
) : DifferentiableFunction() {
    override fun invoke(x: Double): Double {
        return parent(child(x))
    }

    override fun derivative(): DifferentiableFunction {
        return ChainedFunction(parent.derivative(), child) * child.derivative()
    }
}