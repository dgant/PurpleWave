package Micro.Heuristics

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?

object SpellTargets {

  def legal(target: UnitInfo): Boolean = (
       ! target.invincible
    && ! target.stasised
    && target.likelyStillThere
    && (target.burrowed || With.framesSince(target.lastSeen) < ?(target.canMove, 24, 72)))

  def apply(caster: FriendlyUnitInfo): Seq[UnitInfo] = caster.matchups.allUnits.filter(legal)
}
