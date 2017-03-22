package Debugging.Visualization

import Information.Grids.Abstract.GridArray
import Startup.With
import Utilities.TypeEnrichment.EnrichPosition._

object VisualizeGrids {
  def render() {
    //renderGrid(With.grids.psi, 0, 0)
    renderGrid(With.grids.enemyStrength, 0, 0)
    renderGrid(With.grids.mobility, 0, 1)
  }
  
  private def renderGrid[T](map:GridArray[T], offsetX:Int=0, offsetY:Int=0) {
    With.grids.relevantTiles
      .filter(tilePosition => map.get(tilePosition) != map.defaultValue)
      .foreach(tilePosition => DrawMap.text(
        tilePosition.toPosition.add(offsetX*16, offsetY*13),
        map.repr(map.get(tilePosition))))
  }
}
