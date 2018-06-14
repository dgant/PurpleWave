package Macro.BuildRequests

import Macro.Buildables.BuildableUpgrade

case class Upgrade(upgrade: ProxyBwapi.Upgrades.Upgrade, level: Int = 1) extends BuildRequest(BuildableUpgrade(upgrade, level))
