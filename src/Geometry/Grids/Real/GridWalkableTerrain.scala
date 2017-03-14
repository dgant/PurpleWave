package Geometry.Grids.Real

import Geometry.Grids.Abstract.GridBoolean
import Geometry.Shapes.Square
import Startup.With
import Utilities.Enrichment.EnrichPosition._

class GridWalkableTerrain extends GridBoolean {
  override def onInitialization() {
    positions.foreach(tilePosition => set(
      tilePosition,
      Square.pointsDownAndRight(4)
        .map(tilePosition.toWalkPosition.add)
        .forall(With.game.isWalkable)))
  }
}
