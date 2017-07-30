package Micro.Actions.Combat.Spells

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Decisions.MicroValue
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
    
    MicroValue.valuePerDamage(target) *
      Math.max(112, target.totalHealth) *
      (
        if(target.isFriendly)
          -5.0
        else if (target.isEnemy)
          1.0 * Math.max(1.0, PurpleMath.nanToZero(target.matchups.framesToLiveDiffused / 48))
        else
          0.0
      )
  }
}
