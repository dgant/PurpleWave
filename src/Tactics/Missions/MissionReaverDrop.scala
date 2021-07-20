package Tactics.Missions

import Lifecycle.With
import Mathematics.Maff
import Micro.Agency.Intention
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
  transportLock.counter = CountOne

  val reaverLock = new LockUnits(this)
  reaverLock.matcher = unit => Protoss.Reaver(unit) && recruitable(unit)

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
    transportLock.acquire()
    if (transportLock.units.isEmpty) {
      failToRecruit()
      return
    }

    val transportPixel = transportLock.units.head.pixel
    reaverLock.preference = PreferClose(transportPixel)
    zealotDTLock.preference = PreferClose(transportPixel)
    dragoonArchonLock.preference = PreferClose(transportPixel)

    reaverLock.counter = CountUpTo(Maff.clamp(MacroFacts.unitsComplete(Protoss.Reaver) - 1, 1, 2))
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
    targetQueue = targetQueue.map(_.filter(t => t.unitClass.isWorker || units.exists(u => t.canAttack(u) && t.inRangeToAttack(u))))
    transports.foreach(_.intend(this, new Intention { action = new ActionRaidTransport}))
    passengers.foreach(_.intend(this, new Intention { toTravel = Some(vicinity) }))
    passengers.foreach(_.agent.commit = true)
  }
}
