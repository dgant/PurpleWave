package Debugging.Visualizations

import Lifecycle.With
import Mathematics.Points.{Pixel, PixelRectangle, Tile, TileRectangle}
import Performance.Cache

class Viewport {
  
  def start       : Pixel = _start()
  def end         : Pixel = _end()
  def center      : Pixel = start.midpoint(end)
  def areaPixels  : PixelRectangle  = _areaPixels()
  def areaTiles   : TileRectangle   = _areaTiles()

  private val _start      = new Cache(() => new Pixel(With.game.getScreenPosition))
  private val _end        = new Cache(() => start.add(With.configuration.conservativeViewportWidth, With.configuration.conservativeViewportHeight))
  private val _areaPixels = new Cache(() => PixelRectangle(start, end))
  private val _areaTiles  = new Cache(() => TileRectangle(start.tile, end.add(31, 31).tile))
  
  def centerOn(pixel: Pixel): Unit = {
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
}
