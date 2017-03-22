package Debugging.Visualization.Views

import Debugging.Visualization.Data.MovementHeuristicView
import Debugging.Visualization.Rendering.DrawMap
import Micro.Heuristics.HeuristicMath
import Startup.With

object VisualizeMovementHeuristics {
  
  var magnification = 1.0
  val denominator = Math.log(HeuristicMath.heuristicMaximum)
  
  def render() {
    With.movementHeuristicViews.cleanup()
    With.movementHeuristicViews.get.flatten.foreach(render)
  }
  
  def render(view:MovementHeuristicView) {
  
    val valueBase = view.heuristic.weigh(view.intent, view.candidate)
    val valueScale = if (valueBase >= 1.0) valueBase else 1.0 / valueBase
    
    if (valueScale <= 1.0) {
      return
    }
    
    val center = view.intent.unit.tileCenter
    val circleRadius = 15.0 * magnification * Math.log(valueScale) / denominator
    
    if (circleRadius > 1.0) {
      DrawMap.circle(view.candidate.toPosition, circleRadius.toInt, view.heuristic.color)
    }
  }
}
