package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Information.Grids.AbstractGrid
import Lifecycle.With
import Mathematics.Points.TileRectangle

object MapGrids {
  def render() {
    renderGridArray(With.grids.dpsEnemyApproximate,  0, 0)
  }
  
  private def renderGridArray[T](map:AbstractGrid[T], offsetX:Int=0, offsetY:Int=0) {
    val viewportTiles = TileRectangle(With.viewport.start.tileIncluding, With.viewport.end.tileIncluding)
    viewportTiles.tiles
      .filterNot(tile => map.get(tile) == map.defaultValue)
      .foreach(tile => DrawMap.text(
        tile.topLeftPixel.add(offsetX*16, offsetY*13),
        map.repr(map.get(tile))))
  }
  
  private def renderGrid[T](map:AbstractGrid[T], offsetX:Int=0, offsetY:Int=0) {
    With.geography.allTiles
      .foreach(Tile => DrawMap.text(
        Tile.topLeftPixel.add(offsetX*16, offsetY*13),
        map.get(Tile).toString))
  }
}
