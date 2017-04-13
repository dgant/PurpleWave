package Information.Grids.Movement

import Mathematics.Shapes.Square
import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Utilities.EnrichPixel._

class GridWalkableTerrain extends AbstractGridBoolean {
  override def onInitialization() {
    tiles.foreach(Tile => set(
      Tile,
      Square.pointsDownAndRight(4)
        .map(Tile.topLeftWalkPixel.add)
        .forall(With.game.isWalkable)))
  }
}
