package ProxyBwapi.Upgrades

import Lifecycle.With
import bwapi.UpgradeType

object Upgrades {
  def all: Vector[Upgrade] = With.proxy.upgrades
  def get(upgrade: UpgradeType): Upgrade = With.proxy.upgradesById(upgrade.id)
  lazy val None: Upgrade = all.find(_.bwapiType == UpgradeType.None).get
  lazy val Unknown: Upgrade = all.find(_.bwapiType == UpgradeType.Unknown).get
}

