package Information.Grids.Concrete

import Information.Grids.Abstract.GridInt
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._
import bwapi.TilePosition

class GridMobility extends GridInt {
  
  override def update(relevantTiles:Iterable[TilePosition]) {
    val distanceMax = 3
    relevantTiles
      .foreach(tile => {
        var tileMobility = 0
        if (With.grids.walkable.get(tile)) {
          (-1 to 1).foreach(my =>
            (-1 to 1).foreach(mx => {
              var doContinue = mx != 0 || my != 0
              (1 to distanceMax).foreach(distance =>
                if (doContinue) {
                  val nextPosition = tile.add(mx * distance, my * distance)
                  doContinue = With.grids.walkable.get(nextPosition)
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
