package Types.BuildOrders

import bwapi.{UnitType, UpgradeType}

class Buildable(
  val unitType:UnitType = null,
  val upgradeType:UpgradeType = null) {
}
