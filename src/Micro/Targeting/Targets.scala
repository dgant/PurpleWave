package Micro.Targeting

import Startup.With
import Micro.Intentions.Intention
import BWMirrorProxy.UnitInfo.UnitInfo

object Targets {
  def get(intent:Intention):Iterable[UnitInfo] = {
    With.units.inRadius(intent.unit.pixel, intent.unit.range + 32 * 8)
      .filter(intent.unit.enemyOf)
      .filter(_.alive)
      .filter(_.visible)
      .filterNot(_.invincible)
      .filter(target => target.detected || ( ! target.cloaked && ! target.burrowed ))
  }
}
