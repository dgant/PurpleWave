package Planning.Plans.Macro.Automatic

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable

class Gather extends Plan {

  val workers = new LockUnits {
    unitMatcher.set(UnitMatchWorkers)
    unitCounter.set(UnitCountEverything)
  }
  
  private val mineralSaturationRatio = 2.0
  private val gasSaturationRatio = 3.0
  
  private val resourceByWorker  = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  private val workersByResource = new mutable.HashMap[UnitInfo, mutable.HashSet[FriendlyUnitInfo]]
  
  private var activeBases             : Iterable[Base]                    = Vector.empty
  private var allWorkers              : Set[FriendlyUnitInfo]             = Set.empty
  private var minerals                : Set[UnitInfo]                     = Set.empty
  private var gasses                  : Set[UnitInfo]                     = Set.empty
  private var allResources            : Set[UnitInfo]                     = Set.empty
  private var haveEnoughGas           : Boolean                           = false
  private var gasWorkerTargetRatio    : Double                            = 0.0
  private var workersForGas           : Int                               = 0
  private var workersForMinerals      : Int                               = 0
  private var workersPerMineral       : Double                            = 0.0
  private var workersPerGas           : Double                            = 0.0
  private var workersOnMinerals       : Int                               = 0
  private var workersOnGas            : Int                               = 0
  private var excessWorkersOnGas      : Int                               = 0
  private var excessWorkersOnMinerals : Int                               = 0
  private var unassignedWorkers       : Set[FriendlyUnitInfo]             = Set.empty
  private var numberToAddToGas        : Int                               = 0
  private var workersToAddToMinerals  : Set[FriendlyUnitInfo]             = Set.empty
  private var workersToAddToGas       : Set[FriendlyUnitInfo]             = Set.empty
  private var needPerMineral          : mutable.Map[UnitInfo, Double]     = mutable.HashMap.empty
  private var needPerGas              : mutable.Map[UnitInfo, Double]     = mutable.HashMap.empty
  
  private def needPerResource(resource: UnitInfo): Double =
    if (resource.unitClass.isGas)
      needPerGas(resource)
    else
      needPerMineral(resource)
  
  override def onUpdate() {
    updateWorkerInformation()
    updateResourceInformation()
    decideLongDistanceMining()
    removeUnavailableWorkers()
    removeUnavailableResources()
    decideIdealWorkerDistribution()
    distributeUnassignedWorkers()
    orderAllWorkers()
  }
  
  private def updateWorkerInformation() = {
    workers.acquire(this)
    allWorkers = workers.units
  }
  
  private def updateResourceInformation() = {
    activeBases  = With.geography.ourBases.filter(_.townHall.exists(th => th.aliveAndComplete && (th.complete || th.morphing || th.remainingBuildFrames < 24 * 10)))
    minerals     = activeBases.flatten(base => base.minerals).filter(_.alive).toSet
    gasses       = activeBases.flatten(base => base.gas).filter(gas => gas.isOurs && gas.aliveAndComplete).toSet
    allResources = minerals ++ gasses
  }
  
  private def decideLongDistanceMining() {
    if (minerals.size < 9) {
      val globalMinerals = With.units.neutral.filter(_.unitClass.isMinerals)
      if (globalMinerals.nonEmpty) {
        minerals ++= globalMinerals
          .toVector
          .sortBy(_.zone.distancePixels(With.geography.home.zone))
          .take(9)
      }
    }
  }
  
  private def removeUnavailableWorkers() = {
    resourceByWorker.keySet.diff(allWorkers).foreach(unassignWorker)
  }
  
  private def removeUnavailableResources() = {
    workersByResource.keySet.diff(allResources).foreach(unassignResource)
  }
  
  private def gasWorkers: Int = {
    Vector(
      gasses.size * 3,
      Math.max(
        (allWorkers.size * gasWorkerTargetRatio).toInt,
        if (haveEnoughGas) 0 else 200))
    .min
  }
  
  private def decideIdealWorkerDistribution() {
    val gasLimitFloor       = With.blackboard.gasLimitFloor
    val gasLimitCeiling     = With.blackboard.gasLimitCeiling
    val floor               = Math.min(gasLimitFloor, gasLimitCeiling)
    val ceiling             = Math.max(gasLimitFloor, gasLimitCeiling)
    gasWorkerTargetRatio    = With.blackboard.gasTargetRatio
    haveEnoughGas           = With.self.gas >= PurpleMath.clamp(With.self.minerals, floor, ceiling)
    
    val totalWorkers        = allWorkers.size
    workersForGas           = gasWorkers
    workersForMinerals      = totalWorkers - workersForGas
    workersOnGas            = gasses.toVector.map(gas => workersByResource.get(gas).map(_.size).getOrElse(0)).sum
    workersOnMinerals       = minerals.toVector.map(min => workersByResource.get(min).map(_.size).getOrElse(0)).sum
    excessWorkersOnGas      = workersOnGas - workersForGas
    excessWorkersOnMinerals = workersOnMinerals - workersForMinerals
    workersPerGas           = if (gasses.isEmpty) 0 else Math.max(workersForGas.toDouble / gasses.size, gasSaturationRatio)
    needPerMineral = new mutable.HashMap[UnitInfo, Double] ++
      minerals
        .map(mineral => (mineral,
          Math.min(
            mineralSaturationRatio - workersByResource.get(mineral).map(_.size).sum,
            Math.ceil(workersForMinerals.toDouble / minerals.size)
          )))
        .toMap
    needPerGas = new mutable.HashMap[UnitInfo, Double] ++
      gasses
        .map(gas => (gas,
          Math.min(
            gasSaturationRatio - workersByResource.get(gas).map(_.size).sum,
            Math.ceil(workersForMinerals.toDouble / gasses.size)
          )))
        .toMap
    unassignSupersaturatingWorkers()
    unassignedWorkers       = allWorkers.diff(resourceByWorker.keySet)
    numberToAddToGas        = Math.max(0, workersForGas - workersOnGas)
    workersToAddToGas       = unassignedWorkers.take(numberToAddToGas)
    workersToAddToMinerals  = unassignedWorkers.drop(numberToAddToGas)
  }
  
  def unassignSupersaturatingWorkers() {
    allResources
      .toVector
      .filter(needPerResource(_) < 0)
      .sortBy(needPerResource)
      .take(Math.max(excessWorkersOnGas, excessWorkersOnMinerals))
      .foreach(resource =>
        workersByResource(resource)
          .toVector
          .sortBy(- _.pixelsFromEdge(resource))
          .foreach(unassignWorker))
  }
  
  private def distributeUnassignedWorkers() {
    workersToAddToMinerals.foreach(addWorkerToMinerals)
    workersToAddToGas.foreach(addWorkerToGas)
  }
  
  private def addWorkerToMinerals(worker: FriendlyUnitInfo) {
    if (minerals.isEmpty) return
    val mineral = minerals
      .toVector
      .sortBy(_.pixelsFromEdge(worker))
      .maxBy(needPerMineral)
    assignWorker(worker, mineral)
    needPerMineral(mineral) -= 1
  }

  private def addWorkerToGas(worker: FriendlyUnitInfo) {
    if (gasses.isEmpty) return
    val gas = gasses
      .toVector
      .sortBy(_.pixelsFromEdge(worker))
      .maxBy(needPerGas)
    assignWorker(worker, gas)
    needPerGas(gas) -= 1
  }
  
  private def orderAllWorkers() {
    allWorkers.foreach(order)
  }
  
  private def assignWorker(worker: FriendlyUnitInfo, resource: UnitInfo) {
    unassignWorker(worker)
    if ( ! workersByResource.contains(resource)) {
      workersByResource.put(resource, new mutable.HashSet)
    }
    workersByResource(resource).add(worker)
    resourceByWorker.put(worker, resource)
  }
  
  private def unassignWorker(worker: FriendlyUnitInfo) {
    resourceByWorker.get(worker).foreach(resource => workersByResource.get(resource).foreach(_.remove(worker)))
    resourceByWorker.remove(worker)
  }
  
  private def unassignResource(resource: UnitInfo) {
    if (workersByResource.contains(resource)) {
      workersByResource(resource).foreach(unassignWorker)
      workersByResource.remove(resource)
    }
  }
  
  private def order(worker: FriendlyUnitInfo) {
    worker.agent.intend(this, new Intention {
      toGather = resourceByWorker.get(worker)
      canCower = true
    })
  }
}
