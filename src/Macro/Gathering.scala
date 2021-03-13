package Macro

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Performance.Cache
import Performance.TaskQueue.TaskQueueGlobalWeights
import Performance.Tasks.TimedTask
import Planning.Prioritized
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Tactics.Gather
import Utilities.ByOption

import scala.collection.mutable
import scala.util.Random

class Gathering extends TimedTask with AccelerantMinerals with Zippers {

  withWeight(TaskQueueGlobalWeights.Gather)

  def getWorkersByResource(resource: UnitInfo): Iterable[FriendlyUnitInfo] = workersByResource.getOrElse(resource, Iterable.empty)

  private val kDistanceMining: Int = 32 * 12
  private val kInvalidResourceScore = 1e100d
  private val kHysteresisPixels = 12
  private val kLookaheadFrames: Int = 24 * 60
  private val kLightYear: Double = 1e10
  private val kGasCompletionFrames = 48

  // Adapted from https://github.com/TorchCraft/TorchCraftAI/blob/master/src/modules/gatherer/gathererassignments.cpp
  // See LICENSE: https://github.com/TorchCraft/TorchCraftAI/blob/master/LICENSE

  var workers: collection.Set[FriendlyUnitInfo] = Set.empty
  var gatheringPlan: Prioritized = new Gather

  private val resourceByWorker = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  private val workersByResource = new mutable.HashMap[UnitInfo, mutable.Set[FriendlyUnitInfo]]
  private val workerCooldownUntil = new mutable.HashMap[FriendlyUnitInfo, Int]

  private def isValidResource (unit: UnitInfo): Boolean = isValidMineral(unit) || isValidGas(unit)
  private def isValidMineral  (unit: UnitInfo): Boolean = unit.alive && unit.mineralsLeft > 0
  private def isValidGas      (unit: UnitInfo): Boolean = unit.alive && unit.isOurs && unit.unitClass.isGas && unit.remainingCompletionFrames < 24 * 2

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
  private val resourcesToExclude  = new Cache(() => workersByResource
    .keysIterator
    .filterNot(resource =>
          (isValidMineral(resource) && (longMineMinerals()  || ! isLongDistanceResource(resource)))
      ||  (isValidGas(resource)     && (longMineGas()       || ! isLongDistanceResource(resource))))
    .toVector)
  private val resourceHallPixels  = new Cache(() => workersByResource
    .keysIterator
    .map(resource => (resource, ByOption.min(miningBases().map(_.townHall.get.pixelDistanceEdge(resource))).getOrElse(kLightYear)))
    .toMap)

  private def isLongDistanceResource(unit: UnitInfo): Boolean = ! miningBases().exists(unit.base.contains)
  private def isNewResource(unit: UnitInfo): Boolean = ! workersByResource.contains(unit)

  private def assignWorker(worker: FriendlyUnitInfo, resource: UnitInfo): Unit = {
    unassignWorker(worker)
    resourceByWorker(worker) = resource
    workersByResource(resource) += worker
  }

  private def unassignWorker(worker: FriendlyUnitInfo): Unit = {
    resourceByWorker.get(worker).map(workersByResource.get).foreach(_.foreach(_.remove(worker)))
    resourceByWorker.remove(worker)
  }

  private def includeResource(resource: UnitInfo): Unit = {
    workersByResource(resource) = workersByResource.getOrElse(resource, mutable.Set.empty)
  }

  private def containsResource(resource: UnitInfo): Boolean = workersByResource.contains(resource)

  private def excludeResource(resource: UnitInfo): Unit = {
    workersByResource.get(resource).foreach(_.foreach(resourceByWorker.remove))
    workersByResource.remove(resource)
  }

  private def getResource(worker: FriendlyUnitInfo): Option[UnitInfo] = resourceByWorker.get(worker)

  private def countWorkers(resource: UnitInfo): Int = workersByResource.get(resource).map(_.size).getOrElse(0)

  override def onRun(budgetMs: Long) {
    initializeAccelerators()
    if (workers.isEmpty) return

    resourceByWorker.keys.withFilter( ! workers.contains(_)).foreach(unassignWorker)
    workerCooldownUntil.keys.withFilter( ! workers.contains(_)).foreach(workerCooldownUntil.remove)
    // TODO: Unassign any workers who are no longer in the squad -- TCAI does this with the controller

    newMinerals().foreach(includeResource)
    newGas().foreach(includeResource)
    resourcesToExclude().foreach(excludeResource)

    val totalMineralPatches = With.geography.ourBases.view.map(_.minerals.size).sum
    val totalGasPumps       = With.geography.ourBases.view.map(_.gas.count(_.isOurs)).sum
    val canDistanceMine     = totalMineralPatches < 7

    // Respect gas limitations
    // - If our gas is below our floor,       use maximum worker count
    // - If our gas is at/above our ceiling,  use minimum worker count
    val gasWorkersHardMinimum = Math.max(0,                 Math.min(With.blackboard.gasWorkerFloor(), With.blackboard.gasWorkerCeiling()))
    val gasWorkersHardMaximum = Math.min(4 * totalGasPumps, Math.max(With.blackboard.gasWorkerFloor(), With.blackboard.gasWorkerCeiling()))
    val gasGoalMinimum        = Math.min(With.blackboard.gasLimitFloor(), With.blackboard.gasLimitCeiling())
    val gasGoalMaximum        = Math.max(With.blackboard.gasLimitFloor(), With.blackboard.gasLimitCeiling())
    val gasNow                = With.self.gas
    val gasWorkersTripMinimum = (7 + gasGoalMinimum - With.self.gas) / 8
    val gasWorkersTripMaximum = (7 + gasGoalMaximum - With.self.gas) / 8
    val gasWorkersRatioTarget = Math.round(With.blackboard.gasWorkerRatio() * workers.size).toInt
    val gasWorkersBaseTarget  = PurpleMath.clamp(gasWorkersRatioTarget, gasWorkersTripMinimum, gasWorkersTripMaximum)
    val gasWorkerTarget       = PurpleMath.clamp(gasWorkersBaseTarget, gasWorkersHardMinimum, gasWorkersHardMaximum)
    var gasWorkersNow         = resourceByWorker.count(_._2.unitClass.isGas)
    def needMoreGasWorkers    = gasWorkersNow < gasWorkerTarget
    def needFewerGasWorkers   = gasWorkersNow > gasWorkerTarget

    // Evaluates the marginal efficacy of assigning this worker to a resource.
    case class ResourceScore(worker: FriendlyUnitInfo, resource: UnitInfo, var resourceBefore: Option[UnitInfo] = None) {
      resourceBefore = resourceBefore.orElse(resourceByWorker.get(worker))

      // How many workers are already mining this patch?
      // Don't count workers that are further away -- the closest workers get priority on the patch
      val workersBefore = workersByResource
        .get(resource)
        .map(_.count(_ != worker))
        .getOrElse(0)

      // The depot-to-resource distance is used in two ways:
      // -Resources close to the hall should get priority on their first two workers
      // -Resources far from the hall benefit more from the third worker
      val hallToResourcePixels = resourceHallPixels()(resource)

      // Geyser mining speed varies based on direction relative to town hall
      val belowTownHall = resource.base.map(_.townHallTile).exists(t => t.y < resource.tileTopLeft.y - 3 || t.x < resource.tileTopLeft.x - 4)

      // Based on https://tl.net/forum/bw-strategy/551478-efficient-gas-mining
      val needFourOnGas = resource.unitClass.isGas && belowTownHall

      val distanceRatio = PurpleMath.clamp(hallToResourcePixels / (32.0 * 3.0), 1.0, 2.0)

      // How effective will the next worker be on this resource?
      var throughput = 0.001
      if (resource.unitClass.isGas) {
        if (workersBefore == 3 && needFourOnGas) {
          throughput = 0.75
        } else if (workersBefore < 3) {
          throughput = 1.0
        }
        // Account for geyser depletion
        if (resource.resourcesLeft < 8) {
          throughput *= 0.25
        }
      } else {
        if (workersBefore == 0) {
          // First workers prefer closer resources
          // Range on 1.0 - 1.5
          throughput = 1.0 + 0.5 * (2.0 - distanceRatio)
        } else if (workersBefore == 1) {
          // Second workers prefer distant resources (since they'll spend more time mining)
          // Range on 0.8 - 0.95
          throughput = 0.8 + 0.15 * (distanceRatio - 1.0)
        } else if (workersBefore < 3) {
          // Third workers also prefer distant resources
          // Range on 0.5 - 0.7
          throughput = 0.5 + 0.2 * (distanceRatio - 1.0)
        }
      }
      // Don't encourage redistributing a worker who's currently approaching their minerals
      val resourceBeforeEdgePixels    = resourceBefore.map(_.pixelDistanceEdge(worker))
      val stick                       = resourceBefore.contains(resource) && ! worker.carrying && resourceBeforeEdgePixels.forall(_ < 64)
      val baseFrom                    = worker.base.orElse(resourceBefore.flatMap(_.base))
      val baseTo                      = resource.base
      val workerToResourceCost        = baseFrom.flatMap(baseA => baseTo.map(baseB => baseCosts(baseA, baseB)())).getOrElse(worker.pixelDistanceTravelling(resource.pixel))
      val workerToResourceHysteresis  = if (stick) 0 else 32
      val framesToResource            = worker.framesToTravelPixels(workerToResourceCost + workerToResourceHysteresis)
      val framesSpentGathering        = Math.max(24, kLookaheadFrames - framesToResource)
      val gasPreference               = if (resource.unitClass.isGas) (if (needMoreGasWorkers) 2.0 else if (needFewerGasWorkers) 0.1 else 1.0) else 1.0 // When we don't want gas, we still want it more than the third mineral worker
      val stickiness                  = if (stick) 2.0 else 1.0
      val output                      = throughput * framesSpentGathering * gasPreference * stickiness
    }

    // Update workers in priority order.
    //
    // When a new resource is available:
    // Prioritize workers closest to the new resource.
    //
    // Otherwise:
    // Sort workers by frames since update, descending.
    def newResourceDistance(worker: FriendlyUnitInfo): Option[Double] = {
      ByOption.min((newMinerals() ++ newGas()).map(_.pixelDistanceEdge(worker)))
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
                  .map(resource => worker.pixelDistanceTravelling(resource._1.pixel))))) // TODO: Used edge distance?
          .toMap
      } else Map.empty
    val respectCooldown = gasWorkersNow >= gasWorkerTarget

    // Update workers in priority order
    class OrderedWorker(val worker: FriendlyUnitInfo) {
      lazy val cd: Double = workerCooldownUntil.getOrElse(worker, 0).toDouble
      val order: Double = if (respectCooldown) cd else newResourceDistance(worker).orElse(minGasDistance(worker)).getOrElse(cd)
    }
    val workersToUpdate = workers
      .view
      .filter(worker => ! respectCooldown || workerCooldownUntil.get(worker).forall(_ <= With.frame))
      .filter(worker => {
        // Don't interrupt workers who are about to reach minerals.
        val resourceBefore = resourceByWorker.get(worker)
        val resourceBeforeEdgePixels = resourceBefore.map(_.pixelDistanceEdge(worker))
        resourceBefore.forall(r => r.unitClass.isGas || workersByResource(r).size > 5 || resourceBeforeEdgePixels.forall(_ > 64)) // The > 5 check is for disentangling massively popular resources
      })
      .map(new OrderedWorker(_))
      .toVector
      .sortBy(_.order)
    workersToUpdate
      .foreach(orderedWorker => {
        val worker = orderedWorker.worker
        val resourceBefore = resourceByWorker.get(worker)
        unassignWorker(worker)
        if (resourceBefore.exists(_.unitClass.isGas)) {
          gasWorkersNow -= 1
        }

        val gasWorkerDesired = gasWorkersNow < gasWorkerTarget

        // For easy debugging
        if (worker.selected) {
          val scores = workersByResource.keysIterator.map(ResourceScore(worker, _, resourceBefore)).toVector.sortBy(_.output)
          if (With.frame < 0) { // Cheat the optimizer
            System.out.println(scores.toString)
          }
        }

        // Assign the worker to tbhe best resource
        val resourceScores = workersByResource.keysIterator.map(ResourceScore(worker, _, resourceBefore)).toVector // TODO TEMPORARY
        val resourceBestScore = ByOption.maxBy(resourceScores)(_.output)
        resourceBestScore.foreach(bestResourceScore => {
          val bestResource = bestResourceScore.resource
          assignWorker(worker, bestResource)
          if (resourceBefore.exists(_.unitClass.isGas)) gasWorkersNow -= 1
          if (bestResource.unitClass.isGas) gasWorkersNow += 1
        })

        worker.agent.intend(gatheringPlan, new Intention { toGather = resourceByWorker.get(worker) })
        workerCooldownUntil(worker) = With.frame + 48 + Random.nextInt(48)
      })
  }
}