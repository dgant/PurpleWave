package Tactic.Missions

import Information.Geography.Types.Base
import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsAll, IsDetector}
import Utilities.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import Tactic.Squads.SquadAutomation

class MissionDTDrop extends MissionDrop {
  override protected def additionalFormationConditions: Boolean = With.recruiter.available.exists(MatchDroppableDT)

  object MatchDroppableDT extends IsAll(Protoss.DarkTemplar, recruitablePassenger)
  val dtLock: LockUnits = new LockUnits(this, Protoss.DarkTemplar).setCounter(CountUpTo(2))

  override def additionalItineraryConditions(base: Base): Boolean = ! base.enemies.exists(IsDetector)

  override protected def recruit(): Unit = {
    populateItinerary()
    vicinity = itinerary.headOption.map(_.heart).getOrElse(With.scouting.enemyHome).center
    transportLock.setPreference(PreferClose(vicinity)).acquire()
    if (transportLock.units.isEmpty) { terminate("No transports available"); return }
    val transportPixel = transportLock.units.head.pixel
    dtLock.setPreference(PreferClose(transportPixel)).acquire()
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
