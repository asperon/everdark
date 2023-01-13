package actions

class Message(private val message: String):Action() {
    override fun execture() {
        Game.addText(message)
    }
}