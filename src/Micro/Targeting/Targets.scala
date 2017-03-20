package Micro.Targeting

import Startup.With
import Micro.Intentions.Intention
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.UnitInfo

object Targets {
  def get(intent:Intention):Set[UnitInfo] = {
    With.units.inRadius(intent.unit.pixelCenter, intent.unit.unitClass.maxAirGroundRange + 32 * 8)
      .filter(intent.unit.isEnemyOf)
      .filter(_.alive)
      .filter(_.visible)
      .filterNot(_.invincible)
      .filterNot(_.unitClass == Zerg.Larva)
      .filter(target => target.detected || ( ! target.cloaked && ! target.burrowed ))
      .filter(target =>
        (intent.unit.unitClass.attacksGround  && ! target.flying) ||
        (intent.unit.unitClass.attacksAir     &&   target.flying))
  }
}
