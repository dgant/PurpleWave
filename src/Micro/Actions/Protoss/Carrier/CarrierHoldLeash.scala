package Micro.Actions.Protoss.Carrier

import Lifecycle.With
import Micro.Actions.Action
import ProxyBwapi.Races.Protoss
import ProxyBwapi.UnitInfo.FriendlyUnitInfo

object CarrierHoldLeash extends Action {
  override def allowed(unit: FriendlyUnitInfo): Boolean = unit.is(Protoss.Carrier)
  override def perform(unit: FriendlyUnitInfo): Unit = {
    unit.agent.toAttack.foreach(target => {
      unit.agent.toTravel = Some(target.pixel.project(unit.pixel, 7.5 * 32.0))
      With.commander.move(unit)
    })
  }
}
