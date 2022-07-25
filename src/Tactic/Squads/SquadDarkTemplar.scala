package Tactic.Squads

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Points}
import Mathematics.Shapes.Spiral
import Micro.Agency.Intention
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import ProxyBwapi.UnitTracking.UnorderedBuffer
import Utilities.Time.Minutes
import Utilities.UnitCounters.CountEverything
import Utilities.UnitFilters.IsMobileDetector

import scala.collection.mutable.ArrayBuffer

class SquadDarkTemplar extends Squad {
  lock.matcher = Protoss.DarkTemplar
  lock.counter = CountEverything

  override def launch(): Unit = {
    bases.clear()
    bases ++= With.geography.bases
      .filterNot(_.owner.isUs)
      .filterNot(_.metro.units.exists(u => u.isEnemy && IsMobileDetector(u)))
      .filterNot(_.enemies.exists(u =>
        u.complete
        && u.unitClass.isDetector
        && u.zone.edges.exists(_.sidePixels.exists(u.pixelDistanceCenter(_) <= u.sightPixels + 64))))
      .sortBy(_.heart.center.groundPixels(centroidGround))
      .sortBy( - With.scouting.baseIntrigue(_))
      .sortBy( ! _.owner.isEnemy)
      .sortBy(backstabTime && With.scouting.enemyProximity > 0.5 && ! _.naturalOf.exists(_.isEnemy))
    if (bases.isEmpty) return
    lock.acquire()
  }

  private val bases = new ArrayBuffer[Base]
  private def backstabTime: Boolean = With.frame < Minutes(10)()
  private lazy val backstabTargetBase     = With.scouting.enemyNatural.getOrElse(Maff.orElse(With.geography.bases.filter(_.naturalOf.isDefined), With.geography.bases).minBy(_.heart.groundTiles(With.scouting.enemyHome)))
  private lazy val backstabTarget         = backstabTargetBase.zone.exitOriginal.map(_.pixelCenter).getOrElse(Points.middle.midpoint(backstabTargetBase.heart.center)).walkableTile
  private lazy val backstabTargetDistance = backstabTarget.groundTiles(With.geography.home)
  private lazy val hideyholeSpiral        = Spiral(48).map(backstabTarget.add).filter(_.walkable).filterNot(_.metro.exists(_.bases.exists(_.isEnemy)))
  private lazy val hideyhole: Option[Pixel] = {
    val output = hideyholeSpiral
      .find(With.grids.scoutingPathsStartLocations(_) > 16)
      .map(_.center)
      .orElse(With.geography.preferredExpansionsEnemy.filterNot(_.naturalOf.exists(_.isEnemy)).headOption.map(b => b.zone.exitNow.map(_.pixelCenter).getOrElse(b.heart.center)))
    With.logger.debug(f"Selected DT hidey hole: $output")
    output
  }

  private def intendDTToBase(dt: FriendlyUnitInfo, base: Base): Unit = {
    dt.intend(this, new Intention { toTravel = Some(base.heart.center); targets = Some(base.enemies) })
  }

  def run(): Unit = {
    if (bases.isEmpty) { lock.release(); return }
    val dts = new UnorderedBuffer[FriendlyUnitInfo]
    dts.addAll(units)
    val firstBase = bases.head
    var isFirstDT = true
    while (dts.nonEmpty) {
      val base  = bases.headOption.getOrElse(firstBase)
      val dt    = dts.minBy(_.pixelDistanceTravelling(base.heart))
      val dtTargets = Some(base.enemies.toVector)
      if (base.isEnemy && ! base.owner.isZerg) {
        intendDTToBase(dt, base)
      } else {
        bases -= base

        val divisions = With.battles.divisions.filter(d => d.enemies.exists( ! _.flying) && ! d.enemies.exists(_.unitClass.isDetector))
        val division = Maff.minBy(divisions)(_.centroidGround.groundPixels(centroidGround))

        division.foreach(division => {
          dt.intend(this, new Intention { toTravel = Some(base.heart.center); targets = Some(base.enemies) })
        })
        if (division.isEmpty) {
          if (backstabTime && hideyhole.isDefined) {
            dt.intend(this, new Intention { toTravel = hideyhole; targets = Some(Seq.empty) })
            return
          } else {
            intendDTToBase(dt, base)
          }
        }
      }

      if (isFirstDT) {
        vicinity = dt.intent.toTravel.get
      }
      isFirstDT = false
      dts.remove(dt)
    }
  }
}
