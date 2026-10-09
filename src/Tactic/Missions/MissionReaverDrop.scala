package Tactic.Missions

import Lifecycle.With
import Macro.Facts.MacroFacts
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.?
import Utilities.Time.{Minutes, Seconds}
import Utilities.UnitCounters.{CountOne, CountUpTo}
import Utilities.UnitFilters._

class MissionReaverDrop extends MissionDrop {

  override protected def raidTransportAction: Micro.Actions.Action = Micro.Actions.Idle
  override protected def commitWhileRaiding: Boolean = false

  override protected def additionalFormationConditions: Boolean = (
    With.blackboard.reaverDrop()
    && With.recruiter.available.exists(reaverLock.matcher)
    && (
      With.enemies.forall(_.isTerran)
        || With.geography.enemyBases.exists(_.zone.island)
        || (MacroFacts.unitsComplete(Protoss.Reaver) >= 3 && MacroFacts.unitsComplete(Protoss.Shuttle) >= 2))
    && (
      (With.enemies.forall(_.isTerran)
        && With.frame < Minutes(9)()
        && ! MacroFacts.enemyStrategy(With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.twoRaxAcad, With.fingerprints.bio))
      || (
        // Will their base be empty?
        (With.scouting.enemyProximity > 0.35 || MacroFacts.enemyBases > 2 || With.geography.enemyBases.exists(_.zone.island))
        // Can we get there safely?
        && (MacroFacts.upgradeComplete(Protoss.ShuttleSpeed, 1, Seconds(15)()) || With.unitsShown.allEnemies(Terran.Wraith, Terran.Goliath, Protoss.Dragoon, Zerg.Scourge, Zerg.Mutalisk) == 0))))

  override protected def shouldStopRaiding: Boolean = passengers.view
    .filter(Protoss.Reaver)
    .forall(reaver =>
      reaver.doomed
      || (reaver.matchups.threatsInRange.exists(IsWarrior) && ! reaver.battle.exists(_.judgement.exists(_.shouldFightGround)))
      || itinerary.headOption.exists(_.enemies.filter(IsWarrior).filter(_.canAttackGround).map(_.subjectiveValue).sum
        * ?(reaver.matchups.targetsInRange.count(IsWorker) > 2, 1, 2)
        >= passengers.view.map(_.subjectiveValue).sum))
  override protected def shouldGoHome: Boolean = ! passengers.exists(Protoss.Reaver)

  val reaverLock  : LockUnits = new LockUnits(this, u => Protoss.Reaver(u) && recruitablePassenger(u) && (recruited(u) || u.scarabs + u.trainingQueue.size >= 3))
  val smallLock   : LockUnits = new LockUnits(this, u => u.isAny(Protoss.Zealot, Protoss.DarkTemplar) && recruitablePassenger(u)).setCounter(CountUpTo(2))
  val bigLock     : LockUnits = new LockUnits(this, u => u.isAny(Protoss.Archon, Protoss.Dragoon)     && recruitablePassenger(u)).setCounter(CountOne)

  override protected def recruit(): Unit = {
    populateItinerary()
    vicinity = itinerary.headOption.map(_.heart).getOrElse(With.scouting.enemyHome).center
    val available = With.recruiter.available.toVector
    val shuttles = available.filter(u => Protoss.Shuttle(u) && transportLock.matcher(u)).sortBy(u => (u.pixelDistanceSquared(vicinity), u.id))
    val reavers = DropManifest.reavers(shuttles, available.filter(reaverLock.matcher), With.blackboard.reaverDropMaxReavers())
    if (reavers.isEmpty) { terminate("No transportable reavers available"); return }
    val selectedTransports = shuttles.filter(reavers.values.toSet)
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
