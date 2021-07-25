package Tactics.Missions

import Lifecycle.With
import Micro.Agency.Intention
import Planning.Predicates.MacroFacts
import Planning.ResourceLocks.LockUnits
import Planning.UnitCounters.{CountOne, CountUpTo}
import Planning.UnitMatchers.{MatchAnd, MatchComplete, MatchTransport}
import Planning.UnitPreferences.PreferClose
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.UnitInfo
import Tactics.Squads.SquadAutomation
import Utilities.Seconds

class MissionStormDrop extends MissionDrop {

  override protected def additionalFormationConditions: Boolean = (
    With.self.hasTech(Protoss.PsionicStorm)
    && With.recruiter.available.exists(MatchStormDroppable))
  override protected def requireWorkers = true
  override protected def shouldStopRaiding: Boolean = shouldGoHome
  override protected def shouldGoHome: Boolean = passengers.forall(_.energy < 75)

  object MatchStormDroppable extends MatchAnd(MatchComplete, Protoss.HighTemplar, _.energy >= 75)

  private def recruitable(unit: UnitInfo) = unit.complete && ! unit.visibleToOpponents

  val transportLock = new LockUnits(this)
  transportLock.matcher = unit => MatchTransport(unit) && recruitable(unit)
  transportLock.counter = CountOne

  val stormLock = new LockUnits(this)
  stormLock.matcher = MatchStormDroppable
  stormLock.counter = CountUpTo(2)

  val zealotLock = new LockUnits(this)
  zealotLock.matcher = Protoss.Zealot

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
    stormLock.preference = PreferClose(transportPixel)
    stormLock.acquire()
    if (stormLock.units.isEmpty) {
      terminate("No storms available")
      return
    }
    zealotLock.counter = CountUpTo(4 - stormLock.units.size)
    zealotLock.preference = PreferClose(transportPixel)
    zealotLock.acquire()

    transports ++= transportLock.units
    passengers ++= stormLock.units
    passengers ++= zealotLock.units
  }

  override protected def raid(): Unit = {
    SquadAutomation.target(this)
    targetQueue = targetQueue.map(_.filter(t => t.unitClass.isWorker || units.exists(u => t.canAttack(u) && t.inRangeToAttack(u))))
    transports.foreach(_.intend(this, new Intention { action = new ActionRaidTransport}))
    passengers.foreach(_.intend(this, new Intention { toTravel = Some(vicinity) }))
    passengers.foreach(_.agent.commit = true)
  }
}
