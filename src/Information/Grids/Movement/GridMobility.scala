package Information.Grids.Movement

import Information.Grids.ArrayTypes.AbstractGridInt
import Lifecycle.With
import Mathematics.Shapes.Circle

class GridMobility extends AbstractGridInt {
  
  override def update() = {
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
          if (With.grids.chokepoints.get(tile)) {
            tileMobility = distanceMax * 8
          }
          else {
            // Scala's while-loops are faster than its for-comprehensions
            var mx = -1
            while (mx <= 1) {
              var my = -1
              while (my <= 1) {
                var doContinue = mx != 0 || my != 0
                var distance = 1
                while (doContinue && distance <= distanceMax) {
                  val nextTile = tile.add(mx * distance, my * distance)
                  doContinue = nextTile.valid && With.grids.walkable.get(nextTile.i)
                  if (doContinue) tileMobility += 1
                  distance += 1
                }
                my += 1
              }
              mx += 1
            }
          }
        }
        set(tile, tileMobility)
      })
  }
}
