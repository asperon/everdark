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
        resourcesVfs["textures.png"].readBitmap().toBMP32IfRequired(),
        Json.decodeFromString(resourcesVfs["atlas.json"].readString(UTF8))
    )
    addEventListener<GameRestartEvent> {
        gameHolder.restart()
    }
}

class GameRestartEvent : Event()