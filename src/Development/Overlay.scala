package Development

import Startup.With
import Plans.Generic.Allocation.{PlanAcquireCurrency, PlanAcquireUnits}
import Plans.Plan
import bwapi.UnitCommandType

object Overlay {
  
  def render() {
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    With.game.drawTextScreen(
      5, 5,
      _describePlanTree(With.gameplan, 0, 0))
    
    _drawUnits()
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
      .foreach(unit => _drawTextLabel(
      Iterable(unit.getLastCommand.getUnitCommandType.toString),
      unit
    ))
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
}
