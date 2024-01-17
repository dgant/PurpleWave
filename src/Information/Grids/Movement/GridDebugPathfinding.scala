package Information.Grids.Movement

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Information.Geography.Pathfinding.Types.TilePath
import Information.Grids.Versioned.GridVersionedInt
import Lifecycle.With
import Mathematics.Points.Tile

final class GridDebugPathfinding extends GridVersionedInt {
  val Blank   = 0
  val Visited = 1
  val Path    = 2
  val Start   = 3
  val End     = 4

  var lastFrame: Int = -1

  def lock(): Boolean = {
    val output = With.frame > lastFrame
    lastFrame = With.frame
    if (output) {
      reset()
    }
    output
  }

  @inline def visit(tile: Tile): Unit = {
    set(tile, Visited)
  }

  def setPath(tilePath: TilePath): Unit = {
    tilePath.tiles.foreach(_.foreach(set(_, Path)))
    set(tilePath.start, Start)
    set(tilePath.end,   End)
  }

  def drawMap(): Unit = {
    With.viewport.areaTiles.expand(16, 16).tiles.foreach(tile => {
      val v = get(tile)
      if (v > 0) {
        val color = v match {
          case Path   => Colors.BrightGreen
          case Start  => Colors.NeonViolet
          case End    => Colors.NeonTeal
          case _      => Colors.BrightRed
        }
        DrawMap.hatchTile(tile, color)
      }
    })
  }

  override def update(): Unit = {}
}
