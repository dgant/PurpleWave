package Macro

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Performance.Tasks.TimedTask
import ProxyBwapi.UnitInfo.{ForeignUnitInfo, FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, Forever}

import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer

class Gathering4 extends TimedTask {
  private def isValidBase     (base: Base): Boolean = base.townHall.exists(_.remainingCompletionFrames < 24 * 10)
  private def isValidResource (unit: UnitInfo): Boolean = isValidMineral(unit) || isValidGas(unit)
  private def isValidMineral  (unit: UnitInfo): Boolean = unit.alive && unit.mineralsLeft > 0 && (longDistance || unit.base.exists(isValidBase))
  private def isValidGas      (unit: UnitInfo): Boolean = unit.alive && unit.isOurs && unit.unitClass.isGas && unit.remainingCompletionFrames < 24 * 6 && (longDistance || unit.base.exists(isValidBase))
  private class Slot(val resource: ForeignUnitInfo, val order: Int) {
    var worker: Option[FriendlyUnitInfo] = None
    var lastUpdate: Int = - Forever()
    def free: Boolean = worker.isEmpty && isValidResource(resource)
  }
  private lazy val mineralSlots: Map[Base, Vector[Slot]] = With.units.neutral.filter(isValidMineral)
    .groupBy(m => m.base.getOrElse(With.geography.bases.minBy(_.heart.groundPixels(m.pixel))))
    .map(group => {
      val base = group._1
      val sorted = group._2.toVector
        .sortBy(m => PurpleMath.broodWarDistanceBox(m.topLeft, m.bottomRight, base.townHallArea.startPixel.add(47, 80), base.townHallArea.startPixel.add(70, 103))) // Distance to 3rd starting worker
        .sortBy(m => PurpleMath.broodWarDistanceBox(m.topLeft, m.bottomRight, base.townHallArea.startPixel, base.townHallArea.endPixel))
      (base, (sorted.map(new Slot(_, 0)) ++ sorted.reverse.map(new Slot(_, 1)) ++ sorted.reverse.map(new Slot(_, 2)))) })
  private lazy val gasSlots: Map[Base, Vector[Slot]] = With.units.neutral.filter(_.gasLeft > 0)
    .groupBy(m => m.base.getOrElse(With.geography.bases.minBy(_.heart.groundPixels(m.pixel))))
    .map(group => {
      val base = group._1
      val sorted = group._2.toVector
        .sortBy(_.isGasBelowHall)
        .sortBy(g => PurpleMath.broodWarDistanceBox(g.topLeft, g.bottomRight, base.townHallArea.startPixel, base.townHallArea.endPixel))
      (base, sorted.flatMap(g => if (g.isGasBelowHall) Vector(new Slot(g, 0), new Slot(g, 1), new Slot(g, 2), new Slot(g, 3)) else Vector(new Slot(g, 0), new Slot(g, 1), new Slot(g, 2)))) })
  private lazy val baseCosts: Map[(Base, Base), Double] = With.geography.bases
    .flatMap(baseA => With.geography.bases.map(baseB => (baseA, baseB)))
    .map(basePair => (basePair,
      if (basePair._1 == basePair._2) 0.0
      else if (basePair._1.hashCode() > basePair._2.hashCode()) baseCosts((basePair._2, basePair._1))
      else basePair._1.heart.groundPixels(basePair._2.heart))).toMap
  private lazy val naturalCost = With.geography.startBases.view.map(b => baseCosts(b, b.natural.getOrElse(b))).max

  private val assignments = new mutable.HashMap[FriendlyUnitInfo, Slot]()
  private var nextWorkers: Set[FriendlyUnitInfo] = Set.empty
  private var bases: Vector[Base] = Vector.empty
  private var minerals: Vector[UnitInfo] = Vector.empty
  private var gasPumps: Vector[FriendlyUnitInfo] = Vector.empty
  private val idlers: mutable.ArrayBuffer[FriendlyUnitInfo] = ArrayBuffer.empty
  private var longDistance: Boolean = false
  private var gasWorkersToAdd: Int = 0

  def setWorkers(newWorkers: Set[FriendlyUnitInfo]): Unit = { nextWorkers = newWorkers }

  override protected def onRun(budgetMs: Long): Unit = {
    // Respect gas limitations
    // - If our gas is below our floor,       use maximum gas worker count
    // - If our gas is at/above our ceiling,  use minimum gas worker count
    idlers.clear()
    bases         = With.geography.ourBases.filter(_.townHall.exists(t => t.hasEverBeenCompleteHatch || t.remainingCompletionFrames < 240))
    minerals      = bases.view.flatMap(_.minerals).filter(isValidMineral).toVector // TODO: Do we need this?
    gasPumps      = With.units.ours.filter(isValidGas).toVector
    longDistance  = minerals.length < nextWorkers.size /  4
    val gasWorkersHardMinimum = Math.max(0,                 Math.min(With.blackboard.gasWorkerFloor(), With.blackboard.gasWorkerCeiling()))
    val gasWorkersHardMaximum = Math.min(4 * gasPumps.size, Math.max(With.blackboard.gasWorkerFloor(), With.blackboard.gasWorkerCeiling()))
    val gasGoalMinimum        = Math.min(With.blackboard.gasLimitFloor(), With.blackboard.gasLimitCeiling())
    val gasGoalMaximum        = Math.max(With.blackboard.gasLimitFloor(), With.blackboard.gasLimitCeiling())
    val gasNow                = With.self.gas
    val gasWorkersTripMinimum = (7 + gasGoalMinimum - With.self.gas) / 8
    val gasWorkersTripMaximum = (7 + gasGoalMaximum - With.self.gas) / 8
    val gasWorkersRatioTarget = Math.round(With.blackboard.gasWorkerRatio() * nextWorkers.size).toInt
    val gasWorkersBaseTarget  = PurpleMath.clamp(gasWorkersRatioTarget, gasWorkersTripMinimum, gasWorkersTripMaximum)
    val gasWorkerTarget       = PurpleMath.clamp(gasWorkersBaseTarget, gasWorkersHardMinimum, gasWorkersHardMaximum)
    var gasWorkersNow         = assignments.count(_._2.resource.unitClass.isGas)
    gasWorkersToAdd           = gasWorkerTarget - gasWorkersNow

    // Assign workers
    assignments.view.filterNot(a => isValidResource(a._2.resource)).map(_._1).foreach(unassignWorker)
    assignments.keys.view.filterNot(nextWorkers.contains).foreach(unassignWorker)
    nextWorkers.view.filterNot(assignments.contains).foreach(findAssignment)

    // Reassign workers
    // TODO: Reassign workers when gasDelta != 0
    // TODO: Reassign workers when a base becomes available/unavailable

    // Command workers
    idlers.foreach(i => i.agent.intend(this, new Intention { toTravel = Some(PurpleMath.sampleSet(nearestBase(i).tiles).pixelCenter) }))
    assignments.foreach(a => a._1.agent.intend(this, new Intention { toGather = Some(a._2.resource) }))
  }

  private def findAssignment(worker: FriendlyUnitInfo): Unit = {
    (if (gasWorkersToAdd > 0) tryGas(worker) || tryMinerals(worker) || tryLongDistance(worker) else tryMinerals(worker) || tryLongDistance(worker) || tryGas(worker)) || idle(worker)
  }

  private def assignWorker(worker: FriendlyUnitInfo, slot: Slot): Unit = {
    unassignWorker(worker)
    slot.worker.foreach(unassignWorker)
    slot.worker = Some(worker)
    slot.lastUpdate = With.frame
    assignments(worker) = slot
    if (slot.resource.unitClass.isGas) gasWorkersToAdd -= 1
  }

  private def unassignWorker(worker: FriendlyUnitInfo): Unit = {
    assignments.get(worker).foreach(s => { s.lastUpdate = With.frame; s.worker = None; if (s.resource.unitClass.isGas) gasWorkersToAdd += 1})
    assignments.remove(worker)
  }

  private def reassignWorker(worker: FriendlyUnitInfo): Unit = {
    unassignWorker(worker)
    findAssignment(worker)
  }

  private def nearestBase(worker: FriendlyUnitInfo): Base =
    worker.base.orElse(
      ByOption.minBy(bases)(b => worker.pixelDistanceTravelling(b.heart))).getOrElse(
        With.geography.bases.minBy(b => worker.pixelDistanceTravelling(b.heart)))

  private def tryMinerals(worker: FriendlyUnitInfo): Boolean = {
    val base = nearestBase(worker)
    def basesNear(worker: FriendlyUnitInfo): Seq[Base] = bases.view.filter(baseCosts(base, _) <= naturalCost)
    def basesFar(worker: FriendlyUnitInfo): Seq[Base] = bases.view.filter(baseCosts(base, _) > naturalCost)
    val slot = mineralSlots(base).find(s => s.free && s.order == 0).orElse(
      basesNear(worker).sortBy(baseCosts(base, _)).flatMap(mineralSlots(_)).find(s => s.free && s.order == 0)).orElse(
        mineralSlots(base).find(s => s.free && s.order == 1)).orElse(
          basesNear(worker).sortBy(baseCosts(base, _)).flatMap(mineralSlots(_)).find(s => s.free && s.order == 1)).orElse(
            basesFar(worker).sortBy(baseCosts(base, _)).flatMap(mineralSlots(_)).find(s => s.free && s.order < 2))
    slot.foreach(assignWorker(worker, _))
    slot.isDefined
  }

  private def tryGas(worker: FriendlyUnitInfo): Boolean = {
    val base = nearestBase(worker)
    def basesNear(worker: FriendlyUnitInfo): Seq[Base] = bases.view.filter(baseCosts(base, _) <= naturalCost)
    def basesFar(worker: FriendlyUnitInfo): Seq[Base] = bases.view.filter(baseCosts(base, _) > naturalCost)
    val slot = gasSlots(base).find(s => s.free && s.order < 4).orElse(
      basesNear(worker).sortBy(baseCosts(base, _)).flatMap(gasSlots(_)).find(s => s.free && s.order < 4)).orElse(
        basesFar(worker).sortBy(baseCosts(base, _)).flatMap(gasSlots(_)).find(s => s.free && s.order < 4)).orElse(
          gasSlots(base).find(_.free)).orElse(
            basesNear(worker).sortBy(baseCosts(base, _)).flatMap(gasSlots(_)).find(_.free)).orElse(
                basesFar(worker).sortBy(baseCosts(base, _)).flatMap(gasSlots(_)).find(_.free))
    slot.foreach(assignWorker(worker, _))
    slot.isDefined
  }

  private def tryLongDistance(worker: FriendlyUnitInfo): Boolean = {
    if ( ! longDistance) return false
    false // TODO
  }

  private def idle(worker: FriendlyUnitInfo): Boolean = {
    idlers.append(worker)
    true
  }
}
