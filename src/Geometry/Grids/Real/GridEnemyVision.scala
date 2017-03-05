package Geometry.Grids.Real

import Geometry.Circle
import Geometry.Grids.Abstract.GridBoolean
import Startup.With
import Utilities.Caching.Limiter
import Utilities.Enrichment.EnrichPosition._

class GridEnemyVision extends GridBoolean {
  
  val _limitUpdates = new Limiter(24, _update)
  override def update() = _limitUpdates.act()
  def _update() {
    reset()
    With.units.enemy
      .filter(_.possiblyStillThere)
      .foreach(u => {
      Circle.points(u.utype.sightRange / 32)
        .map(u.tileCenter.add)
        .foreach(set(_, true))
    })
  }
}