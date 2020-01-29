package Debugging.Visualizations

import Lifecycle.With
import Mathematics.Points.{Pixel, Tile, TileRectangle}
import Performance.Cache

class Viewport {
  
  def start   : Pixel = startCache()
  def end     : Pixel = endcache()
  def center  : Pixel = start.midpoint(end)
  
  def centerOn(pixel: Pixel) {
    With.game.setScreenPosition(
      pixel.subtract(
        With.configuration.cameraViewportWidth  / 2,
        With.configuration.cameraViewportHeight / 2)
      .bwapi)
  }
  
  def contains(pixel: Pixel): Boolean = {
    pixel.x >= start.x  &&
    pixel.y >= start.y  &&
    pixel.x <= end.x    &&
    pixel.y <= end.y
  }
  
  def contains(tile: Tile): Boolean = {
    contains(tile.pixelCenter)
  }

  val rectangle: Cache[TileRectangle] = new Cache(() => TileRectangle(start.tileIncluding, end.tileIncluding))
  
  private val startCache  = new Cache[Pixel](() => startRecalculate)
  private val endcache    = new Cache[Pixel](() => endRecalculate)
  
  private def startRecalculate  : Pixel = new Pixel(With.game.getScreenPosition)
  private def endRecalculate    : Pixel = start.add(With.configuration.conservativeViewportWidth, With.configuration.conservativeViewportHeight)
}
