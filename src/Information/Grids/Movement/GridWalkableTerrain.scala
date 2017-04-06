package Information.Grids.Movement

import Mathematics.Shapes.Square
import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Utilities.EnrichPosition._

class GridWalkableTerrain extends AbstractGridBoolean {
  override def onInitialization() {
    tiles.foreach(tilePosition => set(
      tilePosition,
      Square.pointsDownAndRight(4)
        .map(tilePosition.topLeftWalkPosition.add)
        .forall(With.game.isWalkable)))
  }
}
