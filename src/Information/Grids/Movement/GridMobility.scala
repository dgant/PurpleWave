package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridInt
import Performance.Caching.Limiter
import Lifecycle.With
import Utilities.EnrichPosition._

class GridMobility extends AbstractGridInt {
  
  val limitUpdates = new Limiter(100, () => updateRecalculate)
  override def update() = limitUpdates.act
  def updateRecalculate() {
    val distanceMax = 3
    With.geography.allTiles
      .filter(With.grids.walkableTerrain.get)
      .foreach(tile => {
        var tileMobility = 0
        if (With.grids.walkableUnits.get(tile)) {
          // While-loops are the fastest way to iterate in Scala
          var mx = -1
          while (mx <= 1) {
            var my = -1
            while (my <= 1) {
              var doContinue = mx != 0 || my != 0
              var distance = 1
              while (doContinue && distance <= distanceMax) {
                val nextTile = tile.add(mx * distance, my * distance)
                doContinue = With.grids.walkable.get(nextTile)
                if (doContinue) tileMobility += 1
                distance += 1
              }
              my += 1
            }
            mx += 1
          }
        }
        set(tile, tileMobility)
      })
  }
}
