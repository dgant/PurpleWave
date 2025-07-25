package Macro.Gathering

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.Tile
import Performance.Tasks.TimedTask
import ProxyBwapi.Races.Terran
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.?
import Utilities.Time.Forever

import scala.collection.mutable

class Gathering extends TimedTask with AccelerantMinerals with Zippers {
  private def isValidBase     (base: Base): Boolean = base.townHall.exists(t => t.isOurs && (t.openForBusiness || t.remainingCompletionFrames < 360))
  private def isValidResource (unit: UnitInfo): Boolean = isValidMineral(unit) || isValidGas(unit)
  private def isValidMineral  (unit: UnitInfo): Boolean = unit.alive && (unit.base.exists(longDistanceBases.contains) || unit.base.exists(isValidBase)) && unit.mineralsLeft > 0
  private def isValidGas      (unit: UnitInfo): Boolean = unit.alive && (unit.base.exists(longDistanceBases.contains) || unit.base.exists(isValidBase)) && unit.isOurs && unit.unitClass.isGas && unit.remainingCompletionFrames < 24 * 5
  private class Slot(var resource: UnitInfo, val order: Int) {
    val tile        : Tile    = resource.tileTopLeft
    val base        : Base    = resource.base.getOrElse(With.geography.bases.minBy(_.heart.groundPixels(resource.tileTopLeft)))
    val distance    : Double  = Maff.broodWarDistanceBox(resource.topLeft, resource.bottomRightExclusive, base.townHallArea.startPixel, base.townHallArea.endPixelExclusive)
    var lastUpdate  : Int     = - Forever()
    var worker      : Option[FriendlyUnitInfo] = None
    def free        : Boolean = worker.isEmpty && mineable
    def mineable    : Boolean = isValidResource(resource)
  }
  private lazy val mineralSlots: Map[Base, Vector[Slot]] = {
    val basesWithMinerals = With.units.neutral.filter(_.mineralsLeft > 0)
      .groupBy(m => m.base.getOrElse(With.geography.bases.minBy(_.heart.groundPixels(m.pixel))))
      .map(group => {
        val base = group._1
        val sorted = group._2.toVector
          .sortBy(m =>
            // Give slight edge to patches near the 3rd starting worker and where future workers will spawn if Terran/Protoss
            Maff.broodWarDistanceBox(m.topLeft, m.bottomRightExclusive, base.townHallArea.startPixel.add(47, 80), base.townHallArea.startPixel.add(70, 103)) * 0.1 +
            Maff.broodWarDistanceBox(m.topLeft, m.bottomRightExclusive, base.townHallArea.startPixel,             base.townHallArea.endPixelExclusive))
        (base, (sorted.map(new Slot(_, 0)) ++ sorted.reverse.map(new Slot(_, 1)) ++ sorted.reverse.map(new Slot(_, 2)))) })
    // A base doesn't *have* to have any minerals in it
    val omitted = With.geography.bases.filterNot(basesWithMinerals.contains)
    basesWithMinerals ++ omitted.map(b => (b, Vector.empty)).toMap
  }

  private lazy val gasSlots: Map[Base, Vector[Slot]] = With.geography.bases.map(b => (b, Vector[Slot]())).toMap ++ // Not all bases have gas! Make sure map contains them
    With.units.neutral.filter(_.gasLeft > 0)
      .groupBy(m => m.base.getOrElse(With.geography.bases.minBy(_.heart.groundPixels(m.pixel))))
      .map(group => {
        val base = group._1
        val sorted = group._2.toVector
          .sortBy(_.gasMinersRequired)
          .sortBy(g => Maff.broodWarDistanceBox(g.topLeft, g.bottomRightExclusive, base.townHallArea.startPixel, base.townHallArea.endPixelExclusive))
        (base, sorted.flatMap(g => (0 until g.gasMinersRequired).map(new Slot(g, _)).toVector)) })
  private lazy val slotsByResource: mutable.Map[UnitInfo, Seq[Slot]] = new mutable.HashMap[UnitInfo, Seq[Slot]] ++ (mineralSlots.values.view.flatten ++ gasSlots.values.view.flatten).groupBy(_.resource).map(p => (p._1, p._2.toSeq))
  private lazy val baseCosts: Map[(Base, Base), Double] = With.geography.bases
    .flatMap(baseA => With.geography.bases.map(baseB => (baseA, baseB)))
    .map(basePair => (basePair, basePair._1.heart.walkableTile.groundPixels(basePair._2.heart.walkableTile))).toMap
  private lazy val naturalCost = With.geography.mains.view.map(b => baseCosts(b, b.natural.getOrElse(b))).max

  private val assignments = new mutable.HashMap[FriendlyUnitInfo, Slot]()
  private var workers: collection.Set[FriendlyUnitInfo] = mutable.Set.empty
  private var bases                 : Vector[Base] = Vector.empty
  private var gasPumps              : Vector[FriendlyUnitInfo] = Vector.empty
  private var mineralSlotCount      : Int = 0
  private var longDistanceBases     : Seq[Base] = Seq.empty
  private var gasWorkersToAdd       : Int = 0

  // A signal on whether we are holding back on gas production due to a surplus,
  // particularly so MacroSim knows to project gas availability optimistically.
  var gasIsCappedOnQuantity: Boolean = _

  // A signal on how many gas pumps we can currently use
  var gasWorkersDesired: Int = _

  def setWorkers(newWorkers: collection.Set[FriendlyUnitInfo]): Unit = { workers = newWorkers }

  def getWorkersByResource(resource: UnitInfo): Iterable[UnitInfo] = slotsByResource.get(resource).view.flatMap(_.flatMap(_.worker))

  override protected def onRun(budgetMs: Long): Unit = {

    val basesBefore = bases
    bases = With.geography.bases.filter(isValidBase) // Geography.ourBases may not be valid on frame 0
    if (bases.isEmpty) { // Yikes. Wait for a base to finish or just go attack
      val goal = Maff.minBy(With.units.ours.filter(_.unitClass.isTownHall))(u => 10000 * u.remainingCompletionFrames + u.id).map(_.pixel).getOrElse(With.scouting.enemyHome.center)
      workers.foreach(_.intend(this).setTerminus(goal))
      return
    }

    gasPumps = With.units.ours.filter(isValidGas).toVector
    mineralSlotCount = bases.view.flatMap(mineralSlots).count(_.mineable)
    val distanceMineralBases = mineralSlots.view
      .filter(s =>
        ! isValidBase(s._1)
        &&   ( ! With.enemies.exists(_.isTerran) || ! With.units.existsEnemy(Terran.Vulture) || s._1.metro.bases.contains(With.geography.ourMain))
        &&   s._1.mineralsLeft > 0
        && ! s._1.owner.isEnemy
        &&   s._1.townHall.forall(_.isOurs)) // With unoccupied/incomplete bases
      .map(p => (p._1, p._2, bases.map(b2 => baseCosts(b2, p._1)).min)) // associate each with the score to the closest extant base
      .filter(p => mineralSlotCount < 14
        ||p._1.isBackyard
        || (
          p._3 <= 1.5 * naturalCost // Distance mine only if it's safe or we're desperate
          && With.scouting.enemyProximity < 0.75
          && (With.scouting.weControlOurFoyer || p._1.townHall.exists(_.isOurs))))
      .toVector
      .sortBy(m => m._3 * ?(m._1.townHall.exists(_.remainingCompletionFrames < 60 * 24), 1.0, 10.0)) // Sort the closest, preferring bases likely to complete soon anyway
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
    val gasWorkersTripMinimum = (7 + gasGoalMinimum - With.self.gas) / 8
    val gasWorkersTripMaximum = (7 + gasGoalMaximum - With.self.gas) / 8
    val gasWorkersRatioBasic  = (400 * With.blackboard.gasWorkerRatio() + With.self.minerals) / (400 + With.self.minerals + With.self.gas)
    val gasWorkersRatioTarget = Math.round(With.blackboard.gasWorkerRatio() * (workers.size - 3)).toInt // -3 covers "we have almost no workers left" scenarios. 0.3 ratio means we still put 3 on gas when eg assimilator finishes
    val gasWorkersBaseTarget  = Maff.clamp(gasWorkersRatioTarget, gasWorkersTripMinimum, gasWorkersTripMaximum)
    val gasWorkerTarget       = Maff.clamp(gasWorkersBaseTarget,  gasWorkersHardMinimum, gasWorkersHardMaximum)
    val gasWorkersNow         = assignments.count(_._2.resource.unitClass.isGas)
    gasWorkersToAdd           = gasWorkerTarget - gasWorkersNow
    gasWorkersDesired         = gasWorkersBaseTarget // For setting this external signal, use the target prior to counting gas pumps
    gasIsCappedOnQuantity     = With.self.gas >= gasGoalMinimum && gasWorkersHardMaximum > 0

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
        .flatMap(gasSlots(_).view.filter(_.worker.isDefined)) // Find gas workers to unassign
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
        .flatMap(mineralSlots(_).view.drop(5).filter(s => s.worker.isDefined && With.framesSince(s.lastUpdate) > 480)) // Take tenured workers from mediocre slots
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
    lazy val longDistanceMinerals = longDistanceBases.flatMap(_.minerals).filter(_.mineralsLeft > 0)
    unassigned.foreach(worker => worker.intend(this)
      .setCanFight(false)
      .setGather(
        ?(longDistanceMinerals.isEmpty,
          Maff.minBy(With.units.ours.filter(_.unitClass.isGas))(gas => worker.pixelDistanceTravelling(gas.pixel.walkableTile)),
          Some(longDistanceMinerals(worker.id % longDistanceMinerals.length)))) // TODO: If we use multiple distance bases this logic will cause long-distance-distance mining
      .setTerminus(Maff.sampleSet(nearestBase(worker).metro.bases.maxBy(_.heart.groundPixels(With.scouting.enemyThreatOrigin)).tiles).center))
    assignments.foreach(a => a._1.intend(this).setGather(a._2.resource))
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

  // Known bug: We can only assign two workers to distance mine a given patch because it only has two slots
  private def tryLongDistance(worker: FriendlyUnitInfo): Boolean = {
    val base = nearestBase(worker)
    val slot = longDistanceBases.sortBy(baseCosts(_, base)).view.flatMap(mineralSlots).find(_.free)
    slot.foreach(assignWorker(worker, _))
    slot.isDefined
  }
}
