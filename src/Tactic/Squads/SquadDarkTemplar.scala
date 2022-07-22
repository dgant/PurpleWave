package Tactic.Squads

import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Points
import Mathematics.Shapes.Spiral
import Micro.Agency.Intention
import Performance.Cache
import ProxyBwapi.Races.Protoss
import Utilities.Time.Minutes
import Utilities.UnitCounters.CountEverything
import Utilities.UnitFilters.IsMobileDetector

class SquadDarkTemplar extends Squad {
  lock.matcher = Protoss.DarkTemplar
  lock.counter = CountEverything

  override def launch(): Unit = {
    if (bases().isEmpty) return
    lock.acquire()
  }

  private val bases = new Cache(() =>
    With.geography.bases
      .filterNot(_.owner.isUs)
      .filterNot(_.metro.units.exists(u => u.isEnemy && IsMobileDetector(u)))
      .filterNot(_.enemies.exists(u =>
        u.complete
        && u.unitClass.isDetector
        && u.zone.edges.exists(_.sidePixels.exists(u.pixelDistanceCenter(_) <= u.sightPixels + 64)))))

  private lazy val backstabTargetBase     = With.scouting.enemyNatural.getOrElse(Maff.orElse(With.geography.bases.filter(_.naturalOf.isDefined), With.geography.bases).minBy(_.heart.groundTiles(With.scouting.enemyHome)))
  private lazy val backstabTarget         = backstabTargetBase.zone.exitOriginal.map(_.pixelCenter).getOrElse(Points.middle.midpoint(backstabTargetBase.heart.center)).walkableTile
  private lazy val backstabTargetDistance = backstabTarget.groundTiles(With.geography.home)
  private lazy val hideyholeSpiral        = Spiral(48).map(backstabTarget.add).filter(_.walkable).filter(_.groundTiles(With.geography.home) < backstabTargetDistance + 16)
  private lazy val hideyhole = {
    val output = hideyholeSpiral.find(With.grids.scoutingPathsStartLocations(_) > 16).orElse(
      hideyholeSpiral.find(With.grids.scoutingPathsStartLocations(_) > 13)).orElse(
        hideyholeSpiral.find(With.grids.scoutingPathsStartLocations(_) > 10)).orElse(
          hideyholeSpiral.find(With.grids.scoutingPathsStartLocations(_) > 8))
    With.logger.debug(f"Selected DT hidey hole: $output")
    output
  }

  def run(): Unit = {
    if (bases().isEmpty) { lock.release(); return }

    val backstabTime = With.frame < Minutes(10)()

    val basesSorted = bases()
      .sortBy(_.heart.center.groundPixels(centroidGround))
      .sortBy( - With.scouting.baseIntrigue(_))
      .sortBy( ! _.owner.isEnemy)
      .sortBy(backstabTime && With.scouting.enemyProximity > 0.5 && ! _.naturalOf.exists(_.isEnemy))

    val base = basesSorted.head
    vicinity = base.heart.center
    targets = Some(base.enemies.toVector)

    if ( ! base.owner.isEnemy) {
      val divisions = With.battles.divisions.filter(d => d.enemies.exists( ! _.flying) && ! d.enemies.exists(_.unitClass.isDetector))
      Maff.minBy(divisions)(_.centroidGround.groundPixels(centroidGround)).foreach(division => {
        vicinity = division.centroidGround
        targets = Some(division.enemies.toVector)
        intendAll()
        return
      })

      if (backstabTime && hideyhole.isDefined) {
        vicinity = hideyhole.get.center
        targets = Some(Seq.empty)
        intendAll()
        return
      }
    }
  }

  private def intendAll(): Unit = {
    units.foreach(_.intend(this, new Intention { toTravel = Some(vicinity) }))
  }
}
