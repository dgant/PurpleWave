package Debugging.Visualizations

import Lifecycle.With
import Mathematics.Points.{Pixel, PixelRectangle, Tile, TileRectangle}
import Performance.Cache

class Viewport {
  
  def start   : Pixel = startCache()
  def end     : Pixel = endcache()
  def center  : Pixel = start.midpoint(end)
  def area    : PixelRectangle = rectangleCache()
  
  def centerOn(pixel: Pixel) {
    With.game.setScreenPosition(
      pixel.subtract(
        With.configuration.cameraViewportWidth  / 2,
        With.configuration.cameraViewportHeight / 2)
      .bwapi)
  }
  
  def contains(pixel: Pixel, buffer: Int = 0): Boolean = {
    pixel.x + buffer >= start.x  &&
    pixel.y + buffer >= start.y  &&
    pixel.x - buffer <= end.x    &&
    pixel.y - buffer <= end.y
  }
  
  def contains(tile: Tile): Boolean = {
    contains(tile.center)
  }

  val rectangleTight: Cache[TileRectangle] = new Cache(() => TileRectangle(start.tile, start.add(640, 440).tile))
  
  private val startCache  = new Cache(() => new Pixel(With.game.getScreenPosition))
  private val endcache    = new Cache(() => start.add(With.configuration.conservativeViewportWidth, With.configuration.conservativeViewportHeight))
  private val rectangleCache = new Cache(() => PixelRectangle(start, end))
}
