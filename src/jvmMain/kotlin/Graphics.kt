import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color

@Composable
fun CellContent(
    cell: Cell,
    modifier: Modifier = Modifier,
) {
    if (cell.visible.not()) return
    Canvas(modifier = modifier) {
        this.drawContext
        size.height
        //walls
        if (cell.walls.u) {
            drawLine(
                color = Color.Black,
                Offset.Zero,
                Offset(size.width, 0f),
                strokeWidth = cell.bordered.u.strokeWidth,
            )
        }
        if (cell.walls.d) {
            drawLine(
                color = Color.Black,
                Offset(0f, size.height),
                Offset(size.width, size.height),
                strokeWidth = cell.bordered.d.strokeWidth,
            )
        }
        if (cell.walls.l) {
            drawLine(
                color = Color.Black,
                Offset.Zero,
                Offset(0f, size.height),
                strokeWidth = cell.bordered.l.strokeWidth,
            )
        }
        if (cell.walls.r) {
            drawLine(
                color = Color.Black,
                Offset(size.width, 0f),
                Offset(size.width, size.height),
                strokeWidth = cell.bordered.r.strokeWidth,
            )
        }
        // connections
        if (cell.connections.u == Connection.Wrong) {
            drawLine(
                color = cell.connections.u.color,
                size.center,
                Offset(size.center.x, 0f),
                strokeWidth = pathWidth,
            )
        }
        if (cell.connections.d == Connection.Wrong) {
            drawLine(
                color = cell.connections.d.color,
                size.center,
                Offset(size.center.x, size.height),
                strokeWidth = pathWidth,
            )
        }
        if (cell.connections.l == Connection.Wrong) {
            drawLine(
                color = cell.connections.l.color,
                size.center,
                Offset(0f, size.center.y),
                strokeWidth = pathWidth,
            )
        }
        if (cell.connections.r == Connection.Wrong) {
            drawLine(
                color = cell.connections.r.color,
                size.center,
                Offset(size.width, size.center.y),
                strokeWidth = pathWidth,
            )
        }

        //right must always be on top
        if (cell.connections.u == Connection.Right) {
            drawLine(
                color = cell.connections.u.color,
                size.center,
                Offset(size.center.x, 0f),
                strokeWidth = pathWidth,
            )
        }
        if (cell.connections.d == Connection.Right) {
            drawLine(
                color = cell.connections.d.color,
                size.center,
                Offset(size.center.x, size.height),
                strokeWidth = pathWidth,
            )
        }
        if (cell.connections.l == Connection.Right) {
            drawLine(
                color = cell.connections.l.color,
                size.center,
                Offset(0f, size.center.y),
                strokeWidth = pathWidth,
            )
        }
        if (cell.connections.r == Connection.Right) {
            drawLine(
                color = cell.connections.r.color,
                size.center,
                Offset(size.width, size.center.y),
                strokeWidth = pathWidth,
            )
        }
    }
}

private val Connection.color: Color
    get() = when (this) {
        Connection.None -> error("Connection.None has no color")
        Connection.Right -> Color.Red
        Connection.Wrong -> Color.LightGray
    }

private val Boolean.strokeWidth: Float
    get() = if (this) 8f else 4f

private val pathWidth: Float
    get() = 8f
