package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Information.Grids.AbstractGrid
import Lifecycle.With
import Mathematics.Physics.Force
import Mathematics.Points.Tile
import bwapi.Color

object ShowMobility extends View {
  
  override def renderMap() {
    With.geography.allTiles
      .filter(With.viewport.contains)
      .filter(With.grids.walkable.get)
      .foreach(tile => {
        renderMapMobility(tile, With.grids.mobilityForceGround, Colors.MediumGreen)
        renderMapMobility(tile, With.grids.mobilityForceAir,    Colors.MediumTeal)
      })
  }
  
  def renderMapMobility(tile: Tile, grid: AbstractGrid[Force], color: Color) {
    val pixelStart  = tile.pixelCenter
    val force       = grid.get(tile)
    if (force.lengthFast > 0.0) {
      val forceNormal = force.normalize(12.0)
      val pixelEnd    = pixelStart.add(forceNormal.x.toInt, forceNormal.y.toInt)
      DrawMap.arrow(pixelStart, pixelEnd, color)
    }
  }
}
