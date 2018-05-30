package Micro.Actions.Combat.Decisionmaking

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.{FriendlyUnitInfo, UnitInfo}
import Utilities.ByOption

object Recharge extends Action {
  
  override def allowed(unit: FriendlyUnitInfo): Boolean = (
    unit.canMove
    && unit.shieldPoints < unit.unitClass.maxShields / 2
    && unit.totalHealth < unit.unitClass.maxTotalHealth / 3.0
  )
  
  protected def validBattery(unit: UnitInfo): Boolean = (
    unit.isOurs
    && unit.aliveAndComplete
    && unit.is(Protoss.ShieldBattery)
    && unit.energy > 20 // 2 shield per energy
  )
  
  override protected def perform(unit: FriendlyUnitInfo) {
    var batteries = unit.matchups.allies.filter(validBattery)
    if (batteries.isEmpty) batteries = unit.zone.units.filter(validBattery).toVector
    val battery = ByOption.minBy(batteries)(_.pixelDistanceEdge(unit))
    battery.foreach(With.commander.rightClick(unit, _))
  }
}
