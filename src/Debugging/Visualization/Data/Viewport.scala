package Debugging.Visualization.Data

import Performance.Caching.CacheFrame
import Lifecycle.With
import bwapi.Position

import Utilities.EnrichPosition._

class Viewport {
  
  def start : Position = startCache.get
  def end   : Position = endcache.get
  
  def contains(pixel:Position):Boolean = {
    pixel.getX >= start.getX &&
    pixel.getY >= start.getY &&
    pixel.getX <= end.getX &&
    pixel.getY <= end.getY
  }
  
  private val startCache  = new CacheFrame[Position](() => startRecalculate)
  private val endcache    = new CacheFrame[Position](() => endRecalculate)
  
  private def startRecalculate  : Position = With.game.getScreenPosition
  private def endRecalculate    : Position = start.add(With.configuration.viewportWidth, With.configuration.viewportHeight)
}
