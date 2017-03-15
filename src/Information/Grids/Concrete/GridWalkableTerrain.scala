package Information.Grids.Concrete

import Information.Grids.Abstract.GridBoolean
import Geometry.Shapes.Square
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

class GridWalkableTerrain extends GridBoolean {
  override def onInitialization() {
    tiles.foreach(tilePosition => set(
      tilePosition,
      Square.pointsDownAndRight(4)
        .map(tilePosition.toWalkPosition.add)
        .forall(With.game.isWalkable)))
  }
}
