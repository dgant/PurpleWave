package Tactics.Missions

import Lifecycle.With
import Mathematics.Maff
import Planning.Predicates.MacroFacts
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{CountOne, CountUpTo}
import Planning.UnitMatchers._
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Utilities.Seconds

class MissionReaverDrop extends MissionDrop {

  override protected def additionalFormationConditions: Boolean = (
    ! With.blackboard.wantToAttack()
    && With.scouting.enemyProgress > 0.5
    && MacroFacts.upgradeComplete(Protoss.ShuttleSpeed, 1, Seconds(15)())
    && With.units.existsOurs(MatchAnd(Protoss.Reaver, MatchComplete)))

  override protected def shouldStopRaiding: Boolean = passengers.view
    .filter(Protoss.Reaver)
    .forall(reaver =>
      reaver.doomed
      || (reaver.matchups.threatsInRange.exists(MatchWarriors) && ! reaver.matchups.targetsInRange.exists(MatchWorker)))
  override protected def shouldGoHome: Boolean = ! passengers.exists(Protoss.Reaver)

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
      terminate("No transports available")
      return
    }

    val transportPixel = transportLock.units.head.pixel
    reaverLock.preference = PreferClose(transportPixel)
    zealotDTLock.preference = PreferClose(transportPixel)
    dragoonArchonLock.preference = PreferClose(transportPixel)

    reaverLock.counter = CountUpTo(Maff.clamp(MacroFacts.unitsComplete(Protoss.Reaver) - 1, 1, 2))
    reaverLock.acquire()
    if (reaverLock.units.isEmpty) {
      terminate("No reavers available")
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
}
