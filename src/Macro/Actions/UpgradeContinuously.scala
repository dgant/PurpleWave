package Macro.Actions

import Lifecycle.With
import Macro.Requests.Get
import ProxyBwapi.Upgrades.Upgrade

object UpgradeContinuously extends MacroActions {
  def apply(upgrade: Upgrade, maxLevel: Int = 3): Boolean = {
    if (With.self.getUpgradeLevel(upgrade) < Math.min(maxLevel, upgrade.levels.size) && With.units.existsOurs(upgrade.whatUpgrades)) {
      get(upgrade, With.self.getUpgradeLevel(upgrade) + 1)
    }
    upgradeStarted(upgrade, maxLevel)
  }
}
