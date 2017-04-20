package Debugging.Visualizations

import Lifecycle.With
import Mathematics.Pixels.Pixel
import Performance.Caching.CacheFrame

class Viewport {
  
  def start   : Pixel = startCache.get
  def end     : Pixel = endcache.get
  def center  : Pixel = start.midpoint(end)
  
  def centerOn(pixel:Pixel) {
    With.game.setScreenPosition(
      pixel.subtract(
        With.configuration.cameraViewportWidth  / 2,
        With.configuration.cameraViewportHeight / 2)
      .bwapi)
  }
  
  def contains(pixel:Pixel):Boolean = {
    pixel.x >= start.x  &&
    pixel.y >= start.y  &&
    pixel.x <= end.x    &&
    pixel.y <= end.y
  }
  
  private val startCache  = new CacheFrame[Pixel](() => startRecalculate)
  private val endcache    = new CacheFrame[Pixel](() => endRecalculate)
  
  private def startRecalculate  : Pixel = new Pixel(With.game.getScreenPosition)
  private def endRecalculate    : Pixel = start.add(With.configuration.conservativeViewportWidth, With.configuration.conservativeViewportHeight)
}
