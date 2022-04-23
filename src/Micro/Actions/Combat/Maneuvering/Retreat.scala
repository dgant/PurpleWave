package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, PixelRay}
import Micro.Actions.Action
import Micro.Actions.Protoss.Shuttle.Shuttling
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import Micro.Coordination.Pushing.TrafficPriorities
import Utilities.UnitFilters.IsTank
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Time.Minutes

object Retreat extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.matchups.threats.nonEmpty

  case class RetreatPlan(unit: FriendlyUnitInfo, to: Pixel, name: String)

  override def perform(unit: FriendlyUnitInfo): Unit = {
    applyRetreat(getRetreat(unit))
  }

  def getRetreat(unit: FriendlyUnitInfo): RetreatPlan = {
    // Decide our goals in retreating
    def timeOriginOfThreat(threat: UnitInfo): Double = threat.framesToTravelTo(unit.agent.safety) - threat.pixelRangeAgainst(unit) * threat.topSpeed
    lazy val distanceOriginUs     = unit.pixelDistanceTravelling(unit.agent.safety)
    lazy val distanceOriginEnemy  = Maff.min(unit.matchups.threats.view.map(t => t.pixelDistanceTravelling(unit.agent.safety) - t.pixelRangeAgainst(unit))).getOrElse(2.0 * With.mapPixelWidth)
    lazy val enemyCloser          = distanceOriginUs + 160 >= distanceOriginEnemy
    lazy val timeOriginUs         = unit.framesToTravelTo(unit.agent.safety)
    lazy val timeOriginEnemy      = Maff.takePercentile(0.1, unit.matchups.threats)(Ordering.by(timeOriginOfThreat)).map(timeOriginOfThreat).headOption.getOrElse(Double.PositiveInfinity)
    lazy val enemySooner          = timeOriginUs + 96 >= timeOriginEnemy
    lazy val enemySieging         = unit.matchups.enemies.exists(_.isAny(IsTank, Zerg.Lurker)) && ! unit.base.exists(_.owner.isEnemy)
    lazy val goalSidestep         = Protoss.DarkTemplar(unit) || (enemySieging && ! enemyCloser && ! enemySooner)
    lazy val safetyIsSafe         = unit.agent.safety.tile.enemyRangeAgainst(unit) < unit.tile.enemyRangeAgainst(unit)
    lazy val goalReturn           = ! unit.agent.isScout && ! goalSidestep && (unit.zone == unit.agent.safety.zone && unit.pixelDistanceCenter(unit.agent.safety) >= 32 * unit.agent.safety.tile.enemyRangeAgainst(unit) && PixelRay(unit.pixel, unit.agent.safety).forall(_.enemyRangeAgainst(unit) <= unit.tile.enemyRangeAgainst(unit)))
    lazy val goalHome             = ! unit.agent.isScout && ! goalSidestep && (unit.zone != unit.agent.safety.zone && (enemyCloser || enemySooner))
    lazy val goalOrigin           = goalReturn || goalHome
    lazy val goalSafety           = ! unit.agent.withinSafetyMargin
    lazy val forceVector = {
      if (unit.agent.forces.isEmpty) MicroPathing.setDefaultForces(unit, goalOrigin = goalOrigin, goalSafety = goalSafety)
      unit.agent.forces.sum
    }
    lazy val force = forceVector.radians

    if (unit.agent.ride.exists(_.pixelDistanceCenter(unit) < Shuttling.pickupRadius) && unit.matchups.targetsInRange.isEmpty) {
      Commander.rightClick(unit, unit.agent.ride.get)
      return RetreatPlan(unit, unit.agent.ride.get.pixel, "Hail")
    }

    // If the return point didn't meet our criteria, let the unit retreat all the way home
    if ( ! goalReturn) {
      unit.agent.toReturn = None
    }

    // Ground units: Shove your way through
    if ( ! unit.airborne) {
      unit.agent.escalatePriority(TrafficPriorities.Pardon)
      if (unit.matchups.pixelsOfEntanglement > -80) unit.agent.escalatePriority(TrafficPriorities.Nudge)
      if (unit.matchups.pixelsOfEntanglement > -48) unit.agent.escalatePriority(TrafficPriorities.Bump)
      if (unit.matchups.pixelsOfEntanglement > -16) unit.agent.escalatePriority(TrafficPriorities.Shove)

    // Against melee rush: Retreat directly to heart so workers can help
    if (With.frame < Minutes(6)() && unit.isAny(Terran.Marine, Protoss.Zealot) && unit.squad.forall(_.units.size < 5) && unit.metro.contains(With.geography.ourMetro) && unit.matchups.threats.forall(_.pixelRangeAgainst(unit) < 64)) {
      val baseToRunTo = Maff.orElse(With.geography.ourBases, Seq(With.geography.ourMain)).minBy(_.heart.groundTiles(With.scouting.enemyThreatOrigin))
      return RetreatPlan(unit, baseToRunTo.heart.center, "Run")
    }

    // Enshuttled VIPs: If not primary passenger, retreat towards primary passenger
    val leadPassenger = unit.agent.ride.flatMap(_.agent.passengersPrioritized.headOption)
    if (leadPassenger.isDefined && ! leadPassenger.contains(unit)) {
      return RetreatPlan(unit, leadPassenger.get.pixel.add(forceVector.normalize(32).toPoint.asPixel.walkablePixel), "Shotgun")
    }}

    lazy val waypointSimple   = MicroPathing.getWaypointInDirection(unit, force, if (goalOrigin) Some(unit.agent.safety) else None, requireSafety = goalSafety && safetyIsSafe).map((_, "Simple"))
    lazy val waypointPath     = MicroPathing.getWaypointAlongTilePath(unit, tilePath).map(_.add(unit.pixel.offsetFromTileCenter)).map((_, "Path"))
    lazy val waypointForces   = Seq(true, false).view.map(safety => MicroPathing.getWaypointInDirection(unit, force, requireSafety = safety)).find(_.nonEmpty).flatten.map((_, "Force"))
    lazy val waypointOrigin   = (unit.agent.safety, "Origin")
    lazy val tilePath         = MicroPathing.getThreatAwarePath(unit, preferHome = goalOrigin)
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
