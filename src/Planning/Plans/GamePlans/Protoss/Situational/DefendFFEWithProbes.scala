package Planning.Plans.GamePlans.Protoss.Situational

import Debugging.Visualizations.Colors
import Debugging.Visualizations.Rendering.DrawMap
import Debugging.Visualizations.Views.Micro.ShowUnitsFriendly
import Lifecycle.With
import Mathematics.Points.{Pixel, TileRectangle}
import Micro.Agency.{Intention, Leash}
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountBetween
import Planning.UnitMatchers.UnitMatchWorkers
import Planning.UnitPreferences.UnitPreferClose
import Planning.{Plan, Property}
import ProxyBwapi.Races.{Protoss, Zerg}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.ByOption

import scala.collection.mutable.ArrayBuffer

abstract class DefendFFEWithProbes extends Plan {
  
  val defenders = new Property[LockUnits](new LockUnits)
  defenders.get.unitMatcher.set(UnitMatchWorkers)
  
  protected def probeCount: Int
  
  override def onUpdate() {
  
    var cannons = With.units.ours.filter(_.is(Protoss.PhotonCannon))
    if (cannons.isEmpty) cannons = With.units.ours.filter(_.is(Protoss.Forge))
    
    lazy val zerglings    = With.units.enemy.find(_.is(Zerg.Zergling))
    lazy val threatSource = zerglings.map(_.pixelCenter).getOrElse(With.scouting.mostBaselikeEnemyTile.pixelCenter)

    if (cannons.isEmpty) return
    cannons.toVector.sortBy(_.totalHealth)
    
    val probesRequired = probeCount
    if (defenders.get.units.size > probesRequired) {
      defenders.get.release()
    }
    defenders.get.unitPreference.set(UnitPreferClose(cannons.map(_.pixelCenter).minBy(_.pixelDistance(threatSource))))
    defenders.get.unitCounter.set(new UnitCountBetween(0, probesRequired))
    defenders.get.acquire(this)
    val closestDistance = cannons.map(_.pixelDistanceEdge(threatSource)).min
    val threatenedCannons = cannons.filter(_.pixelDistanceEdge(threatSource) <= closestDistance + 96)
    val workers = new ArrayBuffer[FriendlyUnitInfo]
    val workersByCannon = threatenedCannons.map(c => (c, new ArrayBuffer[FriendlyUnitInfo])).toMap
    workers ++= defenders.get.units
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
      With.groundskeeper
        .proposalPlacements
        .view
        .flatMap(_._2.tile)
        .exists(t => TileRectangle(
          t,
          t
            .add(1, 1))
            .expand(1, 1)
            .contains(pixel.tileIncluding))
    )
    workersByCannon.foreach(pair => {
      val cannon = pair._1
      val workers = pair._2
      var toDefend = cannon.pixelCenter.project(threatSource, 48.0)
      var steps = 0
      while (steps < 8 && occupied(toDefend)) {
        steps += 1
        toDefend = toDefend.project(threatSource, 16)
      }
      val nearestThreat = ByOption.minBy(cannon.matchups.threats)(_.pixelDistanceEdge(cannon))
      nearestThreat.foreach(someNearestThreat => {
        val threatDistanceToCannon = cannon.pixelDistanceEdge(threatSource)
        if (cannon.pixelDistanceEdge(toDefend) > threatDistanceToCannon) {
          toDefend = cannon.pixelCenter.project(someNearestThreat.pixelCenter, threatDistanceToCannon + 16)
        }
      })

      workers.foreach(_.agent.intend(this, new Intention {
        canFlee   = false
        //toForm    = Some(toDefend)
        toTravel  = Some(toDefend)
        toLeash   = Some(Leash(toDefend, 32.0 * 4.0))
      }))
      if (ShowUnitsFriendly.mapInUse) {
        workers.foreach(w => DrawMap.circle(toDefend, 16, Colors.NeonYellow))
      }
    })
  }
}
