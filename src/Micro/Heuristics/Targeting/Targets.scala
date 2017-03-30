package Micro.Heuristics.Targeting

import Startup.With
import Micro.Intentions.Intention
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

object Targets {
  def get(intent:Intention):Set[UnitInfo] = {
    if ( ! intent.unit.canAttackThisSecond) return Set.empty
    With.units.inPixelRadius(
        intent.unit.pixelCenter,
        32 * 15)
      .filter(_.possiblyStillThere)
      .filter(intent.unit.isEnemyOf)
      .filterNot(target => List(Zerg.Larva, Zerg.Egg).contains(target.unitClass))
  }
}
