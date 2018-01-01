package Micro.Actions.Combat.Techniques.Common

import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

object Weigh {
  
  def delegate(unit: FriendlyUnitInfo, techniques: ActionTechnique*) {
    best(unit, techniques).foreach(_.delegate(unit))
  }
  
  def consider(unit: FriendlyUnitInfo, techniques: ActionTechnique*) {
    best(unit, techniques).foreach(_.consider(unit))
  }
  
  private def best(
    unit: FriendlyUnitInfo,
    techniques: Iterable[ActionTechnique])
      : Option[ActionTechnique] = {
    
    val allowed = techniques.filter(_.allowed(unit))
    ByOption.maxBy(allowed)(_.evaluate(unit))
  }
}
