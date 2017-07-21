package Planning.Plans.Scouting

import Lifecycle.With
import Mathematics.Points.{Pixel, SpecificPoints}
import Micro.Intent.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchMobile
import Planning.Composition.UnitPreferences.UnitPreferClose
import Planning.Plan
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class FindEnemyBase extends Plan {
  
  description.set("Discover an enemy base")
  
  val scouts = new Property[LockUnits](new LockUnits {
    unitCounter.set(new UnitCountExactly(1))
    unitMatcher.set(UnitMatchMobile)
    unitPreference.set(UnitPreferClose(SpecificPoints.middle))
  })
  
  var lastScouts: Iterable[FriendlyUnitInfo] = Iterable.empty
  var lastScoutSlaughter: Int = -24 * 60
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty
  
  override def onUpdate() {
    val scoutsDied = lastScouts.nonEmpty && lastScouts.forall( ! _.alive)
    if (scoutsDied) {
      lastScouts = List.empty
      lastScoutSlaughter = With.frame
    }
    if (With.framesSince(lastScoutSlaughter) < 24 * 60) {
      return
    }
    
    val scoutingDestination = getNextScoutingPixel
    scouts.get.unitPreference.set(UnitPreferClose(scoutingDestination))
    scouts.get.acquire(this)
    lastScouts = scouts.get.units
    lastScouts.foreach(orderScout(_, scoutingDestination))
  }
  
  private def orderScout(scout: FriendlyUnitInfo, destination: Pixel) {
    scout.intend(new Intention(this) {
      toTravel = Some(destination);
      canAttack = ! scout.unitClass.isWorker
    })
  }
  
  private def getNextScoutingPixel: Pixel =
    With.intelligence.leastScoutedBases
      .filter( ! _.zone.island)
      .map(_.townHallArea.midPixel)
      .head
}
