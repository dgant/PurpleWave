package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Lifecycle.With
import Macro.Architecture.BuildingDescriptor
import Mathematics.Points.Pixel
import bwapi.Color

object VisualizeArchitecture {
  
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
    
    if (placement.tile.isEmpty || placement.scoresByTile.isEmpty) return
    
    val heuristicRanges = placement
      .evaluations
      .groupBy(_.heuristic)
      .map(pair =>
        (
          pair._1,
          HeuristicRange(
            pair._2.head.color,
            min = pair._2.map(_.evaluation).min,
            max = pair._2.map(_.evaluation).max)
        )
      )
    
    placement.evaluations
      .filter(evaluation => With.viewport.contains(evaluation.candidate))
      .foreach(evaluation => {
        val range = heuristicRanges(evaluation.heuristic)
        draw(
          descriptor,
          evaluation.color,
          evaluation.candidate.topLeftPixel,
          evaluation.evaluation,
          range.min,
          range.max)
      })
  
    val scoreMin = placement.scoresByTile.values.min
    val scoreMax = placement.scoresByTile.values.max
    placement.scoresByTile
      .filter(pair => With.viewport.contains(pair._1))
      .foreach(pair =>
        draw(
          descriptor,
          Colors.White,
          pair._1.topLeftPixel,
          pair._2,
          scoreMin,
          scoreMax))
  
    With.game.setTextSize(bwapi.Text.Size.Enum.Default)
    DrawScreen.column(
      5,
      5,
      List(
        List(descriptor.toString),
        placement.scoresByTile.toList
          .sortBy(_._2)
          .take(5)
          .zipWithIndex
          .map(pair =>"#" + pair._2 + " " + pair._1._1 + " (" + ( - pair._1._2) + ")"))
      .flatten)
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
  
  private def draw(
    building  : BuildingDescriptor,
    color     : Color,
    pixel     : Pixel,
    value     : Double,
    min       : Double,
    max       : Double
  ) {
    DrawMap.circle(
      pixel.add(
        16 * building.width,
        16 * building.height),
        (16 * (value - min) / max).toInt,
      color)
  }
  
  private case class HeuristicRange(
    color : Color,
    min   : Double,
    max   : Double)
}
