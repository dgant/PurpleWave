package Micro.Actions.Protoss

import Lifecycle.With
import Micro.Actions.Action
import Micro.Actions.Combat.Decisionmaking.FightOrFlee
import Micro.Agency.Commander
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.?

object BeReaver extends Action {

  val cooldownOnLanding = 30

  override def allowed(unit: FriendlyUnitInfo): Boolean = Protoss.Reaver(unit)

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    if (unit.agent.ride.forall(_.pixelDistanceEdge(unit) > ?(unit.agent.isPrimaryPassenger, 64, 32))) {
      return
    }

    // A zero weapon cooldown cannot release another Scarab while the previous one is alive.
    // Rescue before the special Reave behavior can issue a hold or attack-move order.
    if ( ! unit.loaded && ! unit.agent.commit && unit.hasActiveScarab && unit.cooldownLeft <= With.latency.remainingFrames && unit.matchups.threatsInRange.nonEmpty) {
      unit.agent.act("Hop")
      clickShuttle(unit)
      return
    }

    // jonsnow.jpg
    if (unit.agent.fightReason == FightOrFlee.Reave) {
      if (unit.transport.isDefined) {
        if (unit.matchups.targetNearest.exists(unit.hasScarabPathTo(_, unit.pixel))) Commander.requestUnload(unit)
      }
      else if (unit.matchups.targetNearest.exists(unit.framesToFace(_) > 1)) {
        val t =  unit.matchups.targetNearest.get
        unit.agent.decision.set(unit.pixel.project(t.pixel, 6))
        Commander.attackMove(unit)
      } else {
        Commander.hold(unit)
      }
    }

    if (unit.transport.isDefined) {
      // Try to land next to lead passenger
      if (
        unit.agent.shouldFight
          && (unit.scarabs > 0 || unit.trainee.exists(_.remainingCompletionFrames < cooldownOnLanding + With.latency.latencyFrames))
          && unit.tile.walkable
          && unit.agent.toAttack.orElse(unit.matchups.targetNearest).exists(unit.hasScarabPathTo(_, unit.pixel))
          && unit.agent.ride.exists(_.agent.passengersPrioritized.headOption.exists(r =>
          Protoss.Reaver(r)
            && r != unit
            && r.agent.shouldFight
            && ! r.loaded
            && r.pixelDistanceEdge(unit) < 32))) {
        Commander.requestUnload(unit)
      }
    } else {
      lazy val canShoot             = unit.scarabs > 0 && ! unit.hasActiveScarab && unit.matchups.targetsInRange.exists(unit.hasScarabPathTo(_, unit.pixel))
      lazy val inRangeOfPlebian     = unit.matchups.threatsInRange.exists(t => ! t.flying && t.pixelRangeAgainst(unit) < unit.pixelRangeAgainst(t))
      lazy val needRefresh          = unit.cooldownLeft > cooldownOnLanding + 2 * With.latency.latencyFrames // Reaver cooldown resets to 30 when dropped: https://github.com/OpenBW/openbw/blob/master/bwgame.h#L3165
      lazy val needlesslyEndangered = needRefresh && (inRangeOfPlebian || ! canShoot)
      lazy val needlesslyDoomed     = unit.cooldownLeft >= unit.doomedInFrames

      // Dodge danger
      if ( ! unit.agent.commit && (needlesslyEndangered || needlesslyDoomed || unit.matchups.targetedByScarab)) {
        unit.agent.act("Hop")
        clickShuttle(unit)

      // Hopping in and out of a Shuttle actually shortens your cooldown by forcing it to reset to 30
      } else if (needRefresh) {
        unit.agent.act("Refresh")
        clickShuttle(unit)
      }
    }
  }

  def clickShuttle(unit: FriendlyUnitInfo): Unit = {
    Commander.rightClick(unit, unit.agent.ride.get)
  }
}
