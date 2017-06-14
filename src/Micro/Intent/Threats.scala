package Micro.Intent

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object Threats {
  
  def get(intent: Intention): Vector[UnitInfo] = {
    if (intent.unit.battle.isEmpty) return Vector.empty
    With.units.inTileRadius(
      intent.unit.tileIncludingCenter,
      With.configuration.battleMarginTiles)
      .filter(threat => valid(intent, threat))
    .toVector
  }
  
  def valid(intent: Intention, threat: UnitInfo): Boolean = {
    threat.likelyStillThere &&
      With.frame - threat.lastSeen < 24 * 60 &&
      threat.isEnemyOf(intent.unit) &&
      threat.canAttackThisSecond(intent.unit)
  }
}
