package Micro.Intent

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object Threats {
  
  def get(intent:Intention):Set[UnitInfo] =
    With.units.inTileRadius(
      intent.unit.tileCenter,
      With.configuration.combatEvaluationDistanceTiles)
      .filter(threat =>
        threat.possiblyStillThere &&
        With.frame - threat.lastSeen < 24 * 60 &&
        threat.isEnemyOf(intent.unit) &&
        threat.canAttackThisSecond(intent.unit))
}
