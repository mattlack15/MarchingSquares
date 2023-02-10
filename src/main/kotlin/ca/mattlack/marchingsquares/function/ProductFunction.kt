package ca.mattlack.marchingsquares.function

class ProductFunction(vararg val functions: DifferentiableFunction) : DifferentiableFunction() {
    override fun invoke(x: Double): Double {
        return functions.fold(1.0) { acc, function -> acc * function(x) }
    }

    override fun derivative(): DifferentiableFunction {
        return SumFunction(*functions.mapIndexed { index, function ->
            ProductFunction(*functions.mapIndexed { index2, function2 ->
                if (index == index2) {
                    function2.derivative()
                } else {
                    function2
                }
            }.toTypedArray())
        }.toTypedArray())
    }
}