package app.ft.aa

class AaTouches {
    data class Pointer(val x: Int, val y: Int, val id: Int)
    data class Event(val action: Int, val index: Int, val pointers: List<Pointer>)

    companion object {
        const val DOWN = 0
        const val UP = 1
        const val MOVE = 2
        const val POINTER_DOWN = 5
        const val POINTER_UP = 6
    }

    private var fingers = 0
    private var firstId = 0
    private var first = 0 to 0
    private var second = 0 to 0

    fun reset() {
        fingers = 0
        firstId = 0
    }

    fun translate(action: Int, x: Int, y: Int, x2: Int, y2: Int, index: Int): List<Event> {
        val hasSecond = x2 >= 0 && y2 >= 0
        return when (action) {
            DOWN -> {
                fingers = 1
                firstId = 0
                first = x to y
                listOf(one(DOWN))
            }
            POINTER_DOWN -> {
                if (fingers != 1) return emptyList()
                val newcomer = if (hasSecond) x2 to y2 else x to y
                if (hasSecond && (x2 != x || y2 != y)) first = x to y
                fingers = 2
                second = newcomer
                listOf(two(POINTER_DOWN, 1))
            }
            MOVE -> when (fingers) {
                2 -> {
                    first = x to y
                    if (hasSecond) second = x2 to y2
                    listOf(two(MOVE, 0))
                }
                else -> {
                    if (fingers == 0) fingers = 1
                    first = x to y
                    listOf(one(MOVE))
                }
            }
            POINTER_UP -> {
                if (fingers != 2) return emptyList()
                val leaving = index.coerceIn(0, 1)
                if (hasSecond) second = x2 to y2
                val event = two(POINTER_UP, leaving)
                if (leaving == 0) {
                    first = second
                    firstId = 1 - firstId
                }
                fingers = 1
                listOf(event)
            }
            UP -> when (fingers) {
                2 -> {
                    first = x to y
                    if (hasSecond) second = x2 to y2
                    val lift = two(POINTER_UP, 1)
                    fingers = 0
                    listOf(lift, one(UP))
                }
                else -> {
                    first = x to y
                    val event = one(UP)
                    fingers = 0
                    listOf(event)
                }
            }
            else -> emptyList()
        }
    }

    private fun one(action: Int) = Event(action, 0, listOf(Pointer(first.first, first.second, firstId)))

    private fun two(action: Int, index: Int) = Event(
        action, index,
        listOf(Pointer(first.first, first.second, firstId), Pointer(second.first, second.second, 1 - firstId))
    )
}
