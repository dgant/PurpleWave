package Information.Grids.Movement

import Geometry.Shapes.Square
import Information.Grids.ArrayTypes.AbstractGridBoolean
import Startup.With
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
