package Tactic.Squads

import Information.Battles.Types.Division
import Information.Geography.Types.Base
import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Mathematics.Points.{Pixel, Points}
import Mathematics.Shapes.Spiral
import Micro.Targeting.TargetScoring
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.Time.Minutes
import Utilities.UnitCounters.CountEverything
import Utilities.UnitFilters.{IsMobileDetector, IsWorker}
import Utilities.{?, LightYear}

import scala.collection.mutable.ArrayBuffer

class SquadDarkTemplar extends Squad {
  lock.matcher = Protoss.DarkTemplar
  lock.counter = CountEverything

  override def launch(): Unit = {
    if ( ! With.blackboard.darkTemplarSquad()) return
    bases.clear()
    bases ++= With.geography.bases
      .filterNot(_.zone.island)
      .filterNot(_.owner.isUs)
      .filterNot(_.metro.units.exists(u => u.isEnemy && IsMobileDetector(u)))
      .filterNot(entranceDetected)
      .sortBy(_.heart.center.groundPixels(centroidGround))
      .sortBy(base => - With.scouting.baseIntrigue.getOrElse(base, 0.0))
      .sortBy( ! _.owner.isEnemy)
      .sortBy(waitForBackstab && With.scouting.enemyProximity > 0.5 && ! _.naturalOf.exists(_.isEnemy))
    if (bases.isEmpty && vulnerableDivisions.isEmpty) return
    lock.acquire()
  }

  private val bases = new ArrayBuffer[Base]
  private[Squads] def entranceDetected(base: Base): Boolean = With.units.enemy.exists(u =>
    u.complete
    && u.unitClass.isDetector
    && base.zone.edges.exists(_.sidePixels.exists(u.pixelDistanceCenter(_) <= u.sightPixels + 64)))
  private def vulnerableDivisions: Vector[Division] = With.battles.divisions.filter(d => d.enemies.exists( ! _.flying) && d.mobileDetectors.isEmpty && d.centroidGround.proximity > 0.3 && ! d.centroidGround.base.exists(_.isEnemy))
  private def waitForBackstab: Boolean = With.enemy.isProtoss && With.frame < Minutes(9)() && With.units.enemy.filter(IsMobileDetector).forall(o => With.scouting.proximity(o.pixel) < 0.5)
  private lazy val backstabTargetBase     = With.scouting.enemyNatural.getOrElse(Maff.orElse(With.geography.bases.filter(_.naturalOf.isDefined), With.geography.bases).minBy(_.heart.groundTiles(With.scouting.enemyHome)))
  private lazy val backstabTarget         = backstabTargetBase.zone.exitOriginal.map(_.pixelCenter).getOrElse(Points.middle.midpoint(backstabTargetBase.heart.center)).walkableTile
  private lazy val backstabTargetDistance = backstabTarget.groundTiles(With.geography.home)
  private lazy val hideyholeSpiral        = Spiral(48).map(backstabTarget.add).filter(_.walkable).filterNot(_.metro.exists(_.bases.exists(_.isEnemy)))
  private lazy val hideyhole: Option[Pixel] = {
    /*
    val output = hideyholeSpiral
      .find(With.grids.scoutingPathsStartLocations(_) > 16)
      .map(_.center)
      .orElse(With.geography.preferredExpansionsEnemy.filterNot(_.naturalOf.exists(_.isEnemy)).headOption.map(b => b.zone.exitNow.map(_.pixelCenter).getOrElse(b.heart.center)))
     */
    val output = With.geography.preferredExpansionsEnemy.view
      .filterNot(_.metro.bases.exists(With.scouting.enemyMain.contains))
      .headOption
      .map(_.townHallArea.center)
    With.logger.debug(f"Selected DT hidey hole: $output")
    output
  }

  private def intendDTToBase(dt: FriendlyUnitInfo, base: Base): Unit = {
    val bases = (Seq(base) ++ dt.base.filter(_.owner.isEnemy)).distinct

    lazy val engagedDetectors   = bases.flatMap(_.enemies.filter(u => u.unitClass.isDetector && u.matchups.threatsInRange.nonEmpty))
    lazy val killableDetectors  = bases.flatMap(_.enemies.filter(u => u.unitClass.isDetector && ( ! u.complete || ! u.canAttack(dt))))
    lazy val detectionMakers    = bases.flatMap(_.enemies.filter(TargetScoring.aidsDetection))
    lazy val workers            = bases.flatMap(_.enemies.filter(IsWorker))
    lazy val townHall           = bases.flatMap(_.townHall.toVector)
    lazy val hasMobileDetectors = With.units.existsEnemy(IsMobileDetector)

    val baseTargets =
      Maff.orElse(
        Seq(
          ?(hasMobileDetectors, workers, Seq.empty), // If they already have mobile detection, we just want worker kills
          engagedDetectors,
          killableDetectors,
          detectionMakers,
          workers,
          townHall)
        .map(_.filter(dt.canAttack)): _*).toVector
          .sortBy(_.totalHealth)
          .sortBy(_.pixelDistanceEdge(dt))
          .sortBy(_.remainingCompletionFrames)

    // Go berserk if we have a shot at workers
    val nearestDetector = dt.matchups.enemyDetectorDeepest.map(_.pixelsToSightRange(dt)).getOrElse(LightYear().toDouble)
    val nearestThreat   = dt.matchups.threatDeepest.filterNot(IsWorker).map(_.pixelsToGetInRange(dt)).getOrElse(LightYear().toDouble)
    val nearestMiner    = Maff.min(workers
      .filter(w => w.base.exists(b => w.pixelDistanceCenter(b.townHallArea.center) < 256))
      .map(w => dt.pixelDistanceTravelling(w.pixel))).getOrElse(LightYear().toDouble)
    val goBerserk       = (nearestMiner < 256 || nearestMiner < nearestDetector + 64 || nearestMiner < nearestThreat + 64) && ( ! dt.intent.canFlee || MacroFacts.enemiesComplete(Terran.MissileTurret, Protoss.PhotonCannon) == 0)
    dt.intend(this)
      .setCanFlee( ! goBerserk)
      .setTerminus(base.heart.center)
      .setTargets(baseTargets)
  }

  private def intendDTToDivision(dt: FriendlyUnitInfo, divisions: Vector[Division]): Unit = {
    val division = divisions.minBy(_.keyDistanceTo(dt.pixel))
    dt.intend(this).setTerminus(division.centroidGround).setTargets(division.enemies)
  }

  def run(): Unit = {
    val divisions = vulnerableDivisions
    if (bases.isEmpty) {
      if (divisions.isEmpty) { lock.release(); return }
      units.foreach(intendDTToDivision(_, divisions))
      units.headOption.flatMap(_.intent.terminus).foreach(vicinity = _)
      return
    }
    val dts = new UnorderedBuffer[FriendlyUnitInfo]
    dts.addAll(units)
    val firstBase = bases.head
    var isFirstDT = true
    var iDt = 0
    while (dts.nonEmpty) {
      var base  = bases.headOption.getOrElse(firstBase)
      val dt    = dts.minBy(dt => dt.pixelDistanceTravelling(base.heart) * ?(dt.orderTargetPixel.orElse(dt.orderTarget.map(_.pixel)).exists(_.base.contains(base)), 0.75, 1.0))
      val dtTargets = Some(base.enemies.toVector)
      if (base.isEnemy && ! base.owner.isZerg) {
        intendDTToBase(dt, base)
      } else {
        bases -= base

        if (divisions.isEmpty) {
          if (waitForBackstab) {
            base = With.geography.preferredExpansionsEnemy.view
              .filterNot(With.scouting.enemyNatural.contains)
              .filterNot(With.scouting.enemyMain.contains)
              .drop(iDt).headOption.getOrElse(base)
          }
          intendDTToBase(dt, base)
        } else {
          intendDTToDivision(dt, divisions)
        }
      }

      if (iDt == 0) {
        vicinity = dt.intent.terminus.get
      }
      iDt += 1
      dts.remove(dt)
    }
  }
}
