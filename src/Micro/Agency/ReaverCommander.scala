package Micro.Agency

import Mathematics.Points.Pixel
import ProxyBwapi.Orders
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}

// Reaver aiming must use Move: Attack-Move can change the firing timer, and Stop kills live Scarabs.
object ReaverCommander {
  def move(unit: FriendlyUnitInfo, to: Pixel): Unit = {
    unit.agent.setRideGoal(to)
    unit.agent.tryingToMove = unit.pixelDistanceCenter(to) > 8
    if (unit.unready) return
    if (unit.order != Orders.Move || ! unit.orderTargetPixel.contains(to) || unit.seeminglyStuck) {
      unit.bwapiUnit.move(to.bwapi)
    }
    Commander.sleep(unit, 1)
  }

  def hold(unit: FriendlyUnitInfo): Unit = {
    unit.agent.setRideGoal(unit.pixel)
    unit.agent.tryingToMove = false
    if (unit.unready) return
    if (unit.order != Orders.ReaverHoldPosition) unit.bwapiUnit.holdPosition()
    Commander.sleep(unit, 1)
  }

  def attack(unit: FriendlyUnitInfo, target: UnitInfo): Boolean = {
    unit.agent.setRideGoal(unit.pixel)
    unit.agent.tryingToMove = false
    if (unit.unready) return false
    val issued = unit.bwapiUnit.attack(target.bwapiUnit)
    if (issued) target.addFutureAttack(unit)
    Commander.sleep(unit, 1)
    issued
  }
}
