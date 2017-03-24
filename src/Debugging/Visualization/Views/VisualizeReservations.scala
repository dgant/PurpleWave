package Debugging.Visualization.Views

import Debugging.Visualization.Colors
import Debugging.Visualization.Rendering.DrawMap
import Planning.Plan
import Planning.Plans.Allocation.LockArea
import Startup.With

object VisualizeReservations {
  
  def render() = renderPlan(With.gameplan)
  
  def renderPlan(plan:Plan) {
    plan.getChildren.filter(_.isInstanceOf[LockArea]).foreach(lock => renderZone(plan, lock.asInstanceOf[LockArea]))
    plan.getChildren.foreach(renderPlan)
  }
  
  def renderZone(plan:Plan, lock:LockArea) {
    if ( ! lock.isComplete) return
    DrawMap.tileRectangle(lock.area, Colors.DarkRed)
    DrawMap.labelBox(
      List("Reserved by", plan.toString),
      lock.area.midpoint.toPosition,
      drawBackground = true,
      backgroundColor = Colors.DarkRed)
  }
}
