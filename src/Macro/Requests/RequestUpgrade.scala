package Macro.Requests

import ProxyBwapi.Upgrades.Upgrade

case class RequestUpgrade(upgradeType: Upgrade, level: Int = 1) extends RequestBuildable(upgradeType, level)