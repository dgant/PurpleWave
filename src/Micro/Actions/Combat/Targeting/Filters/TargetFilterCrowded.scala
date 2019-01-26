package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCrowded extends TargetFilter {
  
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    val range = actor.pixelRangeAgainst(target)

    if (range > 32) return true
    val framesToGetInRange = actor.framesToGetInRange(target)
    val occupiedPerimeter = target.matchups.threatsViolent
      .view
      .map(threat =>
        if (threat.pixelRangeAgainst(target) <= range && threat.framesToGetInRange(target) < framesToGetInRange)
          threat.unitClass.dimensionMax
        else
          0)
      .sum

    occupiedPerimeter < target.unitClass.perimeter / 2
  }
}
