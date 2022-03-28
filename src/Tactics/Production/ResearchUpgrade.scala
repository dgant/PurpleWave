package Tactics.Production

import Lifecycle.With
import Macro.Buildables.Buildable
import Micro.Agency.Intention
import Planning.ResourceLocks._
import Planning.UnitCounters.CountOne
import Planning.UnitPreferences.PreferIdle
import ProxyBwapi.UnitClasses.{UnitClass, UnitClasses}
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
  override def hasSpent: Boolean = upgraders.units.exists(u => u.upgrading && u.upgradingType == upgrade)

  override def onUpdate() {
    if (isComplete) return
    val requiredClasses = (upgrade.whatsRequired.get(level).toVector :+ upgraderClass).filterNot(UnitClasses.None==)
    currencyLock.framesPreordered = (
      upgraders.units.view.map(_.remainingOccupationFrames)
      ++ requiredClasses.map(With.projections.unit)).max
    if (hasSpent || currencyLock.acquire()) {
      upgraders.acquire()
      upgraders.units.foreach(_.intend(this, new Intention { toUpgrade = Some(upgrade) }))
    }
  }
}
