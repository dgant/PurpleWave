package Tactic.Tactics.WorkerPulls

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Pixel
import Micro.Agency.Intention
import Planning.Predicates.MacroCounting
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.Time.Minutes
import Utilities.UnitFilters.{IsAll, IsComplete, IsWorker}

import scala.collection.mutable.ArrayBuffer

class PullForWall extends WorkerPull with MacroCounting {
  private lazy val defensePoints = Maff.orElse(With.units.ours.filter(Protoss.PhotonCannon), With.units.ours.filter(Protoss.Forge))

  private var perform = true
  perform &&= With.frame <= Minutes(6)()
  perform &&= With.enemies.size == 1
  perform &&= With.units.countOurs(Protoss.PhotonCannon) > 0
  perform &&= With.units.countOurs(IsAll(Protoss.PhotonCannon, IsComplete)) <= 3
  perform &&= With.units.countEverOurs(Protoss.PhotonCannon) + (With.self.minerals + 24) / 150 >= 2
  perform &&= With.enemies.exists(_.isUnknownOrZerg)
  perform &&= ! With.fingerprints.twelveHatch()
  perform &&= ! With.fingerprints.tenHatch()
  perform &&= ! With.fingerprints.twelvePool()
  perform &&= ! With.fingerprints.overpool()
  perform &&= ! With.fingerprints.ninePool()
  perform &&= enemyRecentStrategy(With.fingerprints.fourPool)
  perform &&= With.fingerprints.fourPool() || With.scouting.enemyHasScoutedUsWithWorker
  perform &&= defensePoints.nonEmpty
  private val skip = ! perform

  private lazy val expectedZerglings  : Int = Seq(4, With.units.countEnemy(Zerg.Zergling), 8 - With.units.countEverEnemy(Zerg.Zergling)).max
  private lazy val cannonsComplete    : Int = With.units.countOurs(IsAll(Protoss.PhotonCannon, IsComplete))
  private lazy val cannonsIncomplete  : Int = With.units.countOurs(Protoss.PhotonCannon) - cannonsComplete
  private lazy val scoutCount         : Int = With.units.ours.count(_.agent.isScout)
  private lazy val workerCount        : Int = With.units.countOurs(IsWorker)
  private lazy val workersToMine      : Int = if (skip) 0 else if (cannonsComplete <  2) 4 else 4 + 2 * cannonsComplete
  private lazy val workersDesired     : Int = if (skip) 0 else if (cannonsComplete >= 5) 0 else Math.min(workerCount - workersToMine - scoutCount, expectedZerglings * 4 - cannonsComplete * 3)

  override def apply(): Int = workersDesired
  override def minRemaining: Int = workersToMine

  override def employ(defenders: Seq[FriendlyUnitInfo]): Unit = {
    lazy val zerglings    = With.units.enemy.find(Zerg.Zergling)
    lazy val threatSource = zerglings.map(_.pixel).getOrElse(With.scouting.enemyHome.center)
    val closestDistance = defensePoints.map(_.pixelDistanceTravelling(threatSource)).min
    val threatenedCannons = defensePoints.filter(_.pixelDistanceTravelling(threatSource) <= closestDistance + 96)
    val workers = new ArrayBuffer[FriendlyUnitInfo]
    val workersByCannon = threatenedCannons.map(c => (c, new ArrayBuffer[FriendlyUnitInfo])).toMap
    workers ++= defenders
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
