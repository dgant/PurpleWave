package Tactic.Missions

import Information.Geography.Types.Base
import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.IsAll
import Utilities.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import Tactic.Squads.SquadAutomation

class MissionDTDrop extends MissionDrop {
  override protected def additionalFormationConditions: Boolean = With.recruiter.available.exists(MatchDroppableDT)

  object MatchDroppableDT extends IsAll(Protoss.DarkTemplar, recruitablePassenger)
  val dtLock = new LockUnits(this)
  dtLock.matcher = Protoss.DarkTemplar
  dtLock.counter = CountUpTo(2)

  override def additionalItineraryConditions(base: Base): Boolean = {
    ! base.units.exists(u => u.isEnemy && u.unitClass.isDetector)
  }

  override protected def recruit(): Unit = {
    transportLock.preference = PreferClose(vicinity)
    transportLock.acquire()
    if (transportLock.units.isEmpty) { terminate("No transports available"); return }
    dtLock.preference = PreferClose(transportLock.units.head.pixel)
    dtLock.acquire()
    if (dtLock.units.isEmpty) { terminate("No DTs available"); return }
    transports ++= transportLock.units
    passengers ++= dtLock.units
  }

  override def raid(): Unit = {
    transportLock.release()
    transports.clear()
    SquadAutomation.targetRaid(this)
  }
}
