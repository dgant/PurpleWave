package Information.Grids.Concrete

import Geometry.Shapes.Circle
import Information.Grids.Abstract.GridBoolean
import Startup.With
import Utilities.EnrichPosition._
import bwapi.TilePosition

class GridEnemyVision extends GridBoolean {
  
  override def update(relevantTiles:Iterable[TilePosition]) {
    reset(relevantTiles)
    With.units.enemy
      .filter(_.possiblyStillThere)
      .foreach(u => {
      Circle.points(u.unitClass.sightRange / 32)
        .map(u.tileCenter.add)
        .foreach(set(_, true))
    })
  }
}