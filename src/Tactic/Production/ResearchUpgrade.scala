package Tactic.Production

import Lifecycle.With
import Macro.Requests.RequestBuildable
import Planning.ResourceLocks._
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.UnitInfo.UnitInfo
import ProxyBwapi.Upgrades.Upgrade
import Utilities.UnitCounters.CountOne
import Utilities.UnitPreferences.PreferIdle

class ResearchUpgrade(requestArg: RequestBuildable, expectedFramesArg: Int) extends Production {
  setRequest(requestArg, expectedFramesArg)
  val upgrade       : Upgrade       = request.upgrade.get
  val level         : Int           = request.quantity
  val upgraderClass : UnitClass     = upgrade.whatUpgrades
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, upgrade, level)
  val upgraders     : LockUnits     = new LockUnits(this,
    (u: UnitInfo) => upgraderClass(u) && u.upgradeProducing.forall(upgrade==),
    PreferIdle,
    CountOne)

  override def isComplete: Boolean = upgrade(With.self, level)
  override def hasSpent: Boolean = upgraders.units.exists(_.upgradeProducing.contains(upgrade))

  override def onUpdate(): Unit = {
    if (hasSpent || currencyLock.acquire()) {
      upgraders.acquire()
      if ( ! hasSpent) {
        upgraders.units.foreach(_.intend(this).setUpgrade(upgrade))
      }
    }
  }
}
