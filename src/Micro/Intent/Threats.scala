package Micro.Intent

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object Threats {
  
  def get(intent:Intention):Vector[UnitInfo] =
    With.units.inTileRadius(
      intent.unit.tileIncludingCenter,
      With.configuration.battleMarginTiles)
      .filter(threat => valid(intent, threat))
    .toVector
  
  def valid(intent:Intention, threat:UnitInfo):Boolean = {
    threat.possiblyStillThere &&
      With.frame - threat.lastSeen < 24 * 60 &&
      threat.isEnemyOf(intent.unit) &&
      threat.canAttackThisSecond(intent.unit)
  }
  
  def active(intent:Intention, threat:UnitInfo):Boolean = {
    val threatTarget = threat.target.orElse(threat.orderTarget)
    threatTarget.exists(_ == intent.unit) || threat.framesBeforeAttacking(intent.unit) <= With.configuration.microFrameLookahead
  }
}
