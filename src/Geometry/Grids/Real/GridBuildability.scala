package Geometry.Grids.Real

import Geometry.Grids.Abstract.GridBoolean
import Startup.With
import Utilities.Caching.Limiter

class GridBuildability extends GridBoolean {
  
  val limitUpdates = new Limiter(24 * 2, _update)
  override def update() = limitUpdates.act
  
  def _update() {
    reset()
    
    positions.foreach(tilePosition => set(tilePosition, With.game.isBuildable(tilePosition)))
    
    With.units.buildings.filter( ! _.flying)
      .foreach(building => building.tileArea.tiles
        .foreach(tile => set(tile, false)))
  }
}
