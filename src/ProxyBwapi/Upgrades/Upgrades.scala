package ProxyBwapi.Upgrades

import Lifecycle.With
import bwapi.UpgradeType

object Upgrades {
  def get(upgrade: UpgradeType): Upgrade = With.proxy.upgradesByType.getOrElse(upgrade, With.proxy.upgradesByName(upgrade.toString))
  def all: Iterable[Upgrade] = With.proxy.upgradesByType.values
  def None: Upgrade = get(UpgradeType.None)
  def Unknown: Upgrade = get(UpgradeType.Unknown)
}

