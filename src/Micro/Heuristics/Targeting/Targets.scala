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
      .filter(_.possiblyStillThere)
      .filter(intent.unit.canAttackThisSecond)
      .filter(intent.unit.isEnemyOf)
      .filterNot(target => List(Zerg.Larva, Zerg.Egg).contains(target.unitClass))
  }
}
