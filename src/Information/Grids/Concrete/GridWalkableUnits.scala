package Information.Grids.Concrete

import Information.Grids.Abstract.GridBoolean
import Startup.With
import Performance.Caching.Limiter

class GridWalkableUnits extends GridBoolean {
  
  override def defaultValue:Boolean = true
  
  override def update() = updateLimiter.act()
  private val updateLimiter = new Limiter(5, updateCalculations)
  private def updateCalculations() {
    reset()
    With.units.buildings
      .filter( ! _.flying)
      .foreach(building => building.tileArea.tiles
        .foreach(tile => set(tile, false)))
  }
}
