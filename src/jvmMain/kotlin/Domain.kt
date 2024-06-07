data class Cell(
    val walls: Sides<Boolean>,
    val visible: Boolean = false,
    val isVisited: Boolean = false,
    val bordered: Sides<Boolean>,
    val connections: Sides<Connection>
)

enum class Connection { None, Right, Wrong }

enum class Direction {
    U, D, L, R
}

data class Sides<T>(
    val u: T,
    val d: T,
    val l: T,
    val r: T,
) {
    companion object {
        val filledBoolean: Sides<Boolean>
            get() = Sides(u = true, d = true, l = true, r = true)
        val emptyConnections: Sides<Connection>
            get() = Sides(u = Connection.None, d = Connection.None, l = Connection.None, r = Connection.None)
    }
}

data class Coordinate(
    val x: Int,
    val y: Int,
)

infix fun Int.coord(y: Int) = Coordinate(this, y)
