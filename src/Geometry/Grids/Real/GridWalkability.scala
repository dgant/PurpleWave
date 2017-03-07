package Geometry.Grids.Real

import Geometry.Grids.Abstract.GridBoolean
import Geometry.Shapes.Square
import Startup.With
import Utilities.Caching.Limiter
import Utilities.Enrichment.EnrichPosition._
import Utilities.Enrichment.EnrichUnitType._

class GridWalkability extends GridBoolean {
  
  val limitUpdates = new Limiter(24 * 60, _update)
  override def update() {
    limitUpdates.act()
  }
  def _update() {
    reset()
    
    positions.foreach(tilePosition => set(
      tilePosition,
      Square.pointsDownAndRight(4)
        .map(tilePosition.toWalkPosition.add)
        .forall(With.game.isWalkable)))
    
    With.units.buildings
      .filter( ! _.flying)
      .foreach(building => building.utype.tiles
        .foreach(tile => set(building.tileTopLeft.add(tile), false)))
  }
}
