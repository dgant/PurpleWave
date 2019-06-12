package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Macro.Architecture.{Blueprint, Placement}
import Mathematics.Points.Pixel
import bwapi.Color

object ShowArchitectureHeuristics extends View {
  
  override def renderScreen() {
    blueprintToRender.foreach(blueprint =>
      placementToRender(blueprint).foreach(placement =>
      renderPlacementHeuristicsScreen(blueprint, placement)))
  }
  
  override def renderMap() {
    blueprintToRender.foreach(blueprint =>
      placementToRender(blueprint).foreach(placement =>
        renderPlacementHeuristicsMap(blueprint, placement)))
  }

  private def blueprintToRender: Option[Blueprint] = {
    None
  }

  private def placementToRender(blueprint: Blueprint): Option[Placement] = {
    None
  }
  
  private def renderPlacementHeuristicsScreen(blueprint: Blueprint, placement: Placement) = {
    With.game.setTextSize(bwapi.Text.Size.Enum.Default)
    DrawScreen.column(
      5,
      5 * With.visualization.lineHeightSmall,
      List(
        List(blueprint.toString),
        placement.scoresByTile.toList
          .sortBy(_._2)
          .take(5)
          .zipWithIndex
          .map(pair => "#" + pair._2 + " " + pair._1._1 + " (" + (-pair._1._2) + ")"))
        .flatten)
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
  
  private def renderPlacementHeuristicsMap(blueprint: Blueprint, placement: Placement) = {
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
        drawBlueprint(
          blueprint,
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
        drawBlueprint(
          blueprint,
          Colors.White,
          pair._1.topLeftPixel,
          pair._2,
          scoreMin,
          scoreMax))
  }
  
  private def drawBlueprint(
    building  : Blueprint,
    color     : Color,
    pixel     : Pixel,
    value     : Double,
    min       : Double,
    max       : Double) {
    DrawMap.circle(
      pixel.add(
        16 * building.widthTiles.get,
        16 * building.heightTiles.get),
      (16 * (value - min) / max).toInt,
      color)
  }
  
  private case class HeuristicRange(
    color : Color,
    min   : Double,
    max   : Double)
}
