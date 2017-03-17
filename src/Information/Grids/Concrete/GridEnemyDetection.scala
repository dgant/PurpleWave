package Information.Grids.Concrete

import Information.Grids.Abstract.GridBoolean
import Geometry.Shapes.Circle
import Startup.With
import Performance.Caching.Limiter
import Utilities.TypeEnrichment.EnrichPosition._

class GridEnemyDetection extends GridBoolean {
  
  override def update() = updateLimiter.act()
  val updateLimiter = new Limiter(1, updateCalculations)
  private def updateCalculations() {
    reset()
    With.units.enemy
      .filter(_.possiblyStillThere)
      .filter(_.isDetector)
      .foreach(u => {
      Circle.points(u.unitClass.sightRange / 32)
        .map(u.tileCenter.add)
        .foreach(set(_, true))
    })
  }
}