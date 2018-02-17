package Planning.Plans.Macro.Automatic

import Information.Geography.Types.{Base, Zone}
import Information.Intelligenze.Fingerprinting.Generic.GameTime
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
  
  private val transfersLegal = new mutable.HashSet[(Zone, Zone)]
  
  override def onUpdate() {
    setGoals()
    countUnits()
    workers.foreach(updateWorker)
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
  
  private def countUnits() {
    val activeBases = With.geography.ourBases.filter(_.townHall.exists(hall => hall.morphing || hall.remainingBuildFrames < GameTime(0, 10)()))
    calculateTransferPaths(activeBases)
    minerals  = activeBases.flatMap(_.minerals).toSet
    gasses    = activeBases.flatMap(_.gas.flatMap(_.friendly).filter(u => u.gasLeft > 0 && u.complete)).toSet
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
    
    workersByResource.clear()
    resourceByWorker.clear()
    resources.foreach(resource => workersByResource(resource) = new mutable.HashSet[FriendlyUnitInfo])
    workers.foreach(unit => unit.agent.lastIntent.toGather.foreach(resource => assign(unit, resource)))
    gasWorkersNow = resourceByWorker.count(_._2.unitClass.isGas)
  }
  
  private def calculateTransferPaths(bases: Iterable[Base]) {
    transfersLegal.clear()
    val baseZones = bases.map(_.zone).toSet
    baseZones.foreach(zone1 =>
      baseZones.foreach(zone2 => {
        val path = With.paths.zonePath(zone1, zone2)
        val pathZones: Iterable[Zone] = path.map(_.steps.map(_.to).filter(zone => zone != zone1 && zone != zone2)).getOrElse(Iterable.empty)
        val pathZonesDanger = pathZones.filter(zone =>
                zone.units.exists(e => e.isEnemy  && e.unitClass.attacksGround)
          && !  zone.units.exists(a => a.isOurs   && a.unitClass.attacksGround))
        if (pathZonesDanger.isEmpty) {
          transfersLegal += ((zone1, zone2))
          transfersLegal += ((zone2, zone1))
        }
      }))
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
    
    val safety      = if ( ! worker.zone.bases.exists(_.owner.isUs) || transfersLegal.contains((worker.zone, resource.zone))) 100.0 else 1.0
    val continuity  = if (resourceByWorker.get(worker).contains(resource)) 10.0 else 1.0
    val proximity   = resource.base.flatMap(_.townHall).map(_.pixelDistanceEdge(resource)).getOrElse(32 * 12)
    val distance    = worker.pixelDistanceEdge(resource) + resource.remainingBuildFrames * worker.topSpeed
    val currentWorkerCount = workersByResource(resource).count(_ != worker)
    val saturation  =
      if (resource.unitClass.isMinerals) {
        currentWorkerCount match {
          case 0 => 1.0
          case 1 => 0.6
          case 2 => 0.1
          case _ => 0.0
        }
      }
      else {
        currentWorkerCount match {
          case 0 => 1.0
          case 1 => 1.0
          case 2 => 1.0
          case 3 => 0.0001
          case _ => 0.0
        }
      }
    val output = need * continuity * saturation / (128.0 + distance)
    output
  }
}