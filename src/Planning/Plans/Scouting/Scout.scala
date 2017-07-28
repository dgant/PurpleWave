package Planning.Plans.Scouting

import Lifecycle.With
import Mathematics.Points.SpecificPoints
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchWorkers
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class Scout extends Plan {
  
  description.set("Scout the enemy with a worker")
  
  val scouts = new Property[LockUnits](new LockUnits {
    unitCounter.set(UnitCountExactly(1))
    unitMatcher.set(UnitMatchWorkers)
    unitPreference.set(UnitPreferClose(SpecificPoints.middle))
    interruptable.set(false)
  })
  
  var lastScouts: Iterable[FriendlyUnitInfo] = Iterable.empty
  var lastScoutDeath: Int = -24 * 60
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty && lastScoutDeath > 0
  
  override def onUpdate() {
    if (isComplete) return
    
    val scoutsDied = lastScouts.nonEmpty && lastScouts.exists( ! _.alive)
    if (scoutsDied) {
      lastScouts = List.empty
      lastScoutDeath = With.frame
    }
    if (With.framesSince(lastScoutDeath) < 24 * 30) {
      return
    }
    
    val scoutingDestination = With.intelligence.leastScoutedBases.filter( ! _.zone.island).map(_.townHallArea.midPixel).head
    scouts.get.unitPreference.set(UnitPreferClose(scoutingDestination))
    scouts.get.acquire(this)
    lastScouts = scouts.get.units
    lastScouts.foreach(_.agent.intend(this, new Intention {
      toTravel = Some(scoutingDestination)
      canScout = true
    }))
  }
    
}
