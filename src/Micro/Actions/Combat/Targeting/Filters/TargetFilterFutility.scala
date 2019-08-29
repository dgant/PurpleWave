package Micro.Actions.Combat.Targeting.Filters

import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Actions.Scouting.BlockConstruction
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFutility extends TargetFilter {
  
  private def catchableBy(actor: UnitInfo, target: UnitInfo): Boolean = {
    lazy val targetBusy = target.gathering || target.constructing || target.repairing || ! target.canMove || BlockConstruction.buildOrders.contains(target.order)

    val output = (
      actor.topSpeed >= Math.max(target.topSpeed, actor.friendly.flatMap(_.transport.map(_.topSpeed)).getOrElse(0.0))
      || targetBusy
      || actor.is(Zerg.Scourge)
      || actor.framesToGetInRange(target) < 8
      || (target.unitClass.isWorker && target.base.exists(_.harvestingArea.contains(target.tileIncludingCenter)))
      || (actor.is(Zerg.Zergling) && With.self.hasUpgrade(Zerg.ZerglingSpeed) && ! target.player.hasUpgrade(Terran.VultureSpeed))
      || (actor.is(Protoss.Zealot) && target.is(Protoss.Dragoon) && ! target.player.hasUpgrade(Protoss.DragoonRange))
      || (actor.is(Protoss.DarkTemplar) && target.is(Protoss.Dragoon)))
    output
  }
  // Target units according to our goals.
  // Ignore them if they're distractions.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    if ( ! actor.canMove && ! actor.inRangeToAttack(target)) {
      return false
    }

    // Respect PushKiters (intended for eg. Proxy Zealot rushes coming up against Dragoons)
    if (With.blackboard.pushKiters.get && target.canAttack(actor) && target.pixelRangeAgainst(actor) > 32) {
      return true
    }

    lazy val atOurWorkers = target.base.exists(_.owner.isUs) && target.matchups.targetsInRange.exists(_.unitClass.isWorker)
    lazy val alliesAssisting  = target.matchups.threats.exists(ally =>
      ally != actor
      && catchableBy(ally, target)
      && (ally.topSpeed > target.topSpeed || ally.pixelRangeAgainst(target) > actor.pixelRangeAgainst(target))
      && ally.framesBeforeAttacking(target) <= actor.framesBeforeAttacking(target))
    
    lazy val targetCatchable  = catchableBy(actor, target) || alliesAssisting
    lazy val targetReachable  = (
      target.visible
      || actor.flying
      || ! target.flying
      || Vector(actor.pixelToFireAt(target).tileIncluding, target.tileIncludingCenter)
        .exists(tile =>
          With.grids.walkableTerrain.get(tile)
          && With.grids.altitudeBonus.get(tile) >= With.grids.altitudeBonus.get(target.tileIncludingCenter)))

    if (actor.is(Terran.Vulture) && target.unitClass.isBuilding && With.frame < GameTime(0, 4)()) return false

    val output = targetReachable && (targetCatchable || atOurWorkers)
    
    output
  }
  
}
