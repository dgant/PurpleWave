package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Startup.With

class GridWalkableUnits extends AbstractGridBoolean {
  
  override def defaultValue:Boolean = true
  
  override def update() {
    reset()
    With.units.buildings
      .filter( ! _.flying)
      .foreach(building => building.tileArea.tiles
        .foreach(tile => set(tile, false)))
  }
}
