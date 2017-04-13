package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Mathematics.Shapes.Square

class GridWalkableTerrain extends AbstractGridBoolean {
  override def onInitialization() {
    tiles.foreach(Tile => set(
      Tile,
      Square.pointsDownAndRight(4)
        .map(Tile.topLeftWalkPixel.add)
        .forall(walkTile => With.game.isWalkable(walkTile.bwapi))))
  }
}
