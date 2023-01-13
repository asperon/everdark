import actions.Message
import actions.Move
import actions.MoveAgain
import com.soywiz.klogger.Console
import com.soywiz.korev.Key
import com.soywiz.korge.view.*
import com.soywiz.korim.bitmap.Bitmap32
import com.soywiz.korim.color.Colors
import com.soywiz.korim.color.RGBA
import com.soywiz.korim.font.DefaultTtfFont
import com.soywiz.korim.tiles.tiled.readTiledMapData
import com.soywiz.korio.async.launchImmediately
import com.soywiz.korio.file.std.resourcesVfs

class Game(
    val stage: Stage,
    private val texture: Bitmap32,
    private val atlas: Atlas,
) {

    private val player = Player()

    private val window = Bitmap32(WIDTH, HEIGHT, premultiplied = texture.premultiplied)
    private val display =
        Bitmap32(atlas.resolution.width, atlas.resolution.height, premultiplied = texture.premultiplied)

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
        player.playerX = 1
        player.playerY = 5
        player.playerDirection = Direction.NORTH
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
                when (map[py][px].type) {
                    1 -> {
                        // Door
                        when (map[py][px].state) {
                            0 -> drawImage(4, "front", x, z) // closed door
                            1 -> drawImage(5, "front", x, z) // open door

                        }
                    }
                    4 -> drawImage(3, "front", x, z) // wall
                    5 -> drawImage(6, "front", x, z) // stairs down
                    6 -> drawImage(7, "front", x, z) // stairs up
                }
            }
        }
    }

    private fun drawSides(z: Int) {
        for (x in -atlas.width until atlas.width) {
            val px = player.getPx(x, z)
            val py = player.getPy(x, z)

            if (px >= 0 && py >= 0 && py < map.size && px < map[0].size) {
                when (map[py][px].type) {
                    1 -> {
                        when (map[py][px].state) {
                            0 -> drawImage(4, "side", x, z) // closed door
                            1 -> drawImage(5, "side", x, z) // open door
                        }
                    }
                    4 -> drawImage(3, "side", x, z) // wall
                    5 -> drawImage(6, "side", x, z) // stairs down
                    6 -> drawImage(7, "side", x, z) // stairs up
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
        map[player.playerY][player.playerX].actions.forEach {
            it.execture()
        }
        renderDisplay()
    }

    private fun interact() {
        val vector = Direction.getVector(player.playerDirection)
        val y = player.playerY + vector.y
        val x = player.playerX + vector.x
        when (map[y][x].type) {
            2 -> {
                when (map[y][x].state) {
                    0 -> {
                        map[y][x].state = 1
                        addText("The door slams shut with a bang, that should keep lesser creatures from passing through")
                    }

                    1 -> {
                        map[y][x].state = 0
                        addText("The door glides open with a shrieking sound, that would be heard many rooms away")
                    }
                }
            }
        }
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
        if (currentLevel == 0) {
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
        if (resourcesVfs["level_$level.tmx"].exists()) {
            try {
                val tiles = resourcesVfs["level_$level.tmx"].readTiledMapData()
                for (y in 0 until tiles.height) {
                    val newLine = mutableListOf<Location>()
                    for (x in 0 until tiles.width) {
                        newLine.add(Location(tiles.tileLayers.first()[x, y]))
                    }
                    map.add(newLine.toTypedArray())
                }
                tiles.objectLayers.find { it.name == "actions" }?.objects?.forEach { obj ->
                    obj.properties.forEach {
                        when (it.key) {
                            "text" -> map[((obj.y / tiles.tileheight).toInt())][((obj.x / tiles.tilewidth).toInt())].actions.add(
                                Message(it.value.string)
                            )
                            "move" -> {
                                val data = it.value.string.split(",")
                                map[((obj.y / tiles.tileheight).toInt())][((obj.x / tiles.tilewidth).toInt())].actions.add(
                                    Move(data[0].toInt(), data[1].toInt(), data[2])
                                )
                            }
                            "moveAgain" -> map[((obj.y / tiles.tileheight).toInt())][((obj.x / tiles.tilewidth).toInt())].actions.add(
                                MoveAgain()
                            )
                        }
                    }
                }
                tiles.objectLayers.find { it.name == "interactions" }?.objects?.forEach {

                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            gameOver = false
            return map.toTypedArray()
        } else {
            addText("This was the last level of the dungeon, laden with treasures |you descend to the surface for some much needed celebration and rest.|Game Over")
            return emptyArray()
        }
    }

    companion object {

        private var textBuffer = mutableListOf<String>()
        var textLog = com.soywiz.korio.async.ObservableProperty("")
        lateinit var map: Array<Array<Location>>

        fun addText(text: String) {
            textBuffer.addAll(text.split("|"))
            textLog.update(textBuffer.takeLast(6).toMutableList().joinToString(separator = "|"))
        }
    }
}

// A dummy throwable to cancel updatables
object GameRestart : Throwable()