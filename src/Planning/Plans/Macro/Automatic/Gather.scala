package Planning.Plans.Macro.Automatic

import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Intent.Intention
import Performance.Caching.Limiter
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCountEverything
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Plan
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import bwapi.Race

import scala.collection.mutable

class Gather extends Plan {

  val workers = new LockUnits {
    unitMatcher.set(UnitMatchWorkers)
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
  private var safeBases               : Set[Base]                         = Set.empty
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
    ourActiveBases  = With.geography.ourBases.filter(_.townHall.exists(th => th.aliveAndComplete && (th.complete || th.morphing)))
    allMinerals     = ourActiveBases.flatten(base => base.minerals).filter(_.alive).toSet
    allGas          = ourActiveBases.flatten(base => base.gas).filter(gas => gas.isOurs && gas.aliveAndComplete).toSet
    safeBases       = getSafeBases
    safeMinerals    = allMinerals.filter(safe)
    safeGas         = allGas.filter(safe)
    if (safeMinerals.isEmpty) safeMinerals  = allMinerals
    if (safeGas.isEmpty)      safeGas       = allGas
    safeResources = safeMinerals ++ safeGas
  }
  
  private def getSafeBases: Set[Base] = {
    With.geography.bases.filter(base => {
      val battle = With.battles.byZone(base.zone)
      val distanceToHeart = battle.enemy.centroid.pixelDistanceFast(base.heart.pixelCenter)
      ( ! With.configuration.evacuateDangerousBases
        || With.geography.ourBases.size < 2
        || battle.estimationAbstract.weSurvive
        || base.zone.exit.exists(_.centerPixel.pixelDistanceFast(battle.enemy.centroid) < distanceToHeart))
    }).toSet
  }
  
  private def decideLongDistanceMining() {
    if (safeMinerals.size < 9) {
      val allSafeMinerals = With.units.neutral.filter(_.unitClass.isMinerals).filter(safe)
      if (allSafeMinerals.nonEmpty) {
        safeMinerals ++= allSafeMinerals
          .toVector
          .sortBy(_.pixelCenter.zone.distancePixels(With.geography.home.zone))
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
  
  private def gasWorkers: Int = {
    if (With.self.race == Race.Zerg) {
      Vector(safeGas.size * 3, Math.ceil(allWorkers.size*3.0/8.0).toInt, if(haveEnoughGas) 0 else 200).min
    }
    else {
      Vector(safeGas.size * 3, allWorkers.size/3, if(haveEnoughGas) 0 else 200).min
    }
  }
  
  private def decideIdealWorkerDistribution() {
    haveEnoughGas       = With.self.gas >= Math.max(200, With.self.minerals)
    workersForGas       = Vector(safeGas.size * 3, allWorkers.size/3, if(haveEnoughGas) 0 else 200).min
    workersForMinerals  = allWorkers.size - workersForGas
    workersPerGas       = if (safeGas.isEmpty) 0 else workersForGas.toDouble / safeGas.size
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
  
  val unassignSupersaturatingWorkersLimiter = new Limiter(5, () => unassignSupersaturatingWorkers())
  def unassignSupersaturatingWorkers() {
    val supersaturatedMinerals = safeMinerals.filter(needPerMineral(_) < 0)
    val supersaturationWorkerGroups = supersaturatedMinerals.flatten(workersByResource.get)
    supersaturationWorkerGroups.foreach(_.drop(2).foreach(unassignWorker))
  }
  
  private def distributeUnassignedWorkers() {
    workersToAddToMinerals.foreach(addWorkerToMinerals)
    workersToAddToGas.foreach(addWorkerToGas)
  }
  
  private def addWorkerToMinerals(worker: FriendlyUnitInfo) {
    if (safeMinerals.isEmpty) return
    val mineral = safeMinerals
      .toVector
      .sortBy(_.pixelDistanceSquared(worker.pixelCenter))
      .maxBy(needPerMineral)
    assignWorker(worker, mineral)
    needPerMineral(mineral) -= 1
  }

  private def addWorkerToGas(worker: FriendlyUnitInfo) {
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
  
  private def safe(resource: UnitInfo): Boolean = {
    if (resource.unitClass.isMinerals)
      getSafeBases.exists(_.minerals.contains(resource))
    else
      getSafeBases.exists(_.gas.contains(resource))
  }
  
  private def order(worker: FriendlyUnitInfo) {
    worker.intend(new Intention(this) {
      toGather = resourceByWorker.get(worker)
      canCower = true
    })
  }
}
