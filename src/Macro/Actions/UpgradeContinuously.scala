package Macro.Actions

import Lifecycle.With
import ProxyBwapi.UnitClasses.UnitClasses
import ProxyBwapi.Upgrades.Upgrade

object UpgradeContinuously extends MacroActions {

  def apply(upgrade: Upgrade, maxLevel: Int = 3): Boolean = {

    val nextUpgradeLevel  = With.self.getUpgradeLevel(upgrade) + 1
    val nextRequirement   = upgrade.whatsRequired(nextUpgradeLevel)
    val haveUpgrader      = With.units.existsOurs(upgrade.whatUpgrades)
    val haveRequirement   = nextRequirement == UnitClasses.None || With.units.existsOurs(nextRequirement)

    if (With.self.getUpgradeLevel(upgrade) < Math.min(maxLevel, upgrade.levels.size) && haveUpgrader && haveRequirement) {
      get(upgrade, nextUpgradeLevel)
    }

    upgradeStarted(upgrade, maxLevel)
  }
}
