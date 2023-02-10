package ca.mattlack.marchingsquares.function

abstract class DifferentiableFunction {
    abstract operator fun invoke(x: Double): Double
    abstract fun derivative(): DifferentiableFunction

    operator fun times(other: DifferentiableFunction): DifferentiableFunction {
        return ProductFunction(this, other)
    }

    operator fun div(other: DifferentiableFunction): DifferentiableFunction {
        return RationalFunction(this, other)
    }

    operator fun plus(other: DifferentiableFunction): DifferentiableFunction {
        return SumFunction(this, other)
    }

    operator fun minus(other: DifferentiableFunction): DifferentiableFunction {
        return SumFunction(this, -other)
    }

    operator fun unaryMinus(): DifferentiableFunction {
        return ConstantFunction(-1.0) * this
    }

    operator fun invoke(f: DifferentiableFunction): DifferentiableFunction {
        return ChainedFunction(this, f)
    }

}