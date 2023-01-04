import com.soywiz.klogger.Console

class Player(
    val canMoveTo: (y: Int, x: Int) -> Boolean
) {

    var playerX: Int = 0
    var playerY: Int = 0
    var playerDirection: Direction = Direction.NORTH
    var lastMove = ::moveForward

    fun moveForward() {
        val vector = Direction.getVector(playerDirection)
        if (canMoveTo(playerY + vector.y, playerX + vector.x)) {
            playerX += vector.x
            playerY += vector.y
            lastMove = ::moveForward
        }
    }

    fun moveBackward() {
        val vector = Direction.getVector(playerDirection)
        if (canMoveTo(playerY - vector.y, playerX - vector.x)) {
            playerX -= vector.x
            playerY -= vector.y
            lastMove = ::moveBackward
        }
    }

    fun moveRight() {
        val vector = Direction.getVector(playerDirection)
        if (canMoveTo(playerY + vector.x, playerX + vector.y)) {
            playerX += vector.y
            playerY += vector.x
            lastMove = ::moveRight
        }
    }

    fun moveLeft() {
        val vector = Direction.getVector(playerDirection)
        if (canMoveTo(playerY - vector.x, playerX - vector.y)) {
            playerX -= vector.y
            playerY -= vector.x
            lastMove = ::moveLeft
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

    fun moveAgain() {
        lastMove()
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