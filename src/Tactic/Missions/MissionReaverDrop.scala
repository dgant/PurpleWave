package Tactic.Missions

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Protoss
import Utilities.Time.{Minutes, Seconds}
import Utilities.UnitCounters.{CountOne, CountUpTo}
import Utilities.UnitFilters._

class MissionReaverDrop extends MissionDrop {

  private val plannedTargets = scala.collection.mutable.ArrayBuffer.empty[Base]
  private case class RaidProgress(var frame: Int, var distance: Double, var landed: Set[Int], var shot: Int)
  private val combatProgress = scala.collection.mutable.Map.empty[FriendlyUnitInfo, RaidProgress]
  override def reset(): Unit = { super.reset(); plannedTargets.clear(); onDestinationChanged() }
  override protected def onDestinationChanged(): Unit = combatProgress.clear()
  override protected def refreshItinerary(): Unit = appendTargets(With.geography.bases, With.scouting.possibleEnemyMains)
  private[Missions] def appendTargets(bases: Seq[Base], possibleMains: Seq[Base] = Vector.empty): Unit = {
    plannedTargets ++= itinerary.filterNot(plannedTargets.contains)
    val discovered = MissionReaverDrop.targets(bases, possibleMains).filterNot(plannedTargets.contains)
    itinerary ++= discovered
    plannedTargets ++= discovered
    // Take an undefended worker line encountered en route instead of flying past
    // it to visit a more distant target first. Preserve the rest of the itinerary.
    if (state == Travelling && passengers.forall(_.transport.exists(transports.contains))) {
      def distance(base: Base): Double = transports.map(_.pixelDistanceSquared(base.heart.center)).min
      val encountered = itinerary.drop(1).filter(b => b.isEnemy && b.enemies.exists(IsWorker) &&
        !b.enemies.exists(_.canAttackAir) && transports.exists(_.pixelDistanceCenter(b.heart.center) <= 384))
        .sortBy(distance).headOption.filter(b => itinerary.headOption.forall(distance(b) < distance(_)))
      encountered.foreach(base => {
        val visited = plannedTargets.filterNot(itinerary.contains).toVector
        val remaining = itinerary.filterNot(_ == base).toVector
        itinerary.clear()
        itinerary ++= base +: remaining
        plannedTargets.clear()
        plannedTargets ++= visited ++ itinerary
        With.logger.debug(s"$this stopping at encountered worker base ${base.heart}")
      })
    }
  }

  override protected def continueItinerary(from: Base): Boolean = {
    val enemyBases = With.geography.bases.filter(_.isEnemy)
    if (!With.blackboard.reaverDropContinuePressure() || enemyBases.size < 2 ||
      transports.isEmpty || !passengers.exists(Protoss.Reaver)) return false
    val known = (plannedTargets.toVector ++ MissionReaverDrop.targets(enemyBases)).distinct
    val next = MissionReaverDrop.reverseTargets(known, from).filter(b => b.isEnemy && !targetCleared(b))
    if (next.isEmpty) return false
    itinerary.clear()
    itinerary ++= next
    plannedTargets.clear()
    plannedTargets ++= from +: next
    With.logger.debug(s"$this reversing itinerary from ${from.heart}: ${next.map(_.heart).mkString(" -> ")}")
    true
  }

  override protected def raidTransportAction: Micro.Actions.Action = Micro.Actions.Idle
  override protected def commitWhileRaiding: Boolean = false
  override protected def defaultCombatLanding: Boolean = true
  override protected def requiredPassenger(passenger: FriendlyUnitInfo): Boolean = Protoss.Reaver(passenger)

  private def workers = destinationBase.toVector.flatMap(_.enemies).filter(u => IsWorker(u) && u.visible)
  override protected def readyToRaid(transport: FriendlyUnitInfo): Boolean =
    destinationBase.exists(base => base.isEnemy && passengersFor(transport).exists(Protoss.Reaver) && (
      transport.matchups.threatsInRange.nonEmpty
      || transport.pixelDistanceCenter(vicinity) <= 64
      || (base.minerals.nonEmpty && base.minerals.forall(m =>
        m.tile.lastSeen > 0 && With.framesSince(m.tile.lastSeen) <= Seconds(5)()))))

  override protected def onRaidHandoff(transport: FriendlyUnitInfo): Unit = {
    val reavers = passengersFor(transport).filter(Protoss.Reaver)
    combatProgress(transport) = RaidProgress(With.frame, distanceToWorkers(transport),
      reavers.filterNot(_.loaded).map(_.id).toSet, Maff.max(reavers.map(_.lastFrameStartingAttack)).getOrElse(0))
  }

  private def distanceToWorkers(transport: FriendlyUnitInfo): Double =
    Maff.min(workers.map(_.pixelDistanceCenter(transport))).getOrElse(transport.pixelDistanceCenter(vicinity))

  private def combatStalled: Boolean = {
    combatProgress.foreach { case (transport, progress) =>
      val reavers = passengersFor(transport).filter(Protoss.Reaver)
      val landed = reavers.filterNot(_.loaded).map(_.id).toSet
      val shot = Maff.max(reavers.map(_.lastFrameStartingAttack)).getOrElse(0)
      val distance = distanceToWorkers(transport)
      if (distance + 32 < progress.distance || !landed.subsetOf(progress.landed) ||
        shot > progress.shot || reavers.exists(_.hasActiveScarab)) progress.frame = With.frame
      if (distance + 32 < progress.distance) progress.distance = distance
      progress.landed ++= landed
      progress.shot = Math.max(progress.shot, shot)
    }
    val participating = transports.filter(t => passengersFor(t).exists(Protoss.Reaver))
    participating.nonEmpty && participating.forall(t => combatProgress.get(t).exists(p =>
      With.framesSince(p.frame) >= Seconds(6)()))
  }
  override protected def requireWorkers: Boolean = true
  override protected def targetCleared(base: Base): Boolean =
    requireWorkers && MissionReaverDrop.workersCleared(base, _.visible)

  override protected def missionTimeout: Int = Minutes(3)()
  override protected def approachTimeout: Int = Minutes(2)()
  override protected def isScoutingTarget(base: Base): Boolean =
    MissionReaverDrop.unscoutedNatural(base) ||
      (!base.owner.isFriendly && !base.scoutedByUs && With.scouting.possibleEnemyMains.contains(base))
  override protected def canLandAt(base: Base): Boolean = base.isEnemy
  override protected def additionalItineraryConditions(base: Base): Boolean = base.isEnemy || isScoutingTarget(base)

  override protected def additionalFormationConditions: Boolean =
    With.blackboard.reaverDrop() && reaverManifest.nonEmpty

  override protected def populateItinerary(): Unit = {
    itinerary.clear()
    plannedTargets.clear()
    // If the ground scout died, locate an unscouted enemy start from the air.
    // Passengers stay aboard until ownership is confirmed; empty expansions are not targets.
    appendTargets(With.geography.bases, With.scouting.possibleEnemyMains)
    With.logger.debug(s"$this target: ${itinerary.headOption.map(_.heart)}")
  }

  private def reaverManifest: DropManifest.Manifest = {
    val available = With.recruiter.available.toVector
    val shuttles = available.filter(u => Protoss.Shuttle(u) && transportLock.matcher(u))
      .sortBy(u => (u.pixelDistanceSquared(vicinity), u.id))
    DropManifest.reavers(shuttles, available.filter(reaverLock.matcher), With.blackboard.reaverDropMaxReavers())
  }

  override protected def shouldStopRaiding: Boolean = {
    if (state != Travelling && state != Landing && state != Raiding) return false
    val doomed = passengers.filter(Protoss.Reaver).forall(_.doomed)
    val stalled = combatStalled
    val unableToLand = state == Landing && raidingTransports.isEmpty && stateAge >= Seconds(8)()
    if (doomed || stalled || unableToLand) {
      With.logger.debug(s"$this recovering crew: doomed=$doomed combatStalled=$stalled landingTimedOut=$unableToLand")
      true
    } else false
  }
  override protected def shouldGoHome: Boolean = ! passengers.exists(Protoss.Reaver)

  val reaverLock  : LockUnits = new LockUnits(this, u => Protoss.Reaver(u) && recruitablePassenger(u))
  val smallLock   : LockUnits = new LockUnits(this, u => u.isAny(Protoss.Zealot, Protoss.DarkTemplar) && recruitablePassenger(u)).setCounter(CountUpTo(2))
  val bigLock     : LockUnits = new LockUnits(this, u => u.isAny(Protoss.Archon, Protoss.Dragoon)     && recruitablePassenger(u)).setCounter(CountOne)

  override protected def recruit(): Unit = {
    populateItinerary()
    vicinity = itinerary.headOption.map(_.heart).getOrElse(With.scouting.enemyHome).center
    val available = With.recruiter.available.toVector
    val reavers = reaverManifest
    if (reavers.isEmpty) { terminate("Waiting for a transportable Reaver"); return }
    val selectedTransports = reavers.values.toVector.distinct.sortBy(_.id)
    val filler = available.filter(smallLock.matcher) ++ available.filter(bigLock.matcher)
    manifest = DropManifest.fill(selectedTransports, reavers.keys.toVector ++ filler, reavers, nearby = true)

    def reserve(lock: LockUnits, units: Iterable[ProxyBwapi.UnitInfo.FriendlyUnitInfo]): Unit = {
      lock.setCounter(CountUpTo(units.size))
      With.recruiter.lockTo(lock, units)
    }
    reserve(transportLock, selectedTransports)
    reserve(reaverLock, manifest.keys.filter(Protoss.Reaver))
    reserve(smallLock, manifest.keys.filter(u => u.isAny(Protoss.Zealot, Protoss.DarkTemplar)))
    reserve(bigLock, manifest.keys.filter(u => u.isAny(Protoss.Archon, Protoss.Dragoon)))
    transports ++= selectedTransports
    passengers ++= manifest.keys.toVector.sortBy(_.id)
  }
}

private[Missions] object MissionReaverDrop {
  def workersCleared(base: Base, visible: Mathematics.Points.Tile => Boolean): Boolean =
    visible(base.heart) && base.resources.forall(r => visible(r.tile)) && base.units.forall(u => !u.isEnemy || !IsWorker(u))

  def reverseTargets(planned: Seq[Base], from: Base): Vector[Base] = {
    val ordered = (planned :+ from).distinct
    val index = ordered.indexOf(from)
    (ordered.take(index).reverse ++ ordered.drop(index + 1).reverse).toVector
  }

  def unscoutedNatural(base: Base): Boolean =
    !base.owner.isFriendly && !base.scoutedByUs && base.naturalOf.exists(_.isEnemy)

  def targets(bases: Seq[Base], possibleMains: Seq[Base] = Vector.empty): Vector[Base] = {
    val confirmed = bases.filter(_.isEnemy)
    // Workers can evacuate to a natural that the ground scout never reached.
    // Visit it with the crew aboard before treating the itinerary as exhausted.
    val candidates = if (confirmed.nonEmpty) (confirmed ++ bases.filter(unscoutedNatural)).distinct
      else possibleMains.filter(b => !b.owner.isFriendly && !b.scoutedByUs)
    candidates.sortBy(b => (
      b.enemies.count(_.canAttackAir), -b.workerCount, b.heart.pixelDistance(With.geography.home.center))).toVector
  }
}
