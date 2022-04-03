package Macro.Buildables

import ProxyBwapi.Upgrades.Upgrade

case class RequestUpgrade(upgradeType: Upgrade, level: Int=1) extends RequestProduction(upgradeType, level)