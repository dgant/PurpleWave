package Debugging

import Planning.Plan
import Startup.With
import ProxyBwapi.UnitInfo.UnitInfo

import scala.collection.mutable

object Debugger {
  
  def plans:Iterable[Plan] = flatten(With.gameplan)
  
  def planDescriptions:Iterable[String] = plans.map(plan => plan.toString)
  
  private def flatten(plan:Plan):Iterable[Plan] = List(plan) ++ plan.getChildren.flatten(flatten)
  
  val highlitUnits = new mutable.HashSet[UnitInfo]
  def toggleHighlight(unit:UnitInfo) {
    if (highlitUnits.contains(unit)) {
      highlitUnits.remove(unit)
    } else {
      highlitUnits.add(unit)
    }
  }
}
