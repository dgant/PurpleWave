package Tactic.Missions

import Lifecycle.With
import Planning.MacroFacts
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountExactly
import Utilities.UnitFilters.IsAll
import Utilities.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import Tactic.Squads.SquadAutomation
import Utilities.Time.Seconds

class MissionSpeedlotDrop extends MissionDrop {
  override protected def additionalFormationConditions: Boolean = (
    MacroFacts.upgradeComplete(Protoss.ShuttleSpeed, 1, Seconds(15)())
    && MacroFacts.upgradeComplete(Protoss.ZealotSpeed, 1, Seconds(15)())
    && With.recruiter.available.count(MatchDroppableSpeedlot) >= 4)

  object MatchDroppableSpeedlot extends IsAll(Protoss.Zealot, recruitablePassenger)
  val zealotLock: LockUnits = new LockUnits(this, MatchDroppableSpeedlot).setCounter(CountExactly(4))

  override protected def recruit(): Unit = {
    transportLock.setPreference(PreferClose(With.geography.home.center)).acquire()
    if (transportLock.units.isEmpty) { terminate("No transports available"); return }
    zealotLock.setPreference(PreferClose(transportLock.units.head.pixel)).acquire()
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
