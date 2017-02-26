package Development

import Global.Information.Combat.CombatSimulation
import Plans.Allocation.{LockCurrency, LockUnits}
import Plans.Plan
import Startup.With
import Types.UnitInfo.EnemyUnitInfo
import bwapi.{Color, Position, UnitCommandType}
import Utilities.Enrichment.EnrichUnit._
import bwta.BWTA

import scala.collection.JavaConverters._

object Overlay {
  
  var enabled:Boolean = true
  
  def onFrame() {
    if (!enabled) { return }
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    _drawTerrain()
    _drawEconomy
    _drawUnits()
    With.game.drawTextScreen(5, 5, _describePlanTree(With.gameplan, 0, 0))
    _drawTrackedUnits()
    _drawResources()
    _drawPlans(With.gameplan)
    
    //Buggy because it references old units
    //_drawCombatSimulations
  }
  
  def _drawTextLabel(
    textLines:Iterable[String],
    position:Position,
    drawBackground:Boolean = false) {
    val horizontalMargin = 2
    val estimatedTextWidth = (9 * textLines.map(_.size).max) / 2
    val boxWidth = estimatedTextWidth + 2 * horizontalMargin
    val boxHeight = 11 * textLines.size
    val textX = position.getX - boxWidth/2
    val textY = position.getY - boxHeight/2
    val boxX = textX - horizontalMargin
    val boxY = textY
  
    if (drawBackground) {
      With.game.drawBoxMap(
        boxX,
        boxY,
        boxX + boxWidth,
        boxY + boxHeight,
        bwapi.Color.Grey,
        true) //isSolid
    }
    With.game.drawTextMap(
      textX,
      textY,
      textLines.mkString("\n"))
  }
  
  def _drawUnits() {
    With.ourUnits
      .filter(unit => Debugger.highlitUnits.contains(unit))
      .foreach(unit =>
      With.game.drawCircleMap(unit.getPosition, 32, bwapi.Color.Orange))
    With.ourUnits
      .filterNot(_.getLastCommand.getUnitCommandType == UnitCommandType.None)
      .foreach(unit => _drawTextLabel(
        List(unit.getLastCommand.getUnitCommandType.toString),
        unit.getPosition,
        drawBackground = true))
  }
  
  def _describePlanTree(plan:Plan, childOrder:Integer, depth:Integer):String = {
    if (_isRelevant(plan)) {
      (_describePlan(plan, childOrder, depth)
        ++ plan.getChildren.zipWithIndex.map(x => _describePlanTree(x._1, x._2, depth + 1)))
        .mkString("")
    } else {
      ""
    }
  }
  
  def _describePlan(plan:Plan, childOrder:Integer, depth:Integer):String = {
    val checkbox = if (plan.isComplete) "X " else "  "
    val spacer = "  " * depth
    val leftColumn =
    (checkbox
      ++ spacer
      ++ "#"
      ++ (childOrder + 1).toString
      ++ " "
      ++ plan.toString)
    
    leftColumn + " " * Math.max(0, 45 - leftColumn.length) + "\n"
  }
  
  def _isRelevant(plan:Plan):Boolean = {
    if (plan.isComplete) {
      return plan.isInstanceOf[LockCurrency] || plan.isInstanceOf[LockUnits]
    }
    
    plan.getChildren.exists(_isRelevant(_))
  }
  
  def _drawTerrain() {
    var i = 0
    With.geography.ourHarvestingAreas.foreach(area => With.game.drawBoxMap(
      area.start.toPosition,
      area.end.toPosition,
      Color.Red))
    BWTA.getBaseLocations.asScala.foreach(base => {
      val label = (if (base.isStartLocation) "Start location" else "Expansion") +
        " #" + i
        "\n" +
        (if (With.game.isExplored(base.getTilePosition)) "Explored" else "Unexplored")
      With.game.drawCircleMap(base.getPosition, 80, Color.Blue)
      _drawTextLabel(
        List(label),
        base.getPosition,
        drawBackground = false)
      i += 1
    })
    BWTA.getRegions.asScala .foreach(region => {
  
        _drawPolygonPositions(region.getPolygon.getPoints.asScala)
        
        With.game.drawLineMap(
          region.getPolygon.getPoints.asScala.head,
          region.getPolygon.getPoints.asScala.last,
          bwapi.Color.Brown)
        
        With.game.drawTextMap(
          region.getCenter,
          region.getCenter.toString ++
          "\n" ++
          region.getCenter.toTilePosition.toString)
        
        region.getChokepoints.asScala.foreach(
          choke => {
            With.game.drawLineMap(choke.getSides.first, choke.getSides.second, bwapi.Color.Purple)
            With.game.drawTextMap(
              choke.getCenter,
              choke.getCenter.toString ++
              "\n" ++
              choke.getCenter.toTilePosition.toString)
          })
      }
    )
  }
  
  def _drawPolygonPositions(points:Iterable[Position], color:bwapi.Color = bwapi.Color.Brown) {
    points.reduce((p1, p2) => { With.game.drawLineMap(p1, p2, color); p2 })
    With.game.drawLineMap(points.head, points.last, color)
  }
  
  def _drawPlans(plan:Plan) {
    plan.drawOverlay()
    plan.getChildren.foreach(_drawPlans)
  }
  
  def _drawResources() {
    With.game.drawTextScreen(
      305,
      5,
      
      With.bank.getPrioritizedRequests
        .take(8)
        .map(r =>
          (if (r.isSatisfied) "X " else "  ") ++
          (if (r.minerals > 0)  r.minerals  .toString ++ "m " else "") ++
          (if (r.gas > 0)       r.gas       .toString ++ "g " else "") ++
          (if (r.supply > 0)    r.supply    .toString ++ "s " else ""))
        .mkString("\n"))
  }
  
  def _drawEconomy() {
    val labels = List(
      "Active miners:",
      "Active drillers:",
      "Minerals per minute:",
      "Gas per minute:",
      "Total minerals (est.):",
      "Total gas (est.):",
      "Total minerals (real):",
      "Total gas (real):"
    )
    val values = List(
      With.economy.ourActiveMiners.size,
      With.economy.ourActiveDrillers.size,
      With.economy.ourMineralIncomePerMinute,
      With.economy.ourGasIncomePerMinute,
      With.economy.ourEstimatedTotalMinerals.toInt,
      With.economy.ourEstimatedTotalGas.toInt,
      With.economy.ourActualTotalMinerals,
      With.economy.ourActualTotalGas
    )
    With.game.drawTextScreen(450, 5, labels.mkString("\n"))
    With.game.drawTextScreen(550, 5, values.mkString("\n"))
  }
  
  def _drawTrackedUnits() {
    With.memory.knownEnemyUnits.foreach(_drawTrackedUnit)
  }
  
  def _drawTrackedUnit(trackedUnit:EnemyUnitInfo) {
    if (trackedUnit._possiblyStillThere && trackedUnit.unit.isEmpty) {
      With.game.drawCircleMap(
        trackedUnit.position,
        trackedUnit.unitType.width / 2,
        Color.Grey)
      _drawTextLabel(
        List(TypeDescriber.describeUnitType(trackedUnit.unitType)),
        trackedUnit.position,
        drawBackground = true)
    }
  }
  
  def _drawCombatSimulations {
    With.simulator.combats.foreach(_drawCombatSimulation)
  }
  
  def _drawCombatSimulation(simulation:CombatSimulation) {
    With.game.drawCircleMap(simulation.focalPoint, 32, Color.Red)
    With.game.drawCircleMap(simulation.ourGroup.vanguard, 32, Color.Red)
    With.game.drawCircleMap(simulation.enemyGroup.vanguard, 32, Color.Red)
    With.game.drawLineMap(simulation.focalPoint, simulation.ourGroup.vanguard, Color.Red)
    With.game.drawLineMap(simulation.focalPoint, simulation.enemyGroup.vanguard, Color.Red)
    With.game.drawBoxMap(
      new Position(
        simulation.ourGroup.units.filter(_.stillExists).map(_.getX).min,
        simulation.ourGroup.units.filter(_.stillExists).map(_.getY).min),
      new Position(
        simulation.ourGroup.units.filter(_.stillExists).map(_.getX).max,
        simulation.ourGroup.units.filter(_.stillExists).map(_.getY).max),
      Color.Orange)
    With.game.drawBoxMap(
      new Position(
        simulation.enemyGroup.units.filter(_.stillExists).map(_.getX).min,
        simulation.enemyGroup.units.filter(_.stillExists).map(_.getY).min),
      new Position(
        simulation.enemyGroup.units.filter(_.stillExists).map(_.getX).max,
        simulation.enemyGroup.units.filter(_.stillExists).map(_.getY).max),
      Color.Orange)
  }
}
