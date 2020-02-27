package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Information.Grids.AbstractGrid
import Lifecycle.With
import Mathematics.Points.TileRectangle

object ShowGrids extends View {

  override def renderMap() {
    //renderGridArray(With.units.all.find(_.selected).map(_.tileIncludingCenter).getOrElse(With.geography.home).zone.distanceGrid, 0, 1)
    renderGridArray(With.grids.enemyRangeGround, 0, 0)
    renderGridArray(With.grids.units, 1, 0)
  }
  
  private def renderGridArray[T](map: AbstractGrid[T], offsetX: Int = 0, offsetY: Int = 0) {
    val viewportTiles = TileRectangle(With.viewport.start.tileIncluding, With.viewport.end.tileIncluding)
    viewportTiles.tiles
      .filterNot(tile => map.get(tile) == map.defaultValue)
      .foreach(tile => DrawMap.text(
        tile.topLeftPixel.add(offsetX*10, offsetY*13),
        map.repr(map.get(tile))))
  }
  
  private def renderGrid[T](map: AbstractGrid[T], offsetX: Int=0, offsetY: Int=0) {
    With.geography.allTiles
      .foreach(Tile => DrawMap.text(
        Tile.topLeftPixel.add(offsetX*16, offsetY*13),
        map.get(Tile).toString))
  }
}
