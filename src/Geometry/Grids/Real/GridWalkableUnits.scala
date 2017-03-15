package Geometry.Grids.Real

import Geometry.Grids.Abstract.GridBoolean
import Startup.With
import Utilities.Caching.Limiter

class GridWalkableUnits extends GridBoolean {
  
  override def defaultValue:Boolean = true
  val limitUpdates = new Limiter(5, _update)
  override def update() = limitUpdates.act()
  def _update() {
    reset()
    With.units.buildings
      .filter( ! _.flying)
      .foreach(building => building.tileArea.tiles
        .foreach(tile => set(tile, false)))
  }
}
