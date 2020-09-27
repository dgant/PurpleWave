package Planning.Plans.GamePlans.Protoss.Situational

import Information.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Squads.Goals.GoalRazeProxies
import Micro.Squads.Squad
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{UnitCountEverything, UnitCountExactly}
import Planning.UnitMatchers._
import Planning.{Plan, Property}
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, Forever}

import scala.collection.mutable

class DefendAgainstProxy extends Plan {

  val defenders = new Property[LockUnits](new LockUnits)
  val squad = new Squad(this)
  
  override def onUpdate() {
    if (With.frame > GameTime(7, 0)()) return

    // Get sorted list of proxies
    val proxies = getProxies.toVector
      .sortBy(_.totalHealth)
      .sortBy(_.remainingCompletionFrames)
      .sortBy( ! _.unitClass.trainsGroundUnits)
      .sortBy( - _.dpfGround)

    if (proxies.isEmpty) return

    // Set up collections of available and assigned defenders
    var additionalWorkersAllowed  = With.units.countOurs(UnitMatchWorkers) - 6
    val defendersAssigned         = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
    val defendersAvailable        = new mutable.HashSet[FriendlyUnitInfo]
    defenders.get.release()
    defenders.get.unitCounter.set(UnitCountEverything)
    defenders.get.unitMatcher.set(UnitMatchOr(UnitMatchWorkers, UnitMatchWarriors))
    defenders.get.inquire(this).toVector.foreach(defendersAvailable ++= _)

    // For each proxy, in priority order, decide who if anyone to assign to it
    proxies.foreach(proxy => {
      val isAnnoying          = proxy.unitClass.isGas || proxy.base.exists(_.resourcePathTiles.exists(proxy.tileArea.contains))
      val isEmergency         = proxy.isAny(Terran.Barracks, Terran.Bunker, Protoss.PhotonCannon, Protoss.Gateway)
      val isCloseEnoughToPull = Seq(With.geography.ourMain, With.geography.ourNatural).exists(_.townHallArea.midpoint.groundPixels(proxy.tileIncludingCenter) < 32 * 21)
      val mustPull            = proxy.dpfGround > 0 && With.geography.ourBases.exists(_.resourcePathTiles.exists(_.pixelCenter.pixelDistance(proxy.pixelCenter) < proxy.effectiveRangePixels + 32))
      val framesBeforeDamage  = ByOption
        .min(With.units.enemy.filter(u =>
          u.dpfGround > 0
          && u.unitClass.isBuilding
          && u.pixelDistanceCenter(proxy) < u.effectiveRangePixels + 96).map(_.remainingCompletionFrames))
        .getOrElse(Forever())
      val defendersRequired   = if (isEmergency) 6 else if (isAnnoying) 2 else 1
      val defendersViable     = defendersAvailable
        .toVector
        .filter(defender =>
          ! defender.unitClass.isWorker
          || mustPull
          || (
            isEmergency
            && isCloseEnoughToPull
            && (
              defender.pixelDistanceEdge(proxy) < 64
              || defender.framesToGetInRange(proxy) < framesBeforeDamage)))
        .sortBy(_.pixelDistanceTravelling(proxy.pixelCenter))
        .sortBy(-_.dpfOnNextHitAgainst(proxy))
      val defendersRequested = defendersViable.take(defendersRequired)
      defendersRequested.foreach(defender => {
        var canAdd = ! defender.unitClass.isWorker
        if (defender.unitClass.isWorker && additionalWorkersAllowed > 0) {
          additionalWorkersAllowed -= 1
          canAdd = true
        }
        if (canAdd) {
          defendersAvailable -= defender
          defendersAssigned(defender) = proxy
        }
      })
    })

    defenders.get.unitCounter.set(UnitCountExactly(defendersAssigned.size))
    defenders.get.unitMatcher.set(UnitMatchCustom(_.friendly.exists(defendersAssigned.contains)))
    defenders.get.acquire(this)
    if (defenders.get.units.isEmpty) {
      return
    }
    With.blackboard.status.set(With.blackboard.status.get :+ "DefendingProxy")
    squad.enemies = defendersAssigned.values.toSeq.distinct
    squad.setGoal(new GoalRazeProxies(defendersAssigned.toMap))
    squad.addConscripts(defenders.get.units)
    With.squads.commission(squad)
  }
  
  private def getProxies: Iterable[UnitInfo] = {
    With.units.enemy.view.filter(e =>
      ! e.flying
      && e.likelyStillAlive
      && e.possiblyStillThere
      && e.isAny(scaryTypes: _*)
      && e.is(UnitMatchProxied))
  }
  
  lazy val scaryTypes = Vector(
    Terran.Bunker,
    Terran.Barracks,
    Terran.Factory,
    Terran.Refinery,
    Protoss.Assimilator,
    Protoss.Pylon,
    Protoss.PhotonCannon,
    Protoss.Gateway,
    Zerg.CreepColony,
    Zerg.Extractor,
    Zerg.SunkenColony,
    Zerg.Hatchery
  )
}
