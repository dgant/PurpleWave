package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridInt
import Lifecycle.With
import Performance.Caching.Limiter

class GridMobility extends AbstractGridInt {
  
  val limitUpdates = new Limiter(100, () => updateRecalculate)
  override def update() = limitUpdates.act
  def updateRecalculate() {
    val distanceMax = 3
    indices
      .filter(With.grids.walkableTerrain.get)
      .foreach(i => {
        var tileMobility = 0
        if (With.grids.walkableUnits.get(i)) {
          // While-loops are the fastest way to iterate in Scala
          var mx = -1
          while (mx <= 1) {
            var my = -1
            while (my <= 1) {
              var doContinue = mx != 0 || my != 0
              var distance = 1
              while (doContinue && distance <= distanceMax) {
                val nextIndex = i + mx * distance + my * distance * With.mapWidth
                doContinue = With.grids.walkable.get(nextIndex)
                if (doContinue) tileMobility += 1
                distance += 1
              }
              my += 1
            }
            mx += 1
          }
        }
        set(i, tileMobility)
      })
  }
}
