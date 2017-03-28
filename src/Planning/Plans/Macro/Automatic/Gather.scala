package Planning.Plans.Macro.Automatic

import Micro.Intentions.Intention
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorker
import Planning.Plan
import Planning.Plans.Allocation.LockUnits
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Startup.With

import scala.collection.mutable

class Gather extends Plan {

  val workers = new LockUnits {
    unitMatcher.set(UnitMatchWorker)
    unitCounter.set(UnitCountEverything)
  }
  
  val resourceByWorker = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  val workersByResource = new mutable.HashMap[UnitInfo, mutable.HashSet[FriendlyUnitInfo]]
  
  override def onFrame() {
    
    workers.onFrame()
    
    val ourActiveBases = With.geography.ourBases.filter(_.townHall.exists(_.complete))
    val allWorkers    = workers.units
    val allMinerals   = ourActiveBases.flatten(base => base.minerals).filter(_.alive).toSet
    val allGas        = ourActiveBases.flatten(base => base.gas).filter(gas => gas.isOurs  && gas.complete && gas.alive).toSet
    val allResources  = (allMinerals ++ allGas)
    
    //Remove dead/unassigned units
    resourceByWorker.keySet.diff(allWorkers).foreach(unassignWorker)
    workersByResource.keySet.filterNot(_.alive).foreach(unassignResource)
    
    //Remove dangerous resources
    var safeMinerals  = allMinerals   .filter(safe)
    val safeGas       = allGas        .filter(safe)
    val safeResources = allResources  .filter(safe)
    workersByResource.keySet.diff(safeResources).foreach(unassignResource)
    
    //Long-distance mining
    if (safeMinerals.size < 7) {
      val allSafeMinerals = With.units.neutral.filter(_.unitClass.isMinerals).filter(safe)
      if (allSafeMinerals.nonEmpty) {
        safeMinerals = allSafeMinerals
          .toList
          .sortBy(mineral => With.paths.groundPixels(mineral.tileCenter, With.geography.home))
          .take(7)
          .toSet
      }
    }
    
    //Figure out the ideal worker distribution
    val haveEnoughGas       = With.self.gas >= Math.max(400, With.self.minerals)
    val workersForGas       = List(safeGas.size * 3, allWorkers.size/3, if(haveEnoughGas) 0 else 200).min
    val workersForMinerals  = allWorkers.size - workersForGas
    val workersPerMineral   = Math.min(2.0, workersForMinerals.toDouble / safeMinerals.size)
    val workersPerGas       = if (safeGas.size == 0) 0 else workersForGas.toDouble / safeGas.size
    val workersOnGas        = safeGas.toList.map(gas => workersByResource.get(gas).map(_.size).getOrElse(0)).sum
  
    //Assign the unassigned workers
    //TODO: Occasionally include inefficiently assigned workers for reassignment
    val unassignedWorkers       = allWorkers.diff(resourceByWorker.keySet)
    val numberToAddToGas        = Math.max(0, workersForGas - workersOnGas)
    val workersToAddToGas       = unassignedWorkers.take(numberToAddToGas)
    val workersToAddToMinerals  = unassignedWorkers.drop(numberToAddToGas)
  
    //Figure out which resources can lose workers, and which need them
    distributeWorkers (
      safeMinerals,
      safeGas,
      workersPerMineral,
      workersPerGas,
      workersToAddToMinerals,
      workersToAddToGas)
  
    //Order workers
    allWorkers.foreach(order)
  }
  
  def distributeWorkers (
    safeMinerals            : Set[UnitInfo],
    safeGas                 : Set[UnitInfo],
    workersPerMineral       : Double,
    workersPerGas           : Double,
    workersToAddToMinerals  : Set[FriendlyUnitInfo],
    workersToAddToGas       : Set[FriendlyUnitInfo]) {
    
    val needPerMineral = new mutable.HashMap[UnitInfo, Double] ++
      safeMinerals
        .map(mineral => (mineral,
          workersPerMineral - workersByResource.get(mineral)
            .map(_.size)
            .getOrElse(0)))
        .toMap
    
    val needPerGas = new mutable.HashMap[UnitInfo, Double] ++
      safeGas.map(gas => (gas,
        workersPerGas - workersByResource.get(gas)
          .map(_.size)
          .getOrElse(0))).toMap
    
    val safeMineralsByZone =
      With.geography.ourZones
        .map(zone =>
          (zone,
            zone.bases.flatten(base =>
              base.minerals
                .filter(safeMinerals.contains)
                .toList
                .sortBy(_.pixelDistance(base.townHallRectangle.midPixel)))))
        .toMap
  
    workersToAddToMinerals.foreach(addWorkerToMinerals)
    workersToAddToGas.foreach(addWorkerToGas)
  
    def addWorkerToMinerals(worker:FriendlyUnitInfo) {
      if (safeMinerals.isEmpty) return
      val mineral = safeMinerals
        .toList
        .sortBy(_.pixelDistanceSquared(worker.pixelCenter))
        .maxBy(needPerMineral)
      assignWorker(worker, mineral)
      needPerMineral(mineral) -= 1
    }
  
    def addWorkerToGas(worker:FriendlyUnitInfo) {
      if (safeGas.isEmpty) return
      val gas = safeGas
        .toList
        .sortBy(_.pixelDistanceSquared(worker.pixelCenter))
        .maxBy(needPerGas)
      assignWorker(worker, gas)
      needPerGas(gas) -= 1
    }
  }
  
  def assignWorker(worker:FriendlyUnitInfo, resource:UnitInfo) {
    unassignWorker(worker)
    if ( ! workersByResource.contains(resource)) {
      workersByResource.put(resource, new mutable.HashSet)
    }
    workersByResource(resource).add(worker)
    resourceByWorker.put(worker, resource)
  }
  
  def unassignWorker(worker:FriendlyUnitInfo) {
    resourceByWorker.get(worker).foreach(resource => workersByResource(resource).remove(worker))
    resourceByWorker.remove(worker)
  }
  
  def unassignResource(resource:UnitInfo) {
    if (workersByResource.contains(resource)) {
      workersByResource(resource).foreach(unassignWorker)
      workersByResource.remove(resource)
    }
  }
  
  def safe(resource:UnitInfo):Boolean = {
    With.grids.enemyStrength.get(resource.tileCenter) == 0
  }
  
  def order(worker:FriendlyUnitInfo) {
    //If there's no resource for them to gather, this just produces default behavior
    With.executor.intend(new Intention(this, worker) { toGather = resourceByWorker.get(worker) })
  }
}
