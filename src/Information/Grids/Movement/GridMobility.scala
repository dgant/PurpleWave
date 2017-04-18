package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridInt
import Lifecycle.With
import Mathematics.Shapes.Circle
import Performance.Caching.Limiter

class GridMobility extends AbstractGridInt {
  
  val limitUpdates = new Limiter(100, () => updateRecalculate)
  override def update() = limitUpdates.act
  def updateRecalculate() {
    val tilesToUpdate = With.units.ours
      .filter(_.canMoveThisFrame)
      .map(_.tileIncludingCenter)
      .flatten(tile => Circle.points(4).map(tile.add))
      .filter(With.grids.walkableTerrain.get)
    
    val distanceMax = 2
    tilesToUpdate
      .filter(With.grids.walkableTerrain.get)
      .foreach(tile => {
        var tileMobility = 0
        if (With.grids.walkableUnits.get(tile)) {
          // Scala's while-loops are faster than its for-comprehensions
          var mx = -1
          while (mx <= 1) {
            var my = -1
            while (my <= 1) {
              var doContinue = mx != 0 || my != 0
              var distance = 1
              while (doContinue && distance <= distanceMax) {
                val nextTile = tile.add(mx * distance, my * distance)
                doContinue = nextTile.valid && With.grids.walkable.get(nextTile)
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
