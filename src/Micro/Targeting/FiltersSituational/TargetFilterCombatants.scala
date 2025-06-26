package Micro.Targeting.FiltersSituational

import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCombatants extends TargetFilter {
  
  // If we're fighting, target units that threaten to fight back
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    target.unitClass.attacksOrCastsOrDetectsOrTransports || ( ! actor.team.exists(_.engagedUpon) && actor.matchups.pixelsToThreatRange.forall(_ > 96))
  }
}
