package Micro.Actions.Combat.Targeting.Filters

import Lifecycle.With
import Micro.Actions.Scouting.BlockConstruction
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object TargetFilterFutility extends TargetFilter {
  
  private def catchableBy(actor: UnitInfo, target: UnitInfo): Boolean = {
    lazy val targetBusy = target.gathering || target.constructing || target.repairing || ! target.canMove || BlockConstruction.buildOrders.contains(target.order)
    val output = (
      actor.topSpeed >= target.topSpeed
      || actor.inRangeToAttack(target)
      || targetBusy
      || actor.is(Zerg.Scourge)
      || actor.framesToGetInRange(target) < 8
      || (target.unitClass.isWorker && target.base.exists(_.harvestingArea.contains(target.tileIncludingCenter)))
      || (actor.is(Protoss.Zealot) && target.is(Protoss.Dragoon) && ! target.player.hasUpgrade(Protoss.DragoonRange))
      || (actor.is(Protoss.DarkTemplar) && target.is(Protoss.Dragoon)))
    output
  }
  // Target units according to our goals.
  // Ignore them if they're distractions.
  //
  def legal(actor: FriendlyUnitInfo, target: UnitInfo): Boolean = {
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
      || Vector(actor.pixelToFireAt(target).tileIncluding, target.tileIncludingCenter).exists(With.grids.walkableTerrain.get))
    
    val output = targetReachable && (targetCatchable || atOurWorkers)
    
    output
  }
  
}
