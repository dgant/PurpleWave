package Debugging.Visualization.Views

import Debugging.Visualization.Data.MovementHeuristicView
import Debugging.Visualization.Rendering.DrawMap
import Startup.With
import Utilities.EnrichPosition._

import scala.collection.mutable.ListBuffer

object VisualizeMovementHeuristics {
  
  def render() {
    With.movementHeuristicViews.cleanup()
    With.movementHeuristicViews.get.foreach(renderUnit)
  }
  
  def renderUnit(views:ListBuffer[MovementHeuristicView]) {
    views.groupBy(_.heuristic).foreach(group => renderUnitHeuristic(group._2))
  }
  
  def renderUnitHeuristic(views:ListBuffer[MovementHeuristicView]) {
    val maxEvaluation = views.map(_.evaluation).max
    if (maxEvaluation == 0) {
      return
    }
    
    views.foreach(view => {
      
      // We want to offset the centerpoint slightly for each heuristic
      // so very discrete heuristics (especially booleans) don't completely ovelap
      
      val offsetX = (view.heuristic.color.hashCode)     % 5 - 2
      val offsetY = (view.heuristic.color.hashCode / 2) % 5 - 2
      val center = view.candidate.pixelCenter.add(offsetX, offsetY)
      val radius = 14.0 * view.evaluation / maxEvaluation
  
      DrawMap.circle(center, radius.toInt, view.heuristic.color)
    })
  }
}
