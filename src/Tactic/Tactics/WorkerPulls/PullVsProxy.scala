package Tactic.Tactics.WorkerPulls
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Time.{Forever, Minutes}
import Utilities.UnitFilters.{IsProxied, IsWorker}

import scala.collection.mutable

class PullVsProxy extends WorkerPull {

  var skip = false
  skip ||= With.frame > Minutes(7)()
  skip ||= proxies.isEmpty

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
    Zerg.Hatchery)
  private lazy val proxies = With.units.enemy.view.filter(e =>
    ! e.flying
      && e.likelyStillThere
      && e.isAny(scaryTypes: _*)
      && IsProxied(e)).toVector
    .sortBy(_.remainingCompletionFrames)
    .sortBy(_.totalHealth)
    .sortBy( ! _.unitClass.attacksGround)
    .sortBy( ! _.unitClass.trainsGroundUnits)
    .sortBy( - _.dpfGround)
  private lazy val defendersRequired = if (skip) 0 else proxies.map(proxyDefendersRequired).sum

  private var additionalWorkersAllowed  = With.units.countOurs(IsWorker) - 6

  private def proxyIsAnnoying(proxy: UnitInfo): Boolean = {
    proxy.unitClass.isGas || proxy.base.exists(_.resourcePathTiles.exists(proxy.tileArea.contains))
  }
  private def proxyIsEmergency(proxy: UnitInfo): Boolean = {
    proxy.isAny(Terran.Barracks, Terran.Bunker, Protoss.PhotonCannon) && (proxy.powered || ! proxy.unitClass.isProtoss || With.units.enemy.exists(u => Protoss.Pylon(u) && With.grids.psi3Height.psiPoints.view.map(u.tileTopLeft.add).contains(proxy.tileTopLeft)))
  }
  private def proxyCloseEnoughToPull(proxy: UnitInfo): Boolean = {
    Seq(With.geography.ourMain, With.geography.ourNatural).exists(_.townHallArea.midpoint.groundPixels(proxy.tile) < 32 * 21)
  }
  private def mustPull(proxy: UnitInfo): Boolean = {
    proxy.dpfGround > 0 && With.geography.ourBases.exists(_.resourcePathTiles.exists(t => proxy.pixelDistanceEdge(t.center) < proxy.effectiveRangePixels))
  }
  private def framesBeforeDamage(proxy: UnitInfo) = Maff
    .min(With.units.enemy.filter(u =>
      u.dpfGround > 0
        && u.unitClass.isBuilding
        && u.pixelDistanceCenter(proxy) < u.effectiveRangePixels + 96).map(_.remainingCompletionFrames))
    .getOrElse(Forever())
  private def proxyDefendersRequired(proxy: UnitInfo): Int = {
    if (proxyIsEmergency(proxy)) 6 else if (proxyIsAnnoying(proxy)) 2 else 1
  }

  override def apply(): Int = defendersRequired

  override def employ(defenders: Seq[FriendlyUnitInfo]): Unit = {
    val defendersAssigned         = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
    val defendersAvailable        = new mutable.HashSet[FriendlyUnitInfo]
    defendersAvailable ++= defenders

    proxies.foreach(proxy => {
      defenders.foreach(defender => {
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
  }
}
