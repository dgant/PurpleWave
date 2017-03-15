package Information.Grids.Concrete

import Information.Grids.Abstract.GridBoolean
import Geometry.Shapes.Circle
import Startup.With
import Performance.Caching.Limiter
import Utilities.TypeEnrichment.EnrichPosition._

class GridEnemyVision extends GridBoolean {
  
  override def update() = updateLimiter.act()
  private val updateLimiter = new Limiter(24, updateCalculations)
  private def updateCalculations() {
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