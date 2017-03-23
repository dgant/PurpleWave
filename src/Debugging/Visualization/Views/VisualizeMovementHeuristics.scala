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
    val minEvaluation = views.map(_.evaluation).min
    val maxEvaluation = views.map(_.evaluation).max
  
    if (minEvaluation == 0) {
      With.logger.warn("Heuristic multiplied to 0: " + views.head.heuristic.getClass.toString)
      return
    }
    if (minEvaluation == maxEvaluation) {
      return
    }
  
    val minScale = 1.1
    val maxScale = 1000.0
    val scale = maxEvaluation / minEvaluation
    val minRadius = Math.max(0.0,  3.0  * scale - 2.0)
    val maxRadius = Math.min(15.0, 15.0 * scale / maxScale)
    
    views.foreach(view => {
      
      // We want to offset the centerpoint slightly for each heuristic
      // so very discrete heuristics (especially booleans) don't completely ovelap
      val offsetX = (view.heuristic.color.hashCode)     % 5 - 2
      val offsetY = (view.heuristic.color.hashCode / 2) % 5 - 2
      
      // Use the radius to show which heuristics have the biggest spread of values, and where
      // Big spread: Max 15.0, min 3.0
      // Boolean spread: Max 6.0, min 0.0
      // Tiny spread: Max < 6.0, min 0.0
      val center = view.candidate.pixelCenter.add(offsetX, offsetY)
      val radius = minRadius + maxRadius * (view.evaluation - minEvaluation) / maxEvaluation
      if (radius > 0) {
        DrawMap.circle(center, radius.toInt, view.heuristic.color)
      }
    })
  }
}
