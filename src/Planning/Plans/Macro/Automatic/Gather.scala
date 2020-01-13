package Planning.Plans.Macro.Automatic

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Performance.Cache
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

import scala.collection.mutable

class Gather extends Plan {

  val kDistanceMining: Int = 32 * 12
  val kInvalidResourceScore = 1e100d
  val kHysteresisPixels = 12
  val kLookaheadFrames: Int = 24 * 60
  val kLightYear: Double = 1e10
  val kGasCompletionFrames = 48

  // Adapted from https://github.com/TorchCraft/TorchCraftAI/blob/master/src/modules/gatherer/gathererassignments.cpp
  // See LICENSE: https://github.com/TorchCraft/TorchCraftAI/blob/master/LICENSE

  var workers: collection.Set[FriendlyUnitInfo] = Set.empty

  val workerLock: LockUnits = new LockUnits { unitMatcher.set(UnitMatchWorkers) }
  val resourceByWorker = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  val workersByResource = new mutable.HashMap[UnitInfo, mutable.Set[FriendlyUnitInfo]]

  def isValidResource (unit: UnitInfo): Boolean = isValidMineral(unit) || isValidGas(unit)
  def isValidMineral  (unit: UnitInfo): Boolean = unit.alive && unit.mineralsLeft > 0
  def isValidGas      (unit: UnitInfo): Boolean = unit.alive && unit.isOurs && unit.gasLeft > 0 && unit.remainingCompletionFrames < 24 * 2

  private lazy val baseCosts: Map[(Base, Base), Cache[Double]] = With.geography.bases
    .flatMap(baseA => With.geography.bases.map(baseB => (baseA, baseB)))
    .map(basePair => (basePair, new Cache[Double](() =>
      if (basePair._1 == basePair._2) 0.0
      else if (basePair._1.hashCode() > basePair._2.hashCode()) baseCosts((basePair._2, basePair._1)).apply()
      else basePair._1.heart.groundPixels(basePair._2.heart) // TODO: Account for unsafe travel
    ))).toMap
  private val miningBases         = new Cache(() => With.geography.ourBases.filter(_.townHall.exists(t => t.hasEverBeenCompleteHatch || t.remainingCompletionFrames < 24 * 10)))
  private val ourMinerals         = new Cache(() => miningBases().view.flatMap(_.minerals))
  private val ourGas              = new Cache(() => miningBases().view.flatMap(_.gas).filter(u => u.isOurs && u.remainingCompletionFrames < kGasCompletionFrames).toVector)
  private val allMinerals         = new Cache(() => With.units.neutral.view.filter(_.mineralsLeft > 0).toVector)
  private val allGas              = new Cache(() => With.units.ours.view.filter(u => u.unitClass.isGas && u.remainingCompletionFrames < kGasCompletionFrames).toVector)
  private val longMineMinerals    = new Cache(() => ourMinerals().length < workers.size / 4)
  private val longMineGas         = new Cache(() => ourGas().isEmpty)
  private val legalMinerals       = new Cache(() => if (longMineMinerals()) allMinerals() else ourMinerals())
  private val legalGas            = new Cache(() => if (longMineGas()) allGas() else ourGas())
  private val newMinerals         = new Cache(() => legalMinerals().filter(isNewResource))
  private val newGas              = new Cache(() => legalGas().filter(isNewResource))
  private val resourcesToExclude   = new Cache(() => workersByResource
    .keysIterator
    .filterNot(resource =>
          (isValidMineral(resource) && (longMineMinerals()  || ! isLongDistanceResource(resource)))
      ||  (isValidGas(resource)     && (longMineGas()       || ! isLongDistanceResource(resource))))
    .toVector)
  private val resourceHallPixels  = new Cache(() => workersByResource
    .keysIterator
    .map(resource => (resource, ByOption.min(miningBases().map(_.townHall.get.pixelDistanceEdge(resource))).getOrElse(kLightYear)))
    .toMap)

  def isLongDistanceResource(unit: UnitInfo): Boolean = {
    ! miningBases().exists(unit.base.contains)
  }

  def isNewResource(unit: UnitInfo): Boolean = {
    ! workersByResource.contains(unit)
  }

  def assignWorker(worker: FriendlyUnitInfo, resource: UnitInfo): Unit = {
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

  override def onUpdate() {
    workerLock.acquire(this)
    workers = workerLock.units
    if (workers.isEmpty) return
    resourceByWorker.keys.withFilter( ! workers.contains(_)).foreach(unassignWorker)
    // TODO: Unassign any workers who are no longer in the squad -- TCAI does this with the controller

    newMinerals().foreach(includeResource)
    newGas().foreach(includeResource)
    resourcesToExclude().foreach(excludeResource)

    // Count gas workers
    var gasWorkersNow = resourceByWorker.count(_._2.unitClass.isGas)

    val totalMineralPatches = With.geography.ourBases.view.map(_.minerals.size).sum
    val totalGasPumps = With.geography.ourBases.view.map(_.gas.count(_.isOurs)).sum
    val canDistanceMine = totalMineralPatches < 7
    val gasWorkersMax = Math.min(
      3 * totalGasPumps,
      PurpleMath.clamp(
        Math.round(With.blackboard.gasTargetRatio() * workers.size).toInt,
        With.blackboard.gasWorkerFloor(),
        With.blackboard.gasWorkerCeiling()))
    val needMoreGasWorkers = gasWorkersNow < gasWorkersMax

    // Update workers in priority order.
    //
    // When a new resource is available:
    // Prioritize workers closest to the new resource.
    //
    // Otherwise:
    // Sort workers by frames since update, descending.
    def newGasDistance(worker: FriendlyUnitInfo): Option[Double] = {
      ByOption.min(newGas().map(_.pixelDistanceEdge(worker)))
    }
    // Determine if we need to prioritize updating gas workers.
    val minGasDistance: Map[FriendlyUnitInfo, Option[Double]] =
      if (needMoreGasWorkers) {
        workers
          .map(worker => (
            worker,
            ByOption
              .min(
                workersByResource
                  .filter(pair => pair._1.unitClass.isGas && pair._2.size < 3)
                  .map(resource => worker.pixelDistanceTravelling(resource._1.pixelCenter))) // TODO: Used edge distance?
            ))
          .toMap
      } else Map.empty
    val workersToUpdate = workers.toVector.sortBy(worker =>
      // TODO: Make sure this has the correct sign!
      if (needMoreGasWorkers) {
        newGasDistance(worker)
          .getOrElse(minGasDistance.getOrElse(worker, None)
          .getOrElse(Double.PositiveInfinity))
      } else {
        worker.id.toDouble // TODO: This used to be "Frames since update" using the cooldown mechanism, ie. update least-recently-updated worker
      })

    // Update workers in priority order
    // TODO: Cap worker updates per run, for performance
    // TODO: Respect cooldown, if needed
    workersToUpdate
      .view
      .filter(worker => {
        // Don't interrupt workers who are about to reach minerals.
        val resourceBefore = resourceByWorker.get(worker);
        val resourceBeforeEdgePixels = resourceBefore.map(_.pixelDistanceEdge(worker))
        resourceBefore.forall(_.unitClass.isGas) || resourceBeforeEdgePixels.forall(_ > 32)
      })
      .foreach(worker => {
        //TODO: Update frames since update
        val resourceBefore = resourceByWorker.get(worker)
        unassignWorker(worker)
        if (resourceBefore.exists(_.unitClass.isGas)) {
          gasWorkersNow -= 1
        }

        val gasWorkerDesire = if (gasWorkersNow < gasWorkersMax) 1.0 else 0.1

        // Assign the worker to tbhe best resource
        val resourceBest = ByOption.maxBy(workersByResource.keysIterator)(scoreResource(worker, _, gasWorkerDesire))
        resourceBest.foreach(bestResource => {
          assignWorker(worker, bestResource)
          if (resourceBefore.exists(_.unitClass.isGas)) gasWorkersNow -= 1
          if (bestResource.unitClass.isGas) gasWorkersNow += 1
        })
      })

    workersToUpdate.foreach(worker => worker.agent.intend(this, new Intention {
      toGather = resourceByWorker.get(worker)
    }))
  }

  // Evaluate the marginal efficacy of assigning this worker to a resource.
  def scoreResource(worker: FriendlyUnitInfo, resource: UnitInfo, gasWorkerDesire: Double): Double = {
    val resourceBefore = resourceByWorker.get(worker);
    val resourceBeforeEdgePixels = resourceBefore.map(_.pixelDistanceEdge(worker))

    // How many workers are already mining this patch?
    // TODO: Originally had the unimplemented comment "Count only close-or-closer workers so new miners don't scare us off."
    val workersBefore = workersByResource.get(resource).map(_.size).getOrElse(0)

    // How effective will the next worker be on this resource?
    var throughput = 0.001
    if (resource.unitClass.isGas) {
      // Depends on distance, but generally a geyser only supports three workers
      if (workersBefore == 3) {
        throughput = 0.01
      } else if (workersBefore < 3) {
        throughput = 1.0
      }
      // Account for geyser depletion
      if (resource.resourcesLeft < 8) {
        throughput *= 0.25
      }
    } else {
      if (workersBefore == 0) {
        throughput = 1.0
      } else if (workersBefore == 1) {
        throughput = 0.8
      } else if (workersBefore == 2) {
        throughput = 0.2
      }
    }

    // How fast is mining from this resource?
    val depotToResourceDistance = resourceHallPixels()(resource)
    val speed = 3.0 + 12.0 / Math.max(12.0, depotToResourceDistance)

    // When deciding whether to travel to another base to mine, there's a
    // tradeoff between mining efficiency and the time-discounted value of
    // resources.
    // There's no obvious way to measure the tradeoff, so it's left as a
    // hyperparameter.
    val stick = resourceBefore.contains(resource) &&
        ! worker.carryingResources &&
        resourceBeforeEdgePixels.forall(_ < 8)
    val baseFrom = worker.base.orElse(resourceBefore.flatMap(_.base))
    val baseTo = resource.base
    val workerToResourceCost = baseFrom.flatMap(baseA => baseTo.map(baseB => baseCosts(baseA, baseB)())).getOrElse(worker.pixelDistanceTravelling(resource.pixelCenter))
    val workerToResourceHysteresis = if (stick) 0 else 12
    val framesToResource = worker.framesToTravelPixels(workerToResourceCost + workerToResourceHysteresis)
    val framesSpentGathering = Math.max(24, kLookaheadFrames - framesToResource)
    val gasPreference = if (resource.unitClass.isGas) gasWorkerDesire else 1.0
    val stickiness = if (stick) 2.0 else 1.0
    val output = throughput * speed * framesSpentGathering * gasPreference * stickiness
    output
  }
}