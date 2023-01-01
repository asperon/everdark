import com.soywiz.klogger.Console
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
    val texture = resourcesVfs["textures.png"].readBitmap().toBMP32IfRequired()
    Console.log("0,0 is hex " + texture[10, 0].hexString)
    Console.log(texture.getRgbaAtIndex(0).hexString)

    val atlas = Json.decodeFromString<Atlas>(resourcesVfs["atlas.json"].readString(UTF8))
    val gameHolder = GameHolder(this, parseMap(), texture, atlas)
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

class GameRestartEvent : Event()