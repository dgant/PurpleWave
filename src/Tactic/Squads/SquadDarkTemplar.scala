package Tactic.Squads

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Points}
import Mathematics.Shapes.Spiral
import Micro.Agency.Intention
import Micro.Targeting.Target
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.{?, LightYear}
import Utilities.Time.Minutes
import Utilities.UnitCounters.CountEverything
import Utilities.UnitFilters.{IsMobileDetector, IsWorker}

import scala.collection.mutable.ArrayBuffer

class SquadDarkTemplar extends Squad {
  lock.matcher = Protoss.DarkTemplar
  lock.counter = CountEverything

  override def launch(): Unit = {
    bases.clear()
    bases ++= With.geography.bases
      .filterNot(_.zone.island)
      .filterNot(_.owner.isUs)
      .filterNot(_.metro.units.exists(u => u.isEnemy && IsMobileDetector(u)))
      .filterNot(_.enemies.exists(u =>
        u.complete
        && u.unitClass.isDetector
        && u.zone.edges.exists(_.sidePixels.exists(u.pixelDistanceCenter(_) <= u.sightPixels + 64))))
      .sortBy(_.heart.center.groundPixels(centroidGround))
      .sortBy( - With.scouting.baseIntrigue(_))
      .sortBy( ! _.owner.isEnemy)
      .sortBy(waitForBackstab && With.scouting.enemyProximity > 0.5 && ! _.naturalOf.exists(_.isEnemy))
    if (bases.isEmpty) return
    lock.acquire()
  }

  private val bases = new ArrayBuffer[Base]
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
      .filterNot(b => With.scouting.enemyMain.contains(b) || With.scouting.enemyNatural.contains(b))
      .headOption
      .map(_.townHallArea.center)
    With.logger.debug(f"Selected DT hidey hole: $output")
    output
  }

  private def intendDTToBase(dt: FriendlyUnitInfo, base: Base): Unit = {
    val bases = (Seq(base) ++ dt.base.filter(_.owner.isEnemy)).distinct

    lazy val engagedDetectors   = bases.flatMap(_.enemies.filter(u => u.unitClass.isDetector && u.matchups.threatsInRange.nonEmpty))
    lazy val killableDetectors  = bases.flatMap(_.enemies.filter(u => u.unitClass.isDetector && ( ! u.complete || ! u.canAttack(dt))))
    lazy val detectionMakers    = bases.flatMap(_.enemies.filter(Target.aidsDetection))
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
    val nearestWorker   = Maff.min(workers.map(w => dt.pixelDistanceTravelling(w.pixel))).getOrElse(LightYear().toDouble)
    val nearestDetector = dt.matchups.enemyDetectorDeepest.map(_.pixelsToSightRange(dt)).getOrElse(LightYear().toDouble)
    val nearestThreat   = dt.matchups.threatDeepest.filterNot(IsWorker).map(_.pixelsToGetInRange(dt)).getOrElse(LightYear().toDouble)
    val goBerserk       = nearestWorker < 256 || nearestWorker < nearestDetector || nearestWorker < nearestThreat
    dt.intend(this, new Intention {
      canFlee   = ! goBerserk
      toTravel  = Some(base.heart.center)
      targets   = Some(baseTargets) })
  }

  def run(): Unit = {
    if (bases.isEmpty) { lock.release(); return }
    val dts = new UnorderedBuffer[FriendlyUnitInfo]
    dts.addAll(units)
    val firstBase = bases.head
    var isFirstDT = true
    var iDt = 0
    while (dts.nonEmpty) {
      var base  = bases.headOption.getOrElse(firstBase)
      val dt    = dts.minBy(_.pixelDistanceTravelling(base.heart))
      val dtTargets = Some(base.enemies.toVector)
      if (base.isEnemy && ! base.owner.isZerg) {
        intendDTToBase(dt, base)
      } else {
        bases -= base

        val divisions = With.battles.divisions.filter(d => d.enemies.exists( ! _.flying) && ! d.enemies.exists(_.unitClass.isDetector))
        val division = Maff.minBy(divisions)(_.centroidGround.groundPixels(dt.pixel))

        division.foreach(division => {
          dt.intend(this, new Intention { toTravel = Some(base.heart.center); targets = Some(base.enemies) })
        })

        if (division.isEmpty) {
          if (waitForBackstab) {
            base = With.geography.preferredExpansionsEnemy.view
              .filterNot(With.scouting.enemyNatural.contains)
              .filterNot(With.scouting.enemyMain.contains)
              .drop(iDt).headOption.getOrElse(base)
          }
          intendDTToBase(dt, base)
        }
      }

      if (iDt == 0) {
        vicinity = dt.intent.toTravel.get
      }
      iDt += 1
      dts.remove(dt)
    }
  }
}
