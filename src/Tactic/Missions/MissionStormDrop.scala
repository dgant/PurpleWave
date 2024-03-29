package Tactic.Missions

import Lifecycle.With
import Planning.ResourceLocks.LockUnits
import ProxyBwapi.Races.Protoss
import Tactic.Squads.SquadAutomation
import Utilities.UnitCounters.CountUpTo
import Utilities.UnitFilters.{IsAll, IsComplete}
import Utilities.UnitPreferences.PreferClose

class MissionStormDrop extends MissionDrop {

  override protected def additionalFormationConditions: Boolean = Protoss.PsionicStorm(With.self) && With.recruiter.available.exists(MatchStormDroppable)
  override protected def requireWorkers = true
  override protected def shouldStopRaiding: Boolean = shouldGoHome
  override protected def shouldGoHome: Boolean = passengers.forall(_.energy < 75)

  object MatchStormDroppable extends IsAll(IsComplete, Protoss.HighTemplar, _.energy >= 75, recruitablePassenger)

  val stormLock = new LockUnits(this, MatchStormDroppable, CountUpTo(2))
  val zealotLock = new LockUnits(this, u => Protoss.Zealot(u) && recruitablePassenger(u))

  override protected def recruit(): Unit = {
    populateItinerary()
    vicinity = itinerary.headOption.map(_.heart).getOrElse(With.scouting.enemyHome).center
    transportLock.setPreference(PreferClose(vicinity)).acquire()
    if (transportLock.units.isEmpty) { terminate("No transports available"); return }
    val transportPixel = transportLock.units.head.pixel
    stormLock.setPreference(PreferClose(transportPixel)).acquire()
    if (stormLock.units.isEmpty) { terminate("No storms available"); return }
    zealotLock
      .setCounter(CountUpTo(4 - stormLock.units.size))
      .setPreference(PreferClose(transportPixel))
      .acquire()
    transports ++= transportLock.units
    passengers ++= stormLock.units
    passengers ++= zealotLock.units
  }

  override protected def raid(): Unit = {
    SquadAutomation.targetRaid(this)
    targets.foreach(ts => setTargets(ts.filter(t => t.unitClass.isWorker || units.exists(u => t.canAttack(u) && t.inRangeToAttack(u)))))
    transports.foreach(_.intend(this).setAction(ActionRaidTransport))
    passengers.foreach(_.intend(this).setTerminus(vicinity))
    passengers.foreach(_.agent.commit = true)
  }
}
