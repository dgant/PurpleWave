package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Spells.TargetAOE
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object PsionicStorm extends Action {
  
  override protected def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Protoss.HighTemplar)                    &&
    unit.energy >= Protoss.PsionicStorm.energyCost  &&
    With.self.hasTech(Protoss.PsionicStorm)         &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    val dying = unit.matchups.framesToLiveCurrently < 24.0
    
    val target = TargetAOE.chooseTarget(
      unit,
      (if (dying) 8.0 else 12.0) * 32.0,
      if (dying) 0.0 else unit.subjectiveValue,
      valueTarget)
    
    target.foreach(With.commander.useTechOnPixel(unit, Protoss.PsionicStorm, _))
  }
  
  private def valueTarget(target: UnitInfo): Double = {
    if (target.unitClass.isBuilding) return 0.0
    if (target.underStorm) return 0.0
    if (target.invincible) return 0.0
    
    target.matchups.valuePerDamage *
    Math.min(112.0, target.totalHealth) *
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
