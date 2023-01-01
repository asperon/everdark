import com.soywiz.klogger.Console

class Player(
    var playerX: Int,
    var playerY: Int,
    private var playerDirection: Direction,
    val canMoveTo: (y: Int, x: Int) -> Boolean
) {

    fun moveForward() {
        val vector = Direction.getVector(playerDirection)
        if (canMoveTo(playerY + vector.y, playerX + vector.x)) {
            playerX += vector.x
            playerY += vector.y
        }
    }

    fun moveBackward() {
        val vector = Direction.getVector(playerDirection)
        if (canMoveTo(playerY - vector.y, playerX - vector.x)) {
            playerX -= vector.x
            playerY -= vector.y
        }

        if (canMoveTo(playerY + 1, playerX)) {
            playerY++
        }
    }

    fun turnLeft() {
        playerDirection = when (playerDirection) {
            Direction.NORTH -> Direction.WEST
            Direction.EAST -> Direction.NORTH
            Direction.SOUTH -> Direction.EAST
            Direction.WEST -> Direction.SOUTH
        }
    }

    fun turnRight() {
        playerDirection = when (playerDirection) {
            Direction.NORTH -> Direction.EAST
            Direction.EAST -> Direction.SOUTH
            Direction.SOUTH -> Direction.WEST
            Direction.WEST -> Direction.NORTH
        }
    }

    fun moveLeft() {
    }

    fun moveRight() {
    }

    fun getPx(x: Int, z: Int): Int {
        return when (playerDirection) {
            Direction.NORTH -> playerX + x
            Direction.EAST -> playerX - z
            Direction.SOUTH -> playerX - x
            Direction.WEST -> playerX + z
        }
    }

    fun getPy(x: Int, z: Int): Int {
        return when (playerDirection) {
            Direction.NORTH -> playerY + z
            Direction.EAST -> playerY + x
            Direction.SOUTH -> playerY - z
            Direction.WEST -> playerY - x
        }
    }

    fun logPosition() {
        Console.log("Player is at $playerY,$playerX facing $playerDirection")
    }
}