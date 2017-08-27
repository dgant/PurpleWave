package Planning.Plans.Scouting

import Lifecycle.With
import Mathematics.Points.Pixel
import Micro.Agency.Intention
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
    unitCounter.set(UnitCountExactly(1))
    unitMatcher.set(UnitMatchMobile)
    unitPreference.set(UnitPreferFast)
  })
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty
  
  override def onUpdate() {
    scouts.get.acquire(this)
    scouts.get.units.foreach(orderScout)
  }
  
  private def orderScout(scout: FriendlyUnitInfo) =
    scout.agent.intend(this, new Intention {
      toTravel    = getNextScoutingPixel
      canCower    = true
      canPillage  = true
    })
  
  private def getNextScoutingPixel: Option[Pixel] =
    With.intelligence.leastScoutedBases
      .filter(base => base.owner.isNeutral && ( ! base.zone.island || With.strategy.isPlasma))
      .map(_.townHallArea.midPixel)
      .headOption
}
