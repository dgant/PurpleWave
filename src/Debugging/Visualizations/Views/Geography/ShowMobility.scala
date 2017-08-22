package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Points.Tile
import Micro.Decisions.Potential

object ShowMobility extends View {
  
  override def renderMap() {
    With.geography.allTiles
      .filter(With.viewport.contains)
      .filter(With.grids.walkable.get)
      .foreach(renderMapMobility)
  }
  
  def renderMapMobility(tile: Tile) {
    val pixelStart  = tile.pixelCenter
    val force       = Potential.mobilityForce(pixelStart, tile.zone.maxMobility, With.grids.mobility)
    if (force.lengthFast > 0.0) {
      val forceNormal = force.normalize(12.0)
      val pixelEnd    = pixelStart.add(forceNormal.x.toInt, forceNormal.y.toInt)
      DrawMap.circle(pixelStart, 3, Colors.MediumGreen)
      DrawMap.line(pixelStart, pixelEnd, Colors.MediumGreen)
    }
  }
}
