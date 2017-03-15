package Information.Grids.Concrete

import Information.Grids.Abstract.GridInt
import Startup.With
import Performance.Caching.Limiter
import Utilities.TypeEnrichment.EnrichPosition._

class GridMobility extends GridInt {
  
  val limitUpdates = new Limiter(5, _update)
  override def update() = limitUpdates.act()
  def _update() {
    reset()
    
    val distanceMax = 3
  
    positions
      .filter(With.grids.walkable.get)
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
