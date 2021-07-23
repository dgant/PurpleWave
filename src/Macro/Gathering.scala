package Macro

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Points.Tile
import Mathematics.Maff
import Micro.Agency.Intention
import Performance.Tasks.TimedTask
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.Forever

import scala.collection.mutable

class Gathering extends TimedTask with AccelerantMinerals with Zippers {
  private def isValidBase     (base: Base): Boolean = base.townHall.filter(_.isOurs).exists(_.remainingCompletionFrames < 240)
  private def isValidResource (unit: UnitInfo): Boolean = isValidMineral(unit) || isValidGas(unit)
  private def isValidMineral  (unit: UnitInfo): Boolean = unit.alive && (unit.base.exists(longDistanceBases.contains) || unit.base.exists(isValidBase)) && unit.mineralsLeft > 0
  private def isValidGas      (unit: UnitInfo): Boolean = unit.alive && (unit.base.exists(longDistanceBases.contains) || unit.base.exists(isValidBase)) && unit.isOurs && unit.unitClass.isGas && unit.remainingCompletionFrames < 24 * 5
  private class Slot(var resource: UnitInfo, val order: Int) {
    val tile        : Tile    = resource.tileTopLeft
    val base        : Base    = resource.base.getOrElse(With.geography.bases.minBy(_.heart.pixelDistanceGround(resource.tileTopLeft)))
    val distance    : Double  = Maff.broodWarDistanceBox(resource.topLeft, resource.bottomRight, base.townHallArea.startPixel, base.townHallArea.endPixel)
    var lastUpdate  : Int     = - Forever()
    var worker      : Option[FriendlyUnitInfo] = None
    def free        : Boolean = worker.isEmpty && mineable
    def mineable    : Boolean = isValidResource(resource)
  }
  private lazy val mineralSlots: Map[Base, Vector[Slot]] = With.units.neutral.filter(_.mineralsLeft > 0)
    .groupBy(m => m.base.getOrElse(With.geography.bases.minBy(_.heart.pixelDistanceGround(m.pixel))))
    .map(group => {
      val base = group._1
      val sorted = group._2.toVector
        .sortBy(m =>
          // Give slight edge to patches near the 3rd starting worker and where future workers will spawn if Terran/Protoss
          Maff.broodWarDistanceBox(m.topLeft, m.bottomRight, base.townHallArea.startPixel.add(47, 80), base.townHallArea.startPixel.add(70, 103)) * 0.1 +
          Maff.broodWarDistanceBox(m.topLeft, m.bottomRight, base.townHallArea.startPixel, base.townHallArea.endPixel))
      (base, (sorted.map(new Slot(_, 0)) ++ sorted.reverse.map(new Slot(_, 1)) ++ sorted.reverse.map(new Slot(_, 2)))) })
  private lazy val gasSlots: Map[Base, Vector[Slot]] = With.geography.bases.map(b => (b, Vector[Slot]())).toMap ++ // Not all bases have gas! Make sure map contains them
    With.units.neutral.filter(_.gasLeft > 0)
      .groupBy(m => m.base.getOrElse(With.geography.bases.minBy(_.heart.pixelDistanceGround(m.pixel))))
      .map(group => {
        val base = group._1
        val sorted = group._2.toVector
          .sortBy(_.gasMinersRequired)
          .sortBy(g => Maff.broodWarDistanceBox(g.topLeft, g.bottomRight, base.townHallArea.startPixel, base.townHallArea.endPixel))
        (base, sorted.flatMap(g => (0 until g.gasMinersRequired).map(new Slot(g, _)).toVector)) })
  private lazy val slotsByResource: mutable.Map[UnitInfo, Seq[Slot]] = new mutable.HashMap[UnitInfo, Seq[Slot]] ++ (mineralSlots.values.view.flatten ++ gasSlots.values.view.flatten).groupBy(_.resource).map(p => (p._1, p._2.toSeq))
  private lazy val baseCosts: Map[(Base, Base), Double] = With.geography.bases
    .flatMap(baseA => With.geography.bases.map(baseB => (baseA, baseB)))
    .map(basePair => (basePair, basePair._1.heart.nearestWalkableTile.pixelDistanceGround(basePair._2.heart.nearestWalkableTile))).toMap
  private lazy val naturalCost = With.geography.startBases.view.map(b => baseCosts(b, b.natural.getOrElse(b))).max

  private val assignments = new mutable.HashMap[FriendlyUnitInfo, Slot]()
  private var workers: collection.Set[FriendlyUnitInfo] = mutable.Set.empty
  private var bases                 : Vector[Base] = Vector.empty
  private var gasPumps              : Vector[FriendlyUnitInfo] = Vector.empty
  private var mineralSlotCount      : Int = 0
  private var longDistanceBases     : Seq[Base] = Seq.empty
  private var gasWorkersToAdd       : Int = 0

  def setWorkers(newWorkers: collection.Set[FriendlyUnitInfo]): Unit = { workers = newWorkers }

  def getWorkersByResource(resource: UnitInfo): Iterable[UnitInfo] = slotsByResource.get(resource).view.flatMap(_.flatMap(_.worker))

  override protected def onRun(budgetMs: Long): Unit = {

    val basesBefore = bases
    bases = With.geography.bases.filter(_.townHall.exists(t => t.isOurs && (t.hasEverBeenCompleteHatch || t.remainingCompletionFrames < 240))) // Geography.ourBases isn't valid frame 0
    if (bases.isEmpty) { // Yikes. Wait for a base to finish or just go attack
      val goal = Maff.minBy(With.units.ours.filter(_.unitClass.isTownHall))(u => 10000 * u.remainingCompletionFrames + u.id).map(_.pixel).getOrElse(With.scouting.mostBaselikeEnemyTile.center)
      workers.foreach(_.intend(this, new Intention { toTravel = Some(goal) }))
      return
    }

    gasPumps = With.units.ours.filter(isValidGas).toVector
    mineralSlotCount = bases.view.flatMap(mineralSlots).count(_.mineable)
    val distanceMineralBases = mineralSlots.view
      .filter(s =>
        ! isValidBase(s._1)
        && ! s._1.owner.isEnemy
        && s._1.townHall.forall(_.isOurs)) // With unoccupied/incomplete bases
      .map(p => (p._1, p._2, bases.map(b2 => baseCosts(b2, p._1)).min)) // associate each with the score to the closest extant base
      .filter(p => mineralSlotCount < 14 || (
        p._3 <= 1.5 * naturalCost // Distance mine only if it's safe or we're desperate
        && With.scouting.enemyProgress < 0.6
        && (With.blackboard.wantToAttack() || p._1.townHall.exists(_.isOurs))))
      .toVector
      .sortBy(_._3) // sort the closest
    val distanceMineralBasesNeeded = distanceMineralBases.indices.find(i => distanceMineralBases.take(i).view.map(_._2.size).sum > 5).getOrElse(distanceMineralBases.size)
    longDistanceBases = distanceMineralBases.view.take(distanceMineralBasesNeeded).map(_._1)

    // Replace gas pumps
    gasPumps.foreach(ourGas => {
      // The pump don't work 'cause the vandals took the handles
      val slotsToReplace = ourGas.base.view.flatMap(gasSlots).filter(gasSlot => gasSlot.tile == ourGas.tileTopLeft && gasSlot.resource != ourGas)
      slotsToReplace.foreach(slot => {
        slotsByResource.remove(slot.resource).foreach(slotsByResource(ourGas) = _)
        slot.resource = ourGas
      })})

    // Respect gas limitations
    // - If our gas is below our floor,       use maximum gas worker count
    // - If our gas is at/above our ceiling,  use minimum gas worker count
    val gasWorkersHardMinimum = Math.max(workers.size - mineralSlotCount,                 Math.min(With.blackboard.gasWorkerFloor(), With.blackboard.gasWorkerCeiling()))
    val gasWorkersHardMaximum = Math.min(gasPumps.view.map(slotsByResource(_).size).sum,  Math.max(With.blackboard.gasWorkerFloor(), With.blackboard.gasWorkerCeiling()))
    val gasGoalMinimum        = Math.min(With.blackboard.gasLimitFloor(), With.blackboard.gasLimitCeiling())
    val gasGoalMaximum        = Math.max(With.blackboard.gasLimitFloor(), With.blackboard.gasLimitCeiling())
    val gasNow                = With.self.gas
    val gasWorkersTripMinimum = (7 + gasGoalMinimum - With.self.gas) / 8
    val gasWorkersTripMaximum = (7 + gasGoalMaximum - With.self.gas) / 8
    val gasWorkersRatioTarget = Math.round(With.blackboard.gasWorkerRatio() * workers.size).toInt
    val gasWorkersBaseTarget  = Maff.clamp(gasWorkersRatioTarget, gasWorkersTripMinimum, gasWorkersTripMaximum)
    val gasWorkerTarget       = Maff.clamp(gasWorkersBaseTarget, gasWorkersHardMinimum, gasWorkersHardMaximum)
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
          .sortBy(b => baseCosts(b, slot.base)) // look at bases in ascending distance to the gas
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
      Maff.minBy(underemployed)(_.lastUpdate).foreach(_.worker.foreach(reassignWorker))
    }

    // Assign workers
    // Assign older workers first because they're most likely to keep the same or a similar assignment
    unassigned.toVector.sortBy(_.frameDiscovered).foreach(findAssignment)

    issueCommands()
  }

  private def issueCommands(): Unit = {
    unassigned.foreach(i => i.intend(this, new Intention {
      toGather = Maff.minBy(With.units.ours.filter(_.unitClass.isGas))(u => i.pixelDistanceTravelling(u.pixel.nearestWalkableTile))
      toTravel = Some(Maff.sampleSet(nearestBase(i).metro.bases.maxBy(_.heart.pixelDistanceGround(With.scouting.threatOrigin)).tiles).center)
      canFight = false }))
    assignments.foreach(a => a._1.intend(this, new Intention { toGather = Some(a._2.resource) }))
  }

  private def doInitialSplit(): Boolean = {
    if (With.frame > 0 || workers.size != 4) return false
    val minerals = bases.flatMap(mineralSlots(_))
    if (minerals.size < workers.size) return false
    val splittees = workers.toVector.sortBy(worker => minerals.map(_.resource.pixelDistanceEdge(worker)).min)
    // Most builds are bottlenecked on when we can produce the 6th worker, whose cost you can afford after 56 / 8 = 7 mineral trips
    // Thus, you want to optimize for the return of the 7th mineral trip, which will ordinarily be done by the third-furthest initial worker
    // This is even true for the 4-pool, which needs 150 minerals = 19 trips = 4 complete rounds + 3rd return from the fifth round
    val splitteeOrder = Seq(2, 1, 0, 3)
    val splitteesPrioritized = splitteeOrder.map(splittees(_)) ++ splittees.drop(splitteeOrder.size)
    splitteesPrioritized.foreach(findAssignment)
    issueCommands()
    true
  }

  private def unassigned: Iterable[FriendlyUnitInfo] = workers.view.filterNot(assignments.contains)

  private def mineralSaturation(base: Base): Double = Maff.nanToOne(mineralSlots(base).count( ! _.free).toDouble / mineralSlots(base).count(_.resource.alive))

  private def findAssignment(worker: FriendlyUnitInfo): Unit = {
    (if (gasWorkersToAdd > 0) tryGas(worker) || tryMinerals(worker) else tryMinerals(worker) || tryGas(worker)) || tryLongDistance(worker)
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
    worker.base.filter(bases.contains).orElse(
      Maff.minBy(bases)(b => worker.pixelDistanceTravelling(b.heart))).getOrElse(
        With.geography.bases.minBy(b => worker.pixelDistanceTravelling(b.heart)))

  private def getWorkersClosestToBase(base: Base, minimumOrder: Int, count: Int): Iterable[FriendlyUnitInfo] = {
    Iterable.empty // TODO
  }

  private def tryMinerals(worker: FriendlyUnitInfo): Boolean = {
    val base = nearestBase(worker)
    def basesNear(worker: FriendlyUnitInfo): Seq[Base] = bases.view.filter(baseCosts(_, base) <= naturalCost)
    def basesFar(worker: FriendlyUnitInfo): Seq[Base] = bases.view.filter(baseCosts(_, base) > naturalCost)
    val slot = mineralSlots(base).find(s => s.free && s.order == 0).orElse(
      basesNear(worker).sortBy(baseCosts(_, base)).flatMap(mineralSlots(_)).find(s => s.free && s.order == 0)).orElse(
        mineralSlots(base).find(s => s.free && s.order == 1)).orElse(
          basesNear(worker).sortBy(baseCosts(_, base)).flatMap(mineralSlots(_)).find(s => s.free && s.order == 1)).orElse(
            basesFar(worker).sortBy(baseCosts(_, base)).flatMap(mineralSlots(_)).find(s => s.free && s.order < 2))
    slot.foreach(assignWorker(worker, _))
    slot.isDefined
  }

  private def tryGas(worker: FriendlyUnitInfo): Boolean = {
    val base = nearestBase(worker)
    def basesNear(worker: FriendlyUnitInfo): Seq[Base] = bases.view.filter(baseCosts(_, base) <= naturalCost)
    def basesFar(worker: FriendlyUnitInfo): Seq[Base] = bases.view.filter(baseCosts(_, base) > naturalCost)
    val slot = gasSlots(base).find(s => s.free && s.order < 4).orElse(
      basesNear(worker).sortBy(baseCosts(_, base)).flatMap(gasSlots(_)).find(s => s.free && s.order < 4)).orElse(
        basesFar(worker).sortBy(baseCosts(_, base)).flatMap(gasSlots(_)).find(s => s.free && s.order < 4)).orElse(
          gasSlots(base).find(_.free)).orElse(
            basesNear(worker).sortBy(baseCosts(_, base)).flatMap(gasSlots(_)).find(_.free)).orElse(
                basesFar(worker).sortBy(baseCosts(_, base)).flatMap(gasSlots(_)).find(_.free))
    slot.foreach(assignWorker(worker, _))
    slot.isDefined
  }

  private def tryLongDistance(worker: FriendlyUnitInfo): Boolean = {
    val base = nearestBase(worker)
    val slot = longDistanceBases.sortBy(baseCosts(_, base)).view.flatMap(mineralSlots).find(_.free)
    slot.foreach(assignWorker(worker, _))
    slot.isDefined
  }
}
