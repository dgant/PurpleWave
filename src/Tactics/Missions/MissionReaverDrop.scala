package Tactics.Missions

import Lifecycle.With
import Mathematics.Maff
import Planning.Predicates.MacroFacts
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{CountOne, CountUpTo}
import Planning.UnitMatchers.{MatchAnd, MatchComplete, MatchTransport}
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Tactics.Squads.SquadAutomation

class MissionReaverDrop extends MissionDrop {

  override protected def additionalFormationConditions: Boolean = With.units.existsOurs(MatchAnd(Protoss.Reaver, MatchComplete))

  private def recruitable(unit: UnitInfo) = unit.complete && ! unit.visibleToOpponents

  val transportLock = new LockUnits(this)
  transportLock.matcher = unit => MatchTransport(unit) && recruitable(unit)

  val reaverLock = new LockUnits(this)
  reaverLock.matcher = unit => Protoss.Reaver(unit) && recruitable(unit)
  reaverLock.counter = CountUpTo(2)

  val zealotDTLock = new LockUnits(this)
  zealotDTLock.matcher = unit => unit.isAny(Protoss.Zealot, Protoss.DarkTemplar) && recruitable(unit)
  zealotDTLock.counter = CountUpTo(2)

  val dragoonArchonLock = new LockUnits(this)
  dragoonArchonLock.matcher = unit => unit.isAny(Protoss.Archon, Protoss.Dragoon) && recruitable(unit)
  dragoonArchonLock.counter = CountOne

  override protected def recruit(): Unit = {
    populateItinerary()
    vicinity = itinerary.headOption.map(_.heart).getOrElse(With.scouting.mostBaselikeEnemyTile).center
    transportLock.preference = PreferClose(vicinity)

    transportLock.counter = CountUpTo(Maff.clamp(MacroFacts.unitsComplete(Protoss.Reaver) - 1, 1, 2))
    transportLock.acquire()
    if (transportLock.units.isEmpty) {
      failToRecruit()
      return
    }

    val transportPixel = transportLock.units.head.pixel
    reaverLock.preference = PreferClose(transportPixel)
    zealotDTLock.preference = PreferClose(transportPixel)
    dragoonArchonLock.preference = PreferClose(transportPixel)

    reaverLock.acquire()
    if (reaverLock.units.isEmpty) {
      failToRecruit()
      return
    }
    zealotDTLock.acquire()
    if (zealotDTLock.units.isEmpty) {
      dragoonArchonLock.acquire()
    }

    transports ++= transportLock.units
    passengers ++= reaverLock.units
    passengers ++= zealotDTLock.units
    passengers ++= dragoonArchonLock.units
  }

  private def failToRecruit(): Unit = {
    transportLock.release()
    reaverLock.release()
  }

  override protected def raid(): Unit = {
    SquadAutomation.target(this)
    passengers.foreach(_.agent.commit = true)
  }
}
