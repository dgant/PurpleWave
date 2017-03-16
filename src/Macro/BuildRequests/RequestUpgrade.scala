package Macro.BuildRequests

import Macro.Buildables.BuildableUpgrade
import ProxyBwapi.Upgrades.Upgrade

case class RequestUpgrade(upgrade: Upgrade, level:Int = 1) extends BuildRequest(new BuildableUpgrade(upgrade, level))
