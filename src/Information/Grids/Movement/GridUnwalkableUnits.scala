package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridVersioned
import Lifecycle.With
import ProxyBwapi.Races.{Terran, Zerg}

class GridUnwalkableUnits extends AbstractGridVersioned {
  
  override protected def updateTimestamps() {
    With.units.all
      .foreach(unit =>
        if ((unit.unitClass.isBuilding || unit.isAny(Zerg.Egg, Zerg.LurkerEgg))
          && ! unit.flying
          && ! unit.is(Terran.SiegeTankSieged)) {
          unit.tiles.foreach(stamp)
        })
  }
}
