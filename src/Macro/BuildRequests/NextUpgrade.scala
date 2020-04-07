package Macro.BuildRequests

import Lifecycle.With
import Macro.Buildables.BuildableUpgrade

case class NextUpgrade(upgrade: ProxyBwapi.Upgrades.Upgrade) extends BuildRequest(BuildableUpgrade(upgrade)) {
  override def total: Int = With.self.getUpgradeLevel(upgrade) + 1
}
