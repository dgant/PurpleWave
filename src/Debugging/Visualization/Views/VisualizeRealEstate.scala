package Debugging.Visualization.Views

import Debugging.Visualization.Colors
import Debugging.Visualization.Rendering.DrawMap
import Planning.Composition.ResourceLocks.LockArea
import Startup.With

object VisualizeRealEstate {
  
  def render() = With.realEstate.requests.foreach(renderArea)
  
  def renderArea(lock:LockArea) {
    if ( ! lock.satisfied) return
    DrawMap.tileRectangle(lock.area, Colors.DarkRed)
    DrawMap.labelBox(
      List("Reserved by", lock.owner.toString),
      lock.area.midpoint.toPosition,
      drawBackground = true,
      backgroundColor = Colors.DarkRed)
  }
}
