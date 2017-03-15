package Debugging.Visualization

import Planning.Plans.Allocation.{LockCurrency, LockUnits}
import Planning.Plan
import Startup.With

object VisualizePlans {
  def render() {
    DrawScreen.header(5, _describePlanTree(With.gameplan, 0, 0))
    _drawPlansRecursively(With.gameplan)
  }
  
  def _drawPlansRecursively(plan:Plan) {
    plan.drawOverlay()
    plan.getChildren.foreach(_drawPlansRecursively)
  }
  
  def _describePlanTree(plan:Plan, childOrder:Integer, depth:Integer):String = {
    if (_isRelevant(plan)) {
      (_describePlan(plan, childOrder, depth)
        ++ plan.getChildren.zipWithIndex.map(x => _describePlanTree(x._1, x._2, depth + 1)))
        .mkString("")
    } else ""
  }
  
  def _describePlan(plan:Plan, childOrder:Integer, depth:Integer):String = {
    val checkbox = if (plan.isComplete) "X " else "  "
    val spacer = "  " * depth
    val leftColumn =
      (checkbox
        ++ spacer
        ++ "#"
        ++ (childOrder + 1).toString
        ++ " "
        ++ plan.toString)
    
    leftColumn + " " * Math.max(0, 45 - leftColumn.length) + "\n"
  }
  
  def _isRelevant(plan:Plan):Boolean = {
    plan.getChildren.exists(child => _isRelevant(child) || ((child.isInstanceOf[LockCurrency] || child.isInstanceOf[LockUnits]) && child.isComplete))
  }
}
