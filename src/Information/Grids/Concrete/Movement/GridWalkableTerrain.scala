package Information.Grids.Concrete.Movement

import Geometry.Shapes.Square
import Information.Grids.Abstract.ArrayTypes.GridBoolean
import Startup.With
import Utilities.EnrichPosition._

class GridWalkableTerrain extends GridBoolean {
  override def onInitialization() {
    tiles.foreach(tilePosition => set(
      tilePosition,
      Square.pointsDownAndRight(4)
        .map(tilePosition.topLeftWalkPosition.add)
        .forall(With.game.isWalkable)))
  }
}
