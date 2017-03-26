package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Information.Grids.Abstract.GridArray
import Startup.With
import Utilities.EnrichPosition._

object VisualizeGrids {
  def render() {
    //renderGrid(With.grids.buildable, 0, 0)
    renderGrid(With.grids.buildableTerrain, 0, 0)
    renderGrid(With.grids.walkableUnits, 1, 0)
    renderGrid(With.grids.walkableTerrain, 0, 1)
  }
  
  private def renderGrid[T](map:GridArray[T], offsetX:Int=0, offsetY:Int=0) {
    With.geography.allTiles
      .filter(tilePosition => map.get(tilePosition) != map.defaultValue)
      .foreach(tilePosition => DrawMap.text(
        tilePosition.toPosition.add(offsetX*16, offsetY*13),
        map.repr(map.get(tilePosition))))
  }
}
