package Information.Grids.Concrete

import Information.Grids.Abstract.GridBoolean
import Startup.With
import Performance.Caching.Limiter

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
