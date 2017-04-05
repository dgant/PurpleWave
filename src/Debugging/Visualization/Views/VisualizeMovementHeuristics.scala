package Debugging.Visualization.Views

import Debugging.Visualization.Rendering.DrawMap
import Lifecycle.With
import Micro.Heuristics.MovementHeuristics.MovementHeuristicResult
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.EnrichPosition._

object VisualizeMovementHeuristics {
  
  def render() {
    
    var focus:Iterable[FriendlyUnitInfo] = With.units.ours.filter(unit => unit.selected && eligible(unit))
    
    if (focus.isEmpty) {
      focus = With.executor.states
        .filter(state => state.movementHeuristicResults.nonEmpty && eligible(state.unit))
        .map(_.unit)
        .headOption
    }
    
    focus.foreach(unit => renderUnit(With.executor.getState(unit).movementHeuristicResults))
  }
  
  private def eligible(unit:FriendlyUnitInfo):Boolean = {
    unit.alive && With.viewport.contains(unit.pixelCenter)
  }
  
  def renderUnit(results:Iterable[MovementHeuristicResult]) {
    if (results.isEmpty) return
    
    val heuristicGroups = results.groupBy(_.heuristic)
    val scales = heuristicGroups.map(group => scale(group._2))
    val maxScale = scales.max
    if (maxScale == 1.0) {
      return
    }
    heuristicGroups.foreach(group => renderUnitHeuristic(group._2, maxScale))
  }
  
  def scale(results:Iterable[MovementHeuristicResult]):Double = {
    val rawScale = results.map(_.evaluation).max / results.map(_.evaluation).min
    return normalize(rawScale)
  }
  
  def normalize(value:Double):Double = if (value < 1.0) 1.0/value else value
  
  def renderUnitHeuristic(results:Iterable[MovementHeuristicResult], maxScale:Double) {
    val ourScale = scale(results)
    if (ourScale <= 1.0) return
  
    //Draw line to best tile(s)
    val bestEvaluation = results.map(view => normalize(view.evaluation)).max
    results
      .filter(view => normalize(view.evaluation) == bestEvaluation)
      .foreach(bestView =>
        DrawMap.line(
          bestView.intent.unit.pixelCenter,
          bestView.candidate.pixelCenter,
          bestView.color))
    
    val relativeScale = (ourScale - 1.0) / (maxScale - 1.0)
    val minRadius = Math.min(3.0, relativeScale)
    val radiusMultiplier = Math.min(12.0, 12.0 * relativeScale / results.map(view => normalize(view.evaluation)).max)
    
    results.foreach(result => {
      
      // We want to offset the centerpoint slightly for each heuristic
      // so very discrete heuristics (especially booleans) don't completely ovelap
      val offsetX = (result.color.hashCode)     % 5 - 2
      val offsetY = (result.color.hashCode / 2) % 5 - 2
      
      // Use the radius to show which heuristics have the biggest spread of values, and where
      // Big spread: Max 15.0, min 3.0
      // Boolean spread: Max 6.0, min 0.0
      // Tiny spread: Max < 6.0, min 0.0
      val center = result.candidate.pixelCenter.add(offsetX, offsetY)
      val radius = minRadius + radiusMultiplier * normalize(result.evaluation)
      if (radius > 1.0) {
        DrawMap.circle(center, radius.toInt, result.color)
      }
    })
  }
}
