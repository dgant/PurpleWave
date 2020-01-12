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
  // See LICENSE: https://github.com/TorchCraft/TorchCraftAI/blob/master/LICENSE

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
    var gasWorkersNow = resourceByWorker.count(_._2.unitClass.isGas)

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
    val depotToResourceDistances = workersByResource
      .keys
      .map(resource => (
        resource,
        ByOption
          .min(With.geography.ourBases.filter(_.townHall.isDefined).map(base =>
            if (resource.base.contains(base)) base.townHall.get.pixelDistanceEdge(resource) else base.townHallTile.groundPixels(resource.tileTopLeft)))
          .getOrElse(lightYear)))
      .toMap

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
      ByOption.min(newResources.filter(_.unitClass.isGas).map(_.pixelDistanceEdge(worker)))
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
        if (newResources.nonEmpty) {
          newGasDistance(worker)
        } else {
          minGasDistance.getOrElse(worker, None).getOrElse(Double.PositiveInfinity)
        }
      } else {
        worker.id // TODO: This used to be "Frames since update" using the cooldown mechanism, ie. update least-recently-updated worker
      })

    // Update workers in priority order
    // TODO: Cap worker updates ber run, for performance
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
        val resourceBefore = resourceByWorker.get(worker);
        val resourceBeforeEdgePixels = resourceBefore.map(_.pixelDistanceEdge(worker))
        unassignWorker(worker)
        if (resourceBefore.exists(_.unitClass.isGas)) {
          gasWorkersNow -= 1
        }

        val gasWorkerDesire = if (gasWorkersNow < gasWorkersMax) 1.0 else 0.1

        // Evaluate the marginal efficacy of assigning this worker to a resource.
        def scoreResource(resource: UnitInfo): Double = {
          val kInvalid = 1e100d
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
          val depotToResourceDistance = depotToResourceDistances(resource)
          val speed = 3.0 + 12.0 / Math.max(12.0, depotToResourceDistance)

          if (depotToResourceDistance > kDistanceMining) {
            if ( ! canDistanceMine) {
              return kInvalid
            }
          }

          // When deciding whether to travel to another base to mine, there's a
          // tradeoff between mining efficiency and the time-discounted value of
          // resources.
          // There's no obvious way to measure the tradeoff, so it's left as a
          // hyperparameter.
          val stick = resourceBefore.contains(resource) &&
              ! worker.carryingResources &&
              resourceBeforeEdgePixels.forall(_ < 8)
          val baseFrom = resourceBefore.flatMap(_.base)
          val baseTo = resource.base
          val workerToResource = baseFrom.flatMap(baseA => baseTo.map(baseB => baseCosts(baseA, baseB))).getOrElse(worker.pixelDistanceEdge(resource))
          val kHysteresisPixels = 12
          val kLookaheadFrames = 24 * 60
          val workerToResourceHysteresis = if (stick) 0 else 12
          val framesToResource = worker.framesToTravelPixels(workerToResource + workerToResourceHysteresis)
          val framesSpentGathering = Math.max(24, kLookaheadFrames - framesToResource)
          val gasPreference = if (resource.unitClass.isGas) gasWorkerDesire else 1.0
          val stickiness = if (stick) 2.0 else 1.0
          val output = -throughput * speed * framesSpentGathering * gasPreference * stickiness
          output
        }
        /*




          // When deciding whether to travel to another base to mine, there's a
          // tradeoff between mining efficiency and the time-discounted value of
          // resources.
          // There's no obvious way to measure the tradeoff, so it's left as a
          // hyperparameter.
          const bool stick = resourceBefore == resource &&
              !worker->carryingResources() &&
              (resourceBefore == nullptr || resourceBeforeEdgePixels > 1);
          const auto baseFrom = utils::contains(resourceBases, resourceBefore)
              ? resourceBases[resourceBefore]
              : nullptr;
          const auto baseTo = utils::contains(resourceBases, resource)
              ? resourceBases[resource]
              : nullptr;
          const double workerToResource = (baseFrom && baseTo && baseFrom != baseTo)
              ? baseCosts[baseFrom][baseTo]
              : utils::distanceBB(worker, resource);
          const double framesToResource =
              (workerToResource + (stick ? 0.0 : FLAGS_gatherer_sticky_distance)) /
              std::max(.01, worker->topSpeed);
          const double framesGathering =
              std::max(24.0, FLAGS_gatherer_lookahead - framesToResource);
          const double preference = resource->type->isGas ? gasWorkerDesire : 1.0;
          const double stickiness = stick ? FLAGS_gatherer_sticky_multiplier : 1.0;

          VLOG(6) << fmt::format(
              "Eval of {} for {}: {} {} {} {} {}",
              utils::unitString(resource),
              utils::unitString(worker),
              throughput,
              speed,
              int(framesGathering),
              preference,
              stickiness);
          return -throughput * speed * framesGathering * preference * stickiness;
             */
      })
  }




}