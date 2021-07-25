package Tactics.Missions

import Lifecycle.With
import Planning.Predicates.MacroFacts
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{CountExactly, CountOne}
import Planning.UnitMatchers.{MatchAnd, MatchComplete, MatchTransport}
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import Tactics.Squads.SquadAutomation
import Utilities.Seconds

class MissionSpeedlotDrop extends MissionDrop {
  override protected def additionalFormationConditions: Boolean = (
    MacroFacts.upgradeComplete(Protoss.ShuttleSpeed, 1, Seconds(15)())
    && MacroFacts.upgradeComplete(Protoss.ZealotSpeed, 1, Seconds(15)())
    && With.units.countOurs(MatchAnd(Protoss.Zealot, MatchComplete)) >= 4)

  val zealotLock = new LockUnits(this)
  zealotLock.matcher = Protoss.Zealot
  zealotLock.counter = CountExactly(4)

  val transportLock = new LockUnits(this)
  transportLock.matcher = MatchTransport
  transportLock.counter = CountOne

  override protected def recruit(): Unit = {
    transportLock.preference = PreferClose(With.geography.home.center)
    transportLock.acquire()
    if (transportLock.units.isEmpty) { terminate("No transports available"); return }
    zealotLock.preference = PreferClose(transportLock.units.head.pixel)
    zealotLock.acquire()
    if (zealotLock.units.isEmpty) { terminate("No speedlots available"); return }
    transports ++= transportLock.units
    passengers ++= zealotLock.units
  }

  override def raid(): Unit = {
    transportLock.release()
    transports.clear()
    SquadAutomation.targetRaid(this)
  }
}
