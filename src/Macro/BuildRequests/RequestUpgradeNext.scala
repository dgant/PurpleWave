package Macro.BuildRequests

import Macro.Buildables.BuildableUpgrade
import ProxyBwapi.Upgrades.Upgrade

case class RequestUpgradeNext(upgrade: Upgrade) extends BuildRequest(BuildableUpgrade(upgrade)) {
  override def add: Int = 1
}
