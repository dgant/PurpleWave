package Micro.Execution

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object Threats {
  
  def get(state: ExecutionState): Vector[UnitInfo] = {
    if (state.unit.battle.isEmpty) return Vector.empty
    With.units.inTileRadius(
      state.unit.tileIncludingCenter,
      With.configuration.battleMarginTiles)
      .filter(threat => valid(state, threat))
    .toVector
  }
  
  def valid(state: ExecutionState, threat: UnitInfo): Boolean = {
    threat.likelyStillThere &&
      With.frame - threat.lastSeen < 24 * 60 &&
      threat.isEnemyOf(state.unit) &&
      threat.canAttackThisSecond(state.unit)
  }
}
