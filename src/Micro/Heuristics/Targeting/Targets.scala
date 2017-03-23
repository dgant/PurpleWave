package Micro.Heuristics.Targeting

import Startup.With
import Micro.Intentions.Intention
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

object Targets {
  def get(intent:Intention):Set[UnitInfo] = {
    With.units.inPixelRadius(
        intent.unit.pixelCenter,
        32 * 15)
      .filter(intent.unit.canAttack)
      .filter(intent.unit.isEnemyOf)
      .filterNot(_.unitClass == Zerg.Larva)
  }
}
