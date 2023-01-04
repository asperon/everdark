import com.soywiz.klogger.Console
import com.soywiz.korev.Key
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.font.DefaultTtfFont
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.lang.UTF8

class Game(
    val stage: Stage,
    private val texture: Bitmap32,
    private val atlas: Atlas,
    private val dialog: Array<String>,
) {

    private lateinit var map: Array<Array<Location>>

    private val player = Player() { y: Int, x: Int ->
        return@Player (map[y][x].type != 0 && !(map[y][x].type == 2 && map[y][x].ref == 1))
    }

    private val window = Bitmap32(WIDTH, HEIGHT, premultiplied = texture.premultiplied)
    private val display =
        Bitmap32(atlas.resolution.width, atlas.resolution.height, premultiplied = texture.premultiplied)

    private var textLog = com.soywiz.korio.async.ObservableProperty("")
    private var textBuffer = mutableListOf<String>()
    private var currentLevel = 1

    private var gameOver = true

    init {
        Console.log("Game loaded")
        addText("Loading level")
        stage.launchImmediately {
            map = loadLevel(currentLevel)
            setPlayerPosition()
            stage.image(display)
            stage.text(textLog.value.replace("|", "\n"), 24.0, RGBA(239, 226, 210), DefaultTtfFont) {
                position(0, 500)
                textLog.observe {
                    text = it.replace("|", "\n")
                }
            }
            gameOver = false
            updateScene()
        }
    }

    private val stageUpdater = stage.addUpdater {

        if (!gameOver) {

            if (input.keys.justPressed(Key.I)) {
                interact()
                updateScene()
            }
            if (input.keys.justPressed(Key.Q)) {
                player.turnLeft()
                updateScene()
            }
            if (input.keys.justPressed(Key.W)) {
                player.moveForward()
                updateScene()
            }
            if (input.keys.justPressed(Key.E)) {
                player.turnRight()
                updateScene()
            }
            if (input.keys.justPressed(Key.A)) {
                player.moveLeft()
                updateScene()
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
    }

    fun detach() {
        stageUpdater.cancel(GameRestart)
    }

    private fun setPlayerPosition() {
        // TODO find stairs in map
        player.playerX=1
        player.playerY=5
        player.playerDirection=Direction.NORTH
    }

    private fun drawFloor(z: Int) {
        for (x in -atlas.width until atlas.width) {
            drawImage(1, "floor", x, z)
        }
    }

    private fun drawCeiling(z: Int) {
        for (x in -atlas.width until atlas.width) {
            drawImage(2, "ceiling", x, z)
        }
    }

    private fun drawFront(z: Int) {
        for (x in -atlas.width until atlas.width) {

            val px = player.getPx(x, z)
            val py = player.getPy(x, z)

            if (px >= 0 && py >= 0 && py < map.size && px < map[0].size) {
                if (map[py][px].type == 0) {
                    drawImage(3, "front", x, z)
                }
                if (map[py][px].type == 2) {
                    when (map[py][px].ref) {
                        0 -> drawImage(5, "front", x, z) // open door
                        1 -> drawImage(4, "front", x, z) // closed door
                    }
                }
                if (map[py][px].type == 3) {
                    when (map[py][px].ref) {
                        1 -> drawImage(6, "front", x, z) // stairs down
                        2 -> drawImage(7, "front", x, z) // stairs up
                    }
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
                    drawImage(3, "side", x, z)
                }
                if (map[py][px].type == 2) {
                    when (map[py][px].ref) {
                        0 -> drawImage(5, "side", x, z) // open door
                        1 -> drawImage(4, "side", x, z) // closed door
                    }
                }
                if (map[py][px].type == 3) {
                    when (map[py][px].ref) {
                        1 -> drawImage(6, "side", x, z) // stairs down
                        2 -> drawImage(7, "side", x, z) // stairs up
                    }
                }

            }
        }
    }

    private fun drawImage(layerId: Int, tileType: String, x: Int, z: Int) {
        val layer = atlas.layers.find { it.id == layerId }
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

    private fun updateScene() {

        when (map[player.playerY][player.playerX].type) {
            1 -> {
                if (map[player.playerY][player.playerX].ref > 0) {
                    addText(dialog[map[player.playerY][player.playerX].ref])
                    map[player.playerY][player.playerX].ref = 0
                }
            }
            2 -> {
                player.moveAgain()
            }

            3 -> {
                when (map[player.playerY][player.playerX].ref) {
                    1 -> nextLevel()
                    2 -> previousLevel()
                }
            }
        }
        renderDisplay()
    }

    private fun interact() {
        val vector = Direction.getVector(player.playerDirection)
        val y = player.playerY + vector.y
        val x = player.playerX + vector.x
        when (map[y][x].type) {
            2 -> {
                when (map[y][x].ref) {
                    0 -> {
                        map[y][x].ref = 1
                        addText("The door slams shut with a bang, that should keep lesser creatures from passing through")
                    }

                    1 -> {
                        map[y][x].ref = 0
                        addText("The door glides open with a shrieking sound, that would be heard many rooms away")
                    }
                }
            }
        }
    }

    private fun addText(text: String) {
        textBuffer.addAll(text.split("|"))
        textLog.update(textBuffer.takeLast(6).toMutableList().joinToString(separator = "|"))
    }

    private fun nextLevel() {
        currentLevel++
        stage.launchImmediately {
            map = loadLevel(currentLevel)
            setPlayerPosition()
            updateScene()
        }
    }

    private fun previousLevel() {
        currentLevel--
        if (currentLevel==0) {
            addText("You leave the dungeon behind and head back to the village you |came from. Maybe you can return some other day.|Game Over")
            gameOver = true
        } else {
            stage.launchImmediately {
                map = loadLevel(currentLevel)
                setPlayerPosition()
                updateScene()
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

        display.copy(0, 0, window, 0, 0, display.width, display.height)

        display.contentVersion++
    }

    private suspend fun loadLevel(level: Int): Array<Array<Location>> {
        val map = mutableListOf<Array<Location>>()
        gameOver = true
        if (resourcesVfs["level_$level.csv"].exists()) {
            val lines = resourcesVfs["level_$level.csv"].readLines(UTF8)
            lines.forEach { line ->
                val newLine = mutableListOf<Location>()
                line.split(",").forEach {
                    val item = it.split(":")
                    newLine.add(Location(item[0].toInt(), item[1].toInt()))
                }
                map.add(newLine.toTypedArray())
            }
            gameOver = false
            return map.toTypedArray()
        } else {
            addText("This was the last level of the dungeon, laden with treasures |you descend to the surface for some much needed celebration and rest.|Game Over")
            return emptyArray()
        }
    }

}

// A dummy throwable to cancel updatables
object GameRestart : Throwable()