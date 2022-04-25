package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.DebugView
import Information.Grids.Grid
import Lifecycle.With
import Mathematics.Points.TileRectangle

object ShowGrids extends DebugView {

  override def renderMap(): Unit = {
    With.grids.selected.foreach(renderGrid)
  }
  
  private def renderGrid(grid: Grid): Unit = {
    val viewportTiles = TileRectangle(With.viewport.start.tile, With.viewport.end.tile)
    viewportTiles.tiles
      .foreach(tile => {
        val repr = grid.reprAt(tile.i)
        if (repr != "0" && repr != "" && repr != "false") {
          DrawMap.text(tile.topLeftPixel.add(14, 12), repr)
        }
      })
  }
}
