package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.DrawMap
import Information.Grids.AbstractGrid
import Information.Grids.ArrayTypes.AbstractGridArray
import Lifecycle.With

object VisualizeGrids {
  def render() {
    renderGridArray(With.grids.dpsEnemyGroundNormal,  0, 0)
  }
  
  private def renderGridArray[T](map:AbstractGridArray[T], offsetX:Int=0, offsetY:Int=0) {
    With.geography.allTiles
      .filterNot(Tile => map.get(Tile) == map.defaultValue)
      .foreach(Tile => DrawMap.text(
        Tile.topLeftPixel.add(offsetX*16, offsetY*13),
        map.repr(map.get(Tile))))
  }
  
  private def renderGrid[T](map:AbstractGrid[T], offsetX:Int=0, offsetY:Int=0) {
    With.geography.allTiles
      .foreach(Tile => DrawMap.text(
        Tile.topLeftPixel.add(offsetX*16, offsetY*13),
        map.get(Tile).toString))
  }
}
