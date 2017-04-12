package Micro.Intent

import Lifecycle.With
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

object Targets {
  
  val ineligibleClasses = Vector(Zerg.Larva, Zerg.Egg)
  
  def get(intent:Intention):Set[UnitInfo] = {
    
    if ( ! intent.unit.canAttackThisSecond) return Set.empty
    
    With.units.inTileRadius(
      intent.unit.tileIncludingCenter,
      With.configuration.combatEvaluationDistanceTiles)
      .filter(target =>
        target.possiblyStillThere &&
        With.frame - target.lastSeen < 24 * 60 &&
        target.isEnemyOf(intent.unit) &&
        intent.unit.canAttackThisSecond(target) &&
        ! ineligibleClasses.contains(target.unitClass))
  }
}
