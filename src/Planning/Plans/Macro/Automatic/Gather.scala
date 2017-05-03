package Planning.Plans.Macro.Automatic

import Information.Geography.Types.Base
import Micro.Intent.Intention
import Performance.Caching.Limiter
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorker
import Planning.Plan
import Planning.Composition.ResourceLocks.LockUnits
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Lifecycle.With

import scala.collection.mutable

class Gather extends Plan {

  val workers = new LockUnits {
    unitMatcher.set(UnitMatchWorker)
    unitCounter.set(UnitCountEverything)
  }
  
  private val mineralSaturationRatio = 2.0
  
  private val resourceByWorker  = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  private val workersByResource = new mutable.HashMap[UnitInfo, mutable.HashSet[FriendlyUnitInfo]]
  
  private var ourActiveBases          : Iterable[Base]                    = Vector.empty
  private var allWorkers              : Set[FriendlyUnitInfo]             = Set.empty
  private var allMinerals             : Set[UnitInfo]                     = Set.empty
  private var allGas                  : Set[UnitInfo]                     = Set.empty
  private var allResources            : Set[UnitInfo]                     = Set.empty
  private var safeMinerals            : Set[UnitInfo]                     = Set.empty
  private var safeGas                 : Set[UnitInfo]                     = Set.empty
  private var safeResources           : Set[UnitInfo]                     = Set.empty
  private var haveEnoughGas           : Boolean                           = false
  private var workersForGas           : Int                               = 0
  private var workersForMinerals      : Int                               = 0
  private var workersPerMineral       : Double                            = 0.0
  private var workersPerGas           : Double                            = 0.0
  private var workersOnGas            : Int                               = 0
  private var unassignedWorkers       : Set[FriendlyUnitInfo]             = Set.empty
  private var numberToAddToGas        : Int                               = 0
  private var workersToAddToMinerals  : Set[FriendlyUnitInfo]             = Set.empty
  private var workersToAddToGas       : Set[FriendlyUnitInfo]             = Set.empty
  private var needPerMineral          : mutable.Map[UnitInfo, Double]     = mutable.HashMap.empty
  private var needPerGas              : mutable.Map[UnitInfo, Double]     = mutable.HashMap.empty
  
  override def update() {
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
    ourActiveBases  = With.geography.ourBases.filter(_.townHall.exists(_.complete))
    allMinerals     = ourActiveBases.flatten(base => base.minerals).filter(_.alive).toSet
    allGas          = ourActiveBases.flatten(base => base.gas).filter(gas => gas.isOurs && gas.complete && gas.alive).toSet
    safeMinerals    = allMinerals.filter(safe)
    safeGas         = allGas.filter(safe)
    if (safeMinerals.isEmpty) safeMinerals  = allMinerals
    if (safeGas.isEmpty)      safeGas       = allGas
    safeResources = (safeMinerals ++ safeGas)
  }
  
  private def decideLongDistanceMining() {
    if (safeMinerals.size < 9) {
      val allSafeMinerals = With.units.neutral.filter(_.unitClass.isMinerals).filter(safe)
      if (allSafeMinerals.nonEmpty) {
        safeMinerals ++= allSafeMinerals
          .toVector
          .sortBy(mineral => With.paths.groundPixels(mineral.tileIncludingCenter, With.geography.home))
          .take(9)
      }
    }
  }
  
  private def removeUnavailableWorkers() = {
    resourceByWorker.keySet.diff(allWorkers).foreach(unassignWorker)
  }
  
  private def removeUnavailableResources() = {
    workersByResource.keySet.diff(safeResources).foreach(unassignResource)
  }
  
  private def decideIdealWorkerDistribution() {
    haveEnoughGas       = With.self.gas >= Math.max(400, With.self.minerals)
    workersForGas       = Vector(safeGas.size * 3, allWorkers.size/3, if(haveEnoughGas) 0 else 200).min
    workersForMinerals  = allWorkers.size - workersForGas
    workersPerGas       = if (safeGas.size == 0) 0 else workersForGas.toDouble / safeGas.size
    workersOnGas        = safeGas.toVector.map(gas => workersByResource.get(gas).map(_.size).getOrElse(0)).sum
    
    needPerMineral = new mutable.HashMap[UnitInfo, Double] ++
      safeMinerals
        .map(mineral => (mineral,
          mineralSaturationRatio - workersByResource.get(mineral)
            .map(_.size)
            .getOrElse(0)))
        .toMap
  
    needPerGas = new mutable.HashMap[UnitInfo, Double] ++
      safeGas.map(gas => (gas,
        workersPerGas - workersByResource.get(gas)
          .map(_.size)
          .getOrElse(0))).toMap
  
    unassignSupersaturatingWorkersLimiter.act()
    
    unassignedWorkers       = allWorkers.diff(resourceByWorker.keySet)
    numberToAddToGas        = Math.max(0, workersForGas - workersOnGas)
    workersToAddToGas       = unassignedWorkers.take(numberToAddToGas)
    workersToAddToMinerals  = unassignedWorkers.drop(numberToAddToGas)
  }
  
  val unassignSupersaturatingWorkersLimiter = new Limiter(5, () => unassignSupersaturatingWorkers)
  def unassignSupersaturatingWorkers() {
    val supersaturatedMinerals = safeMinerals.filter(needPerMineral(_) < 0)
    val supersaturationWorkerGroups = supersaturatedMinerals.flatten(workersByResource.get)
    supersaturationWorkerGroups.foreach(_.drop(2).foreach(unassignWorker))
  }
  
  private def distributeUnassignedWorkers() {
    workersToAddToMinerals.foreach(addWorkerToMinerals)
    workersToAddToGas.foreach(addWorkerToGas)
  }
  
  private def addWorkerToMinerals(worker:FriendlyUnitInfo) {
    if (safeMinerals.isEmpty) return
    val mineral = safeMinerals
      .toVector
      .sortBy(_.pixelDistanceSquared(worker.pixelCenter))
      .maxBy(needPerMineral)
    assignWorker(worker, mineral)
    needPerMineral(mineral) -= 1
  }

  private def addWorkerToGas(worker:FriendlyUnitInfo) {
    if (safeGas.isEmpty) return
    val gas = safeGas
      .toVector
      .sortBy(_.pixelDistanceSquared(worker.pixelCenter))
      .maxBy(needPerGas)
    assignWorker(worker, gas)
    needPerGas(gas) -= 1
  }
  
  private def orderAllWorkers() {
    allWorkers.foreach(order)
  }
  
  private def assignWorker(worker:FriendlyUnitInfo, resource:UnitInfo) {
    unassignWorker(worker)
    if ( ! workersByResource.contains(resource)) {
      workersByResource.put(resource, new mutable.HashSet)
    }
    workersByResource(resource).add(worker)
    resourceByWorker.put(worker, resource)
  }
  
  private def unassignWorker(worker:FriendlyUnitInfo) {
    resourceByWorker.get(worker).foreach(resource => workersByResource.get(resource).foreach(_.remove(worker)))
    resourceByWorker.remove(worker)
  }
  
  private def unassignResource(resource:UnitInfo) {
    if (workersByResource.contains(resource)) {
      workersByResource(resource).foreach(unassignWorker)
      workersByResource.remove(resource)
    }
  }
  
  private def safe(resource:UnitInfo):Boolean = {
    With.battles.byZone
      .get(With.geography.zoneByTile(resource.tileIncludingCenter))
      .forall(zoneBattle => zoneBattle.estimation.netCost >= 0)
  }
  
  private def order(worker:FriendlyUnitInfo) {
    //If there's no resource for them to gather, that's fine; they'll follow default behavior and be generally useful
    With.executor.intend(new Intention(this, worker) {
      toGather    = resourceByWorker.get(worker)
      destination = resourceByWorker.get(worker).map(_.pixelCenter)
      origin      = resourceByWorker.get(worker).map(_.pixelCenter).getOrElse(this.origin)
    })
  }
}
