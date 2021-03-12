package Planning.Plans.Macro.Build

import Lifecycle.With
import Macro.Buildables.{Buildable, BuildableUpgrade}
import Micro.Agency.Intention
import Planning.ResourceLocks._
import Planning.UnitCounters.CountOne
import Planning.UnitMatchers.{MatchAnd, MatchIdle}
import Planning.UnitPreferences.PreferIdle
import ProxyBwapi.UnitClasses.UnitClasses
import ProxyBwapi.Upgrades.Upgrade

class ResearchUpgrade(upgrade: Upgrade, level: Int) extends Production {

  override def producerCurrencyLocks: Seq[LockCurrency] = Seq(currencyLock)
  override def buildable: Buildable = BuildableUpgrade(upgrade, level)
  
  val upgraderClass = upgrade.whatUpgrades
  val currencyLock = new LockCurrencyForUpgrade(upgrade, level)
  val upgraders = new LockUnits(this)
  upgraders.counter = CountOne
  upgraders.matcher = MatchAnd(upgraderClass, MatchIdle)
  upgraders.preference = PreferIdle

  override def isComplete: Boolean = With.self.getUpgradeLevel(upgrade) >= level

  override def onUpdate() {
    if (isComplete) return
    if (With.units.ours.exists(u => u.upgradingType == upgrade && ! upgraders.units.contains(u))) return

    val requiredClasses = (upgrade.whatsRequired.get(level).toVector :+ upgraderClass).filterNot(_ == UnitClasses.None)

    // Don't even stick a projected expenditure in the queue if we're this far out.
    if (requiredClasses.exists(c => ! With.units.existsOurs(c))) return

    currencyLock.framesPreordered = (
      upgraders.units.view.map(_.remainingOccupationFrames)
      ++ requiredClasses.map(With.projections.unit)).max
    currencyLock.acquire(this)
    currencyLock.isSpent = With.units.ours.exists(upgrader => upgrader.upgrading && upgrader.upgradingType == upgrade)
    if ( ! currencyLock.satisfied) return
    
    upgraders.acquire(this)
    upgraders.units.foreach(_.agent.intend(this, new Intention { toUpgrade = Some(upgrade) }))
  }

  override val toString: String = f"Research $upgrade"
}
