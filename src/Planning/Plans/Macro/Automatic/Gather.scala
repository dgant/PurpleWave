package Planning.Plans.Macro.Automatic

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.PurpleMath
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.UnitCountEverything
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

import scala.collection.mutable

class Gather extends Plan {

  // Adapted from https://github.com/TorchCraft/TorchCraftAI/blob/master/src/modules/gatherer/gathererassignments.cpp

  val workerLock: LockUnits = new LockUnits {
    unitMatcher.set(UnitMatchWorkers)
    unitCounter.set(UnitCountEverything)
  }
  var workers: collection.Set[FriendlyUnitInfo] = Set.empty

  val resourceByWorker = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  val workersByResource = new mutable.HashMap[UnitInfo, mutable.Set[FriendlyUnitInfo]]
  def assignWorker(worker: FriendlyUnitInfo, resource: FriendlyUnitInfo): Unit = {
    unassignWorker(worker)
    resourceByWorker(worker) = resource
    workersByResource(resource) += worker
  }

  def unassignWorker(worker: FriendlyUnitInfo): Unit = {
    resourceByWorker.get(worker).map(workersByResource.get).foreach(_.foreach(_.remove(worker)))
    resourceByWorker.remove(worker)
  }

  def includeResource(resource: UnitInfo): Unit = {
    workersByResource(resource) = workersByResource.getOrElse(resource, mutable.Set.empty)
  }

  def containsResource(resource: UnitInfo): Boolean = {
    workersByResource.contains(resource)
  }

  def excludeResource(resource: UnitInfo): Unit = {
    workersByResource.get(resource).foreach(_.foreach(resourceByWorker.remove))
  }

  def getResource(worker: FriendlyUnitInfo): Option[UnitInfo] = {
    resourceByWorker.get(worker)
  }

  def countWorkers(resource: UnitInfo): Int = {
    workersByResource.get(resource).map(_.size).getOrElse(0)
  }

  def isValidResource(unit: UnitInfo): Boolean = {
    if ( ! unit.alive) return false
    if ( ! unit.unitClass.isResource) return false
    unit.unitClass.isMinerals || (unit.isOurs && unit.remainingCompletionFrames < 24 * 2)
  }

  val kDistanceMining: Int = 32 * 12
  val lightYear: Double = 1e10

  override def onUpdate() {
    workerLock.acquire(this)
    workers = workerLock.units
    if (workers.isEmpty) return
    resourceByWorker.keys.withFilter( ! workers.contains(_)).foreach(unassignWorker)
    // TODO: Unassign any workers who are no longer in the squad -- TCAI does this with the controller

    // Add/remove resources
    val resourcesToRemove = workersByResource.keys.filterNot(isValidResource)
    resourcesToRemove.foreach(excludeResource)

    val newResources: Seq[UnitInfo] =
      (With.units.neutral.view ++ With.units.ours.view)
        .withFilter(resource => isValidResource(resource) && ! workersByResource.contains(resource))
        .toVector
    newResources.foreach(includeResource)

    // Count gas workers
    val gasWorkersNow = resourceByWorker.count(_._2.unitClass.isGas)

    // Identify base-to-base transfer costs, considering distance and threats
    val baseCosts = new mutable.HashMap[(Base, Base), Double]
    With.geography.ourBases.foreach(base0 =>
      With.geography.ourBases.foreach(base1 => {
        val cost = base0.heart.groundPixels(base1.heart)
        // TODO: Account for unsafe travel
        baseCosts((base0, base1)) = cost
        baseCosts((base1, base0)) = cost
      }))

    // Map resources to bases and measure distance
    // TODO: Save calculation
    val depotToResourceDistances = workersByResource.keys.map(resource =>
      ByOption
        .min(With.geography.ourBases.filter(_.townHall.isDefined).map(base =>
          if (resource.base.contains(base)) base.townHall.get.pixelDistanceEdge(resource) else base.townHallTile.groundPixels(resource.tileTopLeft)))
        .getOrElse(lightYear))

    val totalMineralPatches = With.geography.ourBases.view.map(_.minerals.size).sum
    val totalGasPumps = With.geography.ourBases.view.map(_.gas.count(_.isOurs)).sum
    val canDistanceMine = totalMineralPatches < 7
    val gasWorkersMax = Math.min(
      3 * totalGasPumps,
      PurpleMath.clamp(
        Math.round(With.blackboard.gasTargetRatio() * workers.size).toInt,
        With.blackboard.gasWorkerFloor(),
        With.blackboard.gasWorkerCeiling()))

    // Update workers in priority order.
    //
    // When a new resource is available:
    // Prioritize workers closest to the new resource.
    //
    // Otherwise:
    // Sort workers by frames since update, descending.
    def newGasDistance(worker: FriendlyUnitInfo): Option[Double] = {
      ByOption.min(newResources.filter(_.unitClass.isGas).map(_.pixelDistanceEdge(worker)))
    }
    val minGasDistance =
      if (gasWorkersNow < gasWorkersMax) {
        workers
          .map(worker => (
            worker,
            ByOption.min(workersByResource.filter(pair => pair._1.unitClass.isGas && pair._2.size < 3))
              map(resource =>
            )
          ))
          .toMap
      }
      else Map.empty

  }




}