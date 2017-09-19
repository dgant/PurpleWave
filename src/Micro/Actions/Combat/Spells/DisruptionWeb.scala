package Micro.Actions.Combat.Spells

import Lifecycle.With
import Micro.Actions.Action
import Micro.Heuristics.Spells.TargetAOE
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object DisruptionWeb extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = {
    unit.is(Protoss.Corsair)                          &&
    unit.energy >= Protoss.DisruptionWeb.energyCost   &&
    With.self.hasTech(Protoss.DisruptionWeb)          &&
    unit.matchups.enemies.nonEmpty
  }
  
  override protected def perform(unit: FriendlyUnitInfo) {
    
    val target = TargetAOE.chooseTarget(
      unit,
      32.0 * 15.0,
      unit.subjectiveValue,
      valueTarget)
    
    target.foreach(With.commander.useTechOnPixel(unit, Protoss.DisruptionWeb, _))
  }
  
  private def valueTarget(target: UnitInfo): Double = {
    if (target.underDisruptionWeb) return 0.0
    if (target.flying) return 0.0
    
    val output = (
      target.subjectiveValue *
      Math.max(1.0, target.matchups.targets.size          / 3.0)  *
      Math.max(1.0, target.matchups.framesToLiveDiffused  / 72.0) *
      (
        if(target.isFriendly)
          -2.0
        else if (target.isEnemy)
          1.0
        else
          0.0
        )
      )
    
    output
  }
}
