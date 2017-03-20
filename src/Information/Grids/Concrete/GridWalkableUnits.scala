package Information.Grids.Concrete

import Information.Grids.Abstract.GridBoolean
import Startup.With
import bwapi.TilePosition

class GridWalkableUnits extends GridBoolean {
  
  override def defaultValue:Boolean = true
  
  override def update(relevantTiles:Iterable[TilePosition]) {
    reset(relevantTiles)
    With.units.buildings
      .filter( ! _.flying)
      .foreach(building => building.tileArea.tiles
        .foreach(tile => set(tile, false)))
  }
}
