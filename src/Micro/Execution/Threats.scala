package Micro.Execution

import Lifecycle.With
import ProxyBwapi.UnitInfo.UnitInfo

object Threats {
  
  def enemies(state: ActionState): Vector[UnitInfo] = {
    if (state.unit.battle.isEmpty) return Vector.empty
    With.units.inTileRadius(
      state.unit.tileIncludingCenter,
      With.configuration.battleMarginTiles)
        .filter(threat =>
          threat.likelyStillThere &&
          threat.isEnemyOf(state.unit))
        .toVector
  }
  
  def threats(state: ActionState): Vector[UnitInfo] = {
    state.enemies.filter(_.canAttackThisSecond(state.unit))
  }
  
  def violent(state: ActionState): Vector[UnitInfo] = {
    state.threats.filter(_.isBeingViolentTo(state.unit))
  }
}
