package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Spells.TargetAOE
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Stasis extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Protoss.Arbiter)                  &&
    unit.energy >= Protoss.Stasis.energyCost  &&
    With.self.hasTech(Protoss.Stasis)         &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    val target = TargetAOE.chooseTarget(
      unit,
      (if (unit.agent.dying) 9.0 else 15.0) * 32.0,
       if (unit.agent.dying) 0.0 else unit.subjectiveValue,
      valueTarget)
    
    target.foreach(With.commander.useTechOnPixel(unit, Protoss.Stasis, _))
  }
  
  private def valueTarget(target: UnitInfo): Double = {
    if (target.unitClass.isBuilding) return 0.0
    if (target.underStorm) return 0.0
    if (target.invincible) return 0.0
  
    target.subjectiveValue *
    Math.min(1.0, target.matchups.targets.size          / 3.0)  *
    Math.min(1.0, target.matchups.framesToLiveDiffused  / 72.0) *
    (
      if(target.isFriendly)
        -2.0
      else if (target.isEnemy)
        1.0
      else
        0.0
    )
  }
}
