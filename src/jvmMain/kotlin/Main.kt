import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.delay

val maze = Maze(x = 30, y = 30)

@Composable
@Preview
fun App(modifier: Modifier = Modifier) {
    val field by maze.fieldState.collectAsState()
    LaunchedEffect(Unit) {
        delay(500)
        maze.fill()
        maze.generate()
        maze.generateExits()
        maze.solve()
    }
    Box(modifier) {
        LazyColumn(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            items(field) { rows ->
                LazyRow {
                    items(rows) { cell ->
                        CellContent(
                            modifier = Modifier.size(CELL_SIZE.dp),
                            cell = cell,
                        )
                    }
                }
            }
        }
    }
}

private const val CELL_SIZE = 12

fun main() = application {
    Window(onCloseRequest = ::exitApplication) {
        App(Modifier.fillMaxSize())
    }
}
