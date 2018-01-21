package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Mathematics.Points.Tile
import ProxyBwapi.Players.Players

class GridCreep extends AbstractGridBoolean {
  
  // For Pylon power we can rely on the predictable grid it provides.
  // But creep spreads, and can be stopped by terrain or buildings (minerals/gas?)
  // So we need to actually query for this.
  
  // Performance optimization: Don't bother checking for creep if none is possible.
  private lazy val creepPossible: Boolean = Players.all.exists(_.isZerg) || With.units.neutral.exists(_.unitClass.producesCreep)
  
  override def update(): Unit = {
    if ( ! creepPossible) return
    val length = values.length
    var i = 0
    while (i < length) {
      val tile = new Tile(i)
      values(i) = With.game.hasCreep(tile.x, tile.y)
      i += 1
    }
  }
    
}
