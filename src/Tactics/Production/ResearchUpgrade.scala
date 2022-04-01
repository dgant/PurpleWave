package Tactics.Production

import Lifecycle.With
import Macro.Buildables.Buildable
import Micro.Agency.Intention
import Planning.ResourceLocks._
import Planning.UnitCounters.CountOne
import Planning.UnitPreferences.PreferIdle
import ProxyBwapi.UnitClasses.UnitClass
import ProxyBwapi.Upgrades.Upgrade

class ResearchUpgrade(buildableUpgrade: Buildable) extends Production {

  setBuildable(buildableUpgrade)
  val upgrade       : Upgrade       = buildable.upgrade.get
  val level         : Int           = buildable.quantity
  val upgraderClass : UnitClass     = upgrade.whatUpgrades
  val currencyLock  : LockCurrency  = new LockCurrencyFor(this, upgrade, level)
  val upgraders     : LockUnits     = new LockUnits(this)
  upgraders.matcher     = u => upgraderClass(u) && u.upgradeProducing.forall(upgrade==)
  upgraders.counter     = CountOne
  upgraders.preference  = PreferIdle

  override def isComplete: Boolean = upgrade(With.self, level)
  override def hasSpent: Boolean = upgraders.units.exists(_.upgradeProducing.contains(upgrade))

  override def onUpdate() {
    if (isComplete) return
    if (hasSpent || currencyLock.acquire()) {
      upgraders.acquire()
      if ( ! hasSpent) {
        upgraders.units.foreach(_.intend(this, new Intention { toUpgrade = Some(upgrade) }))
      }
    }
  }
}
