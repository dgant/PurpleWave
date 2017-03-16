package Information.Grids.Concrete

import Geometry.Shapes.Square
import Information.Grids.Abstract.GridInt
import Performance.Caching.Limiter
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

class GridMobility extends GridInt {
  
  override def update() = updateLimiter.act()
  private val updateLimiter = new Limiter(3, updateCalculations)
  private def updateCalculations() {
    val distanceMax = 3
    
    //This is the most expensive grid to update, so limit updates to relevant tiles
    val nearbyPoints = Square.points(4).toList
    val tilesWeCareAbout = With.units.all.filterNot(_.flying).flatten(unit => nearbyPoints.map(unit.tileCenter.add))
  
    tilesWeCareAbout
      .foreach(tile => {
        var tileMobility = 0
        if (With.grids.walkable.get(tile)) {
          (-1 to 1).foreach(my =>
            (-1 to 1).foreach(mx => {
              var doContinue = mx != 0 || my != 0
              (1 to distanceMax).foreach(distance =>
                if (doContinue) {
                  val nextPosition = tile.add(mx * distance, my * distance)
                  doContinue = With.grids.walkableUnits.get(nextPosition)
                  if (doContinue) {
                    tileMobility += 1
                  }
                })
            }))
        }
        set(tile, tileMobility)
      })
  }
}
