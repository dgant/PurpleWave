package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.Points.Pixel
import Mathematics.Maff
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Time.{Minutes, Seconds}
import bwapi.Race

import scala.util.Random

object Bust extends Action {
  
  // TODO:
  // jaj22: quick bunker test, range goon has at least a 33 pixel variation for where it stops on a straight line attack command.
  // Conclusion: We need to identify where Dragoons are supposed to stand to shoot at a Bunker, then tell them to Hold Position
  
  // Killing bunkers with Dragoons is an important technique that we can't yet perform on first princples.
  // Range-upgraded Dragoons just barely outrange a Bunker containing non-range-upgraded Marines.
  
  protected def safeFromThreat(
    dragoon: FriendlyUnitInfo,
    threat: UnitInfo,
    pixel: Pixel): Boolean = (
    threat.isAny(Terran.Bunker, Terran.SCV, Terran.Marine, Terran.Vulture)
    || threat.pixelDistanceEdge(dragoon, pixel) > threat.pixelRangeAgainst(dragoon) + 48.0
  )
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.frame < Minutes(8)() // Performance short-circuit
    && unit.shieldPoints > 12 // Don't take hull damage
    && unit.totalHealth > 24
    && With.enemies.exists(_.raceCurrent == Race.Terran)
    && unit.intent.canFight
    && unit.canMove
    && unit.is(Protoss.Dragoon)
    && With.self.hasUpgrade(Protoss.DragoonRange)
    && unit.matchups.threats.forall(threat => safeFromThreat(unit, threat, unit.pixel))
    && unit.matchups.targets.exists(bunker =>
      (bunker.visible || bunker.altitude <= unit.altitude)
      && bunker.aliveAndComplete
      && bunker.is(Terran.Bunker)
      && ! bunker.player.hasUpgrade(Terran.MarineRange)
      && unit.matchups.threats.forall(threat =>
        threat == bunker || (
          threat.pixelsToGetInRange(unit) > bunker.pixelsToGetInRange(unit)
          && safeFromThreat(
            unit,
            threat,
            unit.pixel.project(bunker.pixel, unit.pixelDistanceEdge(bunker) - unit.pixelRangeAgainst(bunker)))))))
  
  override protected def perform(unit: FriendlyUnitInfo) {
    // Goal: Take down the bunker. Don't take any damage from it.
    // If we're getting shot at by the bunker, back off.
    lazy val bunkers = unit.matchups.targets.filter(Terran.Bunker)
    lazy val repairers = bunkers.flatMap(_.matchups.repairers)
    lazy val goons = unit.alliesSquad.filter(u => u.friendly.exists(Bust.allowed) && bunkers.exists(b => u.framesToGetInRange(b) < 24))

    if (unit.readyForAttackOrder && repairers.nonEmpty && goons.size >= repairers.map(_.hitPoints).min / 10) {
      unit.agent.toAttack = Some(repairers.sortBy(_.pixelDistanceCenter(Maff.centroid(goons.map(_.pixel)))).minBy(_.hitPoints))
      Commander.attack(unit)
    }

    if (With.framesSince(unit.lastFrameTakingDamage) < Seconds(1)()) {
      Retreat.delegate(unit)
    }

    unit.agent.toAttack = Maff.minBy(unit.matchups.targets)(_.pixelDistanceEdge(unit))
    if (unit.agent.toAttack.exists(t => t.unitClass == Terran.Bunker && ! unit.inRangeToAttack(t))) {
      val bunker = unit.agent.toAttack.get
      val range = unit.pixelRangeAgainst(bunker)
      val bunkerDistance = unit.pixelDistanceEdge(bunker)
      def stationAcceptable(pixel: Pixel) = (
        pixel.walkable
        && ! unit.alliesSquad.exists(a =>
          ! a.flying
          && unit.pixelDistanceEdge(a, pixel) < 4
          && a.pixelDistanceEdge(bunker) < bunkerDistance)
        && ! unit.matchups.enemies.exists(_.inRangeToAttack(unit, pixel)))
      var station = Some(unit.pixel.project(bunker.pixel, Math.max(Random.nextInt(9), bunkerDistance - range))).filter(stationAcceptable)
      if (station.isEmpty && bunkerDistance > range + 32) {
        val stationCount = 256
        val stations = (0 until stationCount)
          .map(_ * 2 * Math.PI / stationCount)
          .map(bunker.pixel.radiateRadians(_ , range + unit.unitClass.dimensionMax + bunker.unitClass.dimensionMax))
          .filter(stationAcceptable)
        station = Maff.minBy(stations)(unit.pixelDistanceCenter)
      }
      if (station.nonEmpty) {
        unit.agent.toTravel = station
        Commander.move(unit)
      }
    }
    Commander.attack(unit)
  }
}
