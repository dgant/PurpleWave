package Tactic.Missions

import Lifecycle.With
import Macro.Facts.MacroFacts
import Mathematics.Maff
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.{CountOne, CountUpTo}
import Utilities.UnitFilters._
import Utilities.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.?
import Utilities.Time.{Minutes, Seconds}

class MissionReaverDrop extends MissionDrop {

  override protected def additionalFormationConditions: Boolean = (
    With.recruiter.available.exists(reaverLock.matcher)
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
      || (reaver.matchups.threatsInRange.exists(IsWarrior) && ! reaver.battle.exists(_.judgement.exists( ! _.shouldFightGround)))
      || itinerary.headOption.exists(_.enemies.filter(IsWarrior).filter(_.canAttackGround).map(_.subjectiveValue).sum
        * ?(reaver.matchups.targetsInRange.count(IsWorker) > 2, 1, 2)
        >= passengers.view.map(_.subjectiveValue).sum))
  override protected def shouldGoHome: Boolean = ! passengers.exists(Protoss.Reaver)

  val reaverLock  : LockUnits = new LockUnits(this, u => Protoss.Reaver(u)                            && recruitablePassenger(u))
  val smallLock   : LockUnits = new LockUnits(this, u => u.isAny(Protoss.Zealot, Protoss.DarkTemplar) && recruitablePassenger(u)).setCounter(CountUpTo(2))
  val bigLock     : LockUnits = new LockUnits(this, u => u.isAny(Protoss.Archon, Protoss.Dragoon)     && recruitablePassenger(u)).setCounter(CountOne)

  override protected def recruit(): Unit = {
    populateItinerary()
    vicinity = itinerary.headOption.map(_.heart).getOrElse(With.scouting.enemyHome).center
    transportLock.setPreference(PreferClose(vicinity)).acquire()
    if (transportLock.units.isEmpty) { terminate("No transports available"); return }

    val transportPixel = transportLock.units.head.pixel
    reaverLock.setPreference(PreferClose(transportPixel))
    smallLock .setPreference(PreferClose(transportPixel))
    bigLock   .setPreference(PreferClose(transportPixel))

    reaverLock.counter = CountUpTo(Maff.clamp(MacroFacts.unitsComplete(Protoss.Reaver) - 1, 1, 2))
    reaverLock.acquire()
    if (reaverLock.units.isEmpty) { terminate("No reavers available"); return }
    if (reaverLock.units.size < 2 && smallLock.acquire().isEmpty) {
      bigLock.acquire()
    }
    transports ++= transportLock.units
    passengers ++= reaverLock.units
    passengers ++= smallLock.units
    passengers ++= bigLock.units
  }
}
