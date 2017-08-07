package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Spells.TargetSingle
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object DefensiveMatrix extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.ScienceVessel)                     &&
    unit.energy >= Terran.DefensiveMatrix.energyCost  &&
    unit.matchups.allies.exists(_.matchups.vpfReceivingCurrently > 0)
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
  
    def valueTarget(target: UnitInfo): Double = {
      if (target.isFriendly)
        target.matchups.vpfReceivingCurrently
      else
        -1.0
    }
    
    val target = TargetSingle.chooseTarget(
      unit,
      32.0 * 10.0,
      0.0,
      valueTarget)
    
    target.foreach(With.commander.useTechOnUnit(unit, Terran.Yamato, _))
  }
  
  
}
