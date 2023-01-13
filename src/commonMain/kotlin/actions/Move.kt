package actions

import Player

class Move(private val y: Int, private val x: Int, private val direction: String) : Action() {
    override fun execture() {
        Player.getPlayer().setLocation(y, x, direction)
    }
}