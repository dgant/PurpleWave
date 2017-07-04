package Planning.Plans.Information

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
  var lastScoutFrame: Int = 0
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty
  
  override def onUpdate() {
    // Did our scouts die?
    //if (With.framesSince(lastScoutFrame) < 24 * 30 && lastScouts.forall(_.alive))
    val scoutingDestination = getNextScoutingPixel
    scouts.get.unitPreference.set(UnitPreferClose(scoutingDestination))
    scouts.get.acquire(this)
    scouts.get.units.foreach(orderScout(_, scoutingDestination))
  }
  
  private def orderScout(scout: FriendlyUnitInfo, destination: Pixel) =
    With.executor.intend(new Intention(this, scout) { toTravel = Some(destination); canAttack = ! scout.unitClass.isWorker })
  
  private def getNextScoutingPixel: Pixel =
    With.intelligence.leastScoutedBases
      .filter( ! _.zone.island)
      .map(_.townHallArea.midPixel)
      .head
}
