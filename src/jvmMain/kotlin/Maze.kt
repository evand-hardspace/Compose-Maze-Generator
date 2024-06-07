import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update

typealias Matrix = MutableList<Row>
typealias Row = MutableList<Cell>

private fun emptyMatrix(): Matrix = mutableListOf()

class Maze(
    private val x: Int,
    private val y: Int,
) {
    val fieldState: MutableStateFlow<Matrix> = MutableStateFlow(
        emptyMatrix().let { field ->
            for (yy in 0 until y) {
                val row = mutableListOf<Cell>()
                for (xx in 0 until x) {
                    row += Cell(
                        walls = Sides.filledBoolean,
                        bordered = Sides(
                            u = (yy == 0), d = (yy == y - 1), r = (xx == x - 1), l = (xx == 0),
                        ),
                        connections = Sides.emptyConnections,
                    )
                }
                field += row
            }
            field
        }
    )

    suspend fun fill() {
        fieldState.value.forEachIndexed { i, row ->
            delay(DELAY_TIME)
            fieldState.update { matrix ->
                matrix.updateRowAt(i) { oldCell ->
                    oldCell.copy(visible = true)
                }
            }
        }
    }

    suspend fun generateExits() {
        fieldState.update { matrix ->
            matrix.updateAt((0 until y).random() coord 0) { oldCell ->
                oldCell.copy(walls = oldCell.walls.copy(u = false))
            }
        }
        delay(DELAY_TIME)
        fieldState.update { matrix ->
            matrix.updateAt((0 until y).random() coord (y - 1)) { oldCell ->
                oldCell.copy(walls = oldCell.walls.copy(d = false))
            }
        }
    }

    suspend fun generate(currentCellCoordinate: Coordinate = 0 coord 0) {
        delay(DELAY_TIME)
        fieldState.update { matrix ->
            matrix.updateAt(currentCellCoordinate) { oldCell ->
                oldCell.copy(isVisited = true)
            }
        }
        val nextMoves = mutableListOf<Direction>()
        if (currentCellCoordinate.x > 0
            && fieldState.value.at(currentCellCoordinate.copy(x = currentCellCoordinate.x - 1)).isVisited.not()
        ) {
            nextMoves += Direction.L
        }
        if (currentCellCoordinate.x < x - 1
            && fieldState.value.at(currentCellCoordinate.copy(x = currentCellCoordinate.x + 1)).isVisited.not()
        ) {
            nextMoves += Direction.R
        }
        if (currentCellCoordinate.y > 0
            && fieldState.value.at(currentCellCoordinate.copy(y = currentCellCoordinate.y - 1)).isVisited.not()
        ) {
            nextMoves += Direction.U
        }
        if (currentCellCoordinate.y < y - 1
            && fieldState.value.at(currentCellCoordinate.copy(y = currentCellCoordinate.y + 1)).isVisited.not()
        ) {
            nextMoves += Direction.D
        }

        if (nextMoves.isEmpty()) {
            return
        }

        while (nextMoves.isNotEmpty()) {
            val direction = nextMoves.random()
            nextMoves -= direction
            if (stillPossible(currentCellCoordinate, direction).not()) break

            when (direction) {
                Direction.U -> {
                    fieldState.update { matrix ->
                        matrix.updateAt(currentCellCoordinate) { oldCell ->
                            oldCell.copy(walls = oldCell.walls.copy(u = false))
                        }.updateAt(currentCellCoordinate.copy(y = currentCellCoordinate.y - 1)) { oldCell ->
                            oldCell.copy(walls = oldCell.walls.copy(d = false))
                        }
                    }
                    generate(currentCellCoordinate.copy(y = currentCellCoordinate.y - 1))
                }

                Direction.D -> {
                    fieldState.update { matrix ->
                        matrix.updateAt(currentCellCoordinate) { oldCell ->
                            oldCell.copy(walls = oldCell.walls.copy(d = false))
                        }.updateAt(currentCellCoordinate.copy(y = currentCellCoordinate.y + 1)) { oldCell ->
                            oldCell.copy(walls = oldCell.walls.copy(u = false))
                        }
                    }
                    generate(currentCellCoordinate.copy(y = currentCellCoordinate.y + 1))
                }

                Direction.L -> {
                    fieldState.update { matrix ->
                        matrix.updateAt(currentCellCoordinate) { oldCell ->
                            oldCell.copy(walls = oldCell.walls.copy(l = false))
                        }.updateAt(currentCellCoordinate.copy(x = currentCellCoordinate.x - 1)) { oldCell ->
                            oldCell.copy(walls = oldCell.walls.copy(r = false))
                        }
                    }
                    generate(currentCellCoordinate.copy(x = currentCellCoordinate.x - 1))
                }

                Direction.R -> {
                    fieldState.update { matrix ->
                        matrix.updateAt(currentCellCoordinate) { oldCell ->
                            oldCell.copy(walls = oldCell.walls.copy(r = false))
                        }.updateAt(currentCellCoordinate.copy(x = currentCellCoordinate.x + 1)) { oldCell ->
                            oldCell.copy(walls = oldCell.walls.copy(l = false))
                        }
                    }
                    generate(currentCellCoordinate.copy(x = currentCellCoordinate.x + 1))
                }
            }
        }
    }

    private fun stillPossible(currentCellCoordinate: Coordinate, direction: Direction): Boolean {
        return when (direction) {
            Direction.U -> currentCellCoordinate.y > 0
                    && fieldState.value.at(currentCellCoordinate.copy(y = currentCellCoordinate.y - 1)).isVisited.not()

            Direction.D -> currentCellCoordinate.y < y - 1
                    && fieldState.value.at(currentCellCoordinate.copy(y = currentCellCoordinate.y + 1)).isVisited.not()

            Direction.L -> currentCellCoordinate.x > 0
                    && fieldState.value.at(currentCellCoordinate.copy(x = currentCellCoordinate.x - 1)).isVisited.not()

            Direction.R -> currentCellCoordinate.x < x - 1
                    && fieldState.value.at(currentCellCoordinate.copy(x = currentCellCoordinate.x + 1)).isVisited.not()
        }
    }

    private fun stillPossibleSolve(currentCellCoordinate: Coordinate, direction: Direction): Boolean {
        return when (direction) {
            Direction.U -> currentCellCoordinate.y > 0
                    && fieldState.value.at(currentCellCoordinate.copy(y = currentCellCoordinate.y - 1))
                .run { isVisited.not() && walls.d.not() }

            Direction.D -> currentCellCoordinate.y < y - 1
                    && fieldState.value.at(currentCellCoordinate.copy(y = currentCellCoordinate.y + 1))
                .run { isVisited.not() && walls.u.not() }

            Direction.L -> currentCellCoordinate.x > 0
                    && fieldState.value.at(currentCellCoordinate.copy(x = currentCellCoordinate.x - 1))
                .run { isVisited.not() && walls.r.not() }

            Direction.R -> currentCellCoordinate.x < x - 1
                    && fieldState.value.at(currentCellCoordinate.copy(x = currentCellCoordinate.x + 1))
                .run { isVisited.not() && walls.l.not() }
        }
    }

    suspend fun solve() {
        // reset visited
        fieldState.update { matrix ->
            matrix.mapIndexed { y, row ->
                row.mapIndexed { x, cell ->
                    cell.copy(isVisited = false)
                }.toMutableList()
            }.toMutableList()
        }

        val startPoint = fieldState.value.coordinateOfFirst { cell -> cell.walls.u.not() }
        fieldState.update { matrix ->
            matrix.updateAt(startPoint) { oldCell ->
                oldCell.copy(connections = oldCell.connections.copy(u = Connection.Right))
            }
        }
        solveR(currentCellCoordinate = startPoint, mutableListOf())
    }

    private suspend fun solveR(
        currentCellCoordinate: Coordinate,
        visited: MutableList<Coordinate>
    ): Pair<Boolean, List<Coordinate>> {
        visited += currentCellCoordinate
        delay(DELAY_TIME)
        // update current visited
        fieldState.update { matrix ->
            matrix.updateAt(currentCellCoordinate) { oldCell ->
                oldCell.copy(isVisited = true)
            }
        }
        // finish base case
        if (currentCellCoordinate.y == y - 1 && fieldState.value[currentCellCoordinate.y][currentCellCoordinate.x].walls.d.not()) {
            val endPoint = fieldState.value.coordinateOfLast { cell -> cell.walls.d.not() }

            fieldState.update { matrix ->
                matrix.updateAt(endPoint) { oldCell ->
                    oldCell.copy(connections = oldCell.connections.copy(d = Connection.Right))
                }
            }
            return true to emptyList()
        }

        // next moves
        val nextMoves = mutableListOf<Direction>()
        if (currentCellCoordinate.x > 0
            && fieldState.value.at(currentCellCoordinate.copy(x = currentCellCoordinate.x - 1))
                .run { isVisited.not() && walls.r.not() }
        ) {
            nextMoves += Direction.L
        }
        if (currentCellCoordinate.x < x - 1
            && fieldState.value.at(currentCellCoordinate.copy(x = currentCellCoordinate.x + 1))
                .run { isVisited.not() && walls.l.not() }
        ) {
            nextMoves += Direction.R
        }
        if (currentCellCoordinate.y > 0
            && fieldState.value.at(currentCellCoordinate.copy(y = currentCellCoordinate.y - 1))
                .run { isVisited.not() && walls.d.not() }
        ) {
            nextMoves += Direction.U
        }
        if (currentCellCoordinate.y < y - 1
            && fieldState.value.at(currentCellCoordinate.copy(y = currentCellCoordinate.y + 1))
                .run { isVisited.not() && walls.u.not() }
        ) {
            nextMoves += Direction.D
        }
        if (nextMoves.isEmpty()) {
            return false to visited
        }

        while (nextMoves.isNotEmpty()) {
            val direction = nextMoves.random()
            nextMoves -= direction
            if (stillPossible(currentCellCoordinate, direction).not()) break

            when (direction) {
                Direction.U -> {
                    fieldState.update { matrix ->
                        matrix.updateAt(currentCellCoordinate) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.copy(u = Connection.Right))
                        }.updateAt(currentCellCoordinate.copy(y = currentCellCoordinate.y - 1)) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.copy(d = Connection.Right))
                        }
                    }
                    val (finished, wrongVisited) = solveR(
                        currentCellCoordinate = currentCellCoordinate.copy(y = currentCellCoordinate.y - 1),
                        visited = if (nextMoves.size == 1) visited else mutableListOf()
                    )
                    if (finished) return true to emptyList()
                    fieldState.update { matrix ->
                        matrix.updateManyAt(wrongVisited) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.toWrong())
                        } // this
                            .updateAt(currentCellCoordinate) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.toRight(uu = false))
                        }
                    }
                }

                Direction.D -> {
                    fieldState.update { matrix ->
                        matrix.updateAt(currentCellCoordinate) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.copy(d = Connection.Right))
                        }.updateAt(currentCellCoordinate.copy(y = currentCellCoordinate.y + 1)) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.copy(u = Connection.Right))
                        }
                    }
                    val (finished, wrongVisited) = solveR(
                        currentCellCoordinate = currentCellCoordinate.copy(y = currentCellCoordinate.y + 1),
                        visited = if (nextMoves.size == 1) visited else mutableListOf()
                    )
                    if (finished) return true to emptyList()
                    fieldState.update { matrix ->
                        matrix.updateManyAt(wrongVisited) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.toWrong())
                        } // this
                            .updateAt(currentCellCoordinate) { oldCell ->
                                oldCell.copy(connections = oldCell.connections.toRight(dd = false))
                            }
                    }
                }

                Direction.L -> {
                    fieldState.update { matrix ->
                        matrix.updateAt(currentCellCoordinate) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.copy(l = Connection.Right))
                        }.updateAt(currentCellCoordinate.copy(x = currentCellCoordinate.x - 1)) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.copy(r = Connection.Right))
                        }
                    }
                    val (finished, wrongVisited) = solveR(
                        currentCellCoordinate = currentCellCoordinate.copy(x = currentCellCoordinate.x - 1),
                        visited = if (nextMoves.size == 1) visited else mutableListOf()
                    )
                    if (finished) return true to emptyList()
                    fieldState.update { matrix ->
                        matrix.updateManyAt(wrongVisited) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.toWrong())
                        } // this
                            .updateAt(currentCellCoordinate) { oldCell ->
                                oldCell.copy(connections = oldCell.connections.toRight(ll = false))
                            }
                    }
                }

                Direction.R -> {
                    fieldState.update { matrix ->
                        matrix.updateAt(currentCellCoordinate) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.copy(r = Connection.Right))
                        }.updateAt(currentCellCoordinate.copy(x = currentCellCoordinate.x + 1)) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.copy(l = Connection.Right))
                        }
                    }
                    val (finished, wrongVisited) = solveR(
                        currentCellCoordinate = currentCellCoordinate.copy(x = currentCellCoordinate.x + 1),
                        visited = if (nextMoves.size == 1) visited else mutableListOf()
                    )
                    if (finished) return true to emptyList()
                    fieldState.update { matrix ->
                        matrix.updateManyAt(wrongVisited) { oldCell ->
                            oldCell.copy(connections = oldCell.connections.toWrong())
                        } // this
                            .updateAt(currentCellCoordinate) { oldCell ->
                                oldCell.copy(connections = oldCell.connections.toRight(rr = false))
                            }
                    }
                }
            }
        }
        return false to visited
    }
}

private fun Matrix.at(coordinate: Coordinate): Cell = this[coordinate.y][coordinate.x]

private fun Matrix.updateAt(coordinates: Coordinate, action: (old: Cell) -> Cell): Matrix {
    if (coordinates.x >= this[0].size && coordinates.y >= this.size) return this
    return mapIndexed { y, rows ->
        rows.mapIndexed { x, cell ->
            if (x == coordinates.x && y == coordinates.y) action(cell)
            else cell
        }.toMutableList()
    }.toMutableList()
}

private fun Matrix.updateManyAt(coordinates: List<Coordinate>, action: (old: Cell) -> Cell): Matrix {
    if (coordinates.any { it.x >= this[0].size || it.y >= this.size }) return this
    return mapIndexed { y, rows ->
        rows.mapIndexed { x, cell ->
            val coordinate = Coordinate(x, y)
            if (coordinates.contains(coordinate)) action(cell)
            else cell
        }.toMutableList()
    }.toMutableList()
}

private fun Sides<Connection>.toWrong(): Sides<Connection> {
    return this.copy(
        u = if (u == Connection.Right) Connection.Wrong else u,
        d = if (d == Connection.Right) Connection.Wrong else d,
        l = if (l == Connection.Right) Connection.Wrong else l,
        r = if (r == Connection.Right) Connection.Wrong else r,
    )
}

private fun Sides<Connection>.toRight(
    uu: Boolean = true,
    dd: Boolean = true,
    rr: Boolean = true,
    ll: Boolean = true,
): Sides<Connection> {
    return this.copy(
        u = if (u == Connection.Wrong && uu) Connection.Right else u,
        d = if (d == Connection.Wrong && dd) Connection.Right else d,
        l = if (l == Connection.Wrong && ll) Connection.Right else l,
        r = if (r == Connection.Wrong && rr) Connection.Right else r,
    )
}

private fun Matrix.coordinateOfFirst(predicate: (Cell) -> Boolean): Coordinate {
    for (y in indices) {
        for (x in this[y].indices) {
            if (predicate(this[y][x])) {
                return x coord y
            }
        }
    }
    error("No elements with such predicate")
}

private fun Matrix.coordinateOfLast(predicate: (Cell) -> Boolean): Coordinate {
    for (y in indices.reversed()) {
        for (x in this[y].indices.reversed()) {
            if (predicate(this[y][x])) {
                return x coord y
            }
        }
    }
    error("No elements with such predicate")
}

private fun Matrix.updateRowAt(y: Int, action: (old: Cell) -> Cell): Matrix {
    if (y >= this.size) return this
    return mapIndexed { yy, row ->
        if (yy == y) row.map(action).toMutableList()
        else row
    }.toMutableList()
}

private const val DELAY_TIME = 10L