package ca.mattlack.marchingsquares.march

import ca.mattlack.marchingsquares.function.DifferentiableFunction
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.locks.ReentrantLock

class SquareMarcher(
    var function: (Double, Double) -> Double,
    val min: Pair<Double, Double>,
    val max: Pair<Double, Double>,
    val resolution: Pair<Int, Int>,
    private val parallelExecutor: ForkJoinPool
) {

    private val predicate = { x: Double, y: Double ->
        val result = function(x, y)
        if (result.isNaN() || result.isInfinite()) {
            0
        } else if (result >= 0) {
            1
        } else {
            -1
        }
    }

    private val convertPredicate = { x: Int -> x > 0 }

    /**
     * Compute the contour lines using the marching squares algorithm.
     */
    fun march(): List<ContourLine> {

        val dotMap = computeDotMap0()

        val contourLines = mutableListOf<ContourLine>()
        // We also need a lock for the above list because we will be modifying it in parallel
        val lock = ReentrantLock()

        // Parallel tasks
        val tasks = mutableListOf<() -> Unit>()

        forEachSquare { x, y ->
            tasks.add {

                val realX = min.first + x * (max.first - min.first) / resolution.first
                val realY = min.second + y * (max.second - min.second) / resolution.second
                val stepX = (max.first - min.first) / resolution.first
                val stepY = (max.second - min.second) / resolution.second

                val minXY = dotMap[x][y]
                val maxXY = dotMap[x + 1][y + 1]
                val minXmaxY = dotMap[x][y + 1]
                val maxXminY = dotMap[x + 1][y]
                val mid = predicate(realX + stepX / 2, realY + stepY / 2)

                // 0 indicates that the function returned NaN or an infinite value
                // Do not count squares that contain one of those values
                val anyZero = minXY == 0 || maxXY == 0 || minXmaxY == 0 || maxXminY == 0 || mid == 0
                if (anyZero) return@add

                val square = Square(
                    Pair(realX, realY),
                    Pair(realX + stepX, realY + stepY),
                    convertPredicate(minXY),
                    convertPredicate(maxXY),
                    convertPredicate(minXmaxY),
                    convertPredicate(maxXminY),
                    convertPredicate(mid)
                )

                val case = square.getCase()
                if (case == 0 || case == 15) return@add

                val lines = computeContourLines(square)
                lock.lock()
                contourLines.addAll(lines)
                lock.unlock()
            }
        }

        parallel(tasks)

        return contourLines
    }

    /**
     * Compute the dot map as a 2D array of booleans.
     */
    fun computeDotMap(): Array<BooleanArray> {
        return computeDotMap0()
            .map { it.map(convertPredicate).toBooleanArray() }
            .toTypedArray()
    }

    /**
     * Compute the dot map in parallel.
     */
    private fun computeDotMap0(): Array<IntArray> {

        val grid = Array(resolution.first + 1) { IntArray(resolution.second + 1) }

        val stepX = (max.first - min.first) / resolution.first
        val stepY = (max.second - min.second) / resolution.second

        val tasks = mutableListOf<() -> Unit>()
        for (x in 0..resolution.first) {
            for (y in 0..resolution.second) {
                val realX = min.first + x * stepX
                val realY = min.second + y * stepY
                tasks.add {
                    grid[x][y] = predicate(realX, realY)
                }
            }
        }

        parallel(tasks)

        return grid
    }

    /**
     * Run tasks in parallel using the provided executor.
     */
    private fun parallel(tasks: List<() -> Unit>) {
        val futures = mutableListOf<CompletableFuture<*>>()
        for (task in tasks) {
            val future = CompletableFuture<Void>()
            parallelExecutor.submit {
                task()
                future.complete(null)
            }
            futures.add(future)
        }
        CompletableFuture.allOf(*futures.toTypedArray()).join()
    }

    /**
     * Create the approximated contour lines for a square
     * using binary search for more accurate contours.
     */
    private fun computeContourLines(square: Square): List<ContourLine> {

        val case = square.getCase()
        if (case == 0 || case == 15) return emptyList()

        val lines = mutableListOf<ContourLine>()

        val sides = square.sides

        // TODO make this not ugly
        // From left to right in 0b???? format:
        // Bottom Left,
        // Bottom Right,
        // Top Right,
        // Top Left
        when (case) {
            0b1010, 0b0101 -> {
                val cond = if (case == 0b1010) square.mid else !square.mid
                if (cond) {
                    lines.add(getLine(sides[0], sides[1]))
                    lines.add(getLine(sides[2], sides[3]))
                } else {
                    lines.add(getLine(sides[0], sides[3]))
                    lines.add(getLine(sides[1], sides[2]))
                }
            }
            0b1100, 0b0011 -> {
                lines.add(getLine(sides[1], sides[3]))
            }
            0b1001, 0b0110 -> {
                lines.add(getLine(sides[0], sides[2]))
            }
            0b1110, 0b0001 -> {
                lines.add(getLine(sides[2], sides[3]))
            }
            0b1101, 0b0010 -> {
                lines.add(getLine(sides[1], sides[2]))
            }
            0b1011, 0b0100 -> {
                lines.add(getLine(sides[0], sides[1]))
            }
            0b0111, 0b1000 -> {
                lines.add(getLine(sides[0], sides[3]))
            }
        }

        return lines
    }

    /**
     * Do an operation for each square in the grid.
     */
    private fun forEachSquare(operation: (Int, Int) -> Unit) {
        for (x in 0 until resolution.first) {
            for (y in 0 until resolution.second) {
                operation(x, y)
            }
        }
    }

    /**
     * Get the contour line between the two given sides.
     * Uses binary search to find more accurate contour lines.
     */
    private fun getLine(
        side1: Side,
        side2: Side
    ): ContourLine {
        val side1Pt = binarySearch(side1)
        val side2Pt = binarySearch(side2)
        return ContourLine(side1Pt, side2Pt)
    }

    /**
     * Do a binary search to find the end-points of a contour line between the two given sides.
     */
    private fun binarySearch(side: Side): Pair<Double, Double> {

        val numSteps = 16

        val dir = side.startValue

        var left = if (dir) side.start else side.end
        var right = if (dir) side.end else side.start

        for (step in 0 until numSteps) {
            val mid = (left + right) / 2.0
            val midValue = convertPredicate(predicate(mid.first, mid.second))

            if (midValue) {
                left = mid
            } else {
                right = mid
            }

        }

        return (left + right) / 2.0
    }

    private class Square(
        val realMin: Pair<Double, Double>,
        val realMax: Pair<Double, Double>,
        val minXY: Boolean,
        val maxXY: Boolean,
        val minXmaxY: Boolean,
        val maxXminY: Boolean,
        val mid: Boolean
    ) {
        val sides: Array<Side> = Array(4) { computeSide(it) }

        fun getCase(): Int {
            return (if (minXmaxY) 1 else 0) +
                    (if (maxXY) 2 else 0) +
                    (if (maxXminY) 4 else 0) +
                    (if (minXY) 8 else 0)
        }

        private fun computeSide(num: Int): Side {
            val start = when (num) {
                0 -> Triple(realMin.first, realMin.second, minXY)
                1 -> Triple(realMax.first, realMin.second, maxXminY)
                2 -> Triple(realMax.first, realMax.second, maxXY)
                3 -> Triple(realMin.first, realMax.second, minXmaxY)
                else -> throw IllegalArgumentException("Invalid side number")
            }
            val end = when (num) {
                0 -> Triple(realMax.first, realMin.second, maxXminY)
                1 -> Triple(realMax.first, realMax.second, maxXY)
                2 -> Triple(realMin.first, realMax.second, minXmaxY)
                3 -> Triple(realMin.first, realMin.second, minXY)
                else -> throw IllegalArgumentException("Invalid side number")
            }
            return Side(
                Pair(start.first, start.second),
                Pair(end.first, end.second),
                start.third,
                end.third
            )
        }

    }

    private class Side(
        val start: Pair<Double, Double>,
        val end: Pair<Double, Double>,
        val startValue: Boolean,
        val endValue: Boolean
    )

    private operator fun Pair<Double, Double>.plus(other: Pair<Double, Double>): Pair<Double, Double> {
        return Pair(first + other.first, second + other.second)
    }

    private operator fun Pair<Double, Double>.div(other: Double): Pair<Double, Double> {
        return Pair(first / other, second / other)
    }

}