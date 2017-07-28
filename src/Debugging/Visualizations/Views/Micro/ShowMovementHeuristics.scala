package Debugging.Visualizations.Views.Micro

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.View
import Lifecycle.With
import Mathematics.Heuristics.HeuristicMathMultiplicative
import Mathematics.Points.Pixel
import Micro.Heuristics.Movement.MovementHeuristicEvaluation
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object ShowMovementHeuristics extends View {
  
  override def renderMap() {
    
    var focus: Iterable[FriendlyUnitInfo] = With.units.ours.filter(unit => unit.selected && eligible(unit))
    
    if (focus.isEmpty) {
      focus = With.agents.states
        .filter(state => state.movementHeuristicResults.nonEmpty && eligible(state.unit))
        .map(_.unit)
        .headOption
    }
    
    focus.foreach(unit => renderUnit(unit.agent.movementHeuristicResults))
  }
  
  private def eligible(unit: FriendlyUnitInfo): Boolean =
    unit.aliveAndComplete &&
      With.framesSince(With.agents.getState(unit).movedHeuristicallyFrame) < 24 &&
      With.viewport.contains(unit.pixelCenter)
  
  def scale(results: Iterable[MovementHeuristicEvaluation]): Double =
    normalize(results.map(_.evaluation).max / results.map(_.evaluation).min)
  
  def normalize(value: Double): Double = if (value < 1.0) 1.0/value else value
  
  def renderUnit(results: Iterable[MovementHeuristicEvaluation]) {
    if (results.isEmpty) return
    
    val heuristicGroups = results.groupBy(_.heuristic)
    val scales = heuristicGroups.map(group => scale(group._2))
    val maxScale = scales.max
    if (maxScale == HeuristicMathMultiplicative.default) return
    val activeGroups = heuristicGroups.values.filterNot(group => group.forall(_.evaluation == group.head.evaluation))
    activeGroups.foreach(group => renderUnitHeuristic(group, maxScale))
    renderLegend(activeGroups)
    val unit = results.head.context.unit
    DrawMap.circle(unit.pixelCenter, (unit.pixelRangeMax + unit.unitClass.radialHypotenuse).toInt, Colors.BrightOrange)
    DrawMap.circle(unit.pixelCenter, With.configuration.battleMarginPixels.toInt, Colors.BrightRed)
  }
  
  def renderUnitHeuristic(results: Iterable[MovementHeuristicEvaluation], maxScale: Double) {
    val ourScale          = scale(results)
    val relativeScale     = ourScale / maxScale
    val evaluationExtreme = results.maxBy(result => Math.abs(HeuristicMathMultiplicative.default - result.evaluation)).evaluation
    
    if (ourScale == maxScale) {
      val context = results.head.context
      DrawMap.line(context.unit.pixelCenter, context.movingTo.get, Colors.White)
    }
    
    results
      .filter(_.evaluation != HeuristicMathMultiplicative.default)
      .foreach(result => {
        
        // We want to offset the centerpoint slightly for each heuristic
        // so very discrete heuristics (especially booleans) don't completely ovelap
        
        val offsetX = (result.color.hashCode)     % 3 - 1
        val offsetY = (result.color.hashCode / 3) % 3 - 1
        
        val center = result.candidate.add(offsetX, offsetY)
        val radius = 1 + (3.0 * relativeScale * normalize(result.evaluation) / normalize(evaluationExtreme)).toInt
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
  
  def renderLegend(groups: Iterable[Iterable[MovementHeuristicEvaluation]]) {
    val descendingScale = groups
      .toVector
      .sortBy(scale)
      .reverse
    
    descendingScale.zipWithIndex.foreach { case (group, index) => renderLegendKey(group, index) }
  }
  
  def renderLegendKey(group: Iterable[MovementHeuristicEvaluation], order: Int) {
    val left = 5
    val top = 5 * With.visualization.lineHeightSmall
    val rowHeight = 15
    val rowMargin = 2
    val boxStart = Pixel(left, top + (rowHeight + rowMargin) * order)
    With.game.drawBoxScreen(
      boxStart.bwapi,
      boxStart.add(rowHeight, rowHeight).bwapi,
      group.head.color,
      true)
    With.game.setTextSize(bwapi.Text.Size.Enum.Default)
    With.game.drawTextScreen(
      boxStart.add(rowHeight + 4, 0).bwapi,
      group.head.heuristic.getClass.getSimpleName.replace("MovementHeuristic", "").replace("$", ""))
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
  }
}
