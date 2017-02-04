package Development

import Plans.Generic.Allocation.{PlanAcquireCurrency, PlanAcquireUnits}
import Plans.Plan
import Startup.With
import bwapi.{Position, UnitCommandType}
import bwta.BWTA

import scala.collection.JavaConverters._

object Overlay {
  
  def onFrame() {
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    _drawTerrain()
    _drawUnits()
    With.game.drawTextScreen(5, 5, _describePlanTree(With.gameplan, 0, 0))
    _drawPlans(With.gameplan)
    _drawResources()
  }
  
  def _drawTextLabel(textLines:Iterable[String], unit:bwapi.Unit) {
    val horizontalMargin = 2
    val width = (9 * textLines.map(_.size).max) / 2 + 2 * horizontalMargin
    val height = 11 * textLines.size
    val x = unit.getPosition.getX - width/2 - horizontalMargin
    val y = unit.getPosition.getY - height/2
  
    With.game.drawBox(bwapi.CoordinateType.Enum.Map, x, y, x+width, y+height, bwapi.Color.Grey, true)
    With.game.drawTextMap(unit.getX-width/2, unit.getY-height/2, textLines.mkString("\n"))
  }
  
  def _drawUnits() {
    With.ourUnits
      .filterNot(_.getLastCommand.getUnitCommandType == UnitCommandType.None)
      .foreach(unit => _drawTextLabel(List(unit.getLastCommand.getUnitCommandType.toString), unit))
  }
  
  def _describePlanTree(plan:Plan, childOrder:Integer, depth:Integer):String = {
    if (_isRelevant(plan)) {
      (_describePlan(plan, childOrder, depth)
        ++ plan.children.zipWithIndex.map(x => _describePlanTree(x._1, x._2, depth + 1)))
        .mkString("")
    } else {
      ""
    }
  }
  
  def _describePlan(plan:Plan, childOrder:Integer, depth:Integer):String = {
    val planName = plan.getClass.getSimpleName.replace("Plan", "")
    val planDescription = plan.describe.map(d => ": " + d).mkString("")
    val checkbox = if (plan.isComplete) "[X] " else "[_] "
    
    val resources = Iterable(plan)
      .filter(_.isInstanceOf[PlanAcquireCurrency])
      .map(_.asInstanceOf[PlanAcquireCurrency])
      .map(r => r.minerals.toString ++ "m " ++ r.gas.toString ++ "g " ++ r.supply.toString ++ "s")
      .mkString("")
    
    val units = Iterable(plan)
      .filter(_.isInstanceOf[PlanAcquireUnits])
      .flatten(_.asInstanceOf[PlanAcquireUnits].units)
      .groupBy(unit => TypeDescriber.describeUnitType(unit.getType))
      .map(pair => pair._2.size.toString ++ " " ++ pair._1)
      .mkString("")
    
    val spacer = "  " * depth
    val leftColumn =
    (checkbox
      ++ spacer
      ++ "#"
      ++ (childOrder + 1).toString
      ++ " "
      ++ planName
      ++ planDescription)
    
    leftColumn + " " * Math.max(0, 45 - leftColumn.length) + "\n"
  }
  
  def _isRelevant(plan:Plan):Boolean = {
    if (plan.isComplete) {
      return plan.isInstanceOf[PlanAcquireCurrency] || plan.isInstanceOf[PlanAcquireUnits]
    }
    
    plan.children.exists(_isRelevant(_))
  }
  
  def _drawTerrain() {
    BWTA.getRegions.asScala
      .foreach(region => {
  
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
    plan.children.foreach(_drawPlans)
  }
  
  def _drawResources() {
    With.game.drawTextScreen(
      205,
      5,
      With.bank.getPrioritizedRequests
        .map(r =>
          (if (r.getSatisfaction) "[X] " else "[_]") ++
          (if (r.minerals > 0)  r.minerals  .toString ++ "m " else "") ++
          (if (r.gas > 0)       r.gas       .toString ++ "g " else "") ++
          (if (r.supply > 0)    r.supply    .toString ++ "s " else ""))
        .mkString("\n"))
  }
}
