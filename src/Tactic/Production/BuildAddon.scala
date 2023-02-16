package Tactic.Production

import Macro.Requests.RequestBuildable
import Planning.ResourceLocks.{LockCurrency, LockCurrencyFor, LockUnits}
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.FriendlyUnitInfo
import Utilities.UnitCounters.CountOne
import Utilities.UnitFilters.IsAll
import Utilities.UnitPreferences.PreferIdle

class BuildAddon(buildableAddon: RequestBuildable, expectedFramesArg: Int) extends Production {
  setRequest(buildableAddon, expectedFramesArg)
  def addonClass    : UnitClass     = request.unit.get
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, addonClass)
  val builderLock   : LockUnits     = new LockUnits(this,
    IsAll(addonClass.whatBuilds._1, _.addon.filter(addonClass).forall(_.producer.contains(this))),
    PreferIdle,
    CountOne)

  def builder: Option[FriendlyUnitInfo] = builderLock.units.headOption
  override def trainee: Option[FriendlyUnitInfo] = builder.flatMap(_.addon).filter(addonClass).flatMap(_.friendly)
  override def isComplete: Boolean = trainee.exists(_.aliveAndComplete)
  override def hasSpent: Boolean = trainee.isDefined

  override def onUpdate(): Unit = {
    trainee.foreach(_.setProducer(this))
    if (hasSpent || currencyLock.acquire()) {
      builderLock.acquire()
      if (trainee.isEmpty) {
        builder.foreach(_.intend(this).setBuild(addonClass))
      }
    }
  }
}
