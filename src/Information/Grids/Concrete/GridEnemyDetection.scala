package Information.Grids.Concrete

import Geometry.Shapes.Circle
import Information.Grids.Abstract.GridBoolean
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.TilePosition

class GridEnemyDetection extends GridBoolean {
  
  override def update(relevantTiles:Iterable[TilePosition]) {
    reset(relevantTiles)
    With.units.enemy
      .filter(_.possiblyStillThere)
      .filter(_.unitClass.isDetector)
      .foreach(u => {
      Circle.points(u.unitClass.sightRange / 32)
        .map(u.tileCenter.add)
        .foreach(set(_, true))
    })
  }
}