package Micro.Actions.Combat.Targeting.Filters

import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterCrowded extends TargetFilter {

  val maxRange = 32
  
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    val range = actor.pixelRangeAgainst(target)
    if (range > 32) return true
    if (target.unitClass.isBuilding) return true
    if (actor.inRangeToAttack(target)) return true
    val framesToGetInRange = actor.framesToGetInRange(target)
    val occupiedPerimeter = target.matchups.threats
      .view
      .filter(ally =>
        ally.friendly.exists(_.agent.toAttack.contains(target))
          && ally.pixelRangeAgainst(target) <= range
          && ally.framesToGetInRange(target) < framesToGetInRange)
      .map(_.unitClass.dimensionMax)
      .sum
    occupiedPerimeter < target.unitClass.perimeter
  }
}
