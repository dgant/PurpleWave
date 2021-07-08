package Micro.Targeting.FiltersRequired

import Lifecycle.With
import Micro.Targeting.TargetFilter
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFutility extends TargetFilter {

  // Ignore targets we have no chance of reaching
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {

    if (actor.inRangeToAttack(target)) return true
    if ( ! actor.canMove ) return false

    val targetReachable = (
      target.visible
      || actor.flying
      || ! target.flying
      || target.pixel.walkable
      || With.reaction.sluggishness > 0 // The next check is moderately expensive
      || Vector(actor.pixelToFireAt(target).tile, target.tile).exists(t => t.walkable && t.altitude >= target.tile.altitude))
    if ( ! targetReachable) return false

    if (actor.topSpeed >= target.topSpeed) return true
    if (actor.pixelRangeAgainst(target) > 96) return true
    if (With.blackboard.pushKiters.get && target.canAttack(actor) && target.pixelRangeAgainst(actor) > 32) return true
    if ( ! With.scouting.enemyScouts().exists(_ == target)) {
      if (actor.team.exists(t => if (target.flying) t.hasCatchersAir() else t.hasCatchersGround())) return true
      if (target.presumptiveTarget.exists(u => u.isOurs && u.unitClass.isWorker)) return true
    }
    false
  }
  
}
