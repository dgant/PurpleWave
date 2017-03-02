package Geometry.Grids.Real

import Geometry.Grids.Abstract.GridInt
import Startup.With
import Utilities.Caching.Limiter
import Utilities.Enrichment.EnrichPosition._

class GridWalkability extends GridInt {
  
  val limitUpdates = new Limiter(24 * 60, _update)
  override def update() {
    limitUpdates.act()
  }
  def _update() {
    reset()
    positions
      .foreach(tilePosition => set(
        tilePosition,
        if (
          (0 to 3).forall(dy =>
            (0 to 3).forall(dx =>
              With.game.isWalkable(tilePosition.toWalkPosition.add(dx, dy)))))
          1 else 0))
  }
}
