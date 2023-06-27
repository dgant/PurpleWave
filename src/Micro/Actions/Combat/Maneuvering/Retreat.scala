package Micro.Actions.Combat.Maneuvering

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.{Maff, Shapes}
import Micro.Actions.Action
import Micro.Actions.Protoss.Shuttle.Shuttling
import Micro.Agency.Commander
import Micro.Coordination.Pathing.MicroPathing
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitFilters.IsTank

object Retreat extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.canMove && unit.matchups.threats.nonEmpty

  case class RetreatPlan(unit: FriendlyUnitInfo, to: Pixel, name: String) {
    def apply(unit: FriendlyUnitInfo): Boolean = {
      if (unit.ready) {
        unit.agent.toAttack = None // Affects traffic prioritization
        unit.agent.toTravel = Some(to)
        if (With.configuration.debugging) {
          unit.agent.act(f"Retreat$name")
        }
        Commander.move(unit)
      }
      unit.unready
    }
  }

  override def perform(unit: FriendlyUnitInfo): Unit = {
    getRetreat(unit)(unit)
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
    lazy val safetyIsSafe         = unit.agent.safety.tile.enemyRangeAgainst(unit) < Math.max(1, unit.tile.enemyRangeAgainst(unit))
    lazy val goalCircle           = unit.matchups.threats.forall(unit.effectiveRangePixels > _.effectiveRangePixels + 32) && unit.zone == unit.agent.home.zone
    lazy val goalReturn           = ! unit.agent.isScout && ! goalSidestep && (unit.zone == unit.agent.safety.zone && unit.pixelDistanceCenter(unit.agent.safety) >= 32 * unit.agent.safety.tile.enemyRangeAgainst(unit) && Shapes.Ray(unit.pixel, unit.agent.safety).forall(_.tile.enemyRangeAgainst(unit) <= unit.tile.enemyRangeAgainst(unit)))
    lazy val goalHome             = ! unit.agent.isScout && ! goalSidestep && (unit.zone != unit.agent.safety.zone && (enemyCloser || enemySooner))
    lazy val goalOrigin           = goalReturn || goalHome
    lazy val goalSafety           = ! unit.matchups.withinSafetyMargin
    lazy val forceVector = {
      if (unit.agent.forces.isEmpty) MicroPathing.setDefaultForces(unit, goalOrigin = goalOrigin, goalSafety = goalSafety)
      unit.agent.forces.sum
    }
    lazy val force = forceVector.radians

    if (unit.agent.ride.exists(_.pixelDistanceCenter(unit) < Shuttling.pickupRadius + 32) && unit.matchups.targetsInRange.isEmpty) {
      Commander.rightClick(unit, unit.agent.ride.get)
      return RetreatPlan(unit, unit.agent.ride.get.pixel, "Hail")
    }

    // If the return point didn't meet our criteria, let the unit retreat all the way home
    if ( ! goalReturn) {
      unit.agent.toReturn = None
    }

    // Against melee rush: Retreat directly to heart so workers can help
    if (With.frame < Minutes(6)() && unit.isAny(Terran.Marine, Protoss.Zealot) && unit.squad.forall(_.units.size < 5) && unit.metro.contains(With.geography.ourMetro) && unit.matchups.threats.forall(_.pixelRangeAgainst(unit) < 64)) {
      val baseToRunTo = Maff.orElse(With.geography.ourBases, Seq(With.geography.ourMain)).minBy(_.heart.groundTiles(With.scouting.enemyThreatOrigin))
      return RetreatPlan(unit, baseToRunTo.heart.center, "Run")
    }

    // Enshuttled VIPs: If not primary passenger, retreat towards primary passenger
    val leadPassenger = unit.agent.ride.flatMap(_.agent.passengersPrioritized.headOption)
    if (leadPassenger.isDefined && ! leadPassenger.contains(unit)) {
      return RetreatPlan(unit, leadPassenger.get.pixel.add(forceVector.normalize(32).toPoint.asPixel.walkablePixel), "Shotgun")
    }

    lazy val waypointSimple   = MicroPathing.getWaypointInDirection(unit, force, ?(goalOrigin, Some(unit.agent.safety), None), requireSafety = goalSafety && safetyIsSafe).map((_, "Simple"))
    lazy val waypointPath     = MicroPathing.getWaypointAlongTilePath(unit, tilePath).map(_.add(unit.pixel.offsetFromTileCenter)).map((_, "Path"))
    lazy val waypointForces   = Seq(true, false).view.map(safety => MicroPathing.getWaypointInDirection(unit, force, requireSafety = safety)).find(_.nonEmpty).flatten.map((_, "Force"))
    lazy val waypointOrigin   = (unit.agent.safety, "Origin")
    lazy val tilePath         = MicroPathing.getThreatAwarePath(unit, preferHome = goalOrigin)
    val waypoint              = waypointSimple.orElse(waypointPath).orElse(waypointForces).getOrElse(waypointOrigin)
    RetreatPlan(unit, waypoint._1, waypoint._2)
  }
}
