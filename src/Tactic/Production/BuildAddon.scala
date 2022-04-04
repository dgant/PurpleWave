package Tactic.Production

import Macro.Requests.RequestProduction
import Micro.Agency.Intention
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsAll
import Utilities.UnitPreferences.PreferIdle
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

class BuildAddon(buildableAddon: RequestProduction) extends Production {

  setBuildable(buildableAddon)
  def addonClass    : UnitClass     = buildable.unit.get
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, addonClass, 1)
  val builderLock   : LockUnits     = new LockUnits(this)
  builderLock.matcher     = IsAll(addonClass.whatBuilds._1, _.addon.filter(addonClass).forall(_.producer.contains(this)))
  builderLock.counter     = CountOne
  builderLock.preference  = PreferIdle

  def builder: Option[FriendlyUnitInfo] = builderLock.units.headOption
  def addon: Option[UnitInfo] = builder.flatMap(_.addon).filter(addonClass)
  override def isComplete: Boolean = addon.exists(_.aliveAndComplete)
  override def hasSpent: Boolean = addon.isDefined
  
  override def onUpdate() {
    if (isComplete) return
    addon.foreach(_.setProducer(this))
    if (hasSpent || currencyLock.acquire()) {
      builderLock.acquire()
      if (addon.isEmpty) {
        builder.foreach(_.intend(this, new Intention { toAddon = Some(addonClass) }))
      }
    }
  }
}
