package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.TrafficPriorities
import Planning.UnitMatchers.MatchTank
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

object Retreat extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.matchups.threats.nonEmpty

  case class RetreatPlan(unit: FriendlyUnitInfo, to: Pixel, name: String)

  override def perform(unit: FriendlyUnitInfo): Unit = {
    applyRetreat(getRetreat(unit))
  }

  def getRetreat(unit: FriendlyUnitInfo): RetreatPlan = {
    // Decide our goals in retreating
    def timeOriginOfThreat(threat: UnitInfo): Double = threat.framesToTravelTo(unit.agent.origin) - threat.pixelRangeAgainst(unit) * threat.topSpeed
    lazy val distanceOriginUs     = unit.pixelDistanceTravelling(unit.agent.origin)
    lazy val distanceOriginEnemy  = Maff.min(unit.matchups.threats.view.map(t => t.pixelDistanceTravelling(unit.agent.origin) - t.pixelRangeAgainst(unit))).getOrElse(2.0 * With.mapPixelWidth)
    lazy val enemyCloser          = distanceOriginUs + 160 >= distanceOriginEnemy
    lazy val timeOriginUs         = unit.framesToTravelTo(unit.agent.origin)
    lazy val timeOriginEnemy      = Maff.takePercentile(0.1, unit.matchups.threats)(Ordering.by(timeOriginOfThreat)).map(timeOriginOfThreat).getOrElse(Double.PositiveInfinity)
    lazy val enemySooner          = timeOriginUs + 96 >= timeOriginEnemy
    lazy val enemySieging         = unit.matchups.enemies.exists(_.isAny(MatchTank, Zerg.Lurker)) && ! unit.base.exists(_.owner.isEnemy)
    lazy val goalSidestep         = unit.is(Protoss.DarkTemplar) || (enemySieging && ! enemyCloser && ! enemySooner)
    lazy val goalHome             = ! unit.agent.isScout && unit.zone != unit.agent.origin.zone && ! goalSidestep && (enemyCloser || enemySooner)
    lazy val goalSafety           = ! unit.agent.withinSafetyMargin

    // Decide how to retreat
    if (unit.isAny(Terran.Marine, Protoss.Zealot) && unit.metro.contains(With.geography.ourMetro) && unit.matchups.threats.forall(_.pixelRangeAgainst(unit) < 64)) {
      return RetreatPlan(unit, With.geography.ourMain.heart.center, "Run")
    }
    if ( ! unit.airborne) {
      unit.agent.escalatePriority(TrafficPriorities.Pardon)
      if (unit.matchups.pixelsOfEntanglement > -80) unit.agent.escalatePriority(TrafficPriorities.Nudge)
      if (unit.matchups.pixelsOfEntanglement > -48) unit.agent.escalatePriority(TrafficPriorities.Bump)
      if (unit.matchups.pixelsOfEntanglement > -16) unit.agent.escalatePriority(TrafficPriorities.Shove)
    }
    if (unit.agent.forces.isEmpty) {
      MicroPathing.setDefaultForces(unit, goalHome = goalHome, goalSafety = goalSafety)
    }
    lazy val force            = unit.agent.forces.sum.radians
    lazy val tilePath         = MicroPathing.getThreatAwarePath(unit, preferHome = goalHome)
    lazy val waypointSimple   = MicroPathing.getWaypointInDirection(unit, force, if (goalHome) Some(unit.agent.origin) else None, requireSafety = goalSafety).map((_, "Simple"))
    lazy val waypointPath     = MicroPathing.getWaypointAlongTilePath(tilePath).map(_.add(unit.pixel.offsetFromTileCenter)).map((_, "Path"))
    lazy val waypointForces   = Seq(true, false).view.map(safety => MicroPathing.getWaypointInDirection(unit, force, requireSafety = safety)).find(_.nonEmpty).flatten.map((_, "Force"))
    lazy val waypointOrigin   = (unit.agent.origin, "Origin")
    val waypoint              = waypointSimple.orElse(waypointPath).orElse(waypointForces).getOrElse(waypointOrigin)
    RetreatPlan(unit, waypoint._1, waypoint._2)
  }

  def applyRetreat(retreat: RetreatPlan): Unit = {
    if (retreat.unit.unready) return
    retreat.unit.agent.toTravel = Some(retreat.to)
    if (With.configuration.debugging) {
      retreat.unit.agent.act("Retreat" + retreat.name)
    }
    Commander.move(retreat.unit)
  }
}
