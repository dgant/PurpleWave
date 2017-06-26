package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Heuristics.Heuristic
import Mathematics.Points.Tile
import bwapi.Color

object MapArchitecture {
  
  def render() {
    With.architect.exclusions.foreach(exclusion => {
      DrawMap.tileRectangle(exclusion.areaExcluded, Colors.MediumRed)
      DrawMap.label(exclusion.description, exclusion.areaExcluded.midPixel)
    })
    
    With.groundskeeper
      .sortByPriority(With.groundskeeper.placed.keys)
      .headOption
      .foreach(renderPlacement)
  }
  
  def renderPlacement(descriptor: BuildingDescriptor) {
    val placement = With.groundskeeper.placed(descriptor)
    
    val heuristicRanges = placement
      .evaluations
      .groupBy(_.heuristic)
      .map(pair =>
        (
          pair._1,
          HeuristicRange(
            pair._2.head.heuristic,
            pair._2.head.color,
            min = pair._2.map(_.evaluation).min,
            max = pair._2.map(_.evaluation).max)
        )
      )
    
    placement.evaluations
      .filter(evaluation => With.viewport.contains(evaluation.candidate))
      .foreach(evaluation => {
        val range = heuristicRanges(evaluation.heuristic)
        DrawMap.circle(
          evaluation.candidate.pixelCenter,
          (32 * (evaluation.evaluation - range.min) / range.max).toInt,
          range.color)
      })
  }
  
  private case class HeuristicRange(
    heuristic : Heuristic[BuildingDescriptor, Tile],
    color     : Color,
    min       : Double,
    max       : Double)
}
