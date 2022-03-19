package Macro.Buildables

import ProxyBwapi.Upgrades.Upgrade

case class BuildableUpgrade(upgradeType: Upgrade, level: Int=1) extends Buildable(upgradeType, level)