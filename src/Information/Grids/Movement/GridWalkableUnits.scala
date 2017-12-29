package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import ProxyBwapi.Races.Terran

class GridWalkableUnits extends AbstractGridBoolean {
  
  override def defaultValue: Boolean = true
  reset()
  
  override def update() {
    reset()
    With.units.all
      .foreach(unit =>
        if (unit.unitClass.isBuilding
          && ! unit.flying
          && ! unit.is(Terran.SiegeTankSieged)) {
          unit.tileArea.tiles.foreach(tile => set(tile, false))
        })
  }
}
