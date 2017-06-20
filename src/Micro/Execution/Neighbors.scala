package Micro.Execution

import Lifecycle.With
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object Neighbors {
  
  def get(state: ExecutionState): Vector[FriendlyUnitInfo] = {
    With.units.inTileRadius(
      state.unit.tileIncludingCenter,
      With.configuration.battleMarginTiles)
      .flatMap(_.friendly)
      .toVector
  }
}
