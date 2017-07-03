package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With

object ShowArchitecturePaths extends View {
  
  override def renderMap() {
    With.architecture.existingPaths
      .values
      .filter(_.path.isDefined)
      .foreach(pathCache => {
        val path = pathCache.path.get
        DrawMap.circle(path.start.pixelCenter,  16, Colors.BrightYellow, solid = false)
        DrawMap.circle(path.end.pixelCenter,    16, Colors.BrightYellow, solid = false)
        path.tiles.foreach(tileList => {
          var lastTile = path.end
          tileList.drop(1).foreach(tile => {
            DrawMap.arrow(lastTile.pixelCenter, tile.pixelCenter, Colors.BrightYellow)
            lastTile = tile
          })
        })
      })
  }
}
