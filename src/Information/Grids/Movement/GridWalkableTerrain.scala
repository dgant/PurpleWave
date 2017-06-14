package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Shapes.Square

class GridWalkableTerrain extends AbstractGridBoolean {
  override def onInitialization() {
    indices.foreach(i => set(
      i,
      Square.pointsDownAndRight(4)
        .map(new Tile(i).topLeftWalkPixel.add)
        .forall(walkTile => With.game.isWalkable(walkTile.bwapi))))
  }
}
