package Tactic.Missions

import Information.Geography.Types.Base
import Lifecycle.With
import Mathematics.Maff
import Mathematics.Points.{Pixel, Points, Tile}
import Micro.Actions.{Action, Idle}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Protoss
import Utilities.Time.{Minutes, Seconds}
import Utilities.UnitCounters.{CountOne, CountUpTo}
import Utilities.UnitFilters._

import scala.collection.mutable

class MissionReaverDrop extends MissionDrop {

  private val plannedTargets = mutable.ArrayBuffer.empty[Base]
  private var clockwise: Option[Boolean] = None
  private case class RaidProgress(var frame: Int, var distance: Double, var landed: Set[Int], var shot: Int)
  private val combatProgress = mutable.Map.empty[FriendlyUnitInfo, RaidProgress]
  override def reset(): Unit = { super.reset(); plannedTargets.clear(); clockwise = None; onDestinationChanged() }
  override protected def onDestinationChanged(): Unit = combatProgress.clear()
  override protected def refreshItinerary(): Unit = appendTargets(With.geography.bases, With.scouting.possibleEnemyMains)
  protected def appendTargets(bases: Seq[Base], possibleMains: Seq[Base] = Vector.empty): Unit = {
    plannedTargets ++= itinerary.filterNot(plannedTargets.contains)
    val targets = MissionReaverDrop.targets(bases, possibleMains)
    val discovered = targets.filterNot(plannedTargets.contains)
    if (discovered.isEmpty) return
    val origin = transports.headOption.map(_.pixel).getOrElse(With.geography.home.center)
    val remaining = itinerary.toVector ++ discovered
    if (clockwise.isEmpty) {
      clockwise = Some(Vector(true, false).minBy(direction => {
        val sight = transports.headOption.map(_.sightPixels).getOrElse(Protoss.Shuttle.sightPixels)
        val regions = MissionReaverDrop.sweep(bases, remaining, origin, direction).map(b =>
          if (b.isEnemy) (b.heart, 0.0) else (b.townHallArea.center.tile, Math.max(0, sight - 112) / 32.0))
        val stops = origin +: DropRoute.flybys(origin.tile, regions).map(_.center)
        (stops.sliding(2).map(pair => pair.head.pixelDistance(pair.last)).sum,
          Maff.normalize0To2Pi((Points.middle.radiansTo(stops.last) - Points.middle.radiansTo(origin)) * (if (direction) 1 else -1)))
      }))
    }
    val active = if (state == Landing || state == Raiding || state == Evacuating) itinerary.headOption else None
    val ordered = active.toVector ++ MissionReaverDrop.sweep(
      bases.filterNot(b => plannedTargets.contains(b) && !itinerary.contains(b)),
      remaining.filterNot(active.contains), active.map(_.townHallArea.center).getOrElse(origin), clockwise.get).filterNot(active.contains)
    val visited = plannedTargets.filterNot(itinerary.contains).toVector
    itinerary.clear()
    itinerary ++= ordered
    plannedTargets.clear()
    plannedTargets ++= visited ++ ordered
  }

  override protected def continueItinerary(from: Base): Boolean = {
    val enemyBases = With.geography.bases.filter(_.isEnemy)
    if (!With.blackboard.reaverDropContinuePressure() ||
      transports.isEmpty || !passengers.exists(Protoss.Reaver)) return false
    val known = (plannedTargets.toVector ++ MissionReaverDrop.targets(enemyBases)).distinct
    val targets = MissionReaverDrop.reverseTargets(known, from).filter(b => b.isEnemy && !targetCleared(b))
    val next = MissionReaverDrop.sweep(With.geography.bases.filterNot(_ == from), targets, from.townHallArea.center, !clockwise.getOrElse(true))
    if (next.isEmpty) return false
    itinerary.clear()
    itinerary ++= next
    clockwise = clockwise.map(!_)
    plannedTargets.clear()
    plannedTargets ++= from +: next
    With.logger.debug(s"$this reversing itinerary from ${from.heart}: ${next.map(_.heart).mkString(" -> ")}")
    true
  }

  override protected def raidTransportAction: Action = Idle
  override protected def commitWhileRaiding: Boolean = false
  override protected def defaultCombatLanding: Boolean = true
  override protected def requiredPassenger(passenger: FriendlyUnitInfo): Boolean = Protoss.Reaver(passenger)

  private def workers = destinationBase.toVector.flatMap(_.enemies).filter(u => IsWorker(u) && u.visible)
  private def approaching(transport: FriendlyUnitInfo): Boolean = destinationBase.exists(base =>
    base.isEnemy && transport.pixelDistanceCenter(vicinity) <= Math.max(384, transport.sightPixels))

  private def confidentToRaid(transport: FriendlyUnitInfo): Boolean = {
    val crew = passengersFor(transport)
    val reavers = crew.filter(Protoss.Reaver).filterNot(_.doomed)
    val defenders = (destinationBase.toVector.flatMap(_.enemies) ++
      (crew.flatMap(_.matchups.threats) ++ transport.matchups.threats).filter(_.pixelDistanceCenter(transport) <= transport.sightPixels + 128))
      .filter(u => u.likelyStillThere && IsWarrior(u) && u.canAttack).distinct
    reavers.nonEmpty && (defenders.isEmpty || (
      defenders.forall(d => crew.exists(_.canAttack(d))) &&
      defenders.map(_.subjectiveValue).sum * (if (workers.size > 2) 1 else 2) < crew.map(_.subjectiveValue).sum &&
      reavers.exists(_.combatForecast.exists(b => b.judgement.exists(_.shouldFightGround) && defenders.forall(b.enemy.units.contains)))))
  }

  override protected def rejectApproach(transport: FriendlyUnitInfo): Boolean =
    passengersFor(transport).exists(requiredPassenger) && approaching(transport) && !confidentToRaid(transport)

  override protected def readyToRaid(transport: FriendlyUnitInfo): Boolean =
    approaching(transport) && confidentToRaid(transport)

  override protected def routeArrivalRange(transport: FriendlyUnitInfo): Double =
    if (destinationBase.exists(b => !b.isEnemy)) Math.max(0, transport.sightPixels - 112) / 32.0 else 10

  override protected def routeFollowing(transport: FriendlyUnitInfo): Vector[(Tile, Double)] = {
    if (!destinationBase.exists(b => !b.isEnemy)) return Vector.empty
    val remaining = itinerary.drop(1).toVector
    val scouts = remaining.takeWhile(b => !b.isEnemy)
    scouts.map(b => (b.townHallArea.center.tile, Math.max(0, transport.sightPixels - 112) / 32.0)) ++
      remaining.drop(scouts.size).headOption.map(b => (b.heart, 0.0))
  }

  override protected def onRaidHandoff(transport: FriendlyUnitInfo): Unit = {
    destinationBase.foreach(base => vicinity = base.heart.center)
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
    if (!base.isEnemy) base.townHallArea.tiles.forall(_.visible) && !With.units.all.exists(u =>
      u.isEnemy && u.likelyStillThere && u.unitClass.isTownHall && !u.flying && u.tileTopLeft.tileDistanceFast(base.townHallTile) < 12)
    else requireWorkers && MissionReaverDrop.workersCleared(base, _.visible)

  override protected def missionTimeout: Int = Minutes(3)()
  override protected def approachTimeout: Int = Minutes(2)()
  override protected def isScoutingTarget(base: Base): Boolean =
    !base.owner.isFriendly && !base.isEnemy && !targetCleared(base)
  override protected def canLandAt(base: Base): Boolean = base.isEnemy
  override protected def additionalItineraryConditions(base: Base): Boolean = base.isEnemy || isScoutingTarget(base)

  override protected def additionalFormationConditions: Boolean =
    With.blackboard.reaverDrop() && reaverManifest.nonEmpty

  override protected def populateItinerary(): Unit = {
    itinerary.clear()
    plannedTargets.clear()
    clockwise = None
    // If the ground scout died, locate an unscouted enemy start from the air.
    // Scout every perimeter base along the way with passengers aboard.
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
    val unableToLand = state == Landing && raidingTransports.isEmpty && approachStalled
    val defended = transports.exists(rejectApproach)
    if (doomed || stalled || unableToLand || defended) {
      With.logger.debug(s"$this recovering crew: doomed=$doomed combatStalled=$stalled landingTimedOut=$unableToLand defended=$defended")
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
    // The cap limits recruitment; Reavers already aboard a selected Shuttle stay with it.
    val aboard = selectedTransports.flatMap(_.loadedUnits).filter(p => p.alive && Protoss.Reaver(p))
    val filler = available.filter(smallLock.matcher) ++ available.filter(bigLock.matcher)
    val manifest = DropManifest.fill(selectedTransports, (reavers.keys.toVector ++ aboard).distinct ++ filler, reavers, nearby = true)

    def reserve(lock: LockUnits, units: Iterable[FriendlyUnitInfo]): Unit = {
      lock.setCounter(CountUpTo(units.size))
      With.recruiter.lockTo(lock, units)
    }
    reserve(transportLock, selectedTransports)
    reserve(reaverLock, manifest.keys.filter(Protoss.Reaver))
    reserve(smallLock, manifest.keys.filter(u => u.isAny(Protoss.Zealot, Protoss.DarkTemplar)))
    reserve(bigLock, manifest.keys.filter(u => u.isAny(Protoss.Archon, Protoss.Dragoon)))
    transports ++= selectedTransports
    passengers ++= manifest.keys.toVector.sortBy(_.id)
    manifest.foreach { case (p, t) => t.agent.addPassenger(p) }
  }
}

private[Missions] object MissionReaverDrop {
  def workersCleared(base: Base, visible: Tile => Boolean): Boolean =
    visible(base.heart) && base.resources.forall(r => visible(r.tile)) && base.units.forall(u => !u.isEnemy || !IsWorker(u))

  def orderedTargets(bases: Seq[Base], from: Pixel, clockwise: Boolean): Vector[Base] = {
    val origin = Points.middle.radiansTo(from)
    bases.sortBy(base => Maff.normalize0To2Pi(
      (Points.middle.radiansTo(base.townHallArea.center) - origin) * (if (clockwise) 1 else -1))).toVector
  }

  def sweep(bases: Seq[Base], targets: Seq[Base], from: Pixel, clockwise: Boolean): Vector[Base] = {
    val perimeter = bases.filter(b => !b.owner.isFriendly && (targets.contains(b) ||
      Seq(b.townHallTile.x, b.townHallTile.y, With.mapTileWidth - b.townHallTile.x,
        With.mapTileHeight - b.townHallTile.y).min <= Math.min(With.mapTileWidth, With.mapTileHeight) / 4))
    val ordered = orderedTargets((perimeter ++ targets).distinct, from, clockwise)
    ordered.take(ordered.lastIndexWhere(targets.contains) + 1)
  }

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
    candidates.toVector
  }
}
