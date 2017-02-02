package Types.BuildOrders.Protoss

import Types.BuildOrders.{BuildOrder, Buildable}
import Types.PositionFinders.{PositionProxyGateway, PositionProxyPylon}
import bwapi.UnitType

class BuildProxyGateway extends BuildOrder {
  _buildOrder = Array(
    new Buildable(UnitType.Protoss_Probe),
    new Buildable(UnitType.Protoss_Probe),
    new Buildable(UnitType.Protoss_Probe),
    new Buildable(UnitType.Protoss_Probe),
    new Buildable(UnitType.Protoss_Pylon, positionFinder = PositionProxyPylon),
    new Buildable(UnitType.Protoss_Probe),
    new Buildable(UnitType.Protoss_Gateway, positionFinder = PositionProxyGateway),
    new Buildable(UnitType.Protoss_Gateway, positionFinder = PositionProxyGateway),
    new Buildable(UnitType.Protoss_Zealot),
    new Buildable(UnitType.Protoss_Pylon),
    new Buildable(UnitType.Protoss_Zealot),
    new Buildable(UnitType.Protoss_Zealot),
    new Buildable(UnitType.Protoss_Zealot),
    new Buildable(UnitType.Protoss_Zealot)
  )
}
