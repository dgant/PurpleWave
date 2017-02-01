package Development

import Startup.With
import Types.Plans.Generic.Allocation.{PlanAcquireCurrency, PlanAcquireUnits}
import Types.Plans.Plan
import bwapi.UnitCommandType

object Overlay {
  
  def render() {
    With.game.setTextSize(bwapi.Text.Size.Enum.Small)
    With.game.drawTextScreen(
      5, 5,
      _describePlanTree(With.gameplan, 0))
    
    _drawUnits()
  }
  
  def _drawTextLabel(textLines:Iterable[String], unit:bwapi.Unit) {
    val horizontalMargin = 2
    val x = unit.getPosition.getX - horizontalMargin
    val y = unit.getPosition.getY
    val width = (9 * textLines.map(_.size).max) / 2 + 2 * horizontalMargin
    val height = 11 * textLines.size
  
    With.game.drawBox(bwapi.CoordinateType.Enum.Map, x, y, x+width, y+height, bwapi.Color.Grey, true)
    With.game.drawTextMap(unit.getPosition, textLines.mkString("\n"))
  }
  
  def _drawUnits() {
    With.ourUnits
      .filterNot(_.getLastCommand.getUnitCommandType == UnitCommandType.None)
      .foreach(unit => _drawTextLabel(
      Iterable(unit.getLastCommand.getUnitCommandType.toString),
      unit
    ))
  }
  
  def _describePlanTree(plan:Plan, depth:Integer):String = {
    if (_isRelevant(plan)) {
      _describePlan(plan, depth) ++ plan.children.map(_describePlanTree(_, depth + 1)).mkString("")
    } else {
      ""
    }
  }
  
  def _describePlan(plan:Plan, depth:Integer):String = {
    val planName = plan.getClass.getSimpleName
    val qualities = (if (plan.isComplete) " (complete)" else "")
    
    val resources = Iterable(plan)
      .filter(_.isInstanceOf[PlanAcquireCurrency])
      .map(_.asInstanceOf[PlanAcquireCurrency])
      .map(r => ": " ++ r.minerals.toString ++ "m " ++ r.gas.toString ++ "g " ++ r.supply.toString ++ "s")
      .mkString("")
    
    val units = Iterable(plan)
      .filter(_.isInstanceOf[PlanAcquireUnits])
      .flatten(_.asInstanceOf[PlanAcquireUnits].units)
      .groupBy(unit => unit.getType.toString)
      .map(pair => ": " ++ pair._2.size.toString ++ " " ++ _formatUnitTypeName(pair._1))
      .mkString("")
    
    ("  " * depth
      ++ ""
      ++ plan.getClass.getSimpleName.replace("Plan", "")
      ++ " " * Math.max(0, 20 - planName.size)
      ++ resources
      ++ units
      ++ qualities
      ++ "\n")
  }
  
  def _formatUnitTypeName(name: String):String = {
    name
      .replace("Terran_", "")
      .replace("Zerg_", "")
      .replace("Protoss_", "")
      .replace("Neutral_", "")
      .replaceAll("_", " ")
  }
  
  def _isRelevant(plan:Plan):Boolean = {
    if (plan.isComplete) {
      return plan.isInstanceOf[PlanAcquireCurrency] || plan.isInstanceOf[PlanAcquireUnits]
    }
    
    plan.children.exists(_isRelevant(_))
  }
}
