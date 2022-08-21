package Micro.Heuristics

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object SpellTargets {
  def apply(caster: FriendlyUnitInfo): Seq[UnitInfo] =
    caster.matchups.allUnits
      .filterNot(_.invincible)
      .filter(target => target.likelyStillThere && (target.burrowed || With.framesSince(target.lastSeen) < 72))
}
