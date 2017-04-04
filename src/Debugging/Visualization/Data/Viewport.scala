package Debugging.Visualization.Data

import Performance.Caching.CacheFrame
import Lifecycle.With
import bwapi.Position

import Utilities.EnrichPosition._

class Viewport {
  def start:Position = startCache.get
  def end:Position = endcache.get
  private val startCache = new CacheFrame[Position](() => startRecalculate)
  private val endcache = new CacheFrame[Position](() => endRecalculate)
  private def startRecalculate:Position = With.game.getScreenPosition
  private def endRecalculate:Position   = With.game.getScreenPosition.add(2*640, 2*480)
}
