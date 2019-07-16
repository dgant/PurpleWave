package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Information.Grids.AbstractGrid
import Lifecycle.With
import Mathematics.Points.{Tile, TileRectangle}

object ShowGrids extends View {

  lazy val basePaths: Seq[Seq[Tile]] = With.geography.bases.map(_.townHallTile).flatMap(a =>
      With.geography.bases.map(_.townHallTile).filterNot(_ == a).flatMap(b =>
        With.paths.aStarBasic(a, b).tiles))

  lazy val startPaths: Seq[Seq[Tile]] = With.geography.startLocations.flatMap(a =>
      With.geography.startLocations.filterNot(_ == a).flatMap(b =>
        With.paths.aStarBasic(a, b).tiles))

  override def renderMap() {
    val zone = With.units.ours.find(_.selected).map(_.zone).getOrElse(With.viewport.center.tileIncluding.zone)
    //renderGridArray(With.grids.enemyVulnerabilityGround, 0, 0)
    renderGridArray(zone.distanceGrid, 0, 0)
    //renderGridArray(zone.exitDistanceGrid, 1, 0)
    //zone.exit.foreach(e => renderGridArray(e.distanceGrid, 0, 1))
    //With.grids.scoutingPathsBases.update()
    //With.grids.scoutingPathsStartLocations.update()
    //renderGridArray(With.grids.scoutingPathsBases, 0, 0)
    //renderGridArray(With.grids.scoutingPathsStartLocations, 0, 1)
    /*
    basePaths.foreach(path => {
      var i = 0
      while (i < path.length - 1) {
        DrawMap.arrow(path(i).pixelCenter, path(i+1).pixelCenter, Colors.NeonGreen)
        i += 1
      }
    })
    startPaths.foreach(path => {
      var i = 0
      while (i < path.length - 1) {
        DrawMap.arrow(path(i).pixelCenter, path(i+1).pixelCenter, Colors.NeonTeal)
        i += 1
      }
    })
    */
  }
  
  private def renderGridArray[T](map: AbstractGrid[T], offsetX: Int = 0, offsetY: Int = 0) {
    val viewportTiles = TileRectangle(With.viewport.start.tileIncluding, With.viewport.end.tileIncluding)
    viewportTiles.tiles
      .filterNot(tile => map.get(tile) == map.defaultValue)
      .foreach(tile => DrawMap.text(
        tile.topLeftPixel.add(offsetX*12, offsetY*13),
        map.repr(map.get(tile))))
  }
  
  private def renderGrid[T](map: AbstractGrid[T], offsetX: Int=0, offsetY: Int=0) {
    With.geography.allTiles
      .foreach(Tile => DrawMap.text(
        Tile.topLeftPixel.add(offsetX*16, offsetY*13),
        map.get(Tile).toString))
  }
}
