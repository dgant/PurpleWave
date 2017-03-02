package Geometry.Grids.Real

import Geometry.Grids.Abstract.GridInt
import Startup.With
import Utilities.Caching.Limiter
import Utilities.Enrichment.EnrichPosition._
import Utilities.Enrichment.EnrichUnitType._

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
    With.units.buildings
      .filter( ! _.flying)
      .foreach(building => building.utype.tiles
        .foreach(tile => set(building.tilePosition.add(tile), 0)))
  }
}
