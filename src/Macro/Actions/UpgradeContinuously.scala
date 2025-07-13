package Macro.Actions

import Lifecycle.With
import ProxyBwapi.UnitClasses.UnitClasses
import ProxyBwapi.Upgrades.Upgrade

object UpgradeContinuously extends MacroActions {

  def apply(upgrade: Upgrade, maxLevel: Int = 3): Boolean = {

    val nextUpgradeLevel  = With.self.getUpgradeLevel(upgrade) + 1

    if (nextUpgradeLevel > Math.min(maxLevel, upgrade.levels.size)) {
      return true
    }

    val nextRequirement   = upgrade.whatsRequired(nextUpgradeLevel)
    val haveUpgrader      = With.macroCounts.oursExtant(upgrade.whatUpgrades) > 0
    val haveRequirement   = nextRequirement == UnitClasses.None || With.macroCounts.oursExtant(nextRequirement) > 0

    if (haveUpgrader && haveRequirement) {
      get(upgrade, nextUpgradeLevel)
    }

    upgradeStarted(upgrade, maxLevel)
  }
}
