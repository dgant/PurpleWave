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
    resourceByWorker.clear()
    resources.foreach(resource => workersByResource(resource) = new mutable.HashSet[FriendlyUnitInfo])
    workers.foreach(unit => unit.agent.lastIntent.toGather.foreach(resource => assign(unit, resource)))
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
  
  private def isActiveGas(resource: UnitInfo): Boolean = resource.gasLeft > 0
  
  private def assign(worker: FriendlyUnitInfo, resource: UnitInfo) {
    unassign(worker)
    if ( ! resources.contains(resource)) return
    if (isActiveGas(resource)) {
      gasWorkersNow += 1
    }
    resourceByWorker(worker) = resource
    workersByResource(resource) += worker
  }
  
  private def unassign(worker: FriendlyUnitInfo) {
    val resource = resourceByWorker.get(worker)
    if (resource.exists(isActiveGas)) {
      gasWorkersNow += 1
    }
    resource.foreach(workersByResource(_) -= worker)
    resourceByWorker.remove(worker)
  }
  
  private def updateWorker(worker: FriendlyUnitInfo) {
    val bestResource = resources.maxBy(utility(worker, _))
    assign(worker, bestResource)
    worker.agent.intend(this, new Intention {
      toGather = resourceByWorker.get(worker)
      canCower = true
    })
  }
  
  private def utility(worker: FriendlyUnitInfo, resource: UnitInfo): Double = {
    val need =
      if (gasWorkersNow == gasWorkersMax)
        1.0
      else if (resource.unitClass.isGas == (gasWorkersNow < gasWorkersMax))
        100.0
      else
        1.0
    
    val continuity  = if (resourceByWorker.get(worker).contains(resource)) 10.0 else 1.0
    val proximity   = resource.base.flatMap(_.townHall).map(_.pixelDistanceEdge(resource)).getOrElse(32 * 12)
    val distance    = worker.pixelDistanceEdge(resource)
    val saturation  =
      if (resource.unitClass.isMinerals) {
        workersByResource(resource).size match {
          case 0 => 1.0
          case 1 => 0.8
          case 2 => 0.1
          case _ => 0.0
        }
      }
      else {
        workersByResource(resource).size match {
          case 0 => 1.0
          case 1 => 1.0
          case 2 => 1.0
          case 3 => 0.1
          case _ => 0.0
        }
      }
    val output = need * continuity * saturation / (128.0 + distance)
    output
  }
}