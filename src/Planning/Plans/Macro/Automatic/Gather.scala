package Planning.Plans.Macro.Automatic

import Micro.Intentions.Intention
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorker
import Planning.Plan
import Planning.Plans.Allocation.LockUnits
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Startup.With

import Utilities.EnrichPosition._
import scala.collection.mutable
import scala.collection.mutable.ListBuffer

class Gather extends Plan {

  val workers = new LockUnits {
    unitMatcher.set(UnitMatchWorker)
    unitCounter.set(UnitCountEverything)
  }
  
  val resourceByWorker = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  val workersByResource = new mutable.HashMap[UnitInfo, ListBuffer[FriendlyUnitInfo]]
  
  override def onFrame() {
    
    workers.onFrame()
    
    val allWorkers    = workers.units
    val allMinerals   = With.geography.ourBases.flatten(base => base.minerals).toSet
    val allGas        = With.geography.ourBases.flatten(base => base.gas).filter(gas => gas.complete && gas.isOurs).toSet
    val allResources  = (allMinerals ++ allGas)
    
    //Remove dead/unassigned units
    resourceByWorker.keySet.diff(allWorkers).foreach(unassignWorker)
    workersByResource.keySet.filterNot(_.alive).foreach(unassignResource)
    
    //Remove dangerous resources
    var safeMinerals  = allMinerals.filter(safe)
    val safeGas       = allGas.filter(safe)
    val safeResources = allResources.filter(safe)
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
    
    //Figure out which resources can lose workers, and which need them
    val workerDistribution = new WorkerDistribution(
      safeMinerals,
      safeGas,
      workersPerMineral,
      workersPerGas)
  
    //Assign the unassigned workers
    //TODO: Occasionally include inefficiently assigned workers for reassignment
    val unassignedWorkers       = allWorkers.diff(resourceByWorker.keySet)
    val numberToAddToGas        = Math.max(0, workersForGas - workersOnGas)
    val workersToAddToGas       = unassignedWorkers.take(numberToAddToGas)
    val workersToAddToMinerals  = unassignedWorkers.drop(numberToAddToGas)
    
    workersToAddToMinerals.foreach(workerDistribution.addWorkerToMinerals)
    workersToAddToGas.foreach(workerDistribution.addWorkerToGas)
  
    //Order workers
    allWorkers.foreach(order)
  }
  
  private class WorkerDistribution(
    safeMinerals      : Set[UnitInfo],
    safeGas           : Set[UnitInfo],
    workersPerMineral : Double,
    workersPerGas     : Double) {
    
    private val needPerMineral  = new mutable.HashMap[UnitInfo, Double] ++
      safeMinerals
        .map(mineral => (mineral,
          workersPerMineral - workersByResource.get(mineral)
            .map(_.size)
            .getOrElse(0)))
        .toMap
    
    private val needPerGas = new mutable.HashMap[UnitInfo, Double] ++
      safeGas.map(gas => (gas,
        workersPerGas - workersByResource.get(gas)
          .map(_.size)
          .getOrElse(0))).toMap
    
    private val mineralsByZone =
      With.geography.ourZones
        .map(zone =>
          (zone,
            zone.bases.flatten(base =>
              base.minerals.toList.sortBy(_.pixelDistance(base.townHallRectangle.midPixel)))))
        .toMap
    
    def addWorkerToMinerals(worker:FriendlyUnitInfo) {
      if (safeMinerals.isEmpty) return
      val currentZone = worker.pixelCenter.zone
      var candidate:Option[UnitInfo] = None
      if (mineralsByZone.get(currentZone).exists(_.nonEmpty)) {
        candidate = Some(mineralsByZone(currentZone).maxBy(needPerMineral(_)))
      }
      else {
        candidate = Some(safeMinerals.minBy(mineral => needPerMineral(mineral)))
      }
      if (candidate.nonEmpty) {
        assignWorker(worker, candidate.get)
        needPerMineral(candidate.get) -= 1
      }
    }
  
    def addWorkerToGas(worker:FriendlyUnitInfo) {
      if (safeGas.isEmpty) return
      val currentZone = worker.pixelCenter.zone
      var candidate:Option[UnitInfo] = None
      val currentZoneGas = currentZone.bases.filter(_.townHall.exists(_.complete)).flatten(_.gas).filter(gas => gas.complete && safeGas.contains(gas))
      if (currentZoneGas.nonEmpty) {
        candidate = Some(currentZoneGas.maxBy(needPerGas(_)))
      }
      else {
        candidate = Some(safeGas.minBy(Gas => needPerGas(Gas)))
      }
      if (candidate.nonEmpty) {
        assignWorker(worker, candidate.get)
        needPerGas(candidate.get) -= 1
      }
    }
  }
  
  def unassignWorker(worker:FriendlyUnitInfo) {
    resourceByWorker.get(worker).foreach(workersByResource.remove)
    resourceByWorker.remove(worker)
  }
  
  def unassignResource(resource:UnitInfo) {
    workersByResource.get(resource).foreach(workers => workers.foreach(resourceByWorker.remove))
    workersByResource.remove(resource)
  }
  
  def assignWorker(worker:FriendlyUnitInfo, resource:UnitInfo) {
    if ( ! workersByResource.contains(resource)) workersByResource.put(resource, ListBuffer.empty)
    workersByResource(resource).append(worker)
    resourceByWorker.put(worker, resource)
  }
  
  def safe(resource:UnitInfo):Boolean = {
    With.grids.enemyStrength.get(resource.tileCenter) == 0
  }
  
  def order(worker:FriendlyUnitInfo) {
    //If there's no resource for them to gather, this just produces default behavior
    With.executor.intend(new Intention(this, worker) { toGather = resourceByWorker.get(worker) })
  }
}
