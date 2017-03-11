package Global.Combat.Targeting

import Startup.With
import Types.Intents.Intention
import Types.UnitInfo.UnitInfo

object Targets {
  def get(intent:Intention):Iterable[UnitInfo] = {
    With.units.inRadius(intent.unit.position, intent.unit.range + 32 * 8)
      .filter(intent.unit.enemyOf)
      .filter(_.alive)
      .filter(_.visible)
      .filterNot(_.invincible)
      .filter(target => target.detected || ! target.cloaked)
  }
}
