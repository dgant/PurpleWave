package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Information.Grids.Abstract.ArrayTypes.GridArray
import Information.Grids.Abstract.Grid
import Startup.With
import Utilities.EnrichPosition._

object VisualizeGrids {
  def render() {
    //renderGrid(With.grids.buildable, 0, 0)
    renderGridArray(With.grids.dpsEnemyAirExplosive, 1, 0)
    renderGridArray(With.grids.dpsEnemyAirNormal, 0, 0)
    renderGrid(With.grids.dpsEnemyGroundNormal, 0, 1)
    renderGrid(With.grids.dpsEnemyGroundExplosive, 1, 1)
  }
  
  private def renderGridArray[T](map:GridArray[T], offsetX:Int=0, offsetY:Int=0) {
    With.geography.allTiles
      .filter(tilePosition => map.get(tilePosition) != map.defaultValue)
      .foreach(tilePosition => DrawMap.text(
        tilePosition.toPosition.add(offsetX*16, offsetY*13),
        map.repr(map.get(tilePosition))))
  }
  
  private def renderGrid[T](map:Grid[T], offsetX:Int=0, offsetY:Int=0) {
    With.geography.allTiles
      .foreach(tilePosition => DrawMap.text(
        tilePosition.toPosition.add(offsetX*16, offsetY*13),
        map.get(tilePosition).toString))
  }
}
