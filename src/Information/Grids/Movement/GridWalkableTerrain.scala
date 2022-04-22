package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridArrayBoolean
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Shapes.Square
import Strategery.MapGroups

class GridWalkableTerrain extends AbstractGridArrayBoolean {

  override def onInitialization(): Unit = {
    val walkableGoal = if (MapGroups.narrowRamp.exists(_())) 12 else 16
    indices.foreach(i => set(
      i,
      Square(4)
        .map(new Tile(i).topLeftWalkPixel.add)
        .count(walkTile => With.game.isWalkable(walkTile.bwapi)) >= walkableGoal))
  }

  @inline final override def getUnchecked(i: Int): Boolean = values(i)
}
