package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With

class GridWalkableUnits extends AbstractGridBoolean {
  
  override def defaultValue: Boolean = true
  reset()
  
  override def update() {
    reset()
    With.units.buildings
      .filter( ! _.flying)
      .foreach(building => building.tileArea.tiles
        .foreach(tile => set(tile, false)))
  }
}
