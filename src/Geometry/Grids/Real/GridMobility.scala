package Geometry.Grids.Real

import Geometry.Grids.Abstract.GridInt
import Startup.With
import Utilities.Caching.Limiter
import Utilities.Enrichment.EnrichPosition._

class GridMobility extends GridInt {
  
  val limitUpdates = new Limiter(24 * 60, _update)
  override def update() = limitUpdates.act()
  def _update() {
    reset()
    With.grids.walkableUnits.update()
    
    val distanceMax = 3
  
    positions
      .filter(With.grids.walkableUnits.get)
      .foreach(ourPosition =>
        (-1 to 1).foreach(my =>
          (-1 to 1).foreach(mx => {
            var doContinue = mx != 0 || my != 0
            (1 to distanceMax).foreach(distance =>
              if (doContinue) {
                val nextPosition = ourPosition.add(mx * distance, my * distance)
                doContinue = With.grids.walkableUnits.get(nextPosition)
                if (doContinue) { add(ourPosition, 1) }
              })
          })))
  }
}
