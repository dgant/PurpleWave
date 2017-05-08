package Planning.Plans.Information

import Lifecycle.With
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.UnitMatchMobile
import Planning.Composition.UnitPreferences.UnitPreferFast
import Planning.Plan
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class FindExpansions extends Plan {
  
  description.set("Find enemy expansions")
  
  val scouts = new Property[LockUnits](new LockUnits {
    unitCounter.set(new UnitCountExactly(1))
    unitMatcher.set(UnitMatchMobile)
    unitPreference.set(UnitPreferFast)
  })
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty
  
  override def update() {
    scouts.get.acquire(this)
    scouts.get.units.foreach(orderScout)
  }
  
  private def orderScout(scout:FriendlyUnitInfo) =
    With.executor.intend(new Intention(this, scout) { toTravel = getNextScoutingPixel })
  
  private def getNextScoutingPixel:Option[Pixel] =
    With.intelligence.leastScoutedBases
      .filterNot(base => base.zone.island)
      .map(_.townHallArea.midPixel)
      .headOption
}
