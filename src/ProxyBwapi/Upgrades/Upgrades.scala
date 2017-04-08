package ProxyBwapi.Upgrades

import Lifecycle.With
import bwapi.UpgradeType

object Upgrades {
  def get(upgrade: UpgradeType):Upgrade = With.proxy.upgradesByType(upgrade)
  def all:Iterable[Upgrade] = With.proxy.upgradesByType.values
  def none:Upgrade = get(UpgradeType.None)
  def unknown:Upgrade = get(UpgradeType.Unknown)
}

