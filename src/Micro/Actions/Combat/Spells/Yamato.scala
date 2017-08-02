package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Decisions.MicroValue
import Micro.Heuristics.Spells.TargetSingle
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Yamato extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Terran.Battlecruiser)            &&
    unit.energy >= Terran.Yamato.energyCost  &&
    With.self.hasTech(Terran.Yamato)         &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    val target = TargetSingle.chooseTarget(
      unit,
      32.0 * 14.0,
      unit.subjectiveValue / 3.0,
      valueTarget)
    
    target.foreach(With.commander.useTechOnUnit(unit, Terran.Yamato, _))
  }
  
  private def valueTarget(target: UnitInfo): Double = {
    MicroValue.valuePerDamage(target) * Math.min(260.0, target.totalHealth) * (if (target.isEnemy) 1.0 else -1.0)
  }
}
