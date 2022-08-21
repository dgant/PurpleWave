package Tactic.Tactics

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.Predicates.Strategy.EnemyRecentStrategy
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsAll, IsComplete, IsWorker}
import Utilities.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Minutes

import scala.collection.mutable.ArrayBuffer

class DefendFFEWithProbes extends Tactic {
  
  val defenders = new LockUnits(this)
  defenders.matcher = IsWorker
  
  protected def probeCount: Int = {
    val zerglings           = Seq(4, With.units.countEnemy(Zerg.Zergling), 8 - With.units.countEverEnemy(Zerg.Zergling)).max
    val cannonsComplete     = With.units.countOurs(IsAll(Protoss.PhotonCannon, IsComplete))
    val cannonsIncomplete   = With.units.countOurs(Protoss.PhotonCannon) - cannonsComplete
    val workerCount         = With.units.countOurs(IsWorker)
    val workersToMine       = if (cannonsComplete < 2) 4 else 4 + 2 * cannonsComplete
    val workersDesired      = if (cannonsComplete >= 5) 0 else Math.min(workerCount - workersToMine - With.blackboard.workersPulled(), zerglings * 4 - cannonsComplete * 3)
    workersDesired
  }

  var haveMinedEnoughForTwoCannons: Boolean = false
  
  def launch(): Unit = {
    if (With.frame > Minutes(6)()) return
    if (With.enemies.size > 1) return
    haveMinedEnoughForTwoCannons ||= With.units.countOurs(Protoss.PhotonCannon) + (With.self.minerals + 24) / 150 >= 2
    if (With.units.countOurs(Protoss.PhotonCannon) == 0) return
    if (With.units.countOurs(IsAll(Protoss.PhotonCannon, IsComplete)) > 3) return
    if ( ! haveMinedEnoughForTwoCannons) return
    if ( ! With.enemies.exists(_.isUnknownOrZerg)) return
    if (With.fingerprints.twelveHatch()) return
    if (With.fingerprints.tenHatch()) return
    if (With.fingerprints.twelvePool()) return
    if (With.fingerprints.overpool()) return
    if (With.fingerprints.ninePool()) return
    if ( ! EnemyRecentStrategy(With.fingerprints.fourPool).apply) return
    if ( ! With.fingerprints.fourPool() && ! With.scouting.enemyHasScoutedUsWithWorker) return

    val defensePoints = Maff.orElse(With.units.ours.filter(Protoss.PhotonCannon), With.units.ours.filter(Protoss.Forge))
    
    lazy val zerglings    = With.units.enemy.find(Zerg.Zergling)
    lazy val threatSource = zerglings.map(_.pixel).getOrElse(With.scouting.enemyHome.center)

    if (defensePoints.isEmpty) return
    defensePoints.toVector.sortBy(_.totalHealth)
    
    val probesRequired = probeCount
    if (defenders.units.size > probesRequired) {
      defenders.release()
    }
    defenders.preference = PreferClose(defensePoints.map(_.pixel).minBy(_.groundPixels(threatSource)))
    defenders.counter = CountUpTo(probesRequired)
    defenders.acquire()
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

      workers.foreach(_.intend(this, new Intention {
        canFlee   = false
        toTravel  = Some(toDefend)
        toReturn  = Some(toDefend)
        //targetFilters = Seq(TargetFilterLeash(32 * 5))
      }))
      if (ShowUnitsFriendly.mapInUse) {
        workers.foreach(w => DrawMap.circle(toDefend, 16, Colors.NeonYellow))
      }
    })
  }
}
