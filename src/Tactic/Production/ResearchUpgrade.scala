package Tactic.Production

import Lifecycle.With
import Macro.Requests.RequestBuildable
import Micro.Agency.Intention
import Planning.ResourceLocks._
import Utilities.UnitCounters.CountOne
import Utilities.UnitPreferences.PreferIdle
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade

class ResearchUpgrade(requestArg: RequestBuildable, expectedFramesArg: Int) extends Production {
  setRequest(requestArg, expectedFramesArg)
  val upgrade       : Upgrade       = request.upgrade.get
  val level         : Int           = request.quantity
  val upgraderClass : UnitClass     = upgrade.whatUpgrades
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, upgrade, level)
  val upgraders     : LockUnits     = new LockUnits(this)
  upgraders.matcher     = u => upgraderClass(u) && u.upgradeProducing.forall(upgrade==)
  upgraders.counter     = CountOne
  upgraders.preference  = PreferIdle

  override def isComplete: Boolean = upgrade(With.self, level)
  override def hasSpent: Boolean = upgraders.units.exists(_.upgradeProducing.contains(upgrade))

  override def onUpdate() {
    if (hasSpent || currencyLock.acquire()) {
      upgraders.acquire()
      if ( ! hasSpent) {
        upgraders.units.foreach(_.intend(this, new Intention { toUpgrade = Some(upgrade) }))
      }
    }
  }
}
