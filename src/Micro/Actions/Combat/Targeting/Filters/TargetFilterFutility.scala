package Micro.Actions.Combat.Targeting.Filters

import Lifecycle.With
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFutility extends TargetFilter {

  // Ignore targets we have no chance of reaching
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {

    if ( ! actor.canMove && ! actor.inRangeToAttack(target)) return false

    if (actor.inRangeToAttack(target)) return true

    // Respect PushKiters (intended for eg. Proxy Zealot rushes coming up against Dragoons)
    if (With.blackboard.pushKiters.get && target.canAttack(actor) && target.pixelRangeAgainst(actor) > 32) return true

    // The rest of this filter is pretty expensive; avoid using it if possible
    if (With.reaction.sluggishness > 0) return true

    val targetReachable = (
      target.visible
      || actor.flying
      || ! target.flying
      || Vector(actor.pixelToFireAt(target).tile, target.tile).exists(t => t.walkable && t.altitude >= target.tile.altitude))
    if ( ! targetReachable) return false

    lazy val atOurWorkers = target.presumptiveTarget.exists(u => u.isOurs && u.unitClass.isWorker)
    lazy val iAmCatcher = target.matchups.canCatchMe(actor)
    lazy val catcherExists = target.matchups.catchers.exists(ally => ally.friendly.exists(_.agent.toAttack.contains(target)) || ally.framesToGetInRange(target) <= actor.framesToGetInRange(target))
    val output = iAmCatcher || atOurWorkers || catcherExists
    output
  }
  
}
