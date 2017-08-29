package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Scheduling.Project
import Micro.Agency.Intention
import Planning.Composition.ResourceLocks.{LockCurrencyForUnit, LockUnits}
import Planning.Composition.UnitCounters.UnitCountOne
import Planning.Composition.UnitMatchers.{UnitMatchAnd, UnitMatcher}
import Planning.Plan
import ProxyBwapi.UnitClass.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

class BuildAddon(val addonClass: UnitClass) extends Plan {
  
  val buildingDescriptor  = new Blueprint(this, Some(addonClass))
  val currencyLock        = new LockCurrencyForUnit(addonClass)
  
  private var addon: Option[UnitInfo] = None
  
  val builderMatcher = UnitMatchAnd(addonClass.whatBuilds._1, new UnitMatcher {
    override def accept(unit: UnitInfo): Boolean = unit.addon.forall(addon.contains)
  })
  
  val builderLock = new LockUnits {
    description.set("Get a builder")
    unitCounter.set(UnitCountOne)
    unitMatcher.set(builderMatcher)
  }
    
  description.set("Add on a " + addonClass)
  
  override def isComplete: Boolean = addon.exists(_.aliveAndComplete)
  
  override def onUpdate() {
    
    if (isComplete) {
      With.groundskeeper.flagFulfilled(buildingDescriptor, addon.get)
      return
    }
      
    currencyLock.framesAhead = (addonClass.buildUnitsEnabling.map(enablingClass => Project.framesToUnits(enablingClass, 1)) :+ 0).max
    currencyLock.isSpent = addon.isDefined
    currencyLock.acquire(this)
    
    builderLock.acquire(this)
    if (currencyLock.satisfied && builderLock.satisfied) {
      val builder = builderLock.units.head
      addon = builder.addon
      if (addon.isEmpty) {
        builder.agent.intend(this, new Intention {
          toAddon = if (currencyLock.satisfied) Some(addonClass) else None
        })
      }
    }
  }
}
