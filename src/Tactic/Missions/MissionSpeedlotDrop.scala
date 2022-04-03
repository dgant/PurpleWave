package Tactic.Missions

import Lifecycle.With
import Planning.Predicates.MacroFacts
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.CountExactly
import Planning.UnitMatchers.MatchAnd
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import Tactic.Squads.SquadAutomation
import Utilities.Time.Seconds

class MissionSpeedlotDrop extends MissionDrop {
  override protected def additionalFormationConditions: Boolean = (
    MacroFacts.upgradeComplete(Protoss.ShuttleSpeed, 1, Seconds(15)())
    && MacroFacts.upgradeComplete(Protoss.ZealotSpeed, 1, Seconds(15)())
    && With.recruiter.available.count(MatchDroppableSpeedlot) >= 4)

  object MatchDroppableSpeedlot extends MatchAnd(Protoss.Zealot, recruitablePassenger)
  val zealotLock = new LockUnits(this)
  zealotLock.matcher = MatchDroppableSpeedlot
  zealotLock.counter = CountExactly(4)

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
