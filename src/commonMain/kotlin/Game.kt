import com.soywiz.klogger.Console
import com.soywiz.korev.Key
import com.soywiz.korge.view.Stage
import com.soywiz.korge.view.addUpdater
import com.soywiz.korge.view.image
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.color.Colors

class Game(
    stage: Stage,
    private val map: Array<Array<Location>>,
    private val texture: Bitmap32,
    private val atlas: Atlas
) {

    private val player = Player(2, 4, Direction.NORTH) { y: Int, x: Int ->
        return@Player (map[y][x].type != 0)
    }

    private val window = Bitmap32(WIDTH, HEIGHT, premultiplied = texture.premultiplied)
    private val display =
        Bitmap32(atlas.resolution.width, atlas.resolution.height, premultiplied = texture.premultiplied)

    init {
        Console.log("Game loaded")
        stage.image(window)
        renderDisplay()
    }

    private val stageUpdater = stage.addUpdater {

        if (input.keys.justPressed(Key.Q)) {
            player.turnLeft()
            renderDisplay()
        }
        if (input.keys.justPressed(Key.W)) {
            player.moveForward()
            renderDisplay()
        }
        if (input.keys.justPressed(Key.E)) {
            player.turnRight()
            renderDisplay()
        }
        if (input.keys.justPressed(Key.A)) {
            player.moveLeft()
            renderDisplay()
        }
        if (input.keys.justPressed(Key.S)) {
            player.moveBackward()
            renderDisplay()
        }
        if (input.keys.justPressed(Key.D)) {
            player.moveRight()
            renderDisplay()
        }
    }

    fun detach() {
        stageUpdater.cancel(GameRestart)
    }

    private fun drawFloor(z: Int) {
        for (x in -atlas.width until atlas.width) {
            drawImage("Floor", "floor", x, z)
        }
    }

    private fun drawCeiling(z: Int) {
        for (x in -atlas.width until atlas.width) {
            drawImage("Ceiling", "ceiling", x, z)
        }
    }

    private fun drawFront(z: Int) {
        for (x in -atlas.width until atlas.width) {

            val px = player.getPx(x, z)
            val py = player.getPy(x, z)

            if (px >= 0 && py >= 0 && py < map.size && px < map[0].size) {
                if (map[py][px].type == 0) {
                    drawImage("Walls", "front", x, z)
                }
            }
        }
    }

    private fun drawSides(z: Int) {
        for (x in -atlas.width until atlas.width) {
            val px = player.getPx(x, z)
            val py = player.getPy(x, z)

            if (px >= 0 && py >= 0 && py < map.size && px < map[0].size) {
                if (map[py][px].type == 0) {
                    drawImage("Walls", "side", x, z)
                }
            }
        }
    }

    private fun drawImage(layerType: String, tileType: String, x: Int, z: Int) {
        val layer = atlas.layers.find { it.type == layerType }
        layer?.let {
            val tile = if (tileType == "front") {
                layer.tiles.find { it.type == tileType && it.tile.x == 0 && it.tile.z == z }
            } else {
                layer.tiles.find { it.type == tileType && it.tile.x == x && it.tile.z == z }
            }
            tile?.let {
                val tmpBitmap = Bitmap32(tile.coords.w, tile.coords.h, premultiplied = texture.premultiplied)
                texture.copy(
                    tile.coords.x, tile.coords.y, tmpBitmap, 0, 0, tile.coords.w, tile.coords.h
                )
                if (it.flipped) {
                    display.draw(tmpBitmap.flipX().toBMP32IfRequired(), tile.screen.x - tile.coords.w, tile.screen.y)
                } else {
                    if (tileType == "front") {
                        display.draw(tmpBitmap, tile.screen.x + (tile.coords.fullWidth * x), tile.screen.y)
                    } else {
                        display.draw(tmpBitmap, tile.screen.x, tile.screen.y)
                    }
                }
            }
        }
    }

    private fun renderDisplay() {
        player.logPosition()

        display.fill(Colors.TRANSPARENT_BLACK)

        for (z in -atlas.depth..0) {
            drawFloor(z)
            drawCeiling(z)
            drawSides(z)
            drawFront(z)
        }

        window.fill(Colors.TRANSPARENT_BLACK)
        display.copy(0, 0, window, 0, 0, display.width, display.height)

        when(map[player.playerY][player.playerX].type) {
            0-> {

            }
        }

        window.contentVersion++
    }
}

// A dummy throwable to cancel updatables
object GameRestart : Throwable()