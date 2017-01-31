package Development

import Startup.With
import Types.Plans.Plan
import Types.Requirements.{RequireAll, RequireCurrency, RequireUnits, Requirement}

object Overlay {
  def update() {
    With.game.drawTextScreen(
      5, 5,
      _describePlanTree(With.gameplan, 0))
  }
  
  def _describePlanTree(plan:Plan, depth:Integer):String = {
    _describePlan(plan, depth) + plan.children.map(_describePlanTree(_, depth + 1)).mkString("")
  }
  
  def _describePlan(plan:Plan, depth:Integer):String = {
    val planName = plan.getClass.getSimpleName
    
    val qualities =
      (if (plan.active) "active " else "") ++
      (if (plan.isComplete) "complete" else "")
    
    val resources = _getRequirementsCurrency(plan._requirements)
      .map(r => "  " * 2 * depth ++ r.minerals.toString ++ "m " ++ r.gas.toString ++ "g " ++ r.supply.toString ++ "s\n")
      .mkString("")
    
    val units = _getRequirementsUnits(plan._requirements)
      .flatten(With.recruiter.getUnits(_))
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
  
  // This is dumb, but type erasure in Java makes it impossible (I think) to implement these generically
  def _getRequirementsUnits(requirement:Requirement):Iterable[RequireUnits] = {
    val a = Iterable(requirement)
      .filter(_.isInstanceOf[RequireUnits])
      .map(_.asInstanceOf[RequireUnits])
    
    val b = Iterable(requirement)
      .filter(_.isInstanceOf[RequireAll])
      .map(_.asInstanceOf[RequireAll])
      .flatten(r => r._requirements.flatten(_getRequirementsUnits))
    
    a ++ b
  }
  
  // This is dumb, but type erasure in Java makes it impossible (I think) to implement these generically
  def _getRequirementsCurrency(requirement:Requirement):Iterable[RequireCurrency] = {
    val a = Iterable(requirement)
      .filter(_.isInstanceOf[RequireCurrency])
      .map(_.asInstanceOf[RequireCurrency])
    
    val b = Iterable(requirement)
      .filter(_.isInstanceOf[RequireAll])
      .map(_.asInstanceOf[RequireAll])
      .flatten(r => r._requirements.flatten(_getRequirementsCurrency))
    
    a ++ b
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
