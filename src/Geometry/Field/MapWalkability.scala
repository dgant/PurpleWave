package Geometry.Field

import Startup.With
import bwapi.TilePosition
import Utilities.Enrichment.EnrichPosition._

class MapWalkability extends InfluenceMap {
  
  var _initialized = false
  override def update() {
    if (_initialized) return
    
    points
      .map(point => new TilePosition(point._1, point._2))
      .foreach(tilePosition => set(
        tilePosition,
        if(
          (0 to 3).forall(dy =>
            (0 to 3).forall(dx =>
              With.game.isWalkable(tilePosition.toWalkPosition.add(dx, dy)))))
          1 else 0))
    
    _initialized = true
  }
}
