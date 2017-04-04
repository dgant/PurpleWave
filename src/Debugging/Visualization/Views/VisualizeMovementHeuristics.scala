package Debugging.Visualization.Views

import Debugging.Visualization.Data.MovementHeuristicView
import Debugging.Visualization.Rendering.DrawMap
import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.EnrichPosition._

object VisualizeMovementHeuristics {
  
  def render() {
    var highlightUnit:Option[FriendlyUnitInfo] = None
    
    if ( ! With.units.ours.exists(_.selected))
      
    highlightUnit = With.executor.states
      .find(state => With.viewport.contains(state.unit.pixelCenter))
      .map(_.unit)
    
    With.executor.states
      .filter(state => (highlightUnit.contains(state.unit) || state.unit.selected) && state.movementHeuristics.nonEmpty)
      .foreach(state => renderUnit(state.movementHeuristics))
  }
  
  def renderUnit(views:Iterable[MovementHeuristicView]) {
    val heuristicGroups = views.groupBy(_.heuristic)
    
    val scales = heuristicGroups.map(group => scale(group._2))
    val maxScale = scales.max
    if (maxScale == 1.0) {
      return
    }
    heuristicGroups.foreach(group => renderUnitHeuristic(group._2, maxScale))
  }
  
  def scale(views:Iterable[MovementHeuristicView]):Double = {
    val rawScale = views.map(_.evaluation).max / views.map(_.evaluation).min
    return normalize(rawScale)
  }
  
  def normalize(value:Double):Double = if (value < 1.0) 1.0/value else value
  
  def renderUnitHeuristic(views:Iterable[MovementHeuristicView], maxScale:Double) {
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
