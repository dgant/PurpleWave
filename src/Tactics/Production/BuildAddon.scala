package Tactics.Production

import Lifecycle.With
import Macro.Buildables.Buildable
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.MatchAnd
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class BuildAddon(buildableAddon: Buildable) extends Production {

  setBuildable(buildableAddon)
  def addonClass    : UnitClass     = buildable.unit.get
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, addonClass, 1)
  val builderLock   : LockUnits     = new LockUnits(this)
  builderLock.counter = CountOne
  builderLock.matcher = MatchAnd(addonClass.whatBuilds._1, (unit: UnitInfo) => ! unit.addon.exists(_.complete))

  def builder: Option[FriendlyUnitInfo] = builderLock.units.headOption
  def addon: Option[UnitInfo] = builder.flatMap(_.addon).filter(addonClass)
  override def isComplete: Boolean = addon.exists(_.aliveAndComplete)
  override def hasSpent: Boolean = addon.isDefined
  
  override def onUpdate() {
    if (isComplete) return
      
    currencyLock.framesPreordered = (addonClass.buildUnitsEnabling.map(With.projections.unit) :+ 0).max
    if (hasSpent || currencyLock.acquire()) {
      builderLock.acquire()
      if (addon.isEmpty) {
        builder.foreach(_.intend(this, new Intention { toAddon = if (currencyLock.satisfied) Some(addonClass) else None }))
      }
    }

    addon.foreach(_.setProducer(this))
  }
}
