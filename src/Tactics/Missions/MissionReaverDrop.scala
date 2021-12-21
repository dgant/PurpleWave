package Tactics.Missions

import Lifecycle.With
import Mathematics.Maff
import Planning.Predicates.MacroFacts
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{CountOne, CountUpTo}
import Planning.UnitMatchers._
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.{Protoss, Terran, Zerg}
import Utilities.Time.{Minutes, Seconds}

class MissionReaverDrop extends MissionDrop {

  override protected def additionalFormationConditions: Boolean = (
    With.recruiter.available.exists(reaverLock.matcher)
    && (
      (With.enemies.forall(_.isTerran) && With.frame < Minutes(9)() && ! MacroFacts.enemyStrategy(With.fingerprints.bbs, With.fingerprints.twoRax1113, With.fingerprints.twoRaxAcad, With.fingerprints.bio))
      || (
        // Will their base be empty?
        (With.scouting.enemyProgress > 0.35 || MacroFacts.enemyBases > 2 || With.geography.enemyBases.exists(_.zone.island))
        // Can we get there safely?
        && (MacroFacts.upgradeComplete(Protoss.ShuttleSpeed, 1, Seconds(15)()) || With.unitsShown.allEnemies(Terran.Wraith, Terran.Goliath, Protoss.Dragoon, Zerg.Scourge, Zerg.Mutalisk) == 0))))

  override protected def shouldStopRaiding: Boolean = passengers.view
    .filter(Protoss.Reaver)
    .forall(reaver =>
      reaver.doomed
      || itinerary.headOption.exists(_.units.view.map(u => if (u.isEnemy && MatchWarriors(u) && u.canAttackGround) u.subjectiveValue else 0).sum > passengers.view.map(_.subjectiveValue).sum)
      || (reaver.matchups.threatsInRange.exists(MatchWarriors) && ! reaver.matchups.targetsInRange.exists(MatchWorker)))
  override protected def shouldGoHome: Boolean = ! passengers.exists(Protoss.Reaver)

  val reaverLock = new LockUnits(this)
  reaverLock.matcher = unit => Protoss.Reaver(unit) && recruitablePassenger(unit)
  val zealotDTLock = new LockUnits(this)
  zealotDTLock.matcher = unit => unit.isAny(Protoss.Zealot, Protoss.DarkTemplar) && recruitablePassenger(unit)
  zealotDTLock.counter = CountUpTo(2)
  val dragoonArchonLock = new LockUnits(this)
  dragoonArchonLock.matcher = unit => unit.isAny(Protoss.Archon, Protoss.Dragoon) && recruitablePassenger(unit)
  dragoonArchonLock.counter = CountOne

  override protected def recruit(): Unit = {
    populateItinerary()
    vicinity = itinerary.headOption.map(_.heart).getOrElse(With.scouting.mostBaselikeEnemyTile).center
    transportLock.preference = PreferClose(vicinity)
    transportLock.acquire()
    if (transportLock.units.isEmpty) { terminate("No transports available"); return }

    val transportPixel = transportLock.units.head.pixel
    reaverLock.preference = PreferClose(transportPixel)
    zealotDTLock.preference = PreferClose(transportPixel)
    dragoonArchonLock.preference = PreferClose(transportPixel)

    reaverLock.counter = CountUpTo(Maff.clamp(MacroFacts.unitsComplete(Protoss.Reaver) - 1, 1, 2))
    reaverLock.acquire()
    if (reaverLock.units.isEmpty) { terminate("No reavers available"); return }
    if (reaverLock.units.size < 2) {
      zealotDTLock.acquire()
      if (zealotDTLock.units.isEmpty) {
        dragoonArchonLock.acquire()
      }
    }
    transports ++= transportLock.units
    passengers ++= reaverLock.units
    passengers ++= zealotDTLock.units
    passengers ++= dragoonArchonLock.units
  }
}
