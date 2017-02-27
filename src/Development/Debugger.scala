package Development

import Plans.Plan
import Startup.With
import Types.UnitInfo.UnitInfo

import scala.collection.mutable

object Debugger {
  
  def plans:Iterable[Plan] = {
    _flatten(With.gameplan)
  }
  
  def planDescriptions:Iterable[String] = {
    plans.map(plan => plan.description.get.getOrElse(plan.getClass.getSimpleName))
  }
  
  def _flatten(plan:Plan):Iterable[Plan] = {
    List(plan) ++ plan.getChildren.flatten(_flatten)
  }
  
  val highlitUnits = new mutable.HashSet[UnitInfo]
  def toggleHighlight(unit:UnitInfo) {
    if (highlitUnits.contains(unit)) {
      highlitUnits.remove(unit)
    } else {
      highlitUnits.add(unit)
    }
  }
  
}
