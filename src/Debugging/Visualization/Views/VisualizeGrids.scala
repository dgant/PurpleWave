package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Information.Grids.Abstract.GridArray
import Startup.With
import Utilities.EnrichPosition._

object VisualizeGrids {
  def render() {
    renderGrid(With.grids.enemyStrength, 0, 0)
  }
  
  private def renderGrid[T](map:GridArray[T], offsetX:Int=0, offsetY:Int=0) {
    With.grids.relevantTiles
      .filter(tilePosition => map.get(tilePosition) != map.defaultValue)
      .foreach(tilePosition => DrawMap.text(
        tilePosition.toPosition.add(offsetX*16, offsetY*13),
        map.repr(map.get(tilePosition))))
  }
}
