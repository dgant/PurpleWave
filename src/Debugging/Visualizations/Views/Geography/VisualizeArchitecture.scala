package Debugging.Visualizations.Views.Geography

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.{DrawMap, DrawScreen}
import Lifecycle.With
import Macro.Architecture.{Blueprint, Placement}
import Mathematics.Points.Pixel
import bwapi.Color

object VisualizeArchitecture {
  
  def render() {
    
    With.architecture.exclusions.foreach(exclusion => {
      DrawMap.tileRectangle(exclusion.areaExcluded, Colors.MediumRed)
      DrawMap.label(exclusion.description, exclusion.areaExcluded.midPixel)
    })
    
    With.architecture.unwalkable.foreach(tile => DrawMap.box(
      tile.topLeftPixel.add(8, 8),
      tile.topLeftPixel.add(24, 24),
      Colors.MediumOrange,
      solid = false))
  
    With.architecture.unbuildable.foreach(tile => DrawMap.box(
      tile.topLeftPixel.add(4, 4),
      tile.topLeftPixel.add(28, 28),
      Colors.MediumTeal,
      solid = false))
    
    With.architecture.existingPaths
      .values
      .filter(_.path.isDefined)
      .foreach(pathCache => {
        val path = pathCache.path.get
        DrawMap.circle(path.start.pixelCenter,  16, Colors.BrightYellow, solid = false)
        DrawMap.circle(path.end.pixelCenter,    16, Colors.BrightYellow, solid = false)
        path.tiles.foreach(tileList => {
          var lastTile = path.end
          tileList.drop(1).foreach(tile => {
            DrawMap.arrow(lastTile.pixelCenter, tile.pixelCenter, Colors.BrightYellow)
            lastTile = tile
          })
        })
      })
    
    With.groundskeeper.proposalPlacements.keys
      .toVector
      .sortBy(_.proposer.priority)
      .headOption
      .foreach(renderPlacement)
  }
  
  def renderPlacement(descriptor: Blueprint) {
    val placement = With.groundskeeper.proposalPlacements(descriptor)
    if (placement.tile.isEmpty || placement.scoresByTile.isEmpty) return
  
    //renderPlacementHeuristics(descriptor, placement)
    //renderPlacementList(descriptor, placement)
  }
  
  private def renderPlacementList(descriptor: Blueprint, placement: Placement) = {
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
          .map(pair => "#" + pair._2 + " " + pair._1._1 + " (" + (-pair._1._2) + ")"))
        .flatten)
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
  
  private def renderPlacementHeuristics(descriptor: Blueprint, placement: Placement) = {
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
  }
  
  private def draw(
    building  : Blueprint,
    color     : Color,
    pixel     : Pixel,
    value     : Double,
    min       : Double,
    max       : Double
  ) {
    // Temporarily disabled
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
