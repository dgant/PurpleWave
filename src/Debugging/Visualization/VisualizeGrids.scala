package Debugging.Visualization

import Information.Grids.Abstract.GridArray
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

object VisualizeGrids {
  def render() {
    renderGrid(With.grids.mobility, 0, 0)
    renderGrid(With.grids.enemyGroundStrength, 0, 1)
  }
  
  private def renderGrid[T](map:GridArray[T], offsetX:Int=0, offsetY:Int=0) {
    map.tiles
      .filter(tilePosition => map.get(tilePosition) != map.defaultValue)
      .foreach(tilePosition => DrawMap.text(
        tilePosition.toPosition.add(offsetX*16, offsetY*13),
        map.repr(map.get(tilePosition))))
  }
}
