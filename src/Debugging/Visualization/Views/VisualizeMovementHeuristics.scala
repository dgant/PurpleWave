package Debugging.Visualization.Views

import Debugging.Visualization.Colors
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
    views.head.intent.destination.foreach(destination => {
      DrawMap.circle(destination.pixelCenter, 32, Colors.MediumGreen)
      DrawMap.line(destination.pixelCenter, views.head.intent.unit.pixelCenter, Colors.MediumGreen)
      DrawMap.label("Destination", destination.pixelCenter.add(0, 32))
      
    })
    views.head.intent.toAttack.foreach(attackee => {
      DrawMap.circle(attackee.pixelCenter, 32, Colors.MediumOrange)
      DrawMap.line(attackee.pixelCenter, views.head.intent.unit.pixelCenter, Colors.MediumOrange)
      DrawMap.label("Target", attackee.pixelCenter.add(0, 32))
    })
    
    val heuristicGroups = views.groupBy(_.heuristic)
    
    val scales = heuristicGroups.map(group => scale(group._2))
    val maxScale = scales.max
    if (maxScale == 1.0) {
      return
    }
    heuristicGroups.foreach(group => renderUnitHeuristic(group._2, maxScale))
  }
  
  def scale(views:ListBuffer[MovementHeuristicView]):Double = {
    val rawScale = views.map(_.evaluation).max / views.map(_.evaluation).min
    return normalize(rawScale)
  }
  
  def normalize(value:Double):Double = if (value < 1.0) 1.0/value else value
  
  def renderUnitHeuristic(views:ListBuffer[MovementHeuristicView], maxScale:Double) {
    val ourScale = scale(views)
    if (ourScale <= 1.0) return
  
    //Draw line to best tile(s)
    val bestEvaluation = views.map(view => normalize(view.evaluation)).max
    views
      .filter(view => normalize(view.evaluation) == bestEvaluation)
      .foreach(bestView =>
        DrawMap.line(
          bestView.intent.unit.pixelCenter,
          bestView.candidate.pixelCenter,
          bestView.heuristic.color))
    
    val relativeScale = (ourScale - 1.0) / (maxScale - 1.0)
    val minRadius = Math.min(3.0, relativeScale)
    val radiusMultiplier = Math.min(12.0, 12.0 * relativeScale / views.map(view => normalize(view.evaluation)).max)
    
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
      val radius = minRadius + radiusMultiplier * normalize(view.evaluation)
      if (radius > 1.0) {
        DrawMap.circle(center, radius.toInt, view.heuristic.color)
      }
    })
  }
}
