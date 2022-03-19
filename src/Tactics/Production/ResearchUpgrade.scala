package Tactics.Production

import Lifecycle.With
import Macro.Buildables.Buildable
import Micro.Agency.Intention
import Planning.ResourceLocks._
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchAnd, MatchIdle}
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
  upgraders.counter = CountOne
  upgraders.matcher = MatchAnd(upgraderClass, MatchIdle)
  upgraders.preference = PreferIdle

  override def isComplete: Boolean = upgrade(With.self, level)

  override def onUpdate() {
    if (isComplete) return
    if (With.units.ours.exists(u => u.upgradingType == upgrade && ! upgraders.units.contains(u))) return
    val requiredClasses = (upgrade.whatsRequired.get(level).toVector :+ upgraderClass).filterNot(_ == UnitClasses.None)
    currencyLock.framesPreordered = (
      upgraders.units.view.map(_.remainingOccupationFrames)
      ++ requiredClasses.map(With.projections.unit)).max
    currencyLock.acquire()
    currencyLock.isSpent = With.units.ours.exists(upgrader => upgrader.upgrading && upgrader.upgradingType == upgrade)
    if ( ! currencyLock.satisfied) return
    
    upgraders.acquire()
    upgraders.units.foreach(_.intend(this, new Intention { toUpgrade = Some(upgrade) }))
  }
}
