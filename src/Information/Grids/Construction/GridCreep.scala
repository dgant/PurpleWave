package Information.Grids.Construction

import Information.Grids.ArrayTypes.AbstractGridBoolean
import Lifecycle.With
import Mathematics.Points.Tile

class GridCreep extends AbstractGridBoolean {
  
  // For Pylon power we can rely on the predictable grid it provides.
  // But creep spreads, and can be stopped by terrain or buildings (minerals/gas?)
  // So we need to actually query for this
  
  override def update(): Unit = {
    var i = 0
    var length = values.length
    while (i < length) {
      var tile = new Tile(i)
      values(i) = With.game.hasCreep(tile.x, tile.y)
    }
  }
    
}
