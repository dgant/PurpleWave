package Development

import Startup.With
import Types.Plans.Generic.Allocation.{PlanAcquireCurrency, PlanAcquireUnits}
import Types.Plans.Plan

object Overlay {
  def render() {
    With.game.drawTextScreen(
      5, 5,
      _describePlanTree(With.gameplan, 0))
  }
  
  def _describePlanTree(plan:Plan, depth:Integer):String = {
    _describePlan(plan, depth) + plan.children.map(_describePlanTree(_, depth + 1)).mkString("")
  }
  
  def _describePlan(plan:Plan, depth:Integer):String = {
    val planName = plan.getClass.getSimpleName
    
    val qualities = (if (plan.isComplete) "complete" else "")
    
    val resources = Iterable(plan)
      .filter(_.isInstanceOf[PlanAcquireCurrency])
      .map(_.asInstanceOf[PlanAcquireCurrency])
      .map(r => "  " * 2 * depth ++ r.minerals.toString ++ "m " ++ r.gas.toString ++ "g " ++ r.supply.toString ++ "s\n")
      .mkString("")
    
    val units = Iterable(plan)
      .filter(_.isInstanceOf[PlanAcquireUnits])
      .flatten(_.asInstanceOf[PlanAcquireUnits].units)
      .groupBy(unit => unit.getType.toString)
      .map(pair => "  " * 2 * depth ++ pair._2.size.toString ++ " " ++ _formatUnitTypeName(pair._1) ++ "\n")
      .mkString("")
    
    ("  " * depth
      ++ ""
      ++ plan.getClass.getSimpleName.replace("Plan", "")
      ++ " " * Math.max(0, 20 - planName.size)
      ++ qualities
      ++ "\n"
      ++ resources
      ++ units)
  }
  
  def _formatUnitTypeName(name: String):String = {
    name
      .replace("Terran_", "")
      .replace("Zerg_", "")
      .replace("Protoss_", "")
      .replace("Neutral_", "")
      .replaceAll("_", " ")
  }
}
