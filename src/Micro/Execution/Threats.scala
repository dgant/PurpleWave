package Micro.Execution

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object Threats {
  
  def get(state: ActionState): Vector[UnitInfo] = {
    if (state.unit.battle.isEmpty) return Vector.empty
    With.units.inTileRadius(
      state.unit.tileIncludingCenter,
      With.configuration.battleMarginTiles)
        .filter(threat => valid(state, threat))
    .toVector
  }
  
  def valid(state: ActionState, threat: UnitInfo): Boolean = {
    threat.likelyStillThere &&
    threat.isEnemyOf(state.unit) &&
    threat.canAttackThisSecond(state.unit)
  }
}
