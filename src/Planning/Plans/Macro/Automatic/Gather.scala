package Planning.Plans.Macro.Automatic

import Information.Geography.Types.{Base, Zone}
import Information.Intelligenze.Fingerprinting.Generic.GameTime
import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.UnitCountEverything
import Planning.Plan
import Planning.ResourceLocks.LockUnits
import Planning.UnitMatchers.UnitMatchWorkers
import ProxyBwapi.Races.Zerg
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

import scala.collection.mutable

class Gather extends Plan {
  
  val workerLock: LockUnits = new LockUnits {
    unitMatcher.set(UnitMatchWorkers)
    unitCounter.set(UnitCountEverything)
  }
  
  var minerals  : Vector[UnitInfo] = Vector.empty
  var gasses    : Vector[FriendlyUnitInfo] = Vector.empty
  var resources : Vector[UnitInfo] = Vector.empty
  var workers   : collection.Set[FriendlyUnitInfo] = Set.empty
  var gasWorkersNow = 0
  var gasWorkersMax = 0
  var workersByResource = new mutable.HashMap[UnitInfo, mutable.ArrayBuffer[FriendlyUnitInfo]]
  var resourceByWorker = new mutable.HashMap[FriendlyUnitInfo, UnitInfo]
  
  private val transfersLegal = new mutable.HashSet[(Zone, Zone)]
  
  override def onUpdate() {
    workerLock.acquire(this)
    workers = workerLock.units
    if (workers.isEmpty) {
      return
    }
    setGoals()
    countUnits()
    if (resources.nonEmpty) {
      updateWorker(workers.minBy(_.lastGatheringUpdate), true)
      workers.foreach(updateWorker(_, false))
    }
  }

  def isLegalResource(resource: UnitInfo): Boolean = {
    resource.alive && (resource.unitClass.isMinerals || (resource.unitClass.isGas && resource.isOurs))
  }
  
  private def setGoals() {
    val belowFloor    = With.self.gas < Math.min(With.blackboard.gasLimitFloor(), With.blackboard.gasLimitCeiling())
    val aboveCeiling  = With.self.gas > Math.max(With.blackboard.gasLimitFloor(), With.blackboard.gasLimitCeiling())

    gasWorkersMax =
      Vector(
        workers.size - 3,
        With.blackboard.gasWorkerCeiling(),
        if (belowFloor)
          workers.size
        else if (aboveCeiling)
          0
        else
          (workers.size * With.blackboard.gasTargetRatio()).toInt)
        .min
  }
  
  private def countUnits() {
    val activeBases = With.geography.ourBases.filter(_.townHall.exists(hall =>
      hall.is(Zerg.LairOrHive)
      || hall.remainingCompletionFrames < GameTime(0, 10)()
      || hall.hasEverBeenCompleteHatch))
    calculateTransferPaths(activeBases)
    minerals  = activeBases.flatMap(_.minerals)
    gasses    = activeBases.flatMap(_.gas.view.filter(_.complete).flatMap(_.friendly))
    if (minerals.isEmpty) {
      minerals = With.geography.bases.flatMap(_.minerals)
    }
    if (gasses.isEmpty) {
      gasses = With.units.ours.view.filter(_.unitClass.isGas).toVector
    }
    resources = gasses ++ minerals
    workersByResource.clear()
    resourceByWorker.clear()
    resources.foreach(resource => workersByResource(resource) = new mutable.ArrayBuffer[FriendlyUnitInfo])
    workers.foreach(w =>
      w.agent.lastIntent.toGather
        .foreach(resource => if (workersByResource.contains(resource)) assign(w, resource)))
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

  private def assign(worker: FriendlyUnitInfo, resource: UnitInfo) {
    unassign(worker)
    if (resource.unitClass.isGas) {
      gasWorkersNow += 1
    }
    resourceByWorker(worker) = resource
    workersByResource(resource) += worker
  }
  
  private def unassign(worker: FriendlyUnitInfo) {
    val resource = resourceByWorker.get(worker)
    gasWorkersNow -= resource.count(_.unitClass.isGas)
    resource.foreach(workersByResource(_) -= worker)
    resourceByWorker.remove(worker)
  }
  
  private def updateWorker(worker: FriendlyUnitInfo, forceUpdate: Boolean) {
    if (forceUpdate
      || With.framesSince(worker.lastGatheringUpdate) > GameTime(0, 1)()
      || ! resourceByWorker.get(worker).exists(_.aliveAndComplete)
      ) {
      worker.lastGatheringUpdate = With.frame
      val bestResource = resources.maxBy(utility(worker, _))
      assign(worker, bestResource)
    }
    worker.agent.intend(this, new Intention {
      toGather = resourceByWorker.get(worker)
    })
  }
  
  private def utility(worker: FriendlyUnitInfo, resource: UnitInfo): Double = {
    val depleted = resource.gasLeft + resource.mineralsLeft == 0
    val need =
      (if (depleted) 0.2 else 1.0) * (
      if (gasWorkersNow == gasWorkersMax)
        1.0
      else if (resource.unitClass.isGas == (gasWorkersNow < gasWorkersMax) && ! depleted)
        100.0
      else
        1.0
      )
    
    val safety      = if ( ! worker.zone.bases.exists(_.owner.isUs) || transfersLegal.contains((worker.zone, resource.zone))) 100.0 else 1.0
    val continuity  = if (resourceByWorker.get(worker).contains(resource)) 2.0 else 1.0
    val distance    = Math.max(resource.remainingCompletionFrames * worker.topSpeed, worker.pixelDistanceCenter(resource))
    val distanceFactor = 32 * 30 + distance

    val currentWorkerCount = workersByResource(resource).count(_ != worker)
    val saturation  =
      if (resource.unitClass.isMinerals) {
        currentWorkerCount match {
          case 0 => 1.0
          case 1 => 0.8
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