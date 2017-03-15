package Information.Grids.Concrete

import Information.Grids.Abstract.GridBoolean
import Geometry.Shapes.Circle
import Startup.With
import Performance.Caching.Limiter
import Utilities.TypeEnrichment.EnrichPosition._

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