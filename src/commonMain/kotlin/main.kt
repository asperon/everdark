import com.soywiz.korev.Event
import com.soywiz.korev.addEventListener
import com.soywiz.korge.Korge
import com.soywiz.korim.format.readBitmap
import com.soywiz.korio.file.std.resourcesVfs
import com.soywiz.korio.lang.UTF8
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

const val WIDTH = 1024
const val HEIGHT = 768

suspend fun main() = Korge(
    width = WIDTH, height = HEIGHT, virtualWidth = WIDTH, virtualHeight = HEIGHT, clipBorders = false
) {
    val gameHolder = GameHolder(
        this,
        parseMap(),
        resourcesVfs["textures.png"].readBitmap().toBMP32IfRequired(),
        Json.decodeFromString(resourcesVfs["atlas.json"].readString(UTF8)),
        loadDialog(),
    )
    addEventListener<GameRestartEvent> {
        gameHolder.restart()
    }
}

suspend fun parseMap(): Array<Array<Location>> {
    val map = mutableListOf<Array<Location>>()
    val lines = resourcesVfs["map.csv"].readLines(UTF8)
    lines.forEach { line ->
        val newLine = mutableListOf<Location>()
        line.split(",").forEach {
            val item = it.split(":")
            newLine.add(Location(item[0].toInt(), item[1].toInt()))
        }
        map.add(newLine.toTypedArray())
    }
    return map.toTypedArray()
}

suspend fun loadDialog(): Array<String> {
    val dialog = mutableListOf<String>()
    val lines = resourcesVfs["dialog.txt"].readLines(UTF8)
    lines.forEach { line ->
        dialog.add(line)
    }
    return dialog.toTypedArray()
}

class GameRestartEvent : Event()