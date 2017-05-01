package Planning.Plans.Information

import Lifecycle.With
import Mathematics.Pixels.Pixel
import Micro.Intent.Intention
import Planning.Composition.PixelFinders.Generic.TileMiddle
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
    unitPreference.set(new UnitPreferClose { positionFinder.set(TileMiddle) })
  })
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty
  
  override def update() {
    scouts.get.acquire(this)
    scouts.get.units.foreach(orderScout)
  }
  
  private def orderScout(scout:FriendlyUnitInfo) =
    With.executor.intend(new Intention(this, scout) { destination = getNextScoutingPixel; canAttack = false })
  
  private def getNextScoutingPixel:Option[Pixel] =
    With.intelligence.leastScoutedBases
      .map(_.townHallArea.midpoint)
      .filter(base => With.paths.exists(With.geography.home, base))
      .headOption
      .map(_.pixelCenter)
}
