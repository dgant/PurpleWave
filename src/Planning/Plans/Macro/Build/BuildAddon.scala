package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Architecture.Blueprint
import Macro.Buildables.{Buildable, BuildableUnit}
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyForUnit, LockUnits}
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchAnd, UnitMatcher}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo

class BuildAddon(val addonClass: UnitClass) extends Production {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def producerUnitLocks: Seq[LockUnits] = Seq(builderLock)
  override def producerInProgress: Boolean = addon.isDefined
  override def buildable: Buildable = BuildableUnit(addonClass)

  val buildingDescriptor  = new Blueprint(addonClass)
  val currencyLock        = new LockCurrencyForUnit(addonClass)
  
  private var addon: Option[UnitInfo] = None
  
  val builderMatcher = MatchAnd(addonClass.whatBuilds._1, new UnitMatcher {
    override def apply(unit: UnitInfo): Boolean = unit.addon.forall(addon.contains)
  })
  
  val builderLock: LockUnits = new LockUnits
  builderLock.counter.set(CountOne)
  builderLock.matcher.set(builderMatcher)
  
  override def isComplete: Boolean = addon.exists(_.aliveAndComplete)
  
  override def onUpdate() {
    
    if (isComplete) {
      return
    }
      
    currencyLock.framesPreordered = (addonClass.buildUnitsEnabling.map(With.projections.unit) :+ 0).max
    currencyLock.isSpent = addon.isDefined
    currencyLock.acquire(this)
    
    builderLock.acquire(this)
    if (currencyLock.satisfied && builderLock.satisfied) {
      val builder = builderLock.units.head
      addon = builder.addon
      addon.foreach(_.setProducer(this))
      if (addon.isEmpty) {
        builder.agent.intend(this, new Intention {
          toAddon = if (currencyLock.satisfied) Some(addonClass) else None
        })
      }
    }
  }
}
