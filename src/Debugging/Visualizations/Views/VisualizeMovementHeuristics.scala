package Debugging.Visualizations.Views

import Debugging.Visualizations.Rendering.DrawMap
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMath
import Mathematics.Pixels.Pixel
import Micro.Heuristics.MovementHeuristics.MovementHeuristicResult
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

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
  
  private def eligible(unit:FriendlyUnitInfo):Boolean =
    unit.alive &&
      (With.frame - With.executor.getState(unit).movedHeuristicallyFrame) < 48 &&
      With.viewport.contains(unit.pixelCenter)
  
  def scale(results:Iterable[MovementHeuristicResult]):Double =
    normalize(results.map(_.evaluation).max / results.map(_.evaluation).min)
  
  def normalize(value:Double):Double = if (value < 1.0) 1.0/value else value
  
  def renderUnit(results:Iterable[MovementHeuristicResult]) {
    if (results.isEmpty) return
    
    val heuristicGroups = results.groupBy(_.heuristic)
    val scales = heuristicGroups.map(group => scale(group._2))
    val maxScale = scales.max
    if (maxScale == HeuristicMath.default) return
    val activeGroups = heuristicGroups.map(_._2).filterNot(group => group.forall(_.evaluation == group.head.evaluation))
    activeGroups.foreach(group => renderUnitHeuristic(group, maxScale))
    renderLegend(activeGroups)
  }
  
  def renderUnitHeuristic(results:Iterable[MovementHeuristicResult], maxScale:Double) {
    val ourScale          = scale(results)
    val relativeScale     = ourScale / maxScale
    val evaluationExtreme = results.maxBy(result => Math.abs(HeuristicMath.default - result.evaluation)).evaluation
    
    results
      .filter(_.evaluation != HeuristicMath.default)
      .foreach(result => {
        // We want to offset the centerpoint slightly for each heuristic
        // so very discrete heuristics (especially booleans) don't completely ovelap
        val offsetX = (result.color.hashCode)     % 3 - 1
        val offsetY = (result.color.hashCode / 3) % 3 - 1
        
        val center = result.candidate.pixelCenter.add(offsetX, offsetY)
        val radius = 3 + (12.0 * relativeScale * normalize(result.evaluation) / normalize(evaluationExtreme)).toInt
        if (result.evaluation > 1.0) {
          DrawMap.circle(center, radius.toInt, result.color)
        }
        else if (result.evaluation < 1.0) {
          DrawMap.line(
            center.add(-radius, -radius),
            center.add(radius, radius),
            result.color)
          DrawMap.line(
            center.add(radius, -radius),
            center.add(-radius, radius),
            result.color)
        }
      })
  }
  
  def renderLegend(groups:Iterable[Iterable[MovementHeuristicResult]]) {
    val descendingScale = groups
      .toVector
      .sortBy(scale)
      .reverse
    
    descendingScale.zipWithIndex.foreach { case (group, index) => renderLegendKey(group, index) }
  }
  
  def renderLegendKey(group:Iterable[MovementHeuristicResult], order:Int) {
    val left = 5
    val top = 240
    val rowHeight = 11
    val rowMargin = 2
    val boxStart = new Pixel(left, top + (rowHeight + rowMargin) * order)
    With.game.drawBoxScreen(
      boxStart.bwapi,
      boxStart.add(rowHeight, rowHeight).bwapi,
      group.head.color,
      true)
    With.game.drawTextScreen(
      boxStart.add(rowHeight + 4, 0).bwapi,
      group.head.heuristic.getClass.getSimpleName.replace("MovementHeuristic", "").replace("$", ""))
  }
}
