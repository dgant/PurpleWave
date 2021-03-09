package ProxyBwapi.Upgrades

import Lifecycle.With
import bwapi.UpgradeType

object Upgrades {
  def all: Vector[Upgrade] = With.proxy.upgrades
  def get(upgrade: UpgradeType): Upgrade = With.proxy.upgradesById(upgrade.id)
  def None: Upgrade = get(UpgradeType.None)
  def Unknown: Upgrade = get(UpgradeType.Unknown)
}

