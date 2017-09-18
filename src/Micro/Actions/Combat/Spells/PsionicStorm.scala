package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Spells.TargetAOE
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object PsionicStorm extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Protoss.HighTemplar)                    &&
    unit.energy >= Protoss.PsionicStorm.energyCost  &&
    With.self.hasTech(Protoss.PsionicStorm)         &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    val dying = unit.matchups.framesToLiveCurrently < 24.0
    
    val target = TargetAOE.chooseTarget(
      unit,
      (if (dying) 8.0 else 16.0) * 32.0,
      if (dying) 0.0 else unit.subjectiveValue,
      valueTarget)
    
    target.foreach(With.commander.useTechOnPixel(unit, Protoss.PsionicStorm, _))
  }
  
  private def valueTarget(target: UnitInfo): Double = {
    if (target.unitClass.isBuilding)  return 0.0
    if (target.underStorm)            return 0.0
    if (target.invincible)            return 0.0
    if (target.is(Zerg.Larva))        return 0.0 //Shouldn't be necessary (0 value) but keep seeing this
    if (target.is(Zerg.Egg))          return 0.0
    if (target.is(Zerg.LurkerEgg))    return 0.0
    
    val output = (
      target.subjectiveValue *
      Math.min(112.0, target.totalHealth) *
      (
        if (target.isFriendly)
          -10.0
        else if (target.isEnemy)
          1.0
        else
          0.0
      ) /
      target.unitClass.maxTotalHealth
    )
    output
  }
}
