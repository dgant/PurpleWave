package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Decisions.MicroValue
import Micro.Heuristics.Spells.TargetAOE
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Stasis extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Protoss.Arbiter)                  &&
    unit.energy >= Protoss.Stasis.energyCost  &&
    With.self.hasTech(Protoss.Stasis)         &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    val dying = unit.matchups.framesToLiveCurrently < 24.0
    
    val target = TargetAOE.chooseTarget(
      unit,
      (if (dying) 9.0 else 15.0) * 32.0,
      if (dying) 0.0 else unit.subjectiveValue,
      valueTarget)
    
    target.foreach(With.commander.useTechOnPixel(unit, Protoss.Stasis, _))
  }
  
  private def valueTarget(target: UnitInfo): Double = {
    if (target.unitClass.isBuilding) return 0.0
    if (target.underStorm) return 0.0
    if (target.invincible) return 0.0
    
    MicroValue.valuePerDamage(target) *
      target.matchups.vpfNetDiffused * Math.max(target.matchups.framesToLiveDiffused, 48) *
      (
        if(target.isFriendly)
          -5.0
        else if (target.isEnemy)
          1.0
        else
          0.0
      )
  }
}
