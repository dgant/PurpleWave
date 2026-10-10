package Micro.Actions.Combat.Tactics

import Mathematics.Points.Pixel
import Micro.Actions.Action
import Micro.Agency.Commander
import ProxyBwapi.Orders
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

case class HoldWallGap(pixel: Pixel) extends Action {
  override def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.decision.set(pixel)
    unit.agent.redoubt.set(pixel)
    unit.agent.setRideGoal(pixel)
    if (unit.unready) return
    if (unit.pixel == pixel) {
      // Hold even when stationary; a stopped Zealot would acquire and chase targets.
      if (unit.order != Orders.HoldPosition) unit.bwapiUnit.holdPosition()
    } else {
      // Commander.move snaps sub-tile gaps to walkable tiles and may attack-move.
      unit.bwapiUnit.move(pixel.bwapi)
    }
    Commander.sleep(unit)
  }
}
