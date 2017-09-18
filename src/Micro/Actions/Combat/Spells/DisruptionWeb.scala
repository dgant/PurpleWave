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
    
    val dying = unit.matchups.framesToLiveCurrently < 24.0
    
    val target = TargetAOE.chooseTarget(
      unit,
      15.0 * 9.0,
      unit.subjectiveValue / 240.0,
      valueTarget)
    
    target.foreach(With.commander.useTechOnPixel(unit, Protoss.DisruptionWeb, _))
  }
  
  private def valueTarget(target: UnitInfo): Double = {
    if (target.underDisruptionWeb) return 0.0
    if (target.flying) 0.0
    
    val multiplier =
        if (target.isFriendly)
          -2.0
        else if (target.isEnemy)
          1.0
        else
          0.0
    val value = target.matchups.vpfDealingCurrently * Math.min(72, target.matchups.framesToLiveCurrently)
    val output = value * multiplier
    output
  }
}
