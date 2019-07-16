package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Shapes.Square
import Strategery.MapGroups

class GridWalkableTerrain extends AbstractGridBoolean {

  override def onInitialization() {
    val walkableGoal = if (MapGroups.narrowRamp.exists(_.matches)) 12 else 16
    indices.foreach(i => set(
      i,
      Square.pointsDownAndRight(4)
        .map(new Tile(i).topLeftWalkPixel.add)
        .count(walkTile => With.game.isWalkable(walkTile.bwapi)) >= walkableGoal))
  }
}
