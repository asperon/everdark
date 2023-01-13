package actions

import Player

class MoveAgain() : Action() {
    override fun execture() {
        Player.getPlayer().moveAgain()
    }
}