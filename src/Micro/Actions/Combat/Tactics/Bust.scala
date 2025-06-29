package Micro.Actions.Combat.Tactics

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Mathematics.Shapes.Arc
import Micro.Actions.Action
import Micro.Actions.Combat.Maneuvering.Retreat
import Micro.Agency.Commander
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.Time.Minutes

object Bust extends Action {
  
  // TODO:
  // jaj22: quick bunker test, range goon has at least a 33 pixel variation for where it stops on a straight line attack command.
  // Conclusion: We need to identify where Dragoons are supposed to stand to shoot at a Bunker, then tell them to Hold Position
  
  // Killing bunkers with Dragoons is an important skill that we can't yet perform on first princples.
  // Range-upgraded Dragoons just barely outrange a Bunker containing non-range-upgraded Marines.
  
  protected def safeFromThreat(dragoon: FriendlyUnitInfo, threat: UnitInfo, pixel: Pixel): Boolean = (
    threat.isAny(Terran.Bunker, Terran.SCV, Terran.Marine, Terran.Vulture)  || threat.pixelsToGetInRange(dragoon) > 48
  )
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    With.frame < Minutes(8)() // Performance short-circuit
    && Protoss.Dragoon(unit)
    && Protoss.DragoonRange()
    && unit.shieldPoints > 12 // Don't take hull damage
    && unit.totalHealth > 24
    && unit.intent.canFight
    && With.enemies.exists(e => e.isTerran && ! Terran.MarineRange(e))
    && unit.matchups.threatDeepest.exists(Terran.Bunker)
    && unit.matchups.threats.forall(safeFromThreat(unit, _, unit.pixel))
    && unit.matchups.targets.exists(bunker =>
      Terran.Bunker(bunker)
      && (bunker.visible || bunker.altitude <= unit.altitude)
      && unit.matchups.threats.filterNot(bunker==).forall(threat =>
        threat.pixelsToGetInRange(unit)
        > Math.max(bunker.pixelsToGetInRange(unit), unit.pixelsToGetInRange(bunker)))))

  override protected def perform(unit: FriendlyUnitInfo): Unit = {
    lazy val bunkers    = unit.matchups.targets.filter(Terran.Bunker)
    lazy val repairers  = bunkers.flatMap(_.matchups.healers)
    lazy val goons      = unit.alliesSquad
      .flatMap(_.friendly)
      .filter(Bust.allowed)
      .filter(u => bunkers.exists(b => u.framesToGetInRange(b) < 24))

    // Few-shot repairers
    if (unit.readyForAttackOrder && repairers.nonEmpty && goons.size >= repairers.map(_.hitPoints).min / 20) {
      unit.agent.toAttack = Some(repairers
        .sortBy(_.pixelDistanceCenter(Maff.centroid(goons.map(_.pixel))))
        .minBy(_.hitPoints))
      Commander.attack(unit)
    }

    if (unit.matchups.engagedUpon || With.framesSince(unit.lastFrameTakingDamage) < 12) {
      Retreat.delegate(unit)
    }

    unit.agent.toAttack = Maff.minBy(unit.matchups.targets.filter(_.canAttackGround))(unit.pixelDistanceEdge)

    if (unit.agent.toAttack.exists(Terran.Bunker)) {

      val bunker = unit.agent.toAttack.get
      val bunkerPixel = bunker.pixel
      val bunkerAltitude = bunker.altitude
      val bunkerDistance = unit.pixelDistanceEdge(bunker)

      if (unit.inRangeToAttack(bunker) && bunker.visible) {
        // Hold Position should result in an attack faster than Stop:
        // https://docs.google.com/spreadsheets/d/1LhC8rdqHTrhze6Gh7HitemGs-g2Xq-hVRDaA6X4bBnM/edit#gid=1092732143
        if (unit.moving) Commander.hold(unit) else Commander.attack(unit)
        return
      }

      val station = Arc(
        bunkerPixel,
        unit.pixel,
        unit.pixelRangeAgainst(bunker) + unit.unitClass.dimensionMin + bunker.unitClass.dimensionMin,
        8.0, 3, Math.PI / 2)
        .map(bunkerPixel.add)
        .find(station =>
          station.traversableBy(unit)
          && station.altitude >= bunkerAltitude
          && ! unit.alliesSquad.exists(a => ! a.flying && unit.pixelDistanceEdge(a, station) < 4 && a.pixelDistanceEdge(bunker) < bunkerDistance)
          && ! unit.matchups.threats.exists(_.inRangeToAttack(unit, station)))

      if (station.exists(_.pixelDistance(unit.pixel) > ?(unit.pixel == unit.previousPixel(4), 8, 0))) {
        unit.agent.decision.set(station)
        Commander.move(unit)
      }
    }
    Commander.attack(unit)
  }
}
