package Planning.Plans.Macro.Automatic

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable

class Gather extends Plan {
  
  
  val workerLock = new LockUnits {
    unitMatcher.set(UnitMatchWorkers)
    unitCounter.set(UnitCountEverything)
  }
  
  var resource  : Set[UnitInfo] = Set.empty
  var minerals  : Set[UnitInfo] = Set.empty
  var gasses    : Set[FriendlyUnitInfo] = Set.empty
  var resources : Set[UnitInfo] = Set.empty
  var workers   : Set[FriendlyUnitInfo] = Set.empty
  var gasWorkersNow = 0
  var gasWorkersMax = 0
  var workersByResource = new mutable.HashMap[UnitInfo, mutable.HashSet[FriendlyUnitInfo]]
  var resourceByWorker = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  
  override def onUpdate() {
    countUnits()
    setGoals()
    workers.foreach(updateWorker)
  }
  
  private def countUnits() {
    minerals  = With.geography.ourBases.flatMap(_.minerals).toSet
    gasses    = With.geography.ourBases.flatMap(_.gas.flatMap(_.friendly).filter(u => u.gasLeft > 0 && u.complete)).toSet
    if (minerals.isEmpty) {
      minerals = With.geography.bases.flatMap(_.minerals).toSet
    }
    if (gasses.isEmpty) {
      gasses = With.units.ours.filter(_.unitClass.isGas)
    }
    resources = gasses ++ minerals
    if (resources.isEmpty) {
      return
    }
    workerLock.acquire(this)
    workers = workerLock.units
    
    gasWorkersNow = 0
    workersByResource.clear()
    resources.foreach(resource =>
      workersByResource(resource) = new mutable.HashSet[FriendlyUnitInfo])
    workers.foreach(unit =>
      unit.agent.lastIntent.toGather.foreach(resource =>
        assign(unit, resource)
      ))
  }
  
  private def setGoals() {
    val belowFloor    = With.self.gas < With.blackboard.gasLimitFloor
    val aboveCeiling  = With.self.gas > With.blackboard.gasLimitCeiling
    gasWorkersMax =
      if (belowFloor)
        workers.size
      else if (aboveCeiling)
        0
      else
        (workers.size * With.blackboard.gasTargetRatio).toInt
  }
  
  private def utility(worker: FriendlyUnitInfo, resource: UnitInfo): Double = {
    val saturationCap = if (resource.unitClass.isMinerals) 2 else 3
    val continuity = if (resourceByWorker.get(worker).contains(resource)) 1.2 else 1.0
    val distance = worker.pixelsFromEdge(resource)
    val saturation =
      if (workersByResource(resource).size >= saturationCap)
        0.001
      else if (resource.unitClass.isMinerals && workersByResource(resource).nonEmpty)
        0.8
      else
        1.0
    val output = continuity * saturation / Math.log(32.0 + distance)
    output
  }
  
  private def updateWorker(worker: FriendlyUnitInfo) {
    val bestResource = resources.maxBy(utility(worker, _))
    assign(worker, bestResource)
    worker.agent.intend(this, new Intention {
      toGather = resourceByWorker.get(worker)
      canCower = true
    })
    
  }
  
  private def isActiveGas(resource: UnitInfo): Boolean = resource.gasLeft > 0
  
  private def assign(worker: FriendlyUnitInfo, resource: UnitInfo) {
    if (isActiveGas(resource)) {
      gasWorkersNow += 1
    }
    unassign(worker)
    resourceByWorker(worker) = resource
    workersByResource(resource) += worker
  }
  
  private def unassign(worker: FriendlyUnitInfo) {
    val resource = resourceByWorker.get(worker)
    if (resource.exists(isActiveGas)) {
      gasWorkersNow += 1
    }
    resource.foreach(workersByResource(_).remove(worker))
    resourceByWorker.remove(worker)
  }
}