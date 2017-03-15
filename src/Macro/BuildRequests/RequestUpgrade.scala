package Macro.BuildRequests

import Macro.Buildables.BuildableUpgrade
import bwapi.UpgradeType

case class RequestUpgrade(upgrade: UpgradeType, level:Int = 1) extends BuildRequest(new BuildableUpgrade(upgrade, level))
