package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Actions.Action
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.TrafficPriorities
import Planning.UnitMatchers.UnitMatchSiegeTank
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, TakeN}

object Retreat extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.matchups.threats.nonEmpty

  case class DesireProfile(var home: Int = 0, var safety: Int = 0) {
    def this(unit: FriendlyUnitInfo) {
      this(0, 0)
      def timeOriginOfThreat(threat: UnitInfo): Double = threat.framesToTravelTo(unit.agent.origin) - threat.pixelRangeAgainst(unit) * threat.topSpeed
      lazy val distanceOriginUs    = unit.pixelDistanceTravelling(unit.agent.origin)
      lazy val distanceOriginEnemy = ByOption.min(unit.matchups.threats.view.map(t => t.pixelDistanceTravelling(unit.agent.origin) - t.pixelRangeAgainst(unit))).getOrElse(2.0 * With.mapPixelWidth)
      lazy val enemyCloser         = distanceOriginUs + 160 >= distanceOriginEnemy
      lazy val timeOriginUs        = unit.framesToTravelTo(unit.agent.origin)
      lazy val timeOriginEnemy     = TakeN.percentile(0.1, unit.matchups.threats)(Ordering.by(timeOriginOfThreat)).map(timeOriginOfThreat).getOrElse(Double.PositiveInfinity)
      lazy val enemySooner         = timeOriginUs + 96 >= timeOriginEnemy
      lazy val enemySieging        = unit.matchups.enemies.exists(_.isAny(UnitMatchSiegeTank, Zerg.Lurker)) && ! unit.base.exists(_.owner.isEnemy)
      home =
        if (unit.is(Protoss.DarkTemplar))
          -1
        else if (enemySieging && ! enemyCloser && ! enemySooner)
          -1
        else if (unit.agent.isScout || unit.zone == unit.agent.origin.zone)
          0
        else if (unit.base.exists(_.owner.isEnemy))
          2
        else
          ((if (enemyCloser) 1 else 0) + (if (enemySooner) 1 else 0))
      safety = PurpleMath.clamp(0, 3, (3 * (1 - unit.matchups.framesOfSafety / 72d)).toInt)
    }
  }

  override def perform(unit: FriendlyUnitInfo): Unit = {
    if ( ! unit.flying) {
      unit.agent.escalatePriority(TrafficPriorities.Pardon)
      if (unit.matchups.framesOfSafety < 48) unit.agent.escalatePriority(TrafficPriorities.Nudge)
      if (unit.matchups.framesOfSafety < 24) unit.agent.escalatePriority(TrafficPriorities.Bump)
      if (unit.matchups.framesOfSafety <= 0) unit.agent.escalatePriority(TrafficPriorities.Shove)
    }
    val desire = new DesireProfile(unit)
    if (unit.agent.forces.isEmpty) {
      MicroPathing.setDefaultForces(unit, desire)
    }
    lazy val force            = unit.agent.forces.sum.radians
    lazy val tilePath         = MicroPathing.getRealPath(unit, preferHome = desire.home > 0)
    lazy val waypointSimple   = MicroPathing.getWaypointInDirection(unit, force, desire).map((_, "RetreatSimple"))
    lazy val waypointPath     = MicroPathing.getWaypointAlongTilePath(tilePath).map((_, "RetreatPath"))
    lazy val waypointForces   = MicroPathing.getWaypointInDirection(unit, force).map((_, "RetreatForce"))
    lazy val waypointOrigin   = (unit.agent.origin, "RetreatOrigin")
    val waypoint = waypointSimple.orElse(waypointPath).orElse(waypointForces).getOrElse(waypointOrigin)
    unit.agent.toTravel = Some(waypoint._1)
    unit.agent.act(waypoint._2)
    With.commander.move(unit)
  }
}
