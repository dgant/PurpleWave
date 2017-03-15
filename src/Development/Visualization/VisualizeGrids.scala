package Development.Visualization

import Geometry.Grids.Abstract.GridConcrete
import Startup.With
import Utilities.Enrichment.EnrichPosition._

object VisualizeGrids {
  def render() {
    renderGrid(With.grids.mobility, 0, 0)
    renderGrid(With.grids.enemyGroundStrength, 0, 1)
  }
  
  private def renderGrid[T](map:GridConcrete[T], offsetX:Int=0, offsetY:Int=0) {
    map.positions
      .filter(tilePosition => map.get(tilePosition) != map.defaultValue)
      .foreach(tilePosition => DrawMap.text(
        tilePosition.toPosition.add(offsetX*16, offsetY*13),
        map.repr(map.get(tilePosition))))
  }
}
