package Macro.BuildRequests

import Macro.Buildables.BuildableUpgrade

case class UpgradeNext(upgrade: ProxyBwapi.Upgrades.Upgrade) extends BuildRequest(BuildableUpgrade(upgrade)) {
  override def add: Int = 1
}
