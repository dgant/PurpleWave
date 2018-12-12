package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridVersioned
import Lifecycle.With
import ProxyBwapi.Races.Terran

class GridUnwalkableUnits extends AbstractGridVersioned {
  
  override protected def updateTimestamps() {
    With.units.all
      .foreach(unit =>
        if (unit.unitClass.isBuilding
          && ! unit.flying
          && ! unit.is(Terran.SiegeTankSieged)) {
          unit.tileArea.tiles.foreach(stamp)
        })
  }
}