package Tactic.Tactics

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Planning.Predicates.Strategy.EnemyRecentStrategy
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Minutes
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsAll, IsComplete, IsWorker}
import Utilities.UnitPreferences.PreferClose

import scala.collection.mutable.ArrayBuffer

class DefendFFEWithProbes extends Tactic {
  
  val defenders = new LockUnits(this, IsWorker)
  
  protected def probeCount: Int = {
    val zerglings           = Maff.vmax(4, With.units.countEnemy(Zerg.Zergling), 8 - With.units.countEverEnemy(Zerg.Zergling))
    val cannonsComplete     = With.units.countOurs(IsAll(Protoss.PhotonCannon, IsComplete))
    val cannonsIncomplete   = With.units.countOurs(Protoss.PhotonCannon) - cannonsComplete
    val workerCount         = With.units.countOurs(IsWorker)
    val workersToMine       = if (cannonsComplete < 2) 4 else 4 + 2 * cannonsComplete
    val workersDesired      = if (cannonsComplete >= 5) 0 else Math.min(workerCount - workersToMine, zerglings * 4 - cannonsComplete * 3)
    workersDesired
  }

  var haveMinedEnoughForTwoCannons: Boolean = false
  
  def launch(): Unit = {
    if (With.frame > Minutes(6)()) return
    if (With.enemies.size > 1) return
    if ( ! With.geography.ourMain.units.exists(Zerg.Zergling)) return
    if (With.units.countOurs(Protoss.PhotonCannon) == 0) return
    if (With.units.countOurs(IsAll(Protoss.PhotonCannon, IsComplete)) > 3) return
    haveMinedEnoughForTwoCannons ||= With.units.countOurs(Protoss.PhotonCannon) + (With.self.minerals + 24) / 150 >= 2
    if ( ! haveMinedEnoughForTwoCannons) return
    if ( ! With.enemies.exists(_.isUnknownOrZerg)) return
    if (With.fingerprints.twelveHatch())  return
    if (With.fingerprints.tenHatch())     return
    if (With.fingerprints.twelvePool())   return
    if (With.fingerprints.overpool())     return
    if (With.fingerprints.ninePool())     return
    if ( ! EnemyRecentStrategy(With.fingerprints.fourPool).apply) return
    if ( ! With.fingerprints.fourPool() && ! With.scouting.enemyHasScoutedUsWithWorker) return

    val defensePoints = Maff.orElse(
      With.units.ours.filter(Protoss.PhotonCannon)  .filter(_.base.contains(With.geography.ourMain)),
      With.units.ours.filter(Protoss.Forge)         .filter(_.base.contains(With.geography.ourMain)))
    
    lazy val zerglings    = With.units.enemy.find(Zerg.Zergling)
    lazy val threatSource = zerglings.map(_.pixel).getOrElse(With.scouting.enemyHome.center)

    if (defensePoints.isEmpty) return
    defensePoints.toVector.sortBy(_.totalHealth)
    
    val probesRequired = probeCount
    if (defenders.units.size > probesRequired) {
      defenders.release()
    }
    defenders
      .setPreference(PreferClose(defensePoints.map(_.pixel).minBy(_.groundPixels(threatSource))))
      .setCounter(CountUpTo(probesRequired))
      .acquire()
    val closestDistance = defensePoints.map(_.pixelDistanceTravelling(threatSource)).min
    val threatenedCannons = defensePoints.filter(_.pixelDistanceTravelling(threatSource) <= closestDistance + 96)
    val workers = new ArrayBuffer[FriendlyUnitInfo]
    val workersByCannon = threatenedCannons.map(c => (c, new ArrayBuffer[FriendlyUnitInfo])).toMap
    workers ++= defenders.units
    while (workers.nonEmpty) {
      threatenedCannons.foreach(cannon => {
        if (workers.nonEmpty) {
          val worker = workers.minBy(_.pixelDistanceEdge(cannon))
          workers -= worker
          workersByCannon(cannon) += worker
        }
      })
    }

    def occupied(pixel: Pixel): Boolean = (
      ! pixel.tile.adjacent9.forall(With.groundskeeper.isFree)
    )
    workersByCannon.foreach(pair => {
      val cannon = pair._1
      val workers = pair._2
      var toDefend = cannon.pixel.project(threatSource, 48.0)
      var steps = 0
      while (steps < 8 && occupied(toDefend)) {
        steps += 1
        toDefend = toDefend.project(threatSource, 16)
      }
      val nearestThreat = Maff.minBy(cannon.matchups.threats)(_.pixelDistanceEdge(cannon))
      nearestThreat.foreach(someNearestThreat => {
        val threatDistanceToCannon = cannon.pixelDistanceEdge(threatSource)
        if (cannon.pixelDistanceEdge(toDefend) > threatDistanceToCannon) {
          toDefend = cannon.pixel.project(someNearestThreat.pixel, threatDistanceToCannon + 16)
        }
      })

      val targets = With.units.enemy.filter(e => e.canAttack && defensePoints.exists(_.pixelDistanceCenter(toDefend) < 96)).toVector
      workers.foreach(_.intend(this)
        .setCanFlee(false)
        .setTerminus(toDefend)
        .setRedoubt(toDefend)
        .setTargets(targets))

      if (ShowUnitsFriendly.mapInUse) {
        workers.foreach(w => DrawMap.circle(toDefend, 16, Colors.NeonYellow))
      }
    })
  }
}
