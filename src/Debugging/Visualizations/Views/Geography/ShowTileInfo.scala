package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.DebugView
import Information.Geography.Pathfinding.PathfindProfile
import Lifecycle.With
import Mathematics.Points.Pixel

object ShowTileInfo extends DebugView {
  
  override def renderScreen(): Unit = {
    val mousePixelScreen  = new Pixel(With.game.getMousePosition)
    val mousePixelMap     = mousePixelScreen + With.viewport.start
    val mouseTile         = mousePixelMap.tile
    val walkableTile      = mousePixelMap.walkableTile
    val zone              = With.geography.zoneByTile(mouseTile)

    val pathfindProfile = new PathfindProfile(With.geography.home.walkableTile)
    pathfindProfile.end = Some(walkableTile)
    val path = pathfindProfile.find
    if (path.pathExists) {
      path.tiles.foreach(tiles => {
        tiles.toVector.indices.dropRight(1).foreach(i => DrawMap.arrow(tiles(i).center, tiles(i+1).center, Colors.BrightBlue))
      })
    }

    DrawMap.line(mouseTile.center, walkableTile.center, Colors.BrightYellow)
    DrawMap.tileRectangle(mouseTile.toRectangle, Colors.BrightYellow)
    DrawMap.tileRectangle(walkableTile.toRectangle, Colors.hsv((System.currentTimeMillis() % 256L).toInt, 255, 192))
    DrawScreen.text(mousePixelScreen.add(4, -6), (walkableTile.groundPixels(With.geography.home).toInt / 32).toString)

    zone.border.foreach(t => DrawMap.circle(t.center, 4, Colors.DarkBlue))
  }
}
