package Types.BuildOrders

import Types.PositionFinders.PositionFinder
import bwapi.{UnitType, UpgradeType}

class Buildable(
  val unitType:UnitType = null,
  val upgradeType:UpgradeType = null,
  val positionFinder: PositionFinder = null) {
}
