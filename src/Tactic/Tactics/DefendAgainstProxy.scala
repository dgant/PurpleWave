package Tactic.Tactics

import Lifecycle.With
import Mathematics.Maff
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.{CountEverything, CountUpTo}
import Utilities.UnitFilters._
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactic.Squads.SquadRazeProxies
import Utilities.?
import Utilities.Time.{Forever, Minutes}

import scala.collection.mutable

class DefendAgainstProxy extends Tactic {

  // TODO: Vs. bunker rush, exactly 5 probes per https://youtu.be/PqsXjzC0eFk?t=1271

  val defenders = new LockUnits(this)
  
  def launch(): Unit = {
    if (With.frame > Minutes(7)()) return

    // Get sorted list of proxies
    val proxies = With.units.enemy
      .filter(e =>
        e.likelyStillThere
          && e.isAny(scaryTypes: _*)
          && e.proxied
          && (e.metro.exists(_.isOurs) || Protoss.PhotonCannon(e)))
      .toVector
      .sortBy(_.remainingCompletionFrames)
      .sortBy(_.totalHealth)
      .sortBy( ! _.unitClass.attacksGround)
      .sortBy( ! _.unitClass.trainsGroundUnits)
      .sortBy( - _.dpfGround)

    if (proxies.isEmpty) return

    // Set up collections of available and assigned defenders
    var additionalWorkersAllowed  = With.units.countOurs(IsWorker) - 6
    val defendersAssigned         = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
    val defendersAvailable        = new mutable.HashSet[FriendlyUnitInfo]
    defenders.release()
    defenders.counter = CountEverything
    defenders.matcher = IsAny(IsWorker, IsWarrior)
    defenders.inquire().foreach(defendersAvailable++=)

    val isCannony = proxies.exists(_.isAny(Terran.Bunker, Protoss.PhotonCannon, Zerg.CreepColony))

    // For each proxy, in priority order, decide who if anyone to assign to it
    proxies.foreach(proxy => {
      val isAnnoying          = proxy.unitClass.isGas || proxy.base.exists(_.resourcePathTiles.exists(proxy.tileArea.contains))
      val isCloseEnoughToPull = With.geography.ourMetro.bases.exists(_.townHallArea.midpoint.groundPixels(proxy.tile) < 32 * 21)
      val mustPull            = proxy.dpfGround > 0 && With.geography.ourBases.exists(_.resourcePathTiles.exists(t => proxy.pixelDistanceEdge(t.center) < proxy.effectiveRangePixels))
      val framesBeforeDamage  = Maff
        .min(With.units.enemy.filter(u =>
          u.dpfGround > 0
          && u.unitClass.isBuilding
          && u.pixelDistanceCenter(proxy) < u.effectiveRangePixels + 96).map(_.remainingCompletionFrames))
        .getOrElse(Forever())
      val isEmergency = (
        proxy.isAny(Terran.Barracks, Terran.Bunker, Protoss.PhotonCannon)
          && (proxy.powered || ! proxy.unitClass.isProtoss || With.units.enemy.exists(u =>
            Protoss.Pylon(u)
              && With.grids.psi3Height.psiPoints.view.exists(p => u.tileTopLeft.add(p) == proxy.tileTopLeft))))
      val defendersRequired   = ?(isEmergency, 6, ?(isAnnoying, 2, 1))
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
        .sortBy(_.pixelDistanceTravelling(proxy.pixel))
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

    // Re-simplifying it: Against proxies that don't attack, raze in pure priority order
    if ( ! isCannony) {
      defendersAssigned.keySet.foreach(k => defendersAssigned(k) = proxies.head)
    }

    defenders.counter = CountUpTo(defendersAssigned.size)
    defenders.matcher = _.friendly.exists(defendersAssigned.contains)
    defenders.acquire()
    if (defenders.units.isEmpty) return
    val squad = new SquadRazeProxies(defendersAssigned.toMap)
    squad.addUnits(defenders.units)
  }
  
  private lazy val scaryTypes = Vector(
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
