import actions.Action

data class Location(val type: Int) {
    var state = 0
    var actions = mutableListOf<Action>()
    var interactions = mutableListOf<String>()
}