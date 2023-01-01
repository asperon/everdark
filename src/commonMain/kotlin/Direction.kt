enum class Direction(val value: Int) {
    NORTH(0), EAST(1), SOUTH(2), WEST(3);

    companion object {
        fun getVector(direction: Direction): Vector {
            return when (direction) {
                NORTH -> Vector(0, -1)
                EAST -> Vector(1, 0)
                SOUTH -> Vector(0, 1)
                WEST -> Vector(-1, 0)
            }
        }
    }
}

data class Vector(val x: Int, val y: Int)