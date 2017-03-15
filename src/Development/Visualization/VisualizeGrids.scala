package Development.Visualization

import Geometry.Grids.Abstract.GridConcrete
import Startup.With
import Utilities.Enrichment.EnrichPosition._

object VisualizeGrids {
  def render() {
    _drawGrid(With.grids.enemyGroundStrength, 0, 0)
    _drawGrid(With.grids.friendlyGroundStrength, 0, 1)
  }
  def _drawGrid[T](map:GridConcrete[T], offsetX:Int=0, offsetY:Int=0) {
    map.positions
      .filter(tilePosition => map.get(tilePosition) != 0 &&  map.get(tilePosition) != false)
      .foreach(tilePosition => With.game.drawTextMap(tilePosition.toPosition.add(offsetX*16, offsetY*13), map.repr(map.get(tilePosition))))
  }
}
