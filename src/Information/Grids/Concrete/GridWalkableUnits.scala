package Information.Grids.Concrete

import Information.Grids.Abstract.ArrayTypes.GridBoolean
import Startup.With

class GridWalkableUnits extends GridBoolean {
  
  override def defaultValue:Boolean = true
  
  override def update() {
    reset()
    With.units.buildings
      .filter( ! _.flying)
      .foreach(building => building.tileArea.tiles
        .foreach(tile => set(tile, false)))
  }
}
