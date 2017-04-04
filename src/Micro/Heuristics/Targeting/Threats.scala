package Micro.Heuristics.Targeting

import Micro.Intentions.Intention
import ProxyBwapi.UnitInfo.UnitInfo
import Lifecycle.With

object Threats {
  
  def get(intent:Intention):Set[UnitInfo] =
    With.units.inTileRadius(
      intent.unit.tileCenter,
      With.configuration.combatEvaluationDistanceTiles)
      .filter(threat =>
        threat.possiblyStillThere &&
        threat.isEnemyOf(intent.unit) &&
        threat.canAttackThisSecond(intent.unit))
}
