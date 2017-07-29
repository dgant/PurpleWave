package Planning.Plans.Scouting

import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

import scala.collection.mutable

class Scout(scoutCount: Int = 1) extends Plan {
  
  description.set("Scout the enemy with a worker")
  
  val scouts = new Property[LockUnits](new LockUnits {
    unitCounter.set(UnitCountExactly(scoutCount))
    unitMatcher.set(UnitMatchWorkers)
    unitPreference.set(UnitPreferClose(SpecificPoints.middle))
    interruptable.set(false)
  })
  
  var acquiredScouts: Iterable[FriendlyUnitInfo] = Iterable.empty
  var lastScoutDeath: Int = -24 * 60
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty && lastScoutDeath > 0
  
  override def onUpdate() {
    if (isComplete) return
    
    val scoutsDied = acquiredScouts.nonEmpty && acquiredScouts.exists( ! _.alive)
    if (scoutsDied) {
      acquiredScouts = List.empty
      lastScoutDeath = With.frame
    }
    if (With.framesSince(lastScoutDeath) < 24 * 30) {
      return
    }
    
    val unscoutedStartLocations = With.geography.startBases.filterNot(_.lastScoutedFrame > 0)
    val scoutsDesired           = Math.max(1, Math.min(scoutCount, unscoutedStartLocations.size))
    val scoutingDestinations    = new mutable.Queue[Pixel] ++ With.intelligence.leastScoutedBases.filter( ! _.zone.island).map(_.townHallArea.midPixel)
    
    scouts.get.unitCounter.set(UnitCountExactly(scoutsDesired))
    scouts.get.unitPreference.set(UnitPreferClose(scoutingDestinations.head))
    scouts.get.acquire(this)
    acquiredScouts = scouts.get.units
    
    acquiredScouts.foreach(
      _.agent.intend(this, new Intention {
      toTravel = Some(scoutingDestinations.dequeue())
      canScout = true
    }))
  }
    
}
