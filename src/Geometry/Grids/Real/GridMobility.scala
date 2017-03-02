package Geometry.Grids.Real

import Geometry.Circle
import Geometry.Grids.Abstract.GridInt
import Startup.With
import Utilities.Caching.Limiter
import Utilities.Enrichment.EnrichPosition._

class GridMobility extends GridInt {
  
  val limitUpdates = new Limiter(24 * 60, _update)
  override def update() {
    limitUpdates.act()
  }
  def _update() {
    reset()
    With.maps.walkability.update()
  
    positions
      .filter(With.maps.walkability.get(_) > 0)
      .foreach(ourPosition =>
        Circle.points(3)
          .map(delta => ourPosition.add(delta._1, delta._2))
          .foreach(nearbyPosition => add(ourPosition, With.maps.walkability.get(nearbyPosition))))
  }
}
