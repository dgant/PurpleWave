package Tactic.Missions

import Information.Geography.Types.Base
import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Protoss
import Utilities.?
import Utilities.Time.Minutes
import Utilities.UnitCounters.{CountOne, CountUpTo}
import Utilities.UnitFilters._

class MissionReaverDrop extends MissionDrop {

  private val plannedTargets = scala.collection.mutable.ArrayBuffer.empty[Base]
  override def reset(): Unit = { super.reset(); plannedTargets.clear() }
  override protected def refreshItinerary(): Unit = appendTargets(With.geography.bases, With.scouting.possibleEnemyMains)
  private[Missions] def appendTargets(bases: Seq[Base], possibleMains: Seq[Base] = Vector.empty): Unit = {
    plannedTargets ++= itinerary.filterNot(plannedTargets.contains)
    val discovered = MissionReaverDrop.targets(bases, possibleMains).filterNot(plannedTargets.contains)
    itinerary ++= discovered
    plannedTargets ++= discovered
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
  override protected def requireWorkers: Boolean = true
  override protected def targetCleared(base: Base): Boolean =
    requireWorkers && state == Raiding && MissionReaverDrop.workersCleared(base, _.visible)

  override protected def missionTimeout: Int = Minutes(3)()
  override protected def approachTimeout: Int = Minutes(2)()
  override protected def isScoutingTarget(base: Base): Boolean =
    !base.owner.isFriendly && !base.scoutedByUs && With.scouting.possibleEnemyMains.contains(base)
  override protected def canLandAt(base: Base): Boolean = base.isEnemy
  override protected def additionalItineraryConditions(base: Base): Boolean = base.isEnemy || isScoutingTarget(base)

  override protected def additionalFormationConditions: Boolean =
    With.blackboard.reaverDrop() && reaverManifest.size >= 2

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

  override protected def shouldStopRaiding: Boolean = state == Raiding && passengers.view
    .filter(Protoss.Reaver)
    .forall(reaver =>
      reaver.doomed
      || (reaver.matchups.threatsInRange.exists(IsWarrior) && ! reaver.battle.exists(_.judgement.exists(_.shouldFightGround)))
      || itinerary.headOption.exists(_.enemies.filter(IsWarrior).filter(_.canAttackGround).map(_.subjectiveValue).sum
        * ?(reaver.matchups.targetsInRange.count(IsWorker) > 2, 1, 2)
        >= passengers.view.map(_.subjectiveValue).sum))
  override protected def shouldGoHome: Boolean = ! passengers.exists(Protoss.Reaver)

  val reaverLock  : LockUnits = new LockUnits(this, u => Protoss.Reaver(u) && recruitablePassenger(u))
  val smallLock   : LockUnits = new LockUnits(this, u => u.isAny(Protoss.Zealot, Protoss.DarkTemplar) && recruitablePassenger(u)).setCounter(CountUpTo(2))
  val bigLock     : LockUnits = new LockUnits(this, u => u.isAny(Protoss.Archon, Protoss.Dragoon)     && recruitablePassenger(u)).setCounter(CountOne)

  override protected def recruit(): Unit = {
    populateItinerary()
    vicinity = itinerary.headOption.map(_.heart).getOrElse(With.scouting.enemyHome).center
    val available = With.recruiter.available.toVector
    val reavers = reaverManifest
    if (reavers.size < 2) { terminate("Waiting for two transportable Reavers"); return }
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

  def targets(bases: Seq[Base], possibleMains: Seq[Base] = Vector.empty): Vector[Base] = {
    val confirmed = bases.filter(_.isEnemy)
    val candidates = if (confirmed.nonEmpty) confirmed else possibleMains.filter(b => !b.owner.isFriendly && !b.scoutedByUs)
    candidates.sortBy(b => (
      b.enemies.count(_.canAttackAir), -b.workerCount, b.heart.pixelDistance(With.geography.home.center))).toVector
  }
}
