package Planning.Plans.Scouting

import Information.Geography.Types.Base
import Lifecycle.With
import Micro.Agency.Intention
import Planning.Composition.Property
import Planning.Composition.ResourceLocks.LockUnits
import Planning.Composition.UnitCounters.UnitCountExactly
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatchMobile, UnitMatchNot}
import Planning.Composition.UnitPreferences.UnitPreferFast
import Planning.Plan
import ProxyBwapi.Races.{Protoss, Terran}
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

class FindExpansions extends Plan {
  
  description.set("Find enemy expansions")
  
  val scouts = new Property[LockUnits](new LockUnits {
    unitCounter.set(UnitCountExactly(1))
    unitMatcher.set(UnitMatchAnd(
      UnitMatchMobile,
      UnitMatchNot(Terran.Battlecruiser),
      UnitMatchNot(Terran.Valkyrie),
      UnitMatchNot(Protoss.Arbiter),
      UnitMatchNot(Protoss.Carrier)))
    unitPreference.set(UnitPreferFast)
  })
  
  override def isComplete: Boolean = With.geography.enemyBases.nonEmpty
  
  override def onUpdate() {
    scouts.get.acquire(this)
    scouts.get.units.foreach(orderScout)
  }
  
  private def orderScout(scout: FriendlyUnitInfo) = {
    With.intelligence.highlightScout(scout)
    scout.agent.intend(this, new Intention {
      toTravel = getNextScoutingBase.map(_.heart.pixelCenter)
      canCower = true
    })
  }
  
  private def getNextScoutingBase: Option[Base] = {
    def acceptable(base: Base) = {
      base.owner.isNeutral && ( ! base.zone.island || With.strategy.isPlasma)
    }
    val baseFirst = With.intelligence.nextBaseToScout
    var baseNext = baseFirst
    do {
      if (acceptable(baseNext)) return Some(baseNext)
      baseNext = With.intelligence.nextBaseToScout
    } while (baseNext != baseFirst)
    None
  }
}
