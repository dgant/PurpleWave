package Types.BuildRequest

import Types.Buildable.BuildableUpgrade
import bwapi.UpgradeType

case class RequestUpgrade(upgrade: UpgradeType, level:Int = 1) extends BuildRequest(new BuildableUpgrade(upgrade, level))
