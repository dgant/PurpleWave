package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Micro.Actions.Action
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.TrafficPriorities
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, TakeN}

object Retreat extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.matchups.threats.nonEmpty

  override def perform(unit: FriendlyUnitInfo): Unit = {
    // Decide our goals in retreating
    def timeOriginOfThreat(threat: UnitInfo): Double = threat.framesToTravelTo(unit.agent.origin) - threat.pixelRangeAgainst(unit) * threat.topSpeed
    lazy val distanceOriginUs     = unit.pixelDistanceTravelling(unit.agent.origin)
    lazy val distanceOriginEnemy  = ByOption.min(unit.matchups.threats.view.map(t => t.pixelDistanceTravelling(unit.agent.origin) - t.pixelRangeAgainst(unit))).getOrElse(2.0 * With.mapPixelWidth)
    lazy val enemyCloser          = distanceOriginUs + 160 >= distanceOriginEnemy
    lazy val timeOriginUs         = unit.framesToTravelTo(unit.agent.origin)
    lazy val timeOriginEnemy      = TakeN.percentile(0.1, unit.matchups.threats)(Ordering.by(timeOriginOfThreat)).map(timeOriginOfThreat).getOrElse(Double.PositiveInfinity)
    lazy val enemySooner          = timeOriginUs + 96 >= timeOriginEnemy
    lazy val enemySieging         = unit.matchups.enemies.exists(_.isAny(UnitMatchSiegeTank, Zerg.Lurker)) && ! unit.base.exists(_.owner.isEnemy)
    lazy val goalSidestep         = unit.is(Protoss.DarkTemplar) || (enemySieging && ! enemyCloser && ! enemySooner)
    lazy val goalHome             = ! unit.agent.isScout && unit.zone != unit.agent.origin.zone && ! goalSidestep && (enemyCloser || enemySooner)
    lazy val goalSafety           = unit.matchups.pixelsOfEntanglement > -64

    // Decide how to retreat
    if ( ! unit.flying) {
      unit.agent.escalatePriority(TrafficPriorities.Pardon)
      if (unit.matchups.framesOfSafety < 48) unit.agent.escalatePriority(TrafficPriorities.Nudge)
      if (unit.matchups.framesOfSafety < 24) unit.agent.escalatePriority(TrafficPriorities.Bump)
      if (unit.matchups.framesOfSafety <= 0) unit.agent.escalatePriority(TrafficPriorities.Shove)
    }
    if (unit.agent.forces.isEmpty) {
      MicroPathing.setDefaultForces(unit, goalHome = goalHome, goalSafety = goalSafety)
    }
    lazy val force            = unit.agent.forces.sum.radians
    lazy val tilePath         = MicroPathing.getRealPath(unit, preferHome = goalHome)
    lazy val waypointSimple   = MicroPathing.getWaypointInDirection(unit, force, if (goalHome) Some(unit.agent.origin) else None, requireSafety = goalSafety).map((_, "Simple"))
    lazy val waypointPath     = MicroPathing.getWaypointAlongTilePath(tilePath).map(_.add(unit.pixelCenter.relativeToTileCenter)).map((_, "Path"))
    lazy val waypointForces   = Seq(true, false).view.map(safety => MicroPathing.getWaypointInDirection(unit, force, requireSafety = safety)).find(_.nonEmpty).flatten.map((_, "Force"))
    lazy val waypointOrigin   = (unit.agent.origin, "Origin")
    val waypoint = waypointSimple.orElse(waypointPath).orElse(waypointForces).getOrElse(waypointOrigin)
    unit.agent.toTravel = Some(waypoint._1)
    unit.agent.act(unit.agent.lastAction.getOrElse("Retreat") + waypoint._2)
    With.commander.move(unit)
  }
}
