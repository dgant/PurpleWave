package Macro.BuildRequests

import Macro.Buildables.BuildableUpgrade
import ProxyBwapi.Upgrades.Upgrade

case class RequestUpgradeLevel(upgrade: Upgrade, level: Int = 1) extends BuildRequest(BuildableUpgrade(upgrade, level))
