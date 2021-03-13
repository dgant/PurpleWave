package Macro

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.PurpleMath
import Micro.Agency.Intention
import Performance.Tasks.TimedTask
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.{ByOption, Forever}

import scala.collection.mutable

class Gathering extends TimedTask with AccelerantMinerals with Zippers {
  private def isValidBase     (base: Base): Boolean = base.townHall.filter(_.isOurs).exists(_.remainingCompletionFrames < 24 * 10)
  private def isValidResource (unit: UnitInfo): Boolean = isValidMineral(unit) || isValidGas(unit)
  private def isValidMineral  (unit: UnitInfo): Boolean = unit.alive && (longDistance || unit.base.exists(isValidBase) && unit.mineralsLeft > 0)
  private def isValidGas      (unit: UnitInfo): Boolean = unit.alive && (longDistance || unit.base.exists(isValidBase) && unit.isOurs && unit.unitClass.isGas && unit.remainingCompletionFrames < 24 * 6)
  private class Slot(var resource: UnitInfo, val order: Int) {
    val base: Base = resource.base.getOrElse(With.geography.bases.minBy(_.heart.groundPixels(resource.tileTopLeft)))
    var worker: Option[FriendlyUnitInfo] = None
    var lastUpdate: Int = - Forever()
    def free: Boolean = worker.isEmpty && isValidResource(resource)
    val distance: Double = PurpleMath.broodWarDistanceBox(resource.topLeft, resource.bottomRight, base.townHallArea.startPixel, base.townHallArea.endPixel)
  }
  private lazy val mineralSlots: Map[Base, Vector[Slot]] = With.units.neutral.filter(isValidMineral)
    .groupBy(m => m.base.getOrElse(With.geography.bases.minBy(_.heart.groundPixels(m.pixel))))
    .map(group => {
      val base = group._1
      val sorted = group._2.toVector
        .sortBy(m =>
          // Give slight edge to patches near the 3rd starting worker and where future workers will spawn if Terran/Protoss
          PurpleMath.broodWarDistanceBox(m.topLeft, m.bottomRight, base.townHallArea.startPixel.add(47, 80), base.townHallArea.startPixel.add(70, 103)) * 0.05 +
          PurpleMath.broodWarDistanceBox(m.topLeft, m.bottomRight, base.townHallArea.startPixel, base.townHallArea.endPixel))
      (base, (sorted.map(new Slot(_, 0)) ++ sorted.reverse.map(new Slot(_, 1)) ++ sorted.reverse.map(new Slot(_, 2)))) })
  private lazy val gasSlots: Map[Base, Vector[Slot]] = With.units.neutral.filter(_.gasLeft > 0)
    .groupBy(m => m.base.getOrElse(With.geography.bases.minBy(_.heart.groundPixels(m.pixel))))
    .map(group => {
      val base = group._1
      val sorted = group._2.toVector
        .sortBy(_.gasMinersRequired)
        .sortBy(g => PurpleMath.broodWarDistanceBox(g.topLeft, g.bottomRight, base.townHallArea.startPixel, base.townHallArea.endPixel))
      (base, sorted.flatMap(g => (0 until g.gasMinersRequired).map(new Slot(g, _)).toVector)) })
  private lazy val slotsByResource: Map[UnitInfo, Seq[Slot]] = (mineralSlots.values.view.flatten ++ gasSlots.values.view.flatten).groupBy(_.resource).map(p => (p._1, p._2.toSeq))
  private lazy val baseCosts: Map[(Base, Base), Double] = With.geography.bases
    .flatMap(baseA => With.geography.bases.map(baseB => (baseA, baseB)))
    .map(basePair => (basePair, basePair._1.heart.groundPixels(basePair._2.heart))).toMap
  private lazy val naturalCost = With.geography.startBases.view.map(b => baseCosts(b, b.natural.getOrElse(b))).max

  private val assignments = new mutable.HashMap[FriendlyUnitInfo, Slot]()
  private var workers: collection.Set[FriendlyUnitInfo] = mutable.Set.empty
  private var bases: Vector[Base] = Vector.empty
  private var minerals: Vector[UnitInfo] = Vector.empty
  private var gasPumps: Vector[FriendlyUnitInfo] = Vector.empty
  private var longDistance: Boolean = false
  private var gasWorkersToAdd: Int = 0

  def setWorkers(newWorkers: collection.Set[FriendlyUnitInfo]): Unit = { workers = newWorkers }

  def getWorkersByResource(resource: UnitInfo): Iterable[UnitInfo] = slotsByResource.get(resource).view.flatMap(_.flatMap(_.worker))

  override protected def onRun(budgetMs: Long): Unit = {

    // Replace gas units
    gasSlots.values.view.flatten.filter( ! _.resource.alive).foreach(slot => slot.resource = With.units.all.find(u => u.unitClass.isGas && u.tileTopLeft == slot.resource.tileTopLeft).getOrElse(slot.resource))

    // Respect gas limitations
    // - If our gas is below our floor,       use maximum gas worker count
    // - If our gas is at/above our ceiling,  use minimum gas worker count
    val basesBefore = bases
    bases         = With.geography.ourBases.filter(_.townHall.exists(t => t.hasEverBeenCompleteHatch || t.remainingCompletionFrames < 240))
    minerals      = bases.view.flatMap(_.minerals).filter(isValidMineral).toVector // TODO: Do we need this?
    gasPumps      = With.units.ours.filter(isValidGas).toVector
    longDistance  = minerals.length < workers.size /  4
    val gasWorkersHardMinimum = Math.max(0,                                               Math.min(With.blackboard.gasWorkerFloor(), With.blackboard.gasWorkerCeiling()))
    val gasWorkersHardMaximum = Math.min(gasPumps.view.map(slotsByResource(_).size).sum,  Math.max(With.blackboard.gasWorkerFloor(), With.blackboard.gasWorkerCeiling()))
    val gasGoalMinimum        = Math.min(With.blackboard.gasLimitFloor(), With.blackboard.gasLimitCeiling())
    val gasGoalMaximum        = Math.max(With.blackboard.gasLimitFloor(), With.blackboard.gasLimitCeiling())
    val gasNow                = With.self.gas
    val gasWorkersTripMinimum = (7 + gasGoalMinimum - With.self.gas) / 8
    val gasWorkersTripMaximum = (7 + gasGoalMaximum - With.self.gas) / 8
    val gasWorkersRatioTarget = Math.round(With.blackboard.gasWorkerRatio() * workers.size).toInt
    val gasWorkersBaseTarget  = PurpleMath.clamp(gasWorkersRatioTarget, gasWorkersTripMinimum, gasWorkersTripMaximum)
    val gasWorkerTarget       = PurpleMath.clamp(gasWorkersBaseTarget, gasWorkersHardMinimum, gasWorkersHardMaximum)
    var gasWorkersNow         = assignments.count(_._2.resource.unitClass.isGas)
    gasWorkersToAdd           = gasWorkerTarget - gasWorkersNow

    if (doInitialSplit()) return

    // Unassign workers doing something invalid
    assignments.view.filterNot(a => isValidResource(a._2.resource)).map(_._1).foreach(unassignWorker)
    assignments.keys.view.filterNot(workers.contains).foreach(unassignWorker)

    // Reassign workers who could be doing something better
    //
    // Find workers to populate new base
    val newBases = bases.filterNot(basesBefore.contains)
    if (newBases.nonEmpty) {
      val missingCandidates = newBases.view.map(_.minerals.size).sum - unassigned.size
      bases.find( ! basesBefore.contains(_)).foreach(b => getWorkersClosestToBase(b, 1, b.minerals.size).foreach(unassignWorker))
    }
    // Find some workers to reassign to gas
   else if (gasWorkersToAdd > unassigned.size) {
      val reassignableMineralSlots = bases.view
        .flatMap(gasSlots(_).view.filter(_.free)) // For each free gas slot,
        .sortBy(slot => mineralSaturation(slot.base)) // ordered by same-base mineral saturation,
        .flatMap(slot =>
        bases
          .sortBy(b => baseCosts((b, slot.base))) // look at bases in ascending distance to the gas
          .view.flatMap(mineralSlots(_).view.reverse.filterNot(_.free)) // and their slots, starting with the weakest first
          .take(Math.max(0, slot.resource.gasMinersRequired - slotsByResource(slot.resource).count(!_.free)))) // taking the best workers for these slots
        .distinct
        .take(gasWorkersToAdd) // and take only as many mineral slots as we need (which may be an underestimate due to duplication above; this will resolve itself on successive runs)
      reassignableMineralSlots.view.flatMap(_.worker).foreach(reassignWorker) // Find a new job, hopefully in the gas industry!
    }
    // Find some workers to reassign to minerals
    else if (gasWorkersToAdd < 0) {
      val reassignableGasSlots = bases.view
        .flatMap(gasSlots(_).view.filterNot(_.free)) // Find gas workers to unassign
        .sortBy(slot => mineralSaturation(slot.base))
        .take(-gasWorkersToAdd)
        .flatMap(_.worker)
      reassignableGasSlots.foreach(reassignWorker)
    }
    // If nothing else is going on,
    // reassign the longest-tenured mineral worker that's near a better slot
    // The magical drop(5) is because the first five slots are usually all roughly-equally good
    else if (unassigned.isEmpty) {
      val underemployed = bases.view
        .flatMap(mineralSlots(_).view.drop(5).filter(s => With.framesSince(s.lastUpdate) > 480)) // Take tenured workers from mediocre slots
        .filter(s1 => bases.view
        .filter(baseCosts(_, s1.base) <= naturalCost) // Look at bases which are close enough to merit a low-priority transfer
        .exists(base => mineralSlots(base).exists(s2 =>
        s2.order > s1.order // Allow transfers even base-to-base to even slot saturation
          || (s2.distance > s1.distance + 16 && s2.base == s1.base)))) // Only allow same-base transfers for using faster minerals
      ByOption.minBy(underemployed)(_.lastUpdate).foreach(_.worker.foreach(reassignWorker))
    }

    // Assign workers
    // Assign older workers first because they're most likely to keep the same or a similar assignment
    unassigned.toVector.sortBy(_.frameDiscovered).foreach(findAssignment)

    // Command workers
    unassigned.foreach(i => i.agent.intend(this, new Intention { toTravel = Some(PurpleMath.sampleSet(nearestBase(i).tiles).pixelCenter) }))
    assignments.foreach(a => a._1.agent.intend(this, new Intention { toGather = Some(a._2.resource) }))
  }

  private def doInitialSplit(): Boolean = {
    if (With.frame > 0 || workers.size != 4) return false
    val minerals = bases.flatMgap(mineralSlots(_))
    if (minerals.size < workers.size) return false
    val splittees = workers.toVector.sortBy(worker => minerals.map(_.resource.pixelDistanceEdge(worker)).min)
    // Most builds are bottlenecked on when we can produce the 6th worker, whose cost you can afford after 56 / 8 = 7 mineral trips
    // Thus, you want to optimize for the return of the 7th mineral trip, which will ordinarily be done by the third-furthest initial worker
    // This is even true for the 4-pool, which needs 150 minerals = 19 trips = 4 complete rounds + 3rd return from the fifth round
    val splitteeOrder = Seq(3, 2, 1, 4)
    val splitteesPrioritized = splitteeOrder.map(splittees(_)) ++ splittees.drop(splitteeOrder.size)
    splitteesPrioritized.foreach(findAssignment)
    true
  }

  private def unassigned: Iterable[FriendlyUnitInfo] = workers.view.filterNot(assignments.contains)

  private def mineralSaturation(base: Base): Double = PurpleMath.nanToOne(mineralSlots(base).count( ! _.free).toDouble / mineralSlots(base).size)

  private def findAssignment(worker: FriendlyUnitInfo): Unit = {
    (if (gasWorkersToAdd > 0) tryGas(worker) || tryMinerals(worker) || tryLongDistance(worker) else tryMinerals(worker) || tryLongDistance(worker) || tryGas(worker))
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

  private def getWorkersClosestToBase(base: Base, minimumOrder: Int, count: Int): Iterable[FriendlyUnitInfo] = {
    Iterable.empty // TODO
  }

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
}
