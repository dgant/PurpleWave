package Information.Grids.Vision

import Information.Grids.ArrayTypes.AbstractGridInt
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Shapes.Circle

class GridFriendlyVision extends AbstractGridInt {
  
  def visible(tile: Tile): Boolean = get(tile) >= lastUpdateFrame
  
  var lastUpdateFrame = 0
  
  def everSeen(tile: Tile): Boolean = get(tile) > 0
  def framesSince(tile: Tile): Int = lastUpdateFrame - get(tile)
  
  override def update() {
    lastUpdateFrame = With.frame
    With.units.ours
      .flatMap(u => Circle.points(12).map(u.tileIncludingCenter.add))
      .foreach(tile =>
        if (With.game.isVisible(tile.bwapi))
          set(tile, With.frame))
  }
}