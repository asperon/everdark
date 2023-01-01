import kotlinx.serialization.Serializable

@Serializable
data class Atlas(
    val version: String,
    val generated: String,
    val resolution: Resolution,
    val depth: Int,
    val width: Int,
    val layers: List<Layer>
)

@Serializable
data class Resolution(val width: Int, val height: Int)

@Serializable
data class Layer(
    val on: Boolean,
    val index: Int,
    val name: String,
    val type: String,
    val id: Int,
    val scale: Pos,
    val offset: Pos,
    val tiles: List<Tile>
)

@Serializable
data class Pos(val x: Int, val y: Int)

@Serializable
data class T(val x: Int, val z: Int)

@Serializable
data class Tile(
    val type: String,
    val flipped: Boolean,
    val tile: T,
    val screen: Pos,
    val coords: Coords,
)

@Serializable
data class Coords(val x: Int, val y: Int, val w: Int, val h: Int, val fullWidth: Int)